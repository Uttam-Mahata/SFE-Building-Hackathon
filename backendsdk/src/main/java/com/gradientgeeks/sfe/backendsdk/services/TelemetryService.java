package com.gradientgeeks.sfe.backendsdk.services;

import com.gradientgeeks.sfe.backendsdk.config.SfeBackendSdkConfig;
import com.gradientgeeks.sfe.backendsdk.models.TelemetryEventRequest;
import com.gradientgeeks.sfe.backendsdk.utils.SecurityUtils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class TelemetryService {
    
    private final SfeBackendSdkConfig config;
    private final Queue<TelemetryEventRequest> eventQueue = new ConcurrentLinkedQueue<>();
    private final AtomicLong eventCounter = new AtomicLong(0);
    
    public TelemetryService(SfeBackendSdkConfig config) {
        this.config = config;
    }
    
    /**
     * Record a telemetry event
     */
    @Async
    public void recordTelemetry(TelemetryEventRequest telemetryRequest) {
        if (!config.isEnableTelemetry()) {
            log.debug("Telemetry collection is disabled");
            return;
        }
        
        try {
            // Anonymize the event if required
            TelemetryEventRequest processedEvent = config.getTelemetry().isEnableAnonymization() 
                ? anonymizeEvent(telemetryRequest) 
                : telemetryRequest;
            
            // Add to queue for batch processing
            eventQueue.offer(processedEvent);
            eventCounter.incrementAndGet();
            
            log.debug("Telemetry event recorded: {} (Total events in queue: {})", 
                    processedEvent.getEventType(), eventQueue.size());
            
            // Check if we should process batch immediately
            if (eventQueue.size() >= config.getTelemetry().getBatchSize()) {
                processBatch();
            }
            
        } catch (Exception e) {
            log.error("Error recording telemetry event", e);
        }
    }
    
    /**
     * Process events in batches for efficiency
     */
    @Scheduled(fixedDelayString = "#{@sfeBackendSdkConfig.telemetry.batchTimeoutMs}")
    public void processBatch() {
        if (eventQueue.isEmpty()) {
            return;
        }
        
        List<TelemetryEventRequest> batch = new ArrayList<>();
        
        // Drain up to batch size events from queue
        for (int i = 0; i < config.getTelemetry().getBatchSize() && !eventQueue.isEmpty(); i++) {
            TelemetryEventRequest event = eventQueue.poll();
            if (event != null) {
                batch.add(event);
            }
        }
        
        if (!batch.isEmpty()) {
            try {
                submitBatchToStorage(batch);
                log.info("Processed telemetry batch: {} events", batch.size());
            } catch (Exception e) {
                log.error("Error processing telemetry batch", e);
                // Re-queue events on failure
                eventQueue.addAll(batch);
            }
        }
    }
    
    /**
     * Submit batch to regulatory body (for compliance)
     */
    public void submitToRegulatoryBody(List<TelemetryEventRequest> events) {
        if (config.getRegulatoryApiEndpoint() == null || config.getRegulatoryApiEndpoint().isEmpty()) {
            log.debug("No regulatory API endpoint configured");
            return;
        }
        
        try {
            // Filter and format events for regulatory submission
            List<AnonymizedEvent> regulatoryEvents = events.stream()
                .map(this::convertToRegulatoryFormat)
                .filter(Objects::nonNull)
                .toList();
            
            if (!regulatoryEvents.isEmpty()) {
                // In production, this would make HTTP calls to regulatory APIs
                log.info("Submitting {} events to regulatory body at {}", 
                        regulatoryEvents.size(), config.getRegulatoryApiEndpoint());
                
                // Mock submission for prototype
                simulateRegulatorySubmission(regulatoryEvents);
            }
            
        } catch (Exception e) {
            log.error("Error submitting to regulatory body", e);
        }
    }
    
    /**
     * Daily regulatory report generation
     */
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void generateDailyReport() {
        try {
            log.info("Generating daily compliance report");
            
            // Get events from the last 24 hours
            List<TelemetryEventRequest> dailyEvents = getEventsForLastDay();
            
            if (!dailyEvents.isEmpty()) {
                ComplianceReport report = buildComplianceReport(dailyEvents);
                submitComplianceReport(report);
                
                log.info("Daily compliance report generated with {} events", dailyEvents.size());
            } else {
                log.info("No events to report for the last 24 hours");
            }
            
        } catch (Exception e) {
            log.error("Error generating daily compliance report", e);
        }
    }
    
    /**
     * Get telemetry statistics
     */
    public TelemetryStats getStats() {
        return TelemetryStats.builder()
            .totalEventsRecorded(eventCounter.get())
            .eventsInQueue(eventQueue.size())
            .telemetryEnabled(config.isEnableTelemetry())
            .batchingEnabled(config.getTelemetry().isEnableBatching())
            .anonymizationEnabled(config.getTelemetry().isEnableAnonymization())
            .build();
    }
    
    private TelemetryEventRequest anonymizeEvent(TelemetryEventRequest event) {
        // Create anonymized copy
        TelemetryEventRequest anonymized = TelemetryEventRequest.builder()
            .eventType(event.getEventType())
            .eventId(event.getEventId())
            .timestamp(SecurityUtils.truncateToHour(event.getTimestamp()))
            .deviceFingerprint(SecurityUtils.hashDeviceId(
                event.getDeviceFingerprint(), 
                config.getTelemetry().getSaltKey()))
            .riskLevel(event.getRiskLevel())
            .eventData(anonymizeEventData(event.getEventData()))
            .isAnonymized(true)
            .build();
        
        return anonymized;
    }
    
    private Map<String, Object> anonymizeEventData(Map<String, Object> originalData) {
        if (originalData == null) {
            return new HashMap<>();
        }
        
        Map<String, Object> anonymizedData = new HashMap<>();
        
        originalData.forEach((key, value) -> {
            // Remove or hash sensitive fields
            if (isSensitiveField(key)) {
                if (value instanceof String) {
                    anonymizedData.put(key, SecurityUtils.hashDeviceId((String) value));
                } else {
                    anonymizedData.put(key, "***");
                }
            } else {
                anonymizedData.put(key, value);
            }
        });
        
        return anonymizedData;
    }
    
    private boolean isSensitiveField(String fieldName) {
        return fieldName.toLowerCase().contains("device") ||
               fieldName.toLowerCase().contains("id") ||
               fieldName.toLowerCase().contains("token") ||
               fieldName.toLowerCase().contains("imei") ||
               fieldName.toLowerCase().contains("serial");
    }
    
    private void submitBatchToStorage(List<TelemetryEventRequest> batch) {
        // In production, this would persist to database or forward to monitoring systems
        log.debug("Storing telemetry batch of {} events", batch.size());
        
        // Mock storage for prototype
        batch.forEach(event -> {
            log.trace("Stored event: {} - {} - {}", 
                    event.getEventType(), 
                    event.getRiskLevel(), 
                    SecurityUtils.sanitizeForLogging(event.getDeviceFingerprint()));
        });
    }
    
    private AnonymizedEvent convertToRegulatoryFormat(TelemetryEventRequest event) {
        return AnonymizedEvent.builder()
            .eventType(event.getEventType().toString())
            .timestamp(SecurityUtils.truncateToHour(event.getTimestamp()))
            .riskLevel(event.getRiskLevel())
            .anonymizedData(anonymizeEventData(event.getEventData()))
            .complianceRequired(isComplianceRequired(event))
            .build();
    }
    
    private boolean isComplianceRequired(TelemetryEventRequest event) {
        // Determine if this event type requires regulatory reporting
        return event.getEventType() == TelemetryEventRequest.EventType.POLICY_VIOLATION ||
               event.getEventType() == TelemetryEventRequest.EventType.TRANSACTION_BLOCKED ||
               "HIGH".equals(event.getRiskLevel()) ||
               "CRITICAL".equals(event.getRiskLevel());
    }
    
    private void simulateRegulatorySubmission(List<AnonymizedEvent> events) {
        log.info("Simulating regulatory submission for {} events", events.size());
        
        Map<String, Long> eventTypeCounts = events.stream()
            .collect(HashMap::new,
                    (map, event) -> map.merge(event.getEventType(), 1L, Long::sum),
                    (map1, map2) -> { map1.putAll(map2); });
        
        log.info("Event type distribution: {}", eventTypeCounts);
    }
    
    private List<TelemetryEventRequest> getEventsForLastDay() {
        // In production, this would query database for last 24 hours
        // For prototype, return empty list
        return new ArrayList<>();
    }
    
    private ComplianceReport buildComplianceReport(List<TelemetryEventRequest> events) {
        return ComplianceReport.builder()
            .reportDate(Instant.now())
            .totalEvents(events.size())
            .eventTypeCounts(events.stream()
                .collect(HashMap::new,
                        (map, event) -> map.merge(event.getEventType().toString(), 1L, Long::sum),
                        (map1, map2) -> { map1.putAll(map2); }))
            .riskLevelDistribution(events.stream()
                .collect(HashMap::new,
                        (map, event) -> map.merge(event.getRiskLevel(), 1L, Long::sum),
                        (map1, map2) -> { map1.putAll(map2); }))
            .build();
    }
    
    private void submitComplianceReport(ComplianceReport report) {
        log.info("Compliance report: Date={}, Events={}, Risk Distribution={}", 
                report.getReportDate(), 
                report.getTotalEvents(), 
                report.getRiskLevelDistribution());
    }
    
    @lombok.Data
    @lombok.Builder
    public static class TelemetryStats {
        private long totalEventsRecorded;
        private int eventsInQueue;
        private boolean telemetryEnabled;
        private boolean batchingEnabled;
        private boolean anonymizationEnabled;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class AnonymizedEvent {
        private String eventType;
        private long timestamp;
        private String riskLevel;
        private Map<String, Object> anonymizedData;
        private boolean complianceRequired;
    }
    
    @lombok.Data
    @lombok.Builder
    public static class ComplianceReport {
        private Instant reportDate;
        private int totalEvents;
        private Map<String, Long> eventTypeCounts;
        private Map<String, Long> riskLevelDistribution;
    }
} 