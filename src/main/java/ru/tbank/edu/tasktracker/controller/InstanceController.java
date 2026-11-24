package ru.tbank.edu.tasktracker.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/info")
public class InstanceController {

    private static final Logger log = LoggerFactory.getLogger(InstanceController.class);

    private final String instanceId;
    private final String version;

    public InstanceController(@Value("${app.instance-id:unknown}") String instanceId,
                              @Value("${app.version:unknown}") String version) {
        this.instanceId = instanceId;
        this.version = version;
    }

    @GetMapping
    public Map<String, String> info() {
        String hostname = hostname();
        Map<String, String> body = new LinkedHashMap<>();
        body.put("application", "task-tracker");
        body.put("version", version);
        body.put("instanceId", instanceId);
        body.put("hostname", hostname);
        log.info("Serving /api/info version={} instance={} hostname={}", version, instanceId, hostname);
        return body;
    }

    private static String hostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            return "unknown";
        }
    }
}
