package com.freddieapp.loanfrontend.repository;

import com.freddieapp.loanfrontend.enums.FrontendLoanStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LoanFrontendRepository {

    private static final Logger log = LoggerFactory.getLogger(LoanFrontendRepository.class);

    // ORM & Native Query simulation
    public void recordLoanFrontendState(String loanId, FrontendLoanStatus status) {
        log.info("[REPOSITORY-NATIVE-QUERY] INSERT INTO loan_frontend_audit (loan_id, status, updated_at) VALUES ('{}', '{}', CURRENT_TIMESTAMP)", loanId, status);
    }
}
