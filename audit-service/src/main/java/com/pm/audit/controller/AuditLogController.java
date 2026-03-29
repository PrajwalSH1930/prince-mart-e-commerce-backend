package com.pm.audit.controller;

import com.pm.audit.dto.AuditLogRequest;
import com.pm.audit.entity.AuditLog;
import com.pm.audit.service.AuditLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @GetMapping("/welcome")
    public ResponseEntity<String> welcome() {
		return ResponseEntity.ok("Welcome!! This is the Audit Service for Prince Mart by Prince Inc.");
	}
    
    // This endpoint will be called internally by other services
    @PostMapping("/log")
    public ResponseEntity<Void> createLog(@RequestBody AuditLogRequest request) {
        auditLogService.log(request);
        return ResponseEntity.ok().build();
    }

    // Admin endpoint to view logs by service
    @GetMapping("/service/{serviceName}")
    public ResponseEntity<List<AuditLog>> getLogsByService(@PathVariable String serviceName) {
        return ResponseEntity.ok(auditLogService.getLogsByService(serviceName));
    }

    // Admin endpoint to view logs by user
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLog>> getLogsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditLogService.getLogsByUser(userId));
    }
}