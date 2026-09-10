package com.freddieapp.customerservice.repository;

import com.freddieapp.customerservice.entity.CustomerSyncErrLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerSyncErrLogRepository extends JpaRepository<CustomerSyncErrLog, Long> {
}
