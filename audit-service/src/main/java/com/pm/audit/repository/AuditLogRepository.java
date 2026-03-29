package com.pm.audit.repository;

import com.pm.audit.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    
    // Find all logs for a specific service (e.g., all "ORDER-SERVICE" logs)
    List<AuditLog> findByServiceNameOrderByTimestampDesc(String serviceName);

    // Find all actions taken by a specific user (Admin or Customer)
    List<AuditLog> findByUserIdOrderByTimestampDesc(Long userId);
}