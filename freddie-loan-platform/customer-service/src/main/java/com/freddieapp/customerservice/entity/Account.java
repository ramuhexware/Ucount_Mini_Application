package com.freddieapp.customerservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "ucs_cntprty_acct", schema = "freddie_customer")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @Column(name = "id_cntprty_acct", nullable = false, length = 50)
    private String idCntprtyAcct;

    @Column(name = "name_cntprty_acct", nullable = false, length = 255)
    private String nameCntprtyAcct;

    @Column(name = "id_orgtn", nullable = false)
    private Integer idOrgtn;

    @Column(name = "acct_sts", nullable = false, length = 20)
    private String acctSts;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Version
    @Column(name = "version")
    private Long version;
}
