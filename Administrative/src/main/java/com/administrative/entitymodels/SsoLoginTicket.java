package com.administrative.entitymodels;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "sso_login_ticket", indexes =
        @Index(name = "idx_sso_ticket_code_hash", columnList = "code_hash", unique = true))
public class SsoLoginTicket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ticket_id")
    private Long ticketId;

    @Column(name = "code_hash", length = 64, nullable = false, unique = true)
    private String codeHash;

    @Column(name = "employee_no", length = 100, nullable = false)
    private String employeeNo;

    @Column(name = "employee_role", length = 100, nullable = false)
    private String employeeRole;

    @Column(name = "target_app", length = 32, nullable = false)
    private String targetApp;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    @Version
    @Column(name = "row_version", nullable = false)
    private Long rowVersion;

    public SsoLoginTicket() {}

    public SsoLoginTicket(String codeHash, String employeeNo, String employeeRole,
                          String targetApp, Instant createdAt, Instant expiresAt) {
        this.codeHash = codeHash;
        this.employeeNo = employeeNo;
        this.employeeRole = employeeRole;
        this.targetApp = targetApp;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    public Long getTicketId() { return ticketId; }
    public String getCodeHash() { return codeHash; }
    public String getEmployeeNo() { return employeeNo; }
    public String getEmployeeRole() { return employeeRole; }
    public String getTargetApp() { return targetApp; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(Instant consumedAt) { this.consumedAt = consumedAt; }
}
