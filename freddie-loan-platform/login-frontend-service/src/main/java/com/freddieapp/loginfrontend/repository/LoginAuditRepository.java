package com.freddieapp.loginfrontend.repository;

import com.freddieapp.loginfrontend.enums.LoginState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class LoginAuditRepository {

    private static final Logger log = LoggerFactory.getLogger(LoginAuditRepository.class);

    // ORM & Native Query simulation
    public void recordLoginAttempt(String username, LoginState state) {
        log.info("[REPOSITORY-NATIVE-QUERY] INSERT INTO login_audit_log (username, state, timestamp) VALUES ('{}', '{}', CURRENT_TIMESTAMP)", username, state);
    }
}
