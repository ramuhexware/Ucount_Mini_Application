package com.freddieapp.origination.service;

import com.freddieapp.origination.cache.OAuthTokenCache;
import com.freddieapp.origination.domain.LoanApplicationEntity;
import com.freddieapp.origination.dto.*;
import com.freddieapp.origination.exception.UcsApiException;
import com.freddieapp.origination.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Core Business Service Layer for Loan Origination, Counterparty Intake,
 * Account Management, and Reactive WebClient Data Synchronization.
 */
@Service
@Transactional
public class LoanOriginationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanOriginationService.class);
    private final LoanApplicationRepository repository;
    private final OAuthTokenCache oAuthTokenCache;
    private final WebClient webClient;

    @Value("${dataServiceURL}")
    private String dataServiceURL;

    @Value("${underwritingServiceURL}")
    private String underwritingServiceURL;

    @Value("${byPassOimSync}")
    private boolean byPassOimSync;

    private final long apiRetriesMax = 3L;
    private final Duration apiRetriesDelay = Duration.ofSeconds(1);

    // In-memory data store for Stage 1/2 Counterparty Intake
    private final Map<String, Stage1UserResponseDTO> stage1Users = new ConcurrentHashMap<>();
    private final Map<String, Stage2ProfileRequestDTO> stage2Profiles = new ConcurrentHashMap<>();

    @Autowired
    public LoanOriginationService(LoanApplicationRepository repository, OAuthTokenCache oAuthTokenCache, WebClient webClient) {
        this.repository = repository;
        this.oAuthTokenCache = oAuthTokenCache;
        this.webClient = webClient;
    }

    public LoanResponseDTO createLoanApplication(LoanRequestDTO request) {
        LoanApplicationEntity entity = new LoanApplicationEntity();
        entity.setCustomerId(request.customerId());
        entity.setApplicantName(request.applicantName());
        entity.setEmail(request.email());
        entity.setLoanAmount(request.loanAmount());
        entity.setPropertyValue(request.propertyValue());
        entity.setMonthlyIncome(request.monthlyIncome());
        entity.setMonthlyDebt(request.monthlyDebt());
        entity.setCreditScore(request.creditScore());
        entity.setTermMonths(request.termMonths() != null ? request.termMonths() : 360);

        LoanApplicationEntity saved = repository.save(entity);
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public LoanResponseDTO getLoanById(Long loanId) {
        LoanApplicationEntity entity = repository.findById(loanId)
            .orElseThrow(() -> new UcsApiException(HttpStatus.NOT_FOUND, "Loan application not found with ID: " + loanId));
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<LoanResponseDTO> getLoansByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public LoanResponseDTO submitForUnderwritingNative(Long loanId) {
        int rowsUpdated = repository.updateStatusNative(loanId, "UNDER_REVIEW");
        if (rowsUpdated == 0) {
            throw new UcsApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to update status via PostgreSQL native query for ID: " + loanId);
        }

        LoanApplicationEntity entity = repository.findById(loanId)
            .orElseThrow(() -> new UcsApiException(HttpStatus.NOT_FOUND, "Loan not found: " + loanId));

        // Trigger Reactive WebClient inter-service call to Underwriting Service
        triggerUnderwritingWebClient(entity);

        return mapToResponse(entity);
    }

    private void triggerUnderwritingWebClient(LoanApplicationEntity entity) {
        String targetUrl = underwritingServiceURL + "/underwriting/assess";
        Map<String, Object> requestPayload = Map.of(
            "loanId", entity.getId(),
            "customerId", entity.getCustomerId(),
            "loanAmount", entity.getLoanAmount(),
            "propertyValue", entity.getPropertyValue(),
            "monthlyIncome", entity.getMonthlyIncome(),
            "monthlyDebt", entity.getMonthlyDebt(),
            "creditScore", entity.getCreditScore(),
            "termMonths", entity.getTermMonths()
        );

        LOGGER.info("WebClient: Initiating reactive underwriting risk assessment for loan ID: {} via {}", entity.getId(), targetUrl);

        webClient.post()
            .uri(targetUrl)
            .headers(h -> h.add("Authorization", oAuthTokenCache.getOAuthAccessToken()))
            .bodyValue(requestPayload)
            .exchangeToMono(clientResponse -> {
                LOGGER.info("WebClient: Received underwriting response status code {}", clientResponse.statusCode().value());
                if (clientResponse.statusCode().is4xxClientError()) {
                    throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), "Underwriting API Client Error");
                } else if (clientResponse.statusCode().is5xxServerError()) {
                    throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), "Underwriting Service Internal Error");
                } else {
                    return clientResponse.bodyToMono(Map.class);
                }
            })
            .retryWhen(reactor.util.retry.Retry.backoff(apiRetriesMax, apiRetriesDelay)
                .jitter(0.2d)
                .doAfterRetry(retrySignal -> LOGGER.info("WebClient Underwriting Retried: {}", retrySignal.totalRetries()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new UcsApiException(HttpStatus.SERVICE_UNAVAILABLE, "Underwriting Service unavailable after retries"))
            )
            .subscribe(
                responseMap -> {
                    String decision = (String) responseMap.get("decision");
                    LOGGER.info("WebClient: Underwriting decision received for loan {}: {}", entity.getId(), decision);
                    if (decision != null) {
                        repository.updateStatusNative(entity.getId(), decision);
                    }
                },
                error -> {
                    LOGGER.error("WebClient: Failed underwriting assessment for loan {}: {}", entity.getId(), error.getMessage());
                    saveToErrorTable(targetUrl, String.valueOf(entity.getId()), "UNDERWRITING_ASSESSMENT", error);
                }
            );
    }

    // Account Lookup Data
    public AccountLookupDTO getLookupData() {
        LOGGER.info("OrgAPI: Fetching lookup data...");
        return new AccountLookupDTO(
            List.of("MORTGAGE_ORIGINATION_ACCT", "UNDERWRITING_ACCT", "SECONDARY_MARKET_ACCT"),
            List.of("PRIMARY_LENDER", "BROKER", "SERVICER", "CORRESPONDENT"),
            Map.of("SYS_STATUS", "ACTIVE", "REGION", "US_EAST")
        );
    }

    // Account Lookup Update Data
    public AccountLookupUpdateDTO getLookupUpdateData() {
        AccountLookupUpdateDTO accountLookupUpdateDTO = new AccountLookupUpdateDTO();

        List<UcsLineOfBusinessDTO> listUcsLineOfBusiness = List.of(
            new UcsLineOfBusinessDTO(1, "Mortgage Origination"),
            new UcsLineOfBusinessDTO(2, "Underwriting & Risk"),
            new UcsLineOfBusinessDTO(3, "Secondary Market")
        );

        List<UcsLineOfBusinessDTO> listUcsLineOfBusinessDTO = new ArrayList<>();
        for (UcsLineOfBusinessDTO ucsLiOfBus : listUcsLineOfBusiness) {
            UcsLineOfBusinessDTO ucsLineOfBusinessDTO = new UcsLineOfBusinessDTO();
            ucsLineOfBusinessDTO.setIdLiOfBus(ucsLiOfBus.getIdLiOfBus());
            ucsLineOfBusinessDTO.setNameLiOfBus(ucsLiOfBus.getNameLiOfBus());
            listUcsLineOfBusinessDTO.add(ucsLineOfBusinessDTO);
        }
        accountLookupUpdateDTO.setUcsLineOfBusinessDTOs(listUcsLineOfBusinessDTO);

        Map<String, List<UcsOrgtnRoleDTO>> maplistUcsOrgtnRoleDTOs = new HashMap<>();
        Map<String, List<UcsProdtDTO>> maplistUcsProdtDTOs = new HashMap<>();

        List<Integer> listUcsLiOfBusProdtAssn = List.of(1, 2, 3);

        for (Integer idLiOfBus : listUcsLiOfBusProdtAssn) {
            List<UcsProdtDTO> listUcsProdt = List.of(
                new UcsProdtDTO(101, "Fixed 30Y Primary Mortgage"),
                new UcsProdtDTO(102, "Floating 15Y Refinance"),
                new UcsProdtDTO(103, "HELOC Variable Line")
            );

            List<UcsProdtDTO> listUcsProdtDTOs = new ArrayList<>();
            for (UcsProdtDTO ucsProdt : listUcsProdt) {
                UcsProdtDTO ucsProdtDTO = new UcsProdtDTO();
                ucsProdtDTO.setIdProdt(ucsProdt.getIdProdt());
                ucsProdtDTO.setNameProdt(ucsProdt.getNameProdt());
                listUcsProdtDTOs.add(ucsProdtDTO);

                List<UcsOrgtnRoleDTO> listUcsOrgtnRole = List.of(
                    new UcsOrgtnRoleDTO(1001, "Primary Lender Officer"),
                    new UcsOrgtnRoleDTO(1002, "Correspondent Underwriter"),
                    new UcsOrgtnRoleDTO(1003, "Secondary Market Trader")
                );

                List<UcsOrgtnRoleDTO> listUcsOrgtnRoleDTOs = new ArrayList<>();
                for (UcsOrgtnRoleDTO ucsOrgtnRole : listUcsOrgtnRole) {
                    UcsOrgtnRoleDTO ucsOrgtnRoleDTO = new UcsOrgtnRoleDTO();
                    ucsOrgtnRoleDTO.setIdOrgtnRole(ucsOrgtnRole.getIdOrgtnRole());
                    ucsOrgtnRoleDTO.setNameOrgtnRole(ucsOrgtnRole.getNameOrgtnRole());
                    listUcsOrgtnRoleDTOs.add(ucsOrgtnRoleDTO);
                }

                maplistUcsOrgtnRoleDTOs.put(ucsProdt.getIdProdt() + "", listUcsOrgtnRoleDTOs);
            }

            maplistUcsProdtDTOs.put(idLiOfBus + "", listUcsProdtDTOs);
        }

        accountLookupUpdateDTO.setUcsOrgtnRoleDTOs(maplistUcsOrgtnRoleDTOs);
        accountLookupUpdateDTO.setUcsProdtDTOs(maplistUcsProdtDTOs);
        accountLookupUpdateDTO.setUcsCntprtyAcctSt(List.of("ACTIVE", "PENDING", "SUSPENDED"));
        accountLookupUpdateDTO.setUcsCntprtyAcctRoleSt(List.of("PRIMARY", "SECONDARY", "AUDITOR"));
        accountLookupUpdateDTO.setRespSts(new ResponseStatusDTO(200, "Account look up data is retrieved successfully"));
        LOGGER.info("Org API: Account lookup update data is retrieved successfully.");
        return accountLookupUpdateDTO;
    }

    // Create Account with OIM Sync & Expiration workflows
    public AccountSaveDTO createAccount(AccountSaveDTO accountReq) {
        LOGGER.info("OrgAPI: Create Account for {}", accountReq.getOrgName());
        String idCntprtyAcct = (accountReq.getIdCntprtyAcct() != null)
            ? accountReq.getIdCntprtyAcct()
            : "ACC-" + (100000 + new Random().nextInt(900000));
        accountReq.setIdCntprtyAcct(idCntprtyAcct);

        updateOimDatabaseOnNameUpdate(idCntprtyAcct);
        expireAccountEligibilityAndRelationship(1001, accountReq);

        accountReq.setRespSts(new ResponseStatusDTO(200, "Account Created Successfully"));
        return accountReq;
    }

    private void updateOimDatabaseOnNameUpdate(String idCntprtyAcct) {
        LOGGER.info("Org API: OIM Database Sync triggered for counterparty account: {}", idCntprtyAcct);
        String targetUrl = dataServiceURL + "/account/lookup";
        performOimSyncGet(targetUrl, idCntprtyAcct, "FAMILY_NAME_UPDATE", String.class);
    }

    @Async("oimDataSyncThreadPool")
    public <T> void performOimSyncGet(String url, Object object, String operation, Class<T> responseType) {
        if (!byPassOimSync) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(" Thread Interrupted exception " + e);
            }
            LOGGER.info("ORG API: OIM Sync Get service call started... for URL-" + url + " Primary Key-" + object + " Opreation-" + operation + " Thread name " + Thread.currentThread().getName());
            webClient.get().uri(url)
                .headers(h -> h.add("Authorization", oAuthTokenCache.getOAuthAccessToken()))
                .exchangeToMono(clientResponse -> {
                    LOGGER.info("Recived response from OIM Sync application with status code " + clientResponse.statusCode().value());
                    if (clientResponse.statusCode().is4xxClientError()) {
                        throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), "Org API :: URL is wrong ");
                    } else if (clientResponse.statusCode().is5xxServerError()) {
                        throw new UcsApiException(HttpStatus.valueOf(clientResponse.statusCode().value()), " Org API :: Error occured in OIM Sync :: for more details check OIM sync logs");
                    } else {
                        return clientResponse.bodyToMono(responseType);
                    }
                })
                .retryWhen(reactor.util.retry.Retry.backoff(apiRetriesMax, apiRetriesDelay)
                    .jitter(0d)
                    .doAfterRetry(retrySignal -> {
                        LOGGER.info("Retried " + retrySignal.totalRetries());
                    })
                    .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> new UcsApiException(HttpStatus.valueOf(500), "Error in OIM sync"))
                )
                .subscribe(
                    response -> {
                        LOGGER.info("Received reply from " + url + " Primary Key=" + object + " Opreation=" + operation + " Thread name = " + Thread.currentThread().getName() + " with response " + response);
                    },
                    error -> {
                        saveToErrorTable(url, (String) object, operation, error);
                    }
                );
        } else {
            LOGGER.info("OIM Sync is not happening because byPassOimSync = " + byPassOimSync);
        }
    }

    private void saveToErrorTable(String url, String primaryKey, String operation, Throwable exception) {
        LOGGER.error("Exception while calling get for " + url + " for entity " + primaryKey + " while " + operation + " Exception msg " + exception.getMessage() + " Thread name " + Thread.currentThread().getName());
    }

    private void expireAccountEligibilityAndRelationship(int idOrgtnRole, AccountSaveDTO accountReq) {
        LOGGER.info("Org API: Expiring account functional roles and eligibility for org role: {}", idOrgtnRole);
        expireAccountRelationship(idOrgtnRole, accountReq);
    }

    private void expireAccountRelationship(int idOrgtnRole, AccountSaveDTO accountReq) {
        List<Short> expirableRelationshipsList = List.of((short) 25, (short) 30);
        List<Short> activeRelationships = List.of((short) 25, (short) 10);

        List<Short> keepRelationshipsList = new ArrayList<>();
        for (Short relationId : activeRelationships) {
            if (expirableRelationshipsList.contains(relationId)) {
                if (relationId == 25) {
                    LOGGER.info("SELLER-SERVICER-DISCONTINUE-CTOS - A Seller-Ctos Servicer Relationship exists on this account : {}", accountReq.getIdCntprtyAcct());
                    expireActiveIsCashOrMC(relationId, idOrgtnRole, accountReq.getIdCntprtyAcct());
                } else {
                    LOGGER.info("Terminating counterparty relationship ID: {} for account: {}", relationId, accountReq.getIdCntprtyAcct());
                }
            } else {
                keepRelationshipsList.add(relationId);
            }
        }
    }

    private void expireActiveIsCashOrMC(Short relationId, Integer idOrgtnRole, String idCntprtyAcct) {
        List<Integer> isCashMCdReIExtn = Arrays.asList(1, 2);
        LOGGER.info("Expiring active Cash/MC relationships: {} for account: {}", isCashMCdReIExtn, idCntprtyAcct);
    }

    public AccountProfileRespDTO getAccountProfile(AccountProfileReqDTO accountProfileReqDTO) {
        LOGGER.info("OrgAPI: Fetching Account Profile for {}", accountProfileReqDTO.idCntprtyAcct());
        return new AccountProfileRespDTO(
            accountProfileReqDTO.idCntprtyAcct(),
            "VERIFIED_ACTIVE",
            List.of("LOAN_ORIGINATION_PORTAL:FULL", "APPRAISAL_PORTAL:READ", "TITLE_PORTAL:READ")
        );
    }

    // Stage 1 Intake & Onboarding
    public Stage1UserResponseDTO onboardStage1User(Stage1OnboardRequestDTO req) {
        String userId = "USR-" + (100000 + new Random().nextInt(900000));
        Stage1UserResponseDTO resp = new Stage1UserResponseDTO(userId, req.orgName(), req.email(), Stage1Status.PENDING_APPROVAL, LocalDateTime.now());
        stage1Users.put(userId, resp);
        return resp;
    }

    public Stage1UserResponseDTO approveStage1User(String userId) {
        Stage1UserResponseDTO existing = stage1Users.get(userId);
        if (existing == null) {
            existing = new Stage1UserResponseDTO(userId, "Freddie Partner Org", "user@partner.com", Stage1Status.PENDING_APPROVAL, LocalDateTime.now());
        }
        Stage1UserResponseDTO approved = new Stage1UserResponseDTO(userId, existing.orgName(), existing.email(), Stage1Status.APPROVED, LocalDateTime.now());
        stage1Users.put(userId, approved);
        return approved;
    }

    public List<Stage1UserResponseDTO> getPendingStage1Users() {
        return new ArrayList<>(stage1Users.values());
    }

    // Stage 2 Extended Profile & Access Rights
    public Stage2AccessRightsResponseDTO saveStage2Profile(Stage2ProfileRequestDTO req) {
        stage2Profiles.put(req.userId(), req);
        List<String> rights = evaluateAccessRights(req.userType());
        return new Stage2AccessRightsResponseDTO(req.userId(), req.userType(), rights);
    }

    public Stage2AccessRightsResponseDTO getStage2AccessRights(String userId) {
        Stage2ProfileRequestDTO req = stage2Profiles.get(userId);
        UserType userType = (req != null) ? req.userType() : UserType.HOUSE_BUYER;
        return new Stage2AccessRightsResponseDTO(userId, userType, evaluateAccessRights(userType));
    }

    private List<String> evaluateAccessRights(UserType userType) {
        return switch (userType) {
            case HOUSE_SELLER -> List.of("LOAN_ORIGINATION_PORTAL:FULL", "APPRAISAL_PORTAL:READ", "TITLE_PORTAL:READ");
            case HOUSE_BUYER -> List.of("LOAN_ORIGINATION_PORTAL:FULL", "CUSTOMER_PORTAL:FULL", "CARD_SERVICE_PORTAL:READ");
            case INSURANCE_PERSON -> List.of("TITLE_PORTAL:FULL", "DOCUMENT_SERVICE:READ", "ESCROW_PORTAL:READ");
            case MORTGAGE_SERVICER -> List.of("LOAN_SERVICING_PORTAL:FULL", "SECONDARY_MARKET_ACCESS:FULL", "REPORT_PORTAL:READ");
        };
    }

    private LoanResponseDTO mapToResponse(LoanApplicationEntity e) {
        return new LoanResponseDTO(
            e.getId(),
            e.getCustomerId(),
            e.getApplicantName(),
            e.getEmail(),
            e.getLoanAmount(),
            e.getPropertyValue(),
            e.getCreditScore(),
            e.getStatus(),
            e.getCreatedAt()
        );
    }
}
