package com.freddieapp.underwriting.repository;

import com.freddieapp.underwriting.domain.UnderwritingAuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UnderwritingAuditRepository extends JpaRepository<UnderwritingAuditLogEntity, Long> {
    List<UnderwritingAuditLogEntity> findByLoanId(Long loanId);
}
