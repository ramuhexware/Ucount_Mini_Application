package com.freddieapp.documentservice.processor;

import com.freddieapp.documentservice.entity.LoanDocument;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class DocumentProcessor {

    public String computeChecksum(byte[] data) {
        if (data == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(data);
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(data.length);
        }
    }

    public void processDocumentMetaData(LoanDocument document) {
        if (document.getFileName() != null) {
            document.setFileName(document.getFileName().trim());
        }
    }
}
