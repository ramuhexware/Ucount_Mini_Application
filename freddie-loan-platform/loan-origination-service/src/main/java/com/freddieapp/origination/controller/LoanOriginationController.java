package com.freddieapp.origination.controller;

import com.freddieapp.origination.dto.*;
import com.freddieapp.origination.pdf.LoanSummaryPdfExporter;
import com.freddieapp.origination.service.LoanOriginationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class LoanOriginationController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoanOriginationController.class);
    private final LoanOriginationService service;
    private final LoanSummaryPdfExporter pdfExporter;

    @Autowired
    public LoanOriginationController(LoanOriginationService service, LoanSummaryPdfExporter pdfExporter) {
        this.service = service;
        this.pdfExporter = pdfExporter;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields("id", "status", "createdAt");
    }

    @PostMapping("/loans")
    public ResponseEntity<LoanResponseDTO> createLoan(@RequestBody LoanRequestDTO request) {
        LOGGER.info("REST: Request to create loan application for customer: {}", request.customerId());
        LoanResponseDTO response = service.createLoanApplication(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/loans/{id}")
    public ResponseEntity<LoanResponseDTO> getLoanById(@PathVariable Long id) {
        LOGGER.info("REST: Request to fetch loan by ID: {}", id);
        return ResponseEntity.ok(service.getLoanById(id));
    }

    @GetMapping(value = "/loans/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> downloadLoanSummaryPdf(@PathVariable Long id) {
        LOGGER.info("REST: Exporting Loan Summary PDF for loan ID: {}", id);
        LoanResponseDTO loan = service.getLoanById(id);
        byte[] pdfBytes = pdfExporter.generateLoanSummaryPdf(loan);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=Loan_Summary_" + id + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes);
    }

    @GetMapping("/loans/customer/{customerId}")
    public ResponseEntity<List<LoanResponseDTO>> getLoansByCustomer(@PathVariable String customerId) {
        LOGGER.info("REST: Request to fetch loans for customer: {}", customerId);
        return ResponseEntity.ok(service.getLoansByCustomerId(customerId));
    }

    @PostMapping("/loans/{id}/submit-underwriting")
    public ResponseEntity<LoanResponseDTO> submitForUnderwritingNative(@PathVariable Long id) {
        LOGGER.info("REST: Native SQL trigger to transition loan {} to UNDER_REVIEW", id);
        return ResponseEntity.ok(service.submitForUnderwritingNative(id));
    }

    // Account Lookup Endpoints
    @GetMapping("/account/lookup")
    public ResponseEntity<AccountLookupDTO> getLookupData() {
        return ResponseEntity.ok(service.getLookupData());
    }

    @GetMapping("/account/lookup/update")
    public ResponseEntity<AccountLookupUpdateDTO> getLookupUpdateData() {
        return ResponseEntity.ok(service.getLookupUpdateData());
    }

    @PostMapping("/account/create")
    public ResponseEntity<AccountSaveDTO> createAccount(@RequestBody AccountSaveDTO accountSaveDTO) {
        return ResponseEntity.ok(service.createAccount(accountSaveDTO));
    }

    @PostMapping("/account/profile")
    public ResponseEntity<AccountProfileRespDTO> getAccountProfile(@RequestBody AccountProfileReqDTO req) {
        return ResponseEntity.ok(service.getAccountProfile(req));
    }

    // Stage 1 Counterparty Intake Endpoints
    @PostMapping("/counterparty/stage1/onboard")
    public ResponseEntity<Stage1UserResponseDTO> onboardStage1User(@RequestBody Stage1OnboardRequestDTO req) {
        return new ResponseEntity<>(service.onboardStage1User(req), HttpStatus.CREATED);
    }

    @PostMapping("/counterparty/stage1/approve/{userId}")
    public ResponseEntity<Stage1UserResponseDTO> approveStage1User(@PathVariable String userId) {
        return ResponseEntity.ok(service.approveStage1User(userId));
    }

    @GetMapping("/counterparty/stage1/pending")
    public ResponseEntity<List<Stage1UserResponseDTO>> getPendingStage1Users() {
        return ResponseEntity.ok(service.getPendingStage1Users());
    }

    // Stage 2 Profile & Access Rights Endpoints
    @PostMapping("/counterparty/stage2/profile")
    public ResponseEntity<Stage2AccessRightsResponseDTO> saveStage2Profile(@RequestBody Stage2ProfileRequestDTO req) {
        return ResponseEntity.ok(service.saveStage2Profile(req));
    }

    @GetMapping("/counterparty/stage2/access-rights/{userId}")
    public ResponseEntity<Stage2AccessRightsResponseDTO> getStage2AccessRights(@PathVariable String userId) {
        return ResponseEntity.ok(service.getStage2AccessRights(userId));
    }
}
