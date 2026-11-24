package ru.tbank.edu.tasktracker.metrics;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AppInfoMetrics {

    public AppInfoMetrics(MeterRegistry registry,
                          @Value("${app.instance-id:unknown}") String instanceId) {
        Gauge.builder("app_version", () -> 1)
                .description("Application version information")
                .tag("instance", instanceId)
                .register(registry);
    }
}
