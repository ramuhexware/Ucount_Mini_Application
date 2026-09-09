package com.freddieapp.underwriting.repository;

import com.freddieapp.underwriting.entity.UnderwritingAssessment;
import com.freddieapp.underwriting.enums.Decision;
import com.freddieapp.underwriting.enums.RiskLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface UnderwritingAssessmentRepository extends JpaRepository<UnderwritingAssessment, String> {

    // ─── ORM Methods (Spring Data JPA) ───────────────────────────────────────

    Optional<UnderwritingAssessment> findByLoanId(String loanId);

    List<UnderwritingAssessment> findByCustomerId(String customerId);

    List<UnderwritingAssessment> findByDecision(Decision decision);

    List<UnderwritingAssessment> findByRiskLevel(RiskLevel riskLevel);

    // ─── Native SELECT Queries (PostgreSQL) ──────────────────────────────────

    /**
     * Retrieves the most recent underwriting assessment for a given loan using PostgreSQL native SQL.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.underwriting_assessments
            WHERE loan_id = :loanId
            ORDER BY assessed_at DESC
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<UnderwritingAssessment> findLatestByLoanIdNative(@Param("loanId") String loanId);

    /**
     * Retrieves all assessments for a customer, ordered newest first with pagination.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.underwriting_assessments
            WHERE customer_id = :customerId
            ORDER BY assessed_at DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM freddie_loans.underwriting_assessments
            WHERE customer_id = :customerId
            """,
            nativeQuery = true)
    Page<UnderwritingAssessment> findByCustomerIdNative(
            @Param("customerId") String customerId,
            Pageable pageable);

    /**
     * Finds all HIGH or CRITICAL risk assessments pending manual review.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.underwriting_assessments
            WHERE risk_level IN ('HIGH', 'CRITICAL')
              AND decision  = 'REFERRED'
            ORDER BY assessed_at ASC
            LIMIT :limit
            """,
            nativeQuery = true)
    List<UnderwritingAssessment> findHighRiskReferralsNative(@Param("limit") int limit);

    /**
     * Calculates average DTI and LTV ratios grouped by decision type.
     */
    @Query(value = """
            SELECT decision,
                   AVG(dti_ratio)        AS avg_dti,
                   AVG(ltv_ratio)        AS avg_ltv,
                   COUNT(*)              AS assessment_count
            FROM freddie_loans.underwriting_assessments
            GROUP BY decision
            ORDER BY decision
            """,
            nativeQuery = true)
    List<Object[]> getRiskAnalyticsSummaryNative();

    // ─── Native UPDATE / DML Queries (PostgreSQL) ───────────────────────────

    /**
     * Records the final underwriting decision and risk level for a given assessment.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_loans.underwriting_assessments
               SET decision          = :decision,
                   risk_level        = :riskLevel,
                   decision_reason   = :reason,
                   assessed_by       = :assessedBy,
                   assessed_at       = CURRENT_TIMESTAMP
             WHERE assessment_id     = :assessmentId
            """,
            nativeQuery = true)
    int recordDecisionNative(
            @Param("assessmentId") String assessmentId,
            @Param("decision") String decision,
            @Param("riskLevel") String riskLevel,
            @Param("reason") String reason,
            @Param("assessedBy") String assessedBy);

    /**
     * Updates credit bureau reference after an external credit pull completes.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_loans.underwriting_assessments
               SET bureau_ref   = :bureauRef,
                   credit_score = :creditScore
             WHERE assessment_id = :assessmentId
            """,
            nativeQuery = true)
    int updateCreditBureauDataNative(
            @Param("assessmentId") String assessmentId,
            @Param("bureauRef") String bureauRef,
            @Param("creditScore") int creditScore);
}
