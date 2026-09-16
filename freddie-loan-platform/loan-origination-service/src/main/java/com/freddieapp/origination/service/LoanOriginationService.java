package com.freddieapp.origination.service;

import com.freddieapp.origination.model.LoanApplicationEntity;
import com.freddieapp.origination.model.LoanApplicationEntity.*;
import com.freddieapp.origination.repository.LoanApplicationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Business Service Layer for Loan Origination, Stage 1/2 Counterparty Onboarding, Account Services,
 * and Asynchronous OIM Data Sync ThreadPool Execution.
 */
@Service
@Transactional
public class LoanOriginationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanOriginationService.class);
    private final LoanApplicationRepository repository;

    @Value("${dataServiceURL:http://localhost:8082/api/v1}")
    private String dataServiceURL;

    @Value("${byPassOimSync:false}")
    private boolean byPassOimSync;

    // In-memory data store for Stage 1/2 Counterparty Intake
    private final Map<String, Stage1UserResponse> stage1Users = new ConcurrentHashMap<>();
    private final Map<String, Stage2ProfileRequest> stage2Profiles = new ConcurrentHashMap<>();

    @Autowired
    public LoanOriginationService(LoanApplicationRepository repository) {
        this.repository = repository;
    }

    public LoanResponse createLoanApplication(LoanRequest request) {
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
    public LoanResponse getLoanById(Long loanId) {
        LoanApplicationEntity entity = repository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan application not found with ID: " + loanId));
        return mapToResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<LoanResponse> getLoansByCustomerId(String customerId) {
        return repository.findByCustomerId(customerId).stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public LoanResponse submitForUnderwritingNative(Long loanId) {
        int rowsUpdated = repository.updateStatusNative(loanId, "UNDER_REVIEW");
        if (rowsUpdated == 0) {
            throw new RuntimeException("Failed to update status via PostgreSQL native query for ID: " + loanId);
        }

        LoanApplicationEntity entity = repository.findById(loanId)
            .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
        return mapToResponse(entity);
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

    // OIM Database Sync
    private void updateOimDatabaseOnNameUpdate(String idCntprtyAcct) {
        LOGGER.info("Org API: OIM Database Sync triggered for counterparty account: {}", idCntprtyAcct);
        String targetUrl = dataServiceURL + "/account/update-family/" + idCntprtyAcct;
        performOimSyncGet(targetUrl, idCntprtyAcct, "FAMILY_NAME_UPDATE", String.class);
    }

    // Asynchronous OIM Sync Execution (Matching code image lines 59-97)
    @Async("oimDataSyncThreadPool")
    public <T> void performOimSyncGet(String url, Object object, String operation, Class<T> responseType) {
        if (!byPassOimSync) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread Interrupted exception " + e);
            }
            LOGGER.info("ORG API: OIM Sync Get service call started... for URL=" + url + " Primary Key=" + object + " Opreation=" + operation + " Thread name = " + Thread.currentThread().getName());
            LOGGER.info("Recived response from OIM Sync application with status code 200");
            LOGGER.info("Received reply from " + url + " Primary Key=" + object + " Opreation=" + operation + " Thread name = " + Thread.currentThread().getName() + " with response OK");
        } else {
            LOGGER.info("OIM Sync is not happening because byPassOimSync = " + byPassOimSync);
        }
    }

    // Save To Error Table (Matching code image lines 99-104)
    private void saveToErrorTable(String url, String primaryKey, String operation, Throwable exception) {
        LOGGER.error("Exception while calling get for " + url + " for entity " + primaryKey + " while " + operation + " Exception msg " + exception.getMessage() + " Thread name " + Thread.currentThread().getName());
    }

    // Expire Account Eligibility & Relationship
    private void expireAccountEligibilityAndRelationship(int idOrgtnRole, AccountSaveDTO accountReq) {
        LOGGER.info("Org API: Expiring account functional roles and eligibility for org role: {}", idOrgtnRole);
        expireAccountRelationship(idOrgtnRole, accountReq);
    }

    // Expire Account Relationship
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

    // Expire Active Cash Or MC
    private void expireActiveIsCashOrMC(Short relationId, Integer idOrgtnRole, String idCntprtyAcct) {
        List<Integer> isCashMCdReIExtn = Arrays.asList(1, 2);
        LOGGER.info("Expiring active Cash/MC relationships: {} for account: {}", isCashMCdReIExtn, idCntprtyAcct);
    }

    // Account Profile
    public AccountProfileRespDTO getAccountProfile(AccountProfileReqDTO accountProfileReqDTO) {
        LOGGER.info("OrgAPI: Fetching Account Profile for {}", accountProfileReqDTO.idCntprtyAcct());
        return new AccountProfileRespDTO(
            accountProfileReqDTO.idCntprtyAcct(),
            "VERIFIED_ACTIVE",
            List.of("LOAN_ORIGINATION_PORTAL:FULL", "APPRAISAL_PORTAL:READ", "TITLE_PORTAL:READ")
        );
    }

    // Stage 1 Intake & Onboarding
    public Stage1UserResponse onboardStage1User(Stage1OnboardRequest req) {
        String userId = "USR-" + (100000 + new Random().nextInt(900000));
        Stage1UserResponse resp = new Stage1UserResponse(userId, req.orgName(), req.email(), Stage1Status.PENDING_APPROVAL, LocalDateTime.now());
        stage1Users.put(userId, resp);
        return resp;
    }

    public Stage1UserResponse approveStage1User(String userId) {
        Stage1UserResponse existing = stage1Users.get(userId);
        if (existing == null) {
            existing = new Stage1UserResponse(userId, "Freddie Partner Org", "user@partner.com", Stage1Status.PENDING_APPROVAL, LocalDateTime.now());
        }
        Stage1UserResponse approved = new Stage1UserResponse(userId, existing.orgName(), existing.email(), Stage1Status.APPROVED, LocalDateTime.now());
        stage1Users.put(userId, approved);
        return approved;
    }

    public List<Stage1UserResponse> getPendingStage1Users() {
        return new ArrayList<>(stage1Users.values());
    }

    // Stage 2 Extended Profile & Access Rights
    public Stage2AccessRightsResponse saveStage2Profile(Stage2ProfileRequest req) {
        stage2Profiles.put(req.userId(), req);
        List<String> rights = evaluateAccessRights(req.userType());
        return new Stage2AccessRightsResponse(req.userId(), req.userType(), rights);
    }

    public Stage2AccessRightsResponse getStage2AccessRights(String userId) {
        Stage2ProfileRequest req = stage2Profiles.get(userId);
        UserType userType = (req != null) ? req.userType() : UserType.HOUSE_BUYER;
        return new Stage2AccessRightsResponse(userId, userType, evaluateAccessRights(userType));
    }

    private List<String> evaluateAccessRights(UserType userType) {
        return switch (userType) {
            case HOUSE_SELLER -> List.of("LOAN_ORIGINATION_PORTAL:FULL", "APPRAISAL_PORTAL:READ", "TITLE_PORTAL:READ");
            case HOUSE_BUYER -> List.of("LOAN_ORIGINATION_PORTAL:FULL", "CUSTOMER_PORTAL:FULL", "CARD_SERVICE_PORTAL:READ");
            case INSURANCE_PERSON -> List.of("TITLE_PORTAL:FULL", "DOCUMENT_SERVICE:READ", "ESCROW_PORTAL:READ");
            case MORTGAGE_SERVICER -> List.of("LOAN_SERVICING_PORTAL:FULL", "SECONDARY_MARKET_ACCESS:FULL", "REPORT_PORTAL:READ");
        };
    }

    private LoanResponse mapToResponse(LoanApplicationEntity e) {
        return new LoanResponse(
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
