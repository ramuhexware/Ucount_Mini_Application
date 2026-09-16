package com.freddieapp.underwriting.controller;

import com.freddieapp.underwriting.messaging.NotificationJmsPublisher;
import com.freddieapp.underwriting.messaging.NotificationJmsPublisher.NotificationDTO;
import com.freddieapp.underwriting.processor.RateCalculatorProcessor;
import com.freddieapp.underwriting.processor.RateCalculatorProcessor.PricingTier;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor.Decision;
import com.freddieapp.underwriting.processor.UnderwritingRuleProcessor.RiskLevel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * REST Controller for Underwriting, Pricing, and JMS Notification publishing.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Underwriting & Pricing APIs", description = "Endpoints for Risk Assessment Rules, Pricing Tiers, and JMS Notifications")
public class UnderwritingController {

    private final UnderwritingRuleProcessor underwritingProcessor;
    private final RateCalculatorProcessor rateProcessor;
    private final NotificationJmsPublisher jmsPublisher;

    @Autowired
    public UnderwritingController(
            UnderwritingRuleProcessor underwritingProcessor,
            RateCalculatorProcessor rateProcessor,
            NotificationJmsPublisher jmsPublisher) {
        this.underwritingProcessor = underwritingProcessor;
        this.rateProcessor = rateProcessor;
        this.jmsPublisher = jmsPublisher;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id", "decision", "riskLevel");
    }

    public record UnderwritingRequest(Long loanId, BigDecimal monthlyIncome, BigDecimal monthlyDebt, BigDecimal loanAmount, BigDecimal propertyValue, Integer creditScore) {}
    public record UnderwritingResponse(Long loanId, Decision decision, RiskLevel riskLevel, BigDecimal dtiRatio, BigDecimal ltvRatio, String remarks) {}

    public record RateRequest(BigDecimal loanAmount, BigDecimal propertyValue, Integer creditScore, Integer termMonths) {}
    public record RateResponse(PricingTier pricingTier, BigDecimal baseRate, BigDecimal adjustedRate, BigDecimal monthlyEmi, BigDecimal ltvRatio) {}

    @PostMapping("/underwriting/assess")
    @Operation(summary = "Execute Rule Engine Risk Scoring & Decisioning")
    public ResponseEntity<UnderwritingResponse> assessLoan(@Valid @RequestBody UnderwritingRequest request) {
        BigDecimal dti = underwritingProcessor.calculateDti(request.monthlyDebt(), request.monthlyIncome());
        BigDecimal ltv = underwritingProcessor.calculateLtv(request.loanAmount(), request.propertyValue());

        Decision decision = underwritingProcessor.evaluateDecision(request.creditScore(), dti, ltv);
        RiskLevel riskLevel = underwritingProcessor.evaluateRiskLevel(decision, dti, ltv);

        UnderwritingResponse response = new UnderwritingResponse(
            request.loanId(),
            decision,
            riskLevel,
            dti,
            ltv,
            "Automated Underwriting rule engine evaluation completed with decision: " + decision
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/rates/calculate")
    @Operation(summary = "Calculate Real-Time Interest Rate Quote & EMI")
    public ResponseEntity<RateResponse> calculateRateQuote(@Valid @RequestBody RateRequest request) {
        BigDecimal ltv = underwritingProcessor.calculateLtv(request.loanAmount(), request.propertyValue());
        PricingTier tier = rateProcessor.determinePricingTier(request.creditScore());
        BigDecimal rate = rateProcessor.calculateAdjustedRate(request.creditScore(), ltv);
        BigDecimal emi = rateProcessor.calculateMonthlyEmi(request.loanAmount(), rate, request.termMonths() != null ? request.termMonths() : 360);

        return ResponseEntity.ok(new RateResponse(tier, new BigDecimal("6.25"), rate, emi, ltv));
    }

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
