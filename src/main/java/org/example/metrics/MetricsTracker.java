package org.example.metrics;

import org.example.model.MetricsSnapshot;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class MetricsTracker {

    private final AtomicInteger totalQueries = new AtomicInteger(0);
    private final Map<String, ApiMetrics> apiMetrics = new ConcurrentHashMap<>();

    public void incrementTotalQueries() {
        totalQueries.incrementAndGet();
    }

    public void recordApiRequest(String apiName) {
        apiMetrics.computeIfAbsent(apiName, name -> new ApiMetrics()).incrementRequests();
    }

    public void recordApiResponse(String apiName) {
        apiMetrics.computeIfAbsent(apiName, name -> new ApiMetrics()).incrementResponses();
    }

    public MetricsSnapshot getSnapshot() {
        List<MetricsSnapshot.ApiSnapshot> apis = apiMetrics.entrySet().stream()
                .map(entry -> new MetricsSnapshot.ApiSnapshot(
                        entry.getKey(),
                        entry.getValue().getTotalRequests(),
                        entry.getValue().getTotalResponses()
                ))
                .toList();

        return new MetricsSnapshot(totalQueries.get(), apis);
    }

    private static class ApiMetrics {
        private final AtomicInteger totalRequests = new AtomicInteger();
        private final AtomicInteger totalResponses = new AtomicInteger();

        public void incrementRequests() {
            totalRequests.incrementAndGet();
        }

        public void incrementResponses() {
            totalResponses.incrementAndGet();
        }

        public int getTotalRequests() {
            return totalRequests.get();
        }

        public int getTotalResponses() {
            return totalResponses.get();
        }
    }
}