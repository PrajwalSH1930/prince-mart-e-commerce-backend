package com.pm.audit.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Long logId;

    @Column(nullable = false, length = 100, name = "service_name")
    private String serviceName; // e.g., "ORDER-SERVICE", "COUPON-SERVICE"

    @Column(nullable = false)
    private String action; // e.g., "CREATE_ORDER", "UPDATE_PRICE", "DELETE_COUPON"

    @Column(name = "user_id")
    private Long userId;

    @Column(columnDefinition = "TEXT", name = "data_before")
    private String dataBefore; // JSON snapshot of data before change

    @Column(columnDefinition = "TEXT", name = "data_after")
    private String dataAfter;  // JSON snapshot of data after change

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog() {}

    // Getters and Setters
    public Long getLogId() { return logId; }
    public void setLogId(Long logId) { this.logId = logId; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getDataBefore() { return dataBefore; }
    public void setDataBefore(String dataBefore) { this.dataBefore = dataBefore; }

    public String getDataAfter() { return dataAfter; }
    public void setDataAfter(String dataAfter) { this.dataAfter = dataAfter; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

	public AuditLog(Long logId, String serviceName, String action, Long userId, String dataBefore, String dataAfter,
			LocalDateTime timestamp) {
		super();
		this.logId = logId;
		this.serviceName = serviceName;
		this.action = action;
		this.userId = userId;
		this.dataBefore = dataBefore;
		this.dataAfter = dataAfter;
		this.timestamp = timestamp;
	}
    
    
}