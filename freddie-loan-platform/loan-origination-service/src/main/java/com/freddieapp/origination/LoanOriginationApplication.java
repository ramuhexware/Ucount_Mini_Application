package com.freddieapp.origination;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for Loan Origination Microservice (Port 8082).
 */
@SpringBootApplication
public class LoanOriginationApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoanOriginationApplication.class, args);
    }
}
