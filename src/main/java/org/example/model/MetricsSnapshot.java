package org.example.model;

import java.util.List;

public record MetricsSnapshot(int totalQueries, List<ApiSnapshot> apis) {

    public record ApiSnapshot(String name, int totalRequests, int totalResponses) {
        public ApiSnapshot(String name, int totalRequests, int totalResponses) {
            this.name = name;
            this.totalRequests = totalRequests;
            this.totalResponses = totalResponses;
        }
    }
}