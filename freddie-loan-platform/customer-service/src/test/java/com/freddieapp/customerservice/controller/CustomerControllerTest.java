package com.freddieapp.customerservice.controller;

import com.freddieapp.customerservice.client.CardSyncClient;
import com.freddieapp.customerservice.dto.CustomerRequest;
import com.freddieapp.customerservice.dto.CustomerResponse;
import com.freddieapp.customerservice.enums.KycStatus;
import com.freddieapp.customerservice.service.CustomerService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @Mock
    private CardSyncClient cardSyncClient;

    @Mock
    private com.freddieapp.customerservice.service.EmailNotificationClientServicer emailNotificationClientServicer;

    @Mock
    private com.freddieapp.customerservice.config.EmailNotificationClientConfig emailNotificationClientConfig;

    @InjectMocks
    private CustomerController customerController;

    private UUID customerId;
    private CustomerResponse mockResponse;

    @Before
    public void setUp() {
        customerId = UUID.randomUUID();
        mockResponse = CustomerResponse.builder()
                .id(customerId)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("555-0199")
                .kycStatus(KycStatus.VERIFIED)
                .build();
    }

    @Test
    public void testCreateCustomerSuccess() {
        when(customerService.createCustomer(any(CustomerRequest.class))).thenReturn(mockResponse);

        CustomerRequest request = CustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phone("555-0199")
                .ssn("999-00-1234")
                .build();

        ResponseEntity<CustomerResponse> response = customerController.createCustomer(request);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("John", response.getBody().getFirstName());
        assertEquals("john.doe@example.com", response.getBody().getEmail());
    }

    @Test
    public void testGetCustomerByIdSuccess() {
        when(customerService.getCustomerById(customerId)).thenReturn(mockResponse);

        ResponseEntity<CustomerResponse> response = customerController.getCustomerById(customerId);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(customerId, response.getBody().getId());
        assertEquals(KycStatus.VERIFIED, response.getBody().getKycStatus());
    }

    @Test
    public void testDeactivateCustomerSuccess() {
        doNothing().when(customerService).deactivateCustomer(customerId);

        ResponseEntity<Void> response = customerController.deactivateCustomer(customerId);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(customerService, times(1)).deactivateCustomer(customerId);
    }

    @Test
    public void testGetAllCustomersSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerResponse> mockPage = new PageImpl<>(List.of(mockResponse));
        when(customerService.getAllCustomers(any(Pageable.class))).thenReturn(mockPage);

        ResponseEntity<Page<CustomerResponse>> response = customerController.getAllCustomers(pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("John", response.getBody().getContent().get(0).getFirstName());
    }

    @Test
    public void testGetCustomersByKycStatusSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerResponse> mockPage = new PageImpl<>(List.of(mockResponse));
        when(customerService.getCustomersByKycStatus(eq(KycStatus.VERIFIED), any(Pageable.class))).thenReturn(mockPage);

        ResponseEntity<Page<CustomerResponse>> response = customerController.getCustomersByKycStatus(KycStatus.VERIFIED, pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(KycStatus.VERIFIED, response.getBody().getContent().get(0).getKycStatus());
    }

    @Test
    public void testSearchCustomersSuccess() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerResponse> mockPage = new PageImpl<>(List.of(mockResponse));
        when(customerService.searchCustomers(eq("John"), eq(null), eq(null), any(Pageable.class))).thenReturn(mockPage);

        ResponseEntity<Page<CustomerResponse>> response = customerController.searchCustomers("John", null, null, pageable);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
    }
}

