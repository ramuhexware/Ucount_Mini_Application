package com.freddieapp.underwriting.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "underwriting_audit_logs")
public class UnderwritingAuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long loanId;

    @Column(nullable = false)
    private String decision;

    @Column(nullable = false)
    private String riskLevel;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    public UnderwritingAuditLogEntity() {}

    public UnderwritingAuditLogEntity(Long loanId, String decision, String riskLevel) {
        this.loanId = loanId;
        this.decision = decision;
        this.riskLevel = riskLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLoanId() { return loanId; }
    public void setLoanId(Long loanId) { this.loanId = loanId; }
    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
