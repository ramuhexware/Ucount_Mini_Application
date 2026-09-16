package com.freddieapp.origination.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JPA Entity, Enums, and Record DTOs for Loan Origination, Stage 1/2 Intake, and Account Services.
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

    // Enums
    public enum UserType { HOUSE_SELLER, HOUSE_BUYER, INSURANCE_PERSON, MORTGAGE_SERVICER }
    public enum Stage1Status { PENDING_APPROVAL, APPROVED, REJECTED }

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

    // Account Creation & Lookup DTOs (from code image)
    public record ResponseStatusDTO(int statusCode, String statusMessage) {
        public ResponseStatusDTO getStatusCode() { return this; }
        public int value() { return statusCode; }
    }

    public static class AccountSaveDTO {
        private String idCntprtyAcct;
        private String accountName;
        private String orgName;
        private ResponseStatusDTO respSts;

        public AccountSaveDTO() {}
        public AccountSaveDTO(String idCntprtyAcct, String accountName, String orgName, ResponseStatusDTO respSts) {
            this.idCntprtyAcct = idCntprtyAcct;
            this.accountName = accountName;
            this.orgName = orgName;
            this.respSts = respSts;
        }

        public String getIdCntprtyAcct() { return idCntprtyAcct; }
        public void setIdCntprtyAcct(String idCntprtyAcct) { this.idCntprtyAcct = idCntprtyAcct; }
        public String getAccountName() { return accountName; }
        public void setAccountName(String accountName) { this.accountName = accountName; }
        public String getOrgName() { return orgName; }
        public void setOrgName(String orgName) { this.orgName = orgName; }
        public ResponseStatusDTO getRespSts() { return respSts; }
        public void setRespSts(ResponseStatusDTO respSts) { this.respSts = respSts; }
    }

    public record AccountLookupDTO(List<String> accountTypes, List<String> orgTypes, Map<String, String> referenceTables) {}
    public record AccountProfileReqDTO(String idCntprtyAcct) {}
    public record AccountProfileRespDTO(String idCntprtyAcct, String profileStatus, List<String> permissions) {}

    // DTOs matching code image lines 43-76
    public static class UcsProdtDTO {
        private Integer idProdt;
        private String nameProdt;
        public UcsProdtDTO() {}
        public UcsProdtDTO(Integer idProdt, String nameProdt) { this.idProdt = idProdt; this.nameProdt = nameProdt; }
        public Integer getIdProdt() { return idProdt; }
        public void setIdProdt(Integer idProdt) { this.idProdt = idProdt; }
        public String getNameProdt() { return nameProdt; }
        public void setNameProdt(String nameProdt) { this.nameProdt = nameProdt; }
    }

    public static class UcsOrgtnRoleDTO {
        private Integer idOrgtnRole;
        private String nameOrgtnRole;
        public UcsOrgtnRoleDTO() {}
        public UcsOrgtnRoleDTO(Integer idOrgtnRole, String nameOrgtnRole) { this.idOrgtnRole = idOrgtnRole; this.nameOrgtnRole = nameOrgtnRole; }
        public Integer getIdOrgtnRole() { return idOrgtnRole; }
        public void setIdOrgtnRole(Integer idOrgtnRole) { this.idOrgtnRole = idOrgtnRole; }
        public String getNameOrgtnRole() { return nameOrgtnRole; }
        public void setNameOrgtnRole(String nameOrgtnRole) { this.nameOrgtnRole = nameOrgtnRole; }
    }

    public static class UcsLineOfBusinessDTO {
        private Integer idLiOfBus;
        private String nameLiOfBus;
        public UcsLineOfBusinessDTO() {}
        public UcsLineOfBusinessDTO(Integer idLiOfBus, String nameLiOfBus) { this.idLiOfBus = idLiOfBus; this.nameLiOfBus = nameLiOfBus; }
        public Integer getIdLiOfBus() { return idLiOfBus; }
        public void setIdLiOfBus(Integer idLiOfBus) { this.idLiOfBus = idLiOfBus; }
        public String getNameLiOfBus() { return nameLiOfBus; }
        public void setNameLiOfBus(String nameLiOfBus) { this.nameLiOfBus = nameLiOfBus; }
    }

    public static class AccountLookupUpdateDTO {
        private List<UcsLineOfBusinessDTO> ucsLineOfBusinessDTOs = new ArrayList<>();
        private Map<String, List<UcsOrgtnRoleDTO>> ucsOrgtnRoleDTOs = new HashMap<>();
        private Map<String, List<UcsProdtDTO>> ucsProdtDTOs = new HashMap<>();
        private List<String> ucsCntprtyAcctSt = new ArrayList<>();
        private List<String> ucsCntprtyAcctRoleSt = new ArrayList<>();
        private ResponseStatusDTO respSts;

        public AccountLookupUpdateDTO() {}

        public List<UcsLineOfBusinessDTO> getUcsLineOfBusinessDTOs() { return ucsLineOfBusinessDTOs; }
        public void setUcsLineOfBusinessDTOs(List<UcsLineOfBusinessDTO> dtos) { this.ucsLineOfBusinessDTOs = dtos; }
        public Map<String, List<UcsOrgtnRoleDTO>> getUcsOrgtnRoleDTOs() { return ucsOrgtnRoleDTOs; }
        public void setUcsOrgtnRoleDTOs(Map<String, List<UcsOrgtnRoleDTO>> map) { this.ucsOrgtnRoleDTOs = map; }
        public Map<String, List<UcsProdtDTO>> getUcsProdtDTOs() { return ucsProdtDTOs; }
        public void setUcsProdtDTOs(Map<String, List<UcsProdtDTO>> map) { this.ucsProdtDTOs = map; }
        public List<String> getUcsCntprtyAcctSt() { return ucsCntprtyAcctSt; }
        public void setUcsCntprtyAcctSt(List<String> list) { this.ucsCntprtyAcctSt = list; }
        public List<String> getUcsCntprtyAcctRoleSt() { return ucsCntprtyAcctRoleSt; }
        public void setUcsCntprtyAcctRoleSt(List<String> list) { this.ucsCntprtyAcctRoleSt = list; }
        public ResponseStatusDTO getRespSts() { return respSts; }
        public void setRespSts(ResponseStatusDTO respSts) { this.respSts = respSts; }
    }

    // Stage 1 & Stage 2 Records
    public record Stage1OnboardRequest(String orgName, String taxId, String email, String phone, String address, String networkDomain) {}
    public record Stage1UserResponse(String userId, String orgName, String email, Stage1Status status, LocalDateTime createdAt) {}
    public record Stage2ProfileRequest(String userId, UserType userType, String ratingHistory, BigDecimal annualRevenue, int yearsInBusiness) {}
    public record Stage2AccessRightsResponse(String userId, UserType userType, List<String> accessRights) {}
}
