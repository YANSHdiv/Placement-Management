package edu.jecrc.portal.modules.audit.service;

import edu.jecrc.portal.common.api.PageResponse;
import edu.jecrc.portal.modules.audit.entity.AuditLog;
import edu.jecrc.portal.modules.audit.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    private static final Set<String> SENSITIVE_KEYS = Set.of(
            "password", "passwordhash", "secret", "token", "accesstoken", "refreshtoken", "cvv", "pin"
    );

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logAsync(UUID userId, String userEmail, String action, String entityName, String entityId,
                         Map<String, Object> details, String ipAddress, String userAgent) {
        saveAuditRecord(userId, userEmail, action, entityName, entityId, details, ipAddress, userAgent);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSync(UUID userId, String userEmail, String action, String entityName, String entityId,
                        Map<String, Object> details, HttpServletRequest request) {
        String ip = (request != null) ? getClientIp(request) : "SYSTEM";
        String ua = (request != null) ? request.getHeader("User-Agent") : "SYSTEM";
        saveAuditRecord(userId, userEmail, action, entityName, entityId, details, ip, ua);
    }

    private void saveAuditRecord(UUID userId, String userEmail, String action, String entityName, String entityId,
                                 Map<String, Object> details, String ipAddress, String userAgent) {
        try {
            Map<String, Object> sanitizedDetails = sanitizeDetails(details);

            AuditLog logEntry = AuditLog.builder()
                    .userId(userId)
                    .userEmail(userEmail)
                    .action(action)
                    .entityName(entityName)
                    .entityId(entityId)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent != null && userAgent.length() > 255 ? userAgent.substring(0, 255) : userAgent)
                    .details(sanitizedDetails)
                    .build();

            auditLogRepository.save(logEntry);
        } catch (Exception e) {
            log.error("Failed to persist append-only audit log: {}", e.getMessage(), e);
        }
    }

    private Map<String, Object> sanitizeDetails(Map<String, Object> details) {
        if (details == null || details.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<String, Object> entry : details.entrySet()) {
            String key = entry.getKey().toLowerCase();
            if (SENSITIVE_KEYS.stream().anyMatch(key::contains)) {
                sanitized.put(entry.getKey(), "[REDACTED]");
            } else if (entry.getValue() instanceof Map<?, ?> nestedMap) {
                @SuppressWarnings("unchecked")
                Map<String, Object> casted = (Map<String, Object>) nestedMap;
                sanitized.put(entry.getKey(), sanitizeDetails(casted));
            } else {
                sanitized.put(entry.getKey(), entry.getValue());
            }
        }
        return sanitized;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLog> getAuditLogs(String userEmail, String action, String entityName, Pageable pageable) {
        Page<AuditLog> page;
        boolean hasAction = action != null && !action.isBlank();
        boolean hasEmail = userEmail != null && !userEmail.isBlank();

        if (hasAction && hasEmail) {
            page = auditLogRepository.findByActionAndUserEmailContainingIgnoreCase(action.trim(), userEmail.trim(), pageable);
        } else if (hasAction) {
            page = auditLogRepository.findByAction(action.trim(), pageable);
        } else if (hasEmail) {
            page = auditLogRepository.findByUserEmailContainingIgnoreCase(userEmail.trim(), pageable);
        } else {
            page = auditLogRepository.findAll(pageable);
        }
        return PageResponse.from(page);
    }

    public static String getClientIp(HttpServletRequest request) {
        String xf = request.getHeader("X-Forwarded-For");
        if (xf != null && !xf.isBlank()) {
            return xf.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
