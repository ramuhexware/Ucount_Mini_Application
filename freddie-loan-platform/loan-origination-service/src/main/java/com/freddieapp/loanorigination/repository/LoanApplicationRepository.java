package com.freddieapp.loanorigination.repository;

import com.freddieapp.loanorigination.entity.LoanApplication;
import com.freddieapp.loanorigination.enums.LoanStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, String> {

    // ─── ORM Methods (Spring Data JPA) ───────────────────────────────────────

    List<LoanApplication> findByCustomerId(String customerId);

    List<LoanApplication> findByLoanStatus(LoanStatus loanStatus);

    Optional<LoanApplication> findByLoanIdAndLoanStatus(String loanId, LoanStatus loanStatus);

    // ─── Native SELECT Queries (PostgreSQL) ──────────────────────────────────

    /**
     * Fetch all loans for a given customer ordered by application date descending.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.loan_applications
            WHERE customer_id = :customerId
            ORDER BY application_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM freddie_loans.loan_applications
            WHERE customer_id = :customerId
            """,
            nativeQuery = true)
    Page<LoanApplication> findByCustomerIdNative(
            @Param("customerId") String customerId,
            Pageable pageable);

    /**
     * Looks up a single loan by ID and status using native SQL.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.loan_applications
            WHERE loan_id     = :loanId
              AND loan_status = :status
            LIMIT 1
            """,
            nativeQuery = true)
    Optional<LoanApplication> findByLoanIdAndStatusNative(
            @Param("loanId") String loanId,
            @Param("status") String status);

    /**
     * Returns paginated loans filtered by status, ordered newest first.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.loan_applications
            WHERE loan_status = :status
            ORDER BY application_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM freddie_loans.loan_applications
            WHERE loan_status = :status
            """,
            nativeQuery = true)
    Page<LoanApplication> findByLoanStatusNative(
            @Param("status") String status,
            Pageable pageable);

    /**
     * Full loan search by customer ID + optional status + optional loan type filter.
     */
    @Query(value = """
            SELECT * FROM freddie_loans.loan_applications
            WHERE customer_id = :customerId
              AND (:status   IS NULL OR loan_status = :status)
              AND (:loanType IS NULL OR loan_type   = :loanType)
            ORDER BY application_date DESC
            """,
            countQuery = """
            SELECT COUNT(*) FROM freddie_loans.loan_applications
            WHERE customer_id = :customerId
              AND (:status   IS NULL OR loan_status = :status)
              AND (:loanType IS NULL OR loan_type   = :loanType)
            """,
            nativeQuery = true)
    Page<LoanApplication> searchLoansNative(
            @Param("customerId") String customerId,
            @Param("status") String status,
            @Param("loanType") String loanType,
            Pageable pageable);

    /**
     * Retrieves total approved loan amount per customer.
     */
    @Query(value = """
            SELECT customer_id,
                   SUM(approved_amount) AS total_approved,
                   COUNT(*)             AS loan_count
            FROM freddie_loans.loan_applications
            WHERE loan_status IN ('APPROVED', 'DISBURSED')
              AND customer_id = :customerId
            GROUP BY customer_id
            """,
            nativeQuery = true)
    List<Object[]> findCreditExposureByCustomerNative(@Param("customerId") String customerId);

    // ─── Native UPDATE / DML Queries (PostgreSQL) ─────────────────────────────

    /**
     * Transitions a loan to UNDER_REVIEW status.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_loans.loan_applications
               SET loan_status = 'UNDER_REVIEW',
                   updated_at  = CURRENT_TIMESTAMP
             WHERE loan_id     = :loanId
               AND loan_status = 'SUBMITTED'
            """,
            nativeQuery = true)
    int submitForUnderwritingNative(@Param("loanId") String loanId);

    /**
     * Records underwriting approval.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_loans.loan_applications
               SET loan_status      = 'APPROVED',
                   approved_amount  = :approvedAmount,
                   interest_rate    = :interestRate,
                   decision_date    = CURRENT_TIMESTAMP,
                   updated_at       = CURRENT_TIMESTAMP
             WHERE loan_id          = :loanId
               AND loan_status      = 'UNDER_REVIEW'
            """,
            nativeQuery = true)
    int approveLoanNative(
            @Param("loanId") String loanId,
            @Param("approvedAmount") BigDecimal approvedAmount,
            @Param("interestRate") BigDecimal interestRate);

    /**
     * Records underwriting rejection with reason code.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_loans.loan_applications
               SET loan_status       = 'REJECTED',
                   rejection_reason  = :reason,
                   decision_date     = CURRENT_TIMESTAMP,
                   updated_at        = CURRENT_TIMESTAMP
             WHERE loan_id           = :loanId
               AND loan_status       = 'UNDER_REVIEW'
            """,
            nativeQuery = true)
    int rejectLoanNative(
            @Param("loanId") String loanId,
            @Param("reason") String reason);

    /**
     * Marks a loan as DISBURSED after funds are released.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_loans.loan_applications
               SET loan_status        = 'DISBURSED',
                   disbursement_date  = CURRENT_TIMESTAMP,
                   updated_at         = CURRENT_TIMESTAMP
             WHERE loan_id            = :loanId
               AND loan_status        = 'APPROVED'
            """,
            nativeQuery = true)
    int disburseLoanNative(@Param("loanId") String loanId);
}
