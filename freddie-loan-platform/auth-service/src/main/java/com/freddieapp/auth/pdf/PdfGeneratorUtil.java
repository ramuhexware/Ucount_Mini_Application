package com.freddieapp.auth.pdf;

import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;

@Component
public class PdfGeneratorUtil {

    public byte[] generateAuthReport(String username, String status) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        String content = "Freddie Mac Auth Report\nUser: " + username + "\nStatus: " + status;
        baos.writeBytes(content.getBytes());
        return baos.toByteArray();
    }
}
