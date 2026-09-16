package com.freddieapp.origination.pdf;

import com.freddieapp.origination.dto.LoanResponseDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class LoanSummaryPdfExporter {

    public byte[] generateLoanSummaryPdf(LoanResponseDTO loan) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String pdfContent = "%PDF-1.4 Mock Loan Summary PDF for Loan ID: " + loan.id() +
            "\nApplicant: " + loan.applicantName() +
            "\nAmount: $" + loan.loanAmount() +
            "\nStatus: " + loan.status();
        out.writeBytes(pdfContent.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
