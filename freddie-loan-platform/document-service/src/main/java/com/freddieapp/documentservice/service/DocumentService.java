package com.freddieapp.documentservice.service;

import com.freddieapp.documentservice.entity.LoanDocument;
import com.freddieapp.documentservice.enums.DocumentStatus;
import com.freddieapp.documentservice.repository.LoanDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final LoanDocumentRepository documentRepository;

    public Mono<LoanDocument> uploadDocument(String loanId, String customerId, String documentType, Mono<FilePart> filePartMono) {
        return filePartMono.flatMap(filePart -> {
            String documentId = UUID.randomUUID().toString();
            String fileName = filePart.filename();
            log.info("Uploading document reactive to PostgreSQL R2DBC: filename={}, type={}, loanId={}", fileName, documentType, loanId);

            return DataBufferUtils.join(filePart.content())
                    .flatMap(dataBuffer -> {
                        byte[] bytes = new byte[dataBuffer.readableByteCount()];
                        dataBuffer.read(bytes);
                        DataBufferUtils.release(dataBuffer);

                        LoanDocument doc = LoanDocument.builder()
                                .documentId(documentId)
                                .loanId(loanId)
                                .customerId(customerId)
                                .documentType(documentType)
                                .fileName(fileName)
                                .mimeType(filePart.headers().getContentType() != null ? filePart.headers().getContentType().toString() : "application/octet-stream")
                                .sizeBytes((long) bytes.length)
                                .fileData(bytes)
                                .status(DocumentStatus.UPLOADED.name())
                                .uploadedBy("SYSTEM_USER")
                                .build();
                        return documentRepository.save(doc);
                    });
        });
    }

    public Mono<LoanDocument> getDocumentById(String documentId) {
        return documentRepository.findByDocumentId(documentId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Document not found: " + documentId)));
    }

    public Flux<LoanDocument> getDocumentsByLoan(String loanId) {
        return documentRepository.findByLoanId(loanId);
    }

    public Flux<LoanDocument> getDocumentsByCustomerAndStatus(String customerId, DocumentStatus status, Pageable pageable) {
        return documentRepository.findByCustomerIdAndStatus(customerId, status.name(), pageable);
    }

    public Flux<Object> getDocumentSummaryByType(String loanId) {
        return documentRepository.aggregateDocumentSummaryByType(loanId);
    }

    public Mono<Void> deleteDocument(String documentId) {
        return documentRepository.findByDocumentId(documentId)
                .flatMap(doc -> {
                    log.info("Deleting document from PostgreSQL: documentId={}", documentId);
                    return documentRepository.delete(doc);
                });
    }
}
