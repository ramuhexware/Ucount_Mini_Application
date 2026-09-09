package com.freddieapp.documentservice.entity;

import com.freddieapp.documentservice.enums.DocumentStatus;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalDate;

@Table(name = "loan_documents", schema = "freddie_customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanDocument {

    @Id
    @Column("document_id")
    private String documentId;

    @Column("loan_id")
    private String loanId;

    @Column("customer_id")
    private String customerId;

    @Column("document_type")
    private String documentType;

    @Column("file_name")
    private String fileName;

    @Column("mime_type")
    private String mimeType;

    @Column("size_bytes")
    private Long sizeBytes;

    @Column("checksum")
    private String checksum;

    @Column("file_data")
    private byte[] fileData;

    @Column("grid_fs_file_id")
    private String gridFsFileId;

    @Column("status")
    private String status;

    @Column("verified_by")
    private String verifiedBy;

    @Column("verified_at")
    private Instant verifiedAt;

    @Column("expiry_date")
    private LocalDate expiryDate;

    @Column("uploaded_by")
    private String uploadedBy;

    @Column("tags")
    private String tags;

    @CreatedDate
    @Column("uploaded_at")
    private Instant uploadedAt;

    @LastModifiedDate
    @Column("updated_at")
    private Instant updatedAt;

    @Column("version")
    private Integer version;
}
