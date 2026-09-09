package com.freddieapp.customerservice.processor;

import com.freddieapp.customerservice.entity.Customer;
import org.springframework.stereotype.Component;

@Component
public class CustomerDataProcessor {

    public void processCustomerData(Customer customer) {
        if (customer.getFirstName() != null) {
            customer.setFirstName(customer.getFirstName().trim());
        }
        if (customer.getLastName() != null) {
            customer.setLastName(customer.getLastName().trim());
        }
        if (customer.getEmail() != null) {
            customer.setEmail(customer.getEmail().trim().toLowerCase());
        }
    }
}
