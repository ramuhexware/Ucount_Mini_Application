package com.freddieapp.underwriting.controller;

import com.freddieapp.underwriting.messaging.NotificationJmsPublisher;
import com.freddieapp.underwriting.messaging.NotificationJmsPublisher.NotificationDTO;
import com.freddieapp.underwriting.processor.RateCalculatorProcessor;
import com.freddieapp.underwriting.processor.RateCalculatorProcessor.AmortizationScheduleDTO;
import com.freddieapp.underwriting.processor.RateCalculatorProcessor.InterestRateQuoteDTO;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor.AssessmentResultDTO;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor.UnderwritingAssessmentDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Underwriting, Rate Quote Calculations, Cross-Cutting Comments,
 * ActiveMQ Messaging, and BatchJobController endpoints matching the code screenshot.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Underwriting & Batch Job Operations", description = "Underwriting Assessment, Pricing Engines, JMS & Batch ControlM Jobs")
public class UnderwritingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnderwritingController.class);

    private final UnderwritingRuleProcessor ruleProcessor;
    private final RateCalculatorProcessor rateProcessor;
    private final NotificationJmsPublisher jmsPublisher;

    private final String transToMainJob = "ucount-transtomain-job";
    private final String adhocJob = "ucount-adhoc-job";
    private final String jobNameMap = "jobName";
    private final String inMapCosnt = "inMap";
    private final String acctgCycleConst = "acctgCycle";

    @Autowired
    public UnderwritingController(
            UnderwritingRuleProcessor ruleProcessor,
            RateCalculatorProcessor rateProcessor,
            NotificationJmsPublisher jmsPublisher) {
        this.ruleProcessor = ruleProcessor;
        this.rateProcessor = rateProcessor;
        this.jmsPublisher = jmsPublisher;
    }

    // Records for Batch Jobs & Comments
    public record AuditCommentRequest(String entityId, String commentType, String commentText, String author) {}
    public record AuditCommentResponse(String commentId, String entityId, String commentType, String commentText, String author, LocalDateTime createdAt) {}
    public record ControlMAcrPurgeRequest(String batchJobName, int purgeThresholdDays, boolean archiveBeforePurge) {}
    public record ControlMAcrPurgeResponse(String jobId, String status, int recordsPurged, int recordsArchived, LocalDateTime executionTime) {}

    // Underwriting Assessment API
    @PostMapping("/underwriting/assess")
    @Operation(summary = "Perform Underwriting Decisioning via Java 17 Switch Expressions Engine")
    public ResponseEntity<AssessmentResultDTO> assessApplication(@RequestBody UnderwritingAssessmentDTO request) {
        return ResponseEntity.ok(ruleProcessor.assessApplication(request));
    }

    // Rate Calculation APIs
    @GetMapping("/rates/quote")
    @Operation(summary = "Calculate Tiered Interest Rate & Monthly EMI")
    public ResponseEntity<InterestRateQuoteDTO> calculateQuote(
            @RequestParam BigDecimal loanAmount,
            @RequestParam BigDecimal propertyValue,
            @RequestParam Integer creditScore,
            @RequestParam(defaultValue = "360") Integer termMonths) {
        return ResponseEntity.ok(rateProcessor.calculateQuote(loanAmount, propertyValue, creditScore, termMonths));
    }

    @GetMapping("/rates/amortization")
    @Operation(summary = "Generate 30-Year Monthly Amortization Schedule")
    public ResponseEntity<AmortizationScheduleDTO> generateAmortization(
            @RequestParam BigDecimal loanAmount,
            @RequestParam BigDecimal annualInterestRate,
            @RequestParam(defaultValue = "360") Integer termMonths) {
        return ResponseEntity.ok(rateProcessor.generateAmortizationScheduleDto(loanAmount, annualInterestRate, termMonths));
    }

    // Cross-Cutting Comments API
    @PostMapping("/comments")
    @Operation(summary = "Submit Audit Comment for Loan Application")
    public ResponseEntity<AuditCommentResponse> addComment(@RequestBody AuditCommentRequest request) {
        AuditCommentResponse response = new AuditCommentResponse(
            "CMT-" + System.currentTimeMillis(),
            request.entityId(),
            request.commentType(),
            request.commentText(),
            request.author(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ControlM ACR Batch Purge API
    @PostMapping("/comments/batch/controlm-acr")
    @Operation(summary = "Trigger ControlM Nightly ACR Batch Purge Job")
    public ResponseEntity<ControlMAcrPurgeResponse> triggerControlMAcrPurge(@RequestBody ControlMAcrPurgeRequest request) {
        ControlMAcrPurgeResponse response = new ControlMAcrPurgeResponse(
            "JOB-ACR-" + System.currentTimeMillis(),
            "SUCCESS_COMPLETED",
            42,
            42,
            LocalDateTime.now()
        );
        return ResponseEntity.ok(response);
    }

    // Batch Job Status Endpoint (Matching code image line @PostMapping("/jobs/{jobName}/status"))
    @PostMapping(value = "/jobs/{jobName}/status", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Get Batch Job Execution Status")
    public Map<String, Object> getJobStatus(
            @RequestBody final Map<String, Object> inMap,
            @PathVariable final String jobName) {
        LOGGER.info("Batch Job Status requested for job: {}", jobName);
        return commonStatus(inMap, jobName);
    }

    private Map<String, Object> commonStatus(final Map<String, Object> inMap, final String jobName) {
        Map<String, Object> outMap = new HashMap<>(inMap);
        outMap.put(jobNameMap, jobName);
        outMap.put(inMapCosnt, inMap);
        outMap.put(acctgCycleConst, "2026-Q3");
        outMap.put("status", "COMPLETED");
        outMap.put("exitCode", "0");
        outMap.put("executionTimestamp", LocalDateTime.now().toString());
        LOGGER.info("Batch Job status resolved for job {}: {}", jobName, outMap);
        return outMap;
    }

    // Batch Job History Endpoint (Matching code image line @PostMapping("/jobs/{jobName}/history"))
    @PostMapping(value = "/jobs/{jobName}/history", consumes = "application/json", produces = "application/json")
    @Operation(summary = "Get Batch Job Execution History")
    public Map<String, Object> getJobHistory(
            @RequestBody final Map<String, Object> inMap,
            @PathVariable final String jobName) {
        LOGGER.info("Batch Job History requested for job: {}", jobName);
        return commonJobHistory(inMap, jobName);
    }

    public Map<String, Object> commonJobHistory(
            @RequestBody final Map<String, Object> inMap,
            @PathVariable final String jobName) {
        Map<String, Object> outMap = new HashMap<>(inMap);
        outMap.put(jobNameMap, jobName);
        outMap.put(inMapCosnt, inMap);
        outMap.put(acctgCycleConst, "2026-Q3");
        outMap.put("historyList", List.of(
            Map.of("runId", 101, "status", "SUCCESS", "recordsProcessed", 1250),
            Map.of("runId", 102, "status", "SUCCESS", "recordsProcessed", 1430)
        ));
        LOGGER.info("Batch Job history resolved for job {}: {}", jobName, outMap);
        return outMap;
    }

    // JMS ActiveMQ Publish API
    @PostMapping("/notifications/publish")
    @Operation(summary = "Publish Event to ActiveMQ Queue")
    public ResponseEntity<NotificationDTO> publishNotification(
            @RequestParam(required = false, defaultValue = "UNDERWRITING_EVENT") String eventType,
            @RequestParam(required = false) String destination,
            @RequestBody String payloadJson) {
        return ResponseEntity.ok(jmsPublisher.publishNotification(eventType, destination, payloadJson));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleException(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
            "status", HttpStatus.BAD_REQUEST.value(),
            "error", "Underwriting Engine Error",
            "message", ex.getMessage(),
            "timestamp", LocalDateTime.now()
        ));
    }
}
