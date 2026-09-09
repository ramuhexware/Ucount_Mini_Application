package com.freddieapp.loanorigination.controller;

import com.freddieapp.loanorigination.dto.LoanApplicationRequest;
import com.freddieapp.loanorigination.dto.LoanApplicationResponse;
import com.freddieapp.loanorigination.enums.LoanStatus;
import com.freddieapp.loanorigination.enums.LoanType;
import com.freddieapp.loanorigination.service.LoanOriginationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class LoanControllerTest {

    @Mock
    private LoanOriginationService loanOriginationService;

    @InjectMocks
    private LoanController loanController;

    private LoanApplicationResponse mockResponse;

    @Before
    public void setUp() {
        mockResponse = LoanApplicationResponse.builder()
                .loanId("LOAN-9001")
                .customerId("CUST-500")
                .loanType(LoanType.PURCHASE)
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("440000.00"))
                .loanStatus(LoanStatus.SUBMITTED)
                .build();
    }

    @Test
    public void testSubmitLoanApplicationSuccess() {
        when(loanOriginationService.submitLoanApplication(any(LoanApplicationRequest.class))).thenReturn(mockResponse);

        LoanApplicationRequest request = LoanApplicationRequest.builder()
                .customerId("CUST-500")
                .loanType(LoanType.PURCHASE)
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("440000.00"))
                .build();

        ResponseEntity<LoanApplicationResponse> response = loanController.submitLoanApplication(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("LOAN-9001", response.getBody().getLoanId());
        assertEquals(LoanStatus.SUBMITTED, response.getBody().getLoanStatus());
    }

    @Test
    public void testGetLoanByIdSuccess() {
        when(loanOriginationService.getLoanById("LOAN-9001")).thenReturn(mockResponse);

        ResponseEntity<LoanApplicationResponse> response = loanController.getLoanById("LOAN-9001");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("CUST-500", response.getBody().getCustomerId());
    }

    @Test
    public void testSubmitForUnderwritingSuccess() {
        LoanApplicationResponse underReviewResponse = LoanApplicationResponse.builder()
                .loanId("LOAN-9001")
                .customerId("CUST-500")
                .loanType(LoanType.PURCHASE)
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("440000.00"))
                .loanStatus(LoanStatus.UNDER_REVIEW)
                .build();

        when(loanOriginationService.submitForUnderwriting("LOAN-9001")).thenReturn(underReviewResponse);

        ResponseEntity<LoanApplicationResponse> response = loanController.submitForUnderwriting("LOAN-9001");

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(LoanStatus.UNDER_REVIEW, response.getBody().getLoanStatus());
    }
}

