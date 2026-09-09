package com.freddieapp.documentservice.service;

import com.freddieapp.documentservice.entity.LoanDocument;
import com.freddieapp.documentservice.enums.DocumentStatus;
import com.freddieapp.documentservice.repository.LoanDocumentRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class DocumentServiceTest {

    @Mock
    private LoanDocumentRepository documentRepository;

    @InjectMocks
    private DocumentService documentService;

    private LoanDocument sampleDoc;

    @Before
    public void setUp() {
        sampleDoc = LoanDocument.builder()
                .documentId("DOC-1001")
                .loanId("LOAN-9001")
                .customerId("CUST-500")
                .documentType("W2")
                .fileName("w2_tax.pdf")
                .mimeType("application/pdf")
                .sizeBytes(2048L)
                .status(DocumentStatus.UPLOADED.name())
                .uploadedBy("SYSTEM_USER")
                .build();
    }

    @Test
    public void testGetDocumentByIdSuccess() {
        when(documentRepository.findByDocumentId("DOC-1001")).thenReturn(Mono.just(sampleDoc));

        Mono<LoanDocument> result = documentService.getDocumentById("DOC-1001");

        StepVerifier.create(result)
                .assertNext(doc -> {
                    assertNotNull(doc);
                    assertEquals("DOC-1001", doc.getDocumentId());
                    assertEquals("LOAN-9001", doc.getLoanId());
                    assertEquals("W2", doc.getDocumentType());
                })
                .verifyComplete();

        verify(documentRepository, times(1)).findByDocumentId("DOC-1001");
    }

    @Test
    public void testGetDocumentByIdNotFound() {
        when(documentRepository.findByDocumentId("INVALID-DOC")).thenReturn(Mono.empty());

        Mono<LoanDocument> result = documentService.getDocumentById("INVALID-DOC");

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void testGetDocumentsByLoan() {
        when(documentRepository.findByLoanId("LOAN-9001")).thenReturn(Flux.just(sampleDoc));

        Flux<LoanDocument> result = documentService.getDocumentsByLoan("LOAN-9001");

        StepVerifier.create(result)
                .expectNext(sampleDoc)
                .verifyComplete();

        verify(documentRepository, times(1)).findByLoanId("LOAN-9001");
    }

    @Test
    public void testDeleteDocument() {
        when(documentRepository.findByDocumentId("DOC-1001")).thenReturn(Mono.just(sampleDoc));
        when(documentRepository.delete(sampleDoc)).thenReturn(Mono.empty());

        Mono<Void> result = documentService.deleteDocument("DOC-1001");

        StepVerifier.create(result)
                .verifyComplete();

        verify(documentRepository, times(1)).delete(sampleDoc);
    }
}
