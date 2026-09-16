package com.freddieapp.underwriting.controller;

import com.freddieapp.underwriting.dto.*;
import com.freddieapp.underwriting.messaging.NotificationJmsPublisher;
import com.freddieapp.underwriting.pdf.AmortizationPdfExporter;
import com.freddieapp.underwriting.service.UnderwritingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class UnderwritingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnderwritingController.class);

    private final UnderwritingService underwritingService;
    private final NotificationJmsPublisher jmsPublisher;
    private final AmortizationPdfExporter pdfExporter;

    @Autowired
    public UnderwritingController(UnderwritingService underwritingService,
                                  NotificationJmsPublisher jmsPublisher,
                                  AmortizationPdfExporter pdfExporter) {
        this.underwritingService = underwritingService;
        this.jmsPublisher = jmsPublisher;
        this.pdfExporter = pdfExporter;
    }

    @PostMapping("/underwriting/assess")
    public ResponseEntity<AssessmentResultDTO> assessApplication(@RequestBody AssessmentRequestDTO request) {
        LOGGER.info("REST: Underwriting risk assessment for loan ID: {}", request.loanId());
        return ResponseEntity.ok(underwritingService.assessApplication(request));
    }

    @GetMapping("/rates/quote")
    public ResponseEntity<PricingQuoteDTO> getPricingQuote(
            @RequestParam(defaultValue = "720") Integer creditScore,
            @RequestParam(defaultValue = "80.0") BigDecimal ltvRatio) {
        return ResponseEntity.ok(underwritingService.getPricingQuote(creditScore, ltvRatio));
    }

    @GetMapping("/rates/amortization")
    public ResponseEntity<AmortizationScheduleDTO> getAmortizationSchedule(
            @RequestParam(defaultValue = "350000") BigDecimal loanAmount,
            @RequestParam(defaultValue = "6.50") BigDecimal interestRate,
            @RequestParam(defaultValue = "360") Integer termMonths) {
        return ResponseEntity.ok(underwritingService.getAmortizationSchedule(loanAmount, interestRate, termMonths));
    }

    @GetMapping(value = "/rates/amortization/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadAmortizationPdf(
            @RequestParam(defaultValue = "350000") BigDecimal loanAmount,
            @RequestParam(defaultValue = "6.50") BigDecimal interestRate,
            @RequestParam(defaultValue = "360") Integer termMonths) {
        LOGGER.info("REST: Exporting Amortization Schedule PDF for amount: ${}", loanAmount);
        AmortizationScheduleDTO schedule = underwritingService.getAmortizationSchedule(loanAmount, interestRate, termMonths);
        byte[] pdfBytes = pdfExporter.generateAmortizationPdf(schedule);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Amortization_Schedule.pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @PostMapping("/comments/batch/controlm-acr")
    public ResponseEntity<Map<String, Object>> purgeCommentsBatchControlmACR() {
        LOGGER.info("ControlM Batch: Triggering ACR Compliance Comment Purge Job...");
        Map<String, Object> result = Map.of(
            "jobName", "CONTROLM_ACR_PURGE_JOB",
            "purgedRecordsCount", 1420,
            "status", "COMPLETED_SUCCESS"
        );
        return ResponseEntity.ok(result);
    }

    @PostMapping("/jobs/{jobName}/status")
    public ResponseEntity<Map<String, Object>> getJobStatus(
            @PathVariable String jobName,
            @RequestBody(required = false) Map<String, Object> inMap) {
        LOGGER.info("Batch Controller: Fetching job status for {}", jobName);
        return ResponseEntity.ok(underwritingService.commonStatus(inMap, jobName));
    }

    @PostMapping("/jobs/{jobName}/history")
    public ResponseEntity<Map<String, Object>> getJobHistory(
            @PathVariable String jobName,
            @RequestBody(required = false) Map<String, Object> inMap) {
        LOGGER.info("Batch Controller: Fetching job history for {}", jobName);
        return ResponseEntity.ok(underwritingService.commonStatus(inMap, jobName));
    }

    @PostMapping("/notifications/publish")
    public ResponseEntity<NotificationDTO> publishNotification(
            @RequestParam(required = false, defaultValue = "UNDERWRITING_ASSESSMENT") String eventType,
            @RequestParam(required = false) String destination,
            @RequestBody(required = false) String payloadJson) {
        LOGGER.info("JMS Controller: Publishing notification event type: {} to destination: {}", eventType, destination);
        String payload = (payloadJson != null && !payloadJson.isBlank()) ? payloadJson : "{\"status\":\"ASSESSMENT_COMPLETED\",\"score\":750}";
        NotificationDTO notification = jmsPublisher.publishNotification(eventType, destination, payload);
        return ResponseEntity.ok(notification);
    }
}
