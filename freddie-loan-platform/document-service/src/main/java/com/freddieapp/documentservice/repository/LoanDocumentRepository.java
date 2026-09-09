package com.freddieapp.documentservice.repository;

import com.freddieapp.documentservice.entity.LoanDocument;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface LoanDocumentRepository extends R2dbcRepository<LoanDocument, String> {

    Flux<LoanDocument> findByLoanId(String loanId);

    Flux<LoanDocument> findByLoanIdAndDocumentType(String loanId, String documentType);

    Mono<LoanDocument> findByDocumentId(String documentId);

    @Query("SELECT * FROM freddie_customer.loan_documents WHERE customer_id = :customerId AND status = :status")
    Flux<LoanDocument> findByCustomerIdAndStatus(String customerId, String status, Pageable pageable);

    @Query("SELECT document_type AS type, COUNT(*) AS count, SUM(size_bytes) AS total_size " +
           "FROM freddie_customer.loan_documents WHERE loan_id = :loanId GROUP BY document_type")
    Flux<Object> aggregateDocumentSummaryByType(String loanId);
}
