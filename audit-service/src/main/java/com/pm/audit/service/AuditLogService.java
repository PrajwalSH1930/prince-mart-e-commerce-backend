package com.pm.audit.service;

import com.pm.audit.dto.AuditLogRequest;
import com.pm.audit.entity.AuditLog;
import com.pm.audit.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void log(AuditLogRequest request) {
        AuditLog auditLog = new AuditLog();
        auditLog.setServiceName(request.getServiceName());
        auditLog.setAction(request.getAction());
        auditLog.setUserId(request.getUserId());
        auditLog.setDataBefore(request.getDataBefore());
        auditLog.setDataAfter(request.getDataAfter());
        
        auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getLogsByService(String serviceName) {
        return auditLogRepository.findByServiceNameOrderByTimestampDesc(serviceName);
    }

    public List<AuditLog> getLogsByUser(Long userId) {
        return auditLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}