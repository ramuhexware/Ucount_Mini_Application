package com.freddieapp.origination.repository;

import com.freddieapp.origination.model.LoanApplicationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository layer enforcing Dual Data Access Pattern (Spring Data JPA ORM + PostgreSQL Native Queries),
 * including AccountsProfileRepository queries matching the code screenshot.
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {

    // Spring Data JPA ORM Methods
    List<LoanApplicationEntity> findByCustomerId(String customerId);

    Page<LoanApplicationEntity> findDistinctByCustomerId(String customerId, Pageable pageable);

    Page<LoanApplicationEntity> findDistinctByCustomerIdAndStatus(String customerId, String status, Pageable pageable);

    // AccountsProfileRepository Native Queries (Matching Code Image lines 27-32)
    @Query(nativeQuery = true, value = "SELECT DISTINCT acct.ID_CNTPRTY_ACCT, acct.NAME_CNTPRTY_ACCT, acct.ID_ORGTN FROM UCS_CNTPRTY_ACCT_ROLE_ASSN assn, UCS_CNTPRTY_ACCT acct WHERE acct.ID_CNTPRTY_ACCT = assn.ID_CNTPRTY_ACCT")
    List<Object[]> findAllActiveAccounts();

    @Query(nativeQuery = true, value = "SELECT DISTINCT acct.ID_CNTPRTY_ACCT, acct.NAME_CNTPRTY_ACCT, acct.ID_ORGTN FROM UCS_CNTPRTY_ACCT_ROLE_ASSN assn, UCS_CNTPRTY_ACCT acct WHERE acct.ID_CNTPRTY_ACCT = assn.ID_CNTPRTY_ACCT AND acct.ID_ORGTN = :orgId")
    List<Object[]> findAllActiveAccountsOfOrg(@Param("orgId") Integer orgId);

    // PostgreSQL Native Query Status Update
    @Modifying
    @Query(value = "UPDATE loan_applications SET status = :status, updated_at = NOW() WHERE id = :loanId", nativeQuery = true)
    int updateStatusNative(@Param("loanId") Long loanId, @Param("status") String status);
}
