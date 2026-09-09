package com.freddieapp.loanorigination.service;

import com.freddieapp.loanorigination.client.CustomerServiceClient;
import com.freddieapp.loanorigination.dto.CustomerDto;
import com.freddieapp.loanorigination.dto.LoanApplicationRequest;
import com.freddieapp.loanorigination.dto.LoanApplicationResponse;
import com.freddieapp.loanorigination.entity.LoanApplication;
import com.freddieapp.loanorigination.enums.LoanStatus;
import com.freddieapp.loanorigination.enums.LoanType;
import com.freddieapp.loanorigination.repository.LoanApplicationRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 4 unit tests for LoanOriginationService.
 */
@RunWith(MockitoJUnitRunner.class)
public class LoanOriginationServiceTest {

    @Mock
    private LoanApplicationRepository loanRepository;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @InjectMocks
    private LoanOriginationService loanOriginationService;

    private LoanApplicationRequest validRequest;
    private LoanApplication mockLoan;
    private CustomerDto activeCustomer;
    private CustomerDto inactiveCustomer;
    private String customerId;
    private String loanId;

    @Before
    public void setUp() {
        customerId = UUID.randomUUID().toString();
        loanId = UUID.randomUUID().toString();

        validRequest = LoanApplicationRequest.builder()
                .customerId(customerId)
                .loanType(LoanType.PURCHASE)
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("440000.00"))
                .propertyAddress("123 Elm St, McLean, VA 22101")
                .loanTermMonths(360)
                .build();

        activeCustomer = CustomerDto.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .customerStatus("ACTIVE")
                .build();

        inactiveCustomer = CustomerDto.builder()
                .id(customerId)
                .firstName("Jane")
                .lastName("Inactive")
                .customerStatus("INACTIVE")
                .build();

        mockLoan = LoanApplication.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanType(LoanType.PURCHASE)
                .loanAmount(new BigDecimal("350000.00"))
                .propertyValue(new BigDecimal("440000.00"))
                .propertyAddress("123 Elm St, McLean, VA 22101")
                .loanTermMonths(360)
                .loanStatus(LoanStatus.SUBMITTED)
                .build();
    }

    // ─── submitLoanApplication ───────────────────────────────────────────────

    @Test
    public void submitLoanApplication_success() {
        when(customerServiceClient.getCustomerById(any(UUID.class))).thenReturn(activeCustomer);
        when(loanRepository.save(any(LoanApplication.class))).thenReturn(mockLoan);

        LoanApplicationResponse response = loanOriginationService.submitLoanApplication(validRequest);

        assertNotNull(response);
        assertEquals(loanId, response.getLoanId());
        assertEquals(customerId, response.getCustomerId());
        assertEquals(LoanStatus.SUBMITTED, response.getLoanStatus());

        verify(customerServiceClient).getCustomerById(UUID.fromString(customerId));
        verify(loanRepository).save(any(LoanApplication.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void submitLoanApplication_inactiveCustomer_throwsException() {
        when(customerServiceClient.getCustomerById(any(UUID.class))).thenReturn(inactiveCustomer);

        loanOriginationService.submitLoanApplication(validRequest);
    }

    // ─── getLoanById ─────────────────────────────────────────────────────────

    @Test
    public void getLoanById_found() {
        when(loanRepository.findById(loanId)).thenReturn(Optional.of(mockLoan));

        LoanApplicationResponse response = loanOriginationService.getLoanById(loanId);

        assertNotNull(response);
        assertEquals(loanId, response.getLoanId());
        assertEquals(LoanStatus.SUBMITTED, response.getLoanStatus());
    }

    @Test(expected = IllegalArgumentException.class)
    public void getLoanById_notFound_throwsException() {
        when(loanRepository.findById(anyString())).thenReturn(Optional.empty());

        loanOriginationService.getLoanById("non-existent-id");
    }

    // ─── getLoansByCustomer ───────────────────────────────────────────────────

    @Test
    public void getLoansByCustomer_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LoanApplication> mockPage = new PageImpl<>(List.of(mockLoan));
        when(loanRepository.findByCustomerIdNative(eq(customerId), eq(pageable))).thenReturn(mockPage);

        Page<LoanApplicationResponse> result = loanOriginationService.getLoansByCustomer(customerId, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(loanId, result.getContent().get(0).getLoanId());
    }

    @Test
    public void getLoansByCustomer_noLoans_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<LoanApplication> emptyPage = new PageImpl<>(List.of());
        when(loanRepository.findByCustomerIdNative(anyString(), any(Pageable.class))).thenReturn(emptyPage);

        Page<LoanApplicationResponse> result = loanOriginationService.getLoansByCustomer("unknown-id", pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ─── submitForUnderwriting ────────────────────────────────────────────────

    @Test
    public void submitForUnderwriting_success() {
        LoanApplication underReviewLoan = LoanApplication.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanStatus(LoanStatus.UNDER_REVIEW)
                .build();

        when(loanRepository.findById(loanId))
                .thenReturn(Optional.of(mockLoan))
                .thenReturn(Optional.of(underReviewLoan));
        when(loanRepository.submitForUnderwritingNative(loanId)).thenReturn(1);
        when(loanRepository.save(any(LoanApplication.class))).thenReturn(underReviewLoan);

        LoanApplicationResponse response = loanOriginationService.submitForUnderwriting(loanId);

        assertNotNull(response);
    }

    @Test(expected = IllegalArgumentException.class)
    public void submitForUnderwriting_loanNotFound_throwsException() {
        when(loanRepository.findById(anyString())).thenReturn(Optional.empty());

        loanOriginationService.submitForUnderwriting("non-existent-id");
    }

    @Test(expected = IllegalStateException.class)
    public void submitForUnderwriting_wrongStatus_throwsException() {
        LoanApplication approvedLoan = LoanApplication.builder()
                .loanId(loanId)
                .customerId(customerId)
                .loanStatus(LoanStatus.APPROVED)
                .build();

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(approvedLoan));

        loanOriginationService.submitForUnderwriting(loanId);
    }

    // ─── getAllLoans ──────────────────────────────────────────────────────────

    @Test
    public void getAllLoans_returnsPageOfLoans() {
        Pageable pageable = PageRequest.of(0, 20, Sort.by("applicationDate").descending());
        Page<LoanApplication> mockPage = new PageImpl<>(List.of(mockLoan), pageable, 1);
        when(loanRepository.findAll(eq(pageable))).thenReturn(mockPage);

        Page<LoanApplicationResponse> result = loanOriginationService.getAllLoans(pageable);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }
}
