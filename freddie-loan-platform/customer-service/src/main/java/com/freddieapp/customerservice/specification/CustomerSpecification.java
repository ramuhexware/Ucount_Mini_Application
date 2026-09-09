package com.freddieapp.customerservice.specification;

import com.freddieapp.customerservice.entity.Customer;
import com.freddieapp.customerservice.enums.KycStatus;
import org.springframework.data.jpa.domain.Specification;

public class CustomerSpecification {

    public static Specification<Customer> hasEmail(String email) {
        return (root, query, cb) -> email == null ? null : cb.equal(cb.lower(root.get("email")), email.toLowerCase());
    }

    public static Specification<Customer> hasKycStatus(KycStatus kycStatus) {
        return (root, query, cb) -> kycStatus == null ? null : cb.equal(root.get("kycStatus"), kycStatus);
    }
}
