package com.freddieapp.customerservice.service;

import com.freddieapp.customerservice.dto.CustomerRequest;
import com.freddieapp.customerservice.dto.CustomerResponse;
import com.freddieapp.customerservice.entity.Customer;
import com.freddieapp.customerservice.enums.CustomerStatus;
import com.freddieapp.customerservice.enums.KycStatus;
import com.freddieapp.customerservice.event.CustomerEventPublisher;
import com.freddieapp.customerservice.exception.CustomerAlreadyExistsException;
import com.freddieapp.customerservice.exception.CustomerNotFoundException;
import com.freddieapp.customerservice.repository.CustomerRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * JUnit 4 unit tests for CustomerService.
 */
@RunWith(MockitoJUnitRunner.class)
public class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerEventPublisher eventPublisher;

    @Mock
    private SsnEncryptionService ssnEncryptionService;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequest validRequest;
    private Customer mockCustomer;
    private UUID customerId;

    @Before
    public void setUp() {
        customerId = UUID.randomUUID();

        validRequest = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("555-123-4567")
                .ssn("123-45-6789")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .nationality("US")
                .addressLine1("100 Main St")
                .addressLine2("Suite 200")
                .city("McLean")
                .state("VA")
                .zipCode("22101")
                .build();

        mockCustomer = Customer.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("555-123-4567")
                .ssnEncrypted("ENCRYPTED_SSN")
                .dateOfBirth(LocalDate.of(1985, 6, 15))
                .nationality("US")
                .addressLine1("100 Main St")
                .city("McLean")
                .state("VA")
                .zipCode("22101")
                .customerStatus(CustomerStatus.ACTIVE)
                .kycStatus(KycStatus.PENDING)
                .build();
    }

    // ─── createCustomer ─────────────────────────────────────────────────────

    @Test
    public void createCustomer_success() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(ssnEncryptionService.encrypt(anyString())).thenReturn("ENCRYPTED_SSN");
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        CustomerResponse response = customerService.createCustomer(validRequest);

        assertNotNull(response);
        assertEquals("John", response.getFirstName());
        assertEquals("Doe", response.getLastName());
        assertEquals("john.doe@example.com", response.getEmail());
        assertEquals(CustomerStatus.ACTIVE, response.getCustomerStatus());
        assertEquals(KycStatus.PENDING, response.getKycStatus());

        verify(customerRepository).existsByEmail("john.doe@example.com");
        verify(ssnEncryptionService).encrypt("123-45-6789");
        verify(customerRepository).save(any(Customer.class));
        verify(eventPublisher).publishCustomerCreated(any(Customer.class));
    }

    @Test(expected = CustomerAlreadyExistsException.class)
    public void createCustomer_duplicateEmail_throwsException() {
        when(customerRepository.existsByEmail("john.doe@example.com")).thenReturn(true);

        customerService.createCustomer(validRequest);
    }

    @Test
    public void createCustomer_ssnIsEncrypted() {
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(ssnEncryptionService.encrypt("123-45-6789")).thenReturn("AES_ENCRYPTED_VALUE");
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        customerService.createCustomer(validRequest);

        verify(ssnEncryptionService).encrypt("123-45-6789");
    }

    // ─── getCustomerById ─────────────────────────────────────────────────────

    @Test
    public void getCustomerById_found() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));

        CustomerResponse response = customerService.getCustomerById(customerId);

        assertNotNull(response);
        assertEquals(customerId, response.getId());
        assertEquals("john.doe@example.com", response.getEmail());
    }

    @Test(expected = CustomerNotFoundException.class)
    public void getCustomerById_notFound_throwsException() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        customerService.getCustomerById(UUID.randomUUID());
    }

    // ─── updateCustomer ──────────────────────────────────────────────────────

    @Test
    public void updateCustomer_success() {
        CustomerRequest updateRequest = CustomerRequest.builder()
                .firstName("Jane")
                .lastName("Smith")
                .phone("555-999-0000")
                .addressLine1("200 Elm Ave")
                .city("Reston")
                .state("VA")
                .zipCode("20190")
                .build();

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        CustomerResponse response = customerService.updateCustomer(customerId, updateRequest);

        assertNotNull(response);
        verify(customerRepository).save(any(Customer.class));
        verify(eventPublisher).publishCustomerUpdated(any(Customer.class));
    }

    @Test(expected = CustomerNotFoundException.class)
    public void updateCustomer_customerNotFound_throwsException() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        customerService.updateCustomer(UUID.randomUUID(), validRequest);
    }

    // ─── deactivateCustomer ───────────────────────────────────────────────────

    @Test
    public void deactivateCustomer_success() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(mockCustomer));
        when(customerRepository.save(any(Customer.class))).thenReturn(mockCustomer);

        customerService.deactivateCustomer(customerId);

        assertEquals(CustomerStatus.INACTIVE, mockCustomer.getCustomerStatus());
        verify(customerRepository).save(mockCustomer);
    }

    @Test(expected = CustomerNotFoundException.class)
    public void deactivateCustomer_notFound_throwsException() {
        when(customerRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        customerService.deactivateCustomer(UUID.randomUUID());
    }

    // ─── searchCustomers ──────────────────────────────────────────────────────

    @Test
    public void searchCustomers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> mockPage = new PageImpl<>(List.of(mockCustomer));
        when(customerRepository.searchCustomers(anyString(), anyString(), anyString(), eq(pageable)))
                .thenReturn(mockPage);

        Page<CustomerResponse> result = customerService.searchCustomers("John", "john@test.com", "PENDING", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    public void searchCustomers_emptyResult_returnsEmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> emptyPage = new PageImpl<>(List.of());
        when(customerRepository.searchCustomers(anyString(), anyString(), anyString(), eq(pageable)))
                .thenReturn(emptyPage);

        Page<CustomerResponse> result = customerService.searchCustomers("NoMatch", "", "", pageable);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    // ─── getAllCustomers ──────────────────────────────────────────────────────

    @Test
    public void getAllCustomers_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> mockPage = new PageImpl<>(List.of(mockCustomer));
        when(customerRepository.findAll(pageable)).thenReturn(mockPage);

        Page<CustomerResponse> result = customerService.getAllCustomers(pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("John", result.getContent().get(0).getFirstName());
    }

    // ─── getCustomersByKycStatus ──────────────────────────────────────────────

    @Test
    public void getCustomersByKycStatus_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Customer> mockPage = new PageImpl<>(List.of(mockCustomer));
        when(customerRepository.findByKycStatus(eq(KycStatus.PENDING), eq(pageable))).thenReturn(mockPage);

        Page<CustomerResponse> result = customerService.getCustomersByKycStatus(KycStatus.PENDING, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(KycStatus.PENDING, result.getContent().get(0).getKycStatus());
    }
}
