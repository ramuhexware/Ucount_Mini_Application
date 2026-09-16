package com.freddieapp.origination.service;

import com.freddieapp.origination.model.LoanApplicationEntity;
import com.freddieapp.origination.model.LoanApplicationEntity.LoanRequest;
import com.freddieapp.origination.model.LoanApplicationEntity.LoanResponse;
import com.freddieapp.origination.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Business Service Layer for Loan Origination Microservice.
 */
@Service
@Transactional
public class LoanOriginationService {

    private final LoanApplicationRepository repository;

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
