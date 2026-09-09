package com.freddieapp.documentservice.controller;

import com.freddieapp.documentservice.config.EmailNotificationClientConfig;
import com.freddieapp.documentservice.entity.LoanDocument;
import com.freddieapp.documentservice.enums.DocumentStatus;
import com.freddieapp.documentservice.service.DocumentService;
import com.freddieapp.documentservice.service.EmailNotificationClientServicer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Document Management", description = "Reactive APIs for loan documents storage and indexing in PostgreSQL R2DBC")
public class DocumentController {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    private final DocumentService documentService;

    @Autowired
    EmailNotificationClientServicer emailNotificationClientServicer;

    @Autowired
    EmailNotificationClientConfig emailNotificationClientConfig;

    @Value("${bypassPingAuth:false}")
    private String bypassPingAuth;

    @Autowired
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder) {
        binder.setDisallowedFields();
    }

    @Operation(summary = "Upload document reactive to GridFS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public @ResponseBody Mono<LoanDocument> uploadDocument(
            @RequestPart("loanId") String loanId,
            @RequestPart("customerId") String customerId,
            @RequestPart("documentType") String documentType,
            @RequestPart("file") Mono<FilePart> filePartMono) {
        LOGGER.info("OrgAPI: Upload Document...");
        return documentService.uploadDocument(loanId, customerId, documentType, filePartMono)
                .doOnSuccess(doc -> {
                    if (emailNotificationClientServicer != null && doc != null) {
                        emailNotificationClientServicer.sendEmailNotification("docs@freddiemac.com", 
                                "Document Uploaded", "Document ID " + doc.getDocumentId() + " uploaded for loan " + loanId);
                    }
                });
    }

    @Operation(summary = "Get document metadata by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/{documentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Mono<ResponseEntity<LoanDocument>> getDocumentMetadata(@PathVariable String documentId) {
        LOGGER.info("OrgAPI: Get Document Metadata...");
        return documentService.getDocumentById(documentId)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Get all documents for a specific loan")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/loan/{loanId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Flux<LoanDocument> getDocumentsByLoan(@PathVariable String loanId) {
        LOGGER.info("OrgAPI: Get Documents By Loan...");
        return documentService.getDocumentsByLoan(loanId);
    }

    @Operation(summary = "Get paginated customer documents by status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/customer/{customerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Flux<LoanDocument> getCustomerDocuments(
            @PathVariable String customerId,
            @RequestParam(defaultValue = "UPLOADED") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        LOGGER.info("OrgAPI: Get Customer Documents...");
        return documentService.getDocumentsByCustomerAndStatus(
                customerId,
                DocumentStatus.valueOf(status),
                PageRequest.of(page, size));
    }

    @Operation(summary = "Get document count/size summary aggregated by type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @GetMapping(value = "/summary/loan/{loanId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody Flux<Object> getDocumentSummaryByType(@PathVariable String loanId) {
        LOGGER.info("OrgAPI: Get Document Summary By Type...");
        return documentService.getDocumentSummaryByType(loanId);
    }

    @Operation(summary = "Delete document and its binary from GridFS")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "400", description = "Bad Request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "500", description = "Internal Server Error") })
    @DeleteMapping(value = "/{documentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public @ResponseBody Mono<Void> deleteDocument(@PathVariable String documentId) {
        LOGGER.info("OrgAPI: Delete Document...");
        return documentService.deleteDocument(documentId);
    }
}
