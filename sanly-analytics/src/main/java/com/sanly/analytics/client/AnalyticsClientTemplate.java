package com.sanly.analytics.client;

/**
 * Reference template showing how other services integrate with sanly-analytics.
 *
 * Each source service has its own AnalyticsClient in its own package.
 * Example usage in a service:
 *
 *   analyticsClient.pushDaily("CITIZEN_REGISTRY", DailyIngestRequest.builder()
 *       .totalCitizens(totalCount)
 *       .activeCitizens(activeCount)
 *       .newRegistrationsToday(newToday)
 *       .build());
 *
 * The AnalyticsClient is @Async so it never blocks the calling service's transaction.
 * Any exception is caught and logged — analytics failures never affect core operations.
 *
 * Required application.yml entries in each source service:
 *   analytics:
 *     base-url: ${ANALYTICS_URL:http://localhost:8103}
 *     service-key: ${ANALYTICS_SERVICE_KEY:analytics-ingest-key-change-me}
 */
public class AnalyticsClientTemplate {
    // Documentation only — see individual AnalyticsClient.java files in each service package
}
