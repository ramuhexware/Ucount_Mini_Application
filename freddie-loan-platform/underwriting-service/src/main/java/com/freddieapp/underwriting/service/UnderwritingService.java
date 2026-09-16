package com.freddieapp.underwriting.service;

import com.freddieapp.underwriting.domain.UnderwritingAuditLogEntity;
import com.freddieapp.underwriting.dto.*;
import com.freddieapp.underwriting.exception.UnderwritingException;
import com.freddieapp.underwriting.processor.RateCalculatorProcessor;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor;
import com.freddieapp.underwriting.repository.UnderwritingAuditRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class UnderwritingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnderwritingService.class);

    private final UnderwritingRuleProcessor ruleProcessor;
    private final RateCalculatorProcessor rateCalculatorProcessor;
    private final UnderwritingAuditRepository auditRepository;
    private final WebClient webClient;

    @Value("${originationServiceURL:http://localhost:8082/api/v1}")
    private String originationServiceURL;

    @Autowired
    public UnderwritingService(UnderwritingRuleProcessor ruleProcessor,
                               RateCalculatorProcessor rateCalculatorProcessor,
                               UnderwritingAuditRepository auditRepository,
                               WebClient webClient) {
        this.ruleProcessor = ruleProcessor;
        this.rateCalculatorProcessor = rateCalculatorProcessor;
        this.auditRepository = auditRepository;
        this.webClient = webClient;
    }

    public AssessmentResultDTO assessApplication(AssessmentRequestDTO request) {
        AssessmentRequestDTO enrichedRequest = request;

        // If financial details are missing in request, use WebClient to fetch application details from Origination Service
        if (request.loanId() != null && (request.loanAmount() == null || request.monthlyIncome() == null)) {
            LOGGER.info("Underwriting Service: WebClient fetching loan details for ID {} from Origination Service...", request.loanId());
            enrichedRequest = fetchLoanDetailsWebClient(request);
        }

        AssessmentResultDTO result = ruleProcessor.evaluateRisk(enrichedRequest);
        if (enrichedRequest.loanId() != null) {
            UnderwritingAuditLogEntity audit = new UnderwritingAuditLogEntity(enrichedRequest.loanId(), result.decision(), result.riskLevel());
            auditRepository.save(audit);
        }
        return result;
    }

    private AssessmentRequestDTO fetchLoanDetailsWebClient(AssessmentRequestDTO request) {
        String targetUrl = originationServiceURL + "/loans/" + request.loanId();
        try {
            Map loanMap = webClient.get()
                .uri(targetUrl)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(3))
                .onErrorReturn(Map.of())
                .block();

            if (loanMap != null && !loanMap.isEmpty()) {
                BigDecimal loanAmount = loanMap.get("loanAmount") != null ? new BigDecimal(loanMap.get("loanAmount").toString()) : request.loanAmount();
                BigDecimal monthlyIncome = loanMap.get("monthlyIncome") != null ? new BigDecimal(loanMap.get("monthlyIncome").toString()) : request.monthlyIncome();
                BigDecimal propertyValue = loanMap.get("propertyValue") != null ? new BigDecimal(loanMap.get("propertyValue").toString()) : request.propertyValue();
                BigDecimal monthlyDebt = loanMap.get("monthlyDebt") != null ? new BigDecimal(loanMap.get("monthlyDebt").toString()) : request.monthlyDebt();
                Integer creditScore = loanMap.get("creditScore") != null ? (Integer) loanMap.get("creditScore") : request.creditScore();

                return new AssessmentRequestDTO(
                    request.loanId(),
                    request.customerId() != null ? request.customerId() : (String) loanMap.get("customerId"),
                    loanAmount,
                    propertyValue,
                    monthlyIncome,
                    monthlyDebt,
                    creditScore,
                    request.termMonths()
                );
            }
        } catch (Exception ex) {
            LOGGER.warn("WebClient: Could not fetch remote loan details from {}: {}", targetUrl, ex.getMessage());
        }
        return request;
    }

    public PricingQuoteDTO getPricingQuote(Integer creditScore, BigDecimal ltvRatio) {
        return rateCalculatorProcessor.calculatePricingQuote(creditScore, ltvRatio);
    }

    public AmortizationScheduleDTO getAmortizationSchedule(BigDecimal loanAmount, BigDecimal interestRate, int termMonths) {
        return rateCalculatorProcessor.generateAmortizationSchedule(loanAmount, interestRate, termMonths);
    }

    public Map<String, Object> commonStatus(Map<String, Object> inMap, String jobName) {
        String inMapCosnt = "inMap";
        String acctgCycleConst = "acctgCycle";
        String jobNameMap = "jobName";

        Map<String, Object> response = new HashMap<>();
        response.put(jobNameMap, jobName);
        response.put(inMapCosnt, inMap != null ? inMap : Map.of());
        response.put(acctgCycleConst, "2026-Q3");
        response.put("status", "SUCCESS");
        response.put("executedAt", java.time.LocalDateTime.now().toString());
        return response;
    }
}
