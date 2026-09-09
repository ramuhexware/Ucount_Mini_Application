package com.freddieapp.loanfrontend.service;

import com.freddieapp.loanfrontend.client.notification.NotificationClient;
import com.freddieapp.loanfrontend.enums.FrontendLoanStatus;
import com.freddieapp.loanfrontend.processor.LoanFrontendProcessor;
import com.freddieapp.loanfrontend.repository.LoanFrontendRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LoanFrontendService {

    private final LoanFrontendRepository repository;
    private final LoanFrontendProcessor processor;
    private final NotificationClient notificationClient;

    @Autowired
    public LoanFrontendService(LoanFrontendRepository repository, LoanFrontendProcessor processor, NotificationClient notificationClient) {
        this.repository = repository;
        this.processor = processor;
        this.notificationClient = notificationClient;
    }

    public void processLoanAudit(String loanId, String status) {
        String cleanLoanId = processor.sanitizeLoanId(loanId);
        FrontendLoanStatus loanStatus = FrontendLoanStatus.SUBMITTED;
        repository.recordLoanFrontendState(cleanLoanId, loanStatus);
        notificationClient.notifyLoanFrontendEvent(cleanLoanId, loanStatus.name());
    }
}
