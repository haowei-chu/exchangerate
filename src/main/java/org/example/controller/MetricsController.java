package org.example.controller;

import org.example.metrics.MetricsTracker;
import org.example.model.MetricsSnapshot;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
public class MetricsController {

    private final MetricsTracker metricsTracker;

    @GetMapping("/metrics")
    public MetricsSnapshot getMetrics() {
        return metricsTracker.getSnapshot();
    }

}
