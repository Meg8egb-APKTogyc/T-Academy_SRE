package ru.tbank.edu.tasktracker.controller;

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

    private final String instanceId;

    public InstanceController(@Value("${app.instance-id:unknown}") String instanceId) {
        this.instanceId = instanceId;
    }

    @GetMapping
    public Map<String, String> info() {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("application", "task-tracker");
        body.put("instanceId", instanceId);
        body.put("hostname", hostname());
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
