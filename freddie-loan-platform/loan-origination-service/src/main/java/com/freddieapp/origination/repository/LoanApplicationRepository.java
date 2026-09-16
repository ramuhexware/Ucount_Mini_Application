package com.freddieapp.origination.repository;

import com.freddieapp.origination.model.LoanApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository layer enforcing Dual Data Access Pattern (Spring Data JPA ORM + PostgreSQL Native Queries).
 */
@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplicationEntity, Long> {

    // Spring Data JPA ORM Method
    List<LoanApplicationEntity> findByCustomerId(String customerId);

    // PostgreSQL Native Query
    @Modifying
    @Query(value = "UPDATE loan_applications SET status = :status, updated_at = NOW() WHERE id = :loanId", nativeQuery = true)
    int updateStatusNative(@Param("loanId") Long loanId, @Param("status") String status);
}
