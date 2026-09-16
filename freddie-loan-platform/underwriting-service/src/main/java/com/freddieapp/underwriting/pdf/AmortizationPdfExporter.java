package com.freddieapp.underwriting.pdf;

import com.freddieapp.underwriting.dto.AmortizationScheduleDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

@Component
public class AmortizationPdfExporter {

    public byte[] generateAmortizationPdf(AmortizationScheduleDTO schedule) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        String content = "%PDF-1.4 Mock Amortization PDF for Amount: $" + schedule.loanAmount() +
            "\nRate: " + schedule.interestRate() + "%" +
            "\nTerm: " + schedule.termMonths() + " Months" +
            "\nMonthly Payment: $" + schedule.monthlyPayment();
        out.writeBytes(content.getBytes(StandardCharsets.UTF_8));
        return out.toByteArray();
    }
}
