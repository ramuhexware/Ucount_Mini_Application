package com.freddieapp.origination.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity and Record DTOs for Loan Origination Microservice.
 */
@Entity
@Table(name = "loan_applications")
public class LoanApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerId;

    @Column(nullable = false)
    private String applicantName;

    @Column(nullable = false)
    private String email;

    private BigDecimal loanAmount;
    private BigDecimal propertyValue;
    private BigDecimal monthlyIncome;
    private BigDecimal monthlyDebt;
    private Integer creditScore;
    private Integer termMonths;
    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public LoanApplicationEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.status = "SUBMITTED";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
    public String getApplicantName() { return applicantName; }
    public void setApplicantName(String applicantName) { this.applicantName = applicantName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public BigDecimal getLoanAmount() { return loanAmount; }
    public void setLoanAmount(BigDecimal loanAmount) { this.loanAmount = loanAmount; }
    public BigDecimal getPropertyValue() { return propertyValue; }
    public void setPropertyValue(BigDecimal propertyValue) { this.propertyValue = propertyValue; }
    public BigDecimal getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }
    public BigDecimal getMonthlyDebt() { return monthlyDebt; }
    public void setMonthlyDebt(BigDecimal monthlyDebt) { this.monthlyDebt = monthlyDebt; }
    public Integer getCreditScore() { return creditScore; }
    public void setCreditScore(Integer creditScore) { this.creditScore = creditScore; }
    public Integer getTermMonths() { return termMonths; }
    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Domain DTOs
    public record LoanRequest(
        String customerId,
        String applicantName,
        String email,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        BigDecimal monthlyIncome,
        BigDecimal monthlyDebt,
        Integer creditScore,
        Integer termMonths
    ) {}

    public record LoanResponse(
        Long loanId,
        String customerId,
        String applicantName,
        String email,
        BigDecimal loanAmount,
        BigDecimal propertyValue,
        Integer creditScore,
        String status,
        LocalDateTime createdAt
    ) {}
}
