package com.tech.aidocqna.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private static final Logger auditLog = LoggerFactory.getLogger("AUDIT");

    public void logEvent(String action, String actor, String details) {
        auditLog.info("action={} actor={} details={}", action, actor, details);
    }
}
