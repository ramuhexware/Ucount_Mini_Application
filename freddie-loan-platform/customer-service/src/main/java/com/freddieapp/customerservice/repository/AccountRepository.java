package com.freddieapp.customerservice.repository;

import com.freddieapp.customerservice.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String>,
        JpaSpecificationExecutor<Account> {

    // ─── ORM Methods (Spring Data JPA) ───────────────────────────────────────

    List<Account> findByIdOrgtn(Integer idOrgtn);

    Page<Account> findByIdOrgtn(Integer idOrgtn, Pageable pageable);

    Optional<Account> findByIdCntprtyAcctAndIdOrgtn(String idCntprtyAcct, Integer idOrgtn);

    List<Account> findByAcctSts(String acctSts);

    // ─── Native SELECT Queries (PostgreSQL) ──────────────────────────────────

    /**
     * Retrieves all active counterparty accounts joined with role associations.
     */
    @Query(value = """
            SELECT DISTINCT acct.id_cntprty_acct, acct.name_cntprty_acct, acct.id_orgtn
            FROM freddie_customer.ucs_cntprty_acct acct
            INNER JOIN freddie_customer.ucs_cntprty_acct_role_assn assn
                    ON acct.id_cntprty_acct = assn.id_cntprty_acct
            WHERE acct.acct_sts = 'ACTIVE'
            """,
            nativeQuery = true)
    List<Object[]> findAllActiveAccountsNative();

    /**
     * Retrieves all active counterparty accounts for a specific organization.
     */
    @Query(value = """
            SELECT DISTINCT acct.id_cntprty_acct, acct.name_cntprty_acct, acct.id_orgtn
            FROM freddie_customer.ucs_cntprty_acct acct
            INNER JOIN freddie_customer.ucs_cntprty_acct_role_assn assn
                    ON acct.id_cntprty_acct = assn.id_cntprty_acct
            WHERE acct.id_orgtn = :orgId
              AND acct.acct_sts = 'ACTIVE'
            """,
            nativeQuery = true)
    List<Object[]> findAllActiveAccountsOfOrgNative(@Param("orgId") Integer orgId);

    /**
     * Paginated fetch of accounts filtered by Organization ID and Role Status.
     */
    @Query(value = """
            SELECT acct.* FROM freddie_customer.ucs_cntprty_acct acct
            INNER JOIN freddie_customer.ucs_cntprty_acct_role_assn assn
                    ON acct.id_cntprty_acct = assn.id_cntprty_acct
            WHERE acct.id_orgtn = :orgId
              AND assn.role_status = :roleStatus
            ORDER BY acct.name_cntprty_acct ASC
            """,
            countQuery = """
            SELECT COUNT(DISTINCT acct.id_cntprty_acct)
            FROM freddie_customer.ucs_cntprty_acct acct
            INNER JOIN freddie_customer.ucs_cntprty_acct_role_assn assn
                    ON acct.id_cntprty_acct = assn.id_cntprty_acct
            WHERE acct.id_orgtn = :orgId
              AND assn.role_status = :roleStatus
            """,
            nativeQuery = true)
    Page<Account> findByOrgIdAndRoleStatusNative(
            @Param("orgId") Integer orgId,
            @Param("roleStatus") String roleStatus,
            Pageable pageable);

    // ─── Native UPDATE / DML Queries (PostgreSQL) ─────────────────────────────

    /**
     * Deactivates a counterparty account by ID.
     */
    @Modifying
    @Transactional
    @Query(value = """
            UPDATE freddie_customer.ucs_cntprty_acct
               SET acct_sts   = 'INACTIVE',
                   updated_at = CURRENT_TIMESTAMP
             WHERE id_cntprty_acct = :accountId
            """,
            nativeQuery = true)
    int deactivateAccountNative(@Param("accountId") String accountId);
}
