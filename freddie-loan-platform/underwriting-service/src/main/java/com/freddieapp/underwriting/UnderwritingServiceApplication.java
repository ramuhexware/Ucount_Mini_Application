package com.freddieapp.underwriting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.EnableJms;

/**
 * Entry point for Underwriting, Pricing & Risk Engine Microservice (Port 8083).
 */
@SpringBootApplication
@EnableJms
public class UnderwritingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UnderwritingServiceApplication.class, args);
    }
}
