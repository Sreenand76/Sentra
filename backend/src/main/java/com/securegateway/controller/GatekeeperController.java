package com.securegateway.controller;

import com.securegateway.annotation.RateLimited;
import com.securegateway.model.RateLimitLog;
import com.securegateway.repository.RateLimitLogRepository;
import com.securegateway.service.LoggingModeService;
import com.securegateway.service.RateLimitConfigService;
import com.securegateway.service.TokenBucketService;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Scanner;

@RestController
@RequestMapping("/api/v1")
public class GatekeeperController {

    private final RateLimitConfigService configService;
    private final RateLimitLogRepository logRepository;
    private final TokenBucketService tokenBucketService;
    private final LoggingModeService loggingModeService;
    
    public GatekeeperController(RateLimitConfigService configService, RateLimitLogRepository logRepository,TokenBucketService tokenBucketService,LoggingModeService loggingModeService) {
        this.configService = configService;
        this.logRepository = logRepository;
        this.tokenBucketService = tokenBucketService;
        this.loggingModeService=loggingModeService;
    }

    @GetMapping("/secure-data")
    @RateLimited(capacity = 5, refillRate = 1) // Only 5 requests allowed!
    public Map<String, String> getSecureData() {
        return Map.of(
                "status", "Success",
                "message", "You have accessed the high-security data!",
                "thread", Thread.currentThread().toString() // This shows Virtual Thread info
        );
    }

    @GetMapping("/public-data")
    public String getPublicData() {
        return "This endpoint is NOT rate-limited. Feel free to refresh!";
    }

    @GetMapping("/logs")
    public List<RateLimitLog> getLogs() {
        return logRepository.findAllByOrderByTimestampDesc();
    }

    @GetMapping("/config")
    public Map<String, Integer> getConfig() {
        return Map.of(
                "capacity", configService.getGlobalCapacityOverride() != null ? configService.getGlobalCapacityOverride() : 5,
                "refillRate", configService.getGlobalRefillRateOverride() != null ? configService.getGlobalRefillRateOverride() : 1
        );
    }
    
    @GetMapping("/k6-test")
    public Map<String, String> runK6Test() {

        try {

            ProcessBuilder pb = new ProcessBuilder(
                    "k6",
                    "run",
                    "sentra-test.js"
            );

            pb.redirectErrorStream(true);

            Process process = pb.start();

            Scanner sc = new Scanner(process.getInputStream());

            StringBuilder output = new StringBuilder();

            while (sc.hasNextLine()) {
                output.append(sc.nextLine()).append("\n");
            }

            process.waitFor();

            return Map.of(
                    "status", "SUCCESS",
                    "output", output.toString()
            );

        } catch (Exception e) {

            return Map.of(
                    "status", "FAILED",
                    "output", e.getMessage()
            );
        }
    }
    
    @PostMapping("/config")
    public Map<String, String> updateConfig(@RequestParam Integer capacity, @RequestParam Integer refillRate) {
        configService.setOverrides(capacity, refillRate);
        return Map.of("status", "Success", "message", "Rate limit config updated to Capacity: " + capacity + ", Refill Rate: " + refillRate);
    }
    
    @GetMapping("/logging-mode")
    public Map<String, Boolean> getLoggingMode() {
        return Map.of("loadTestMode", loggingModeService.isLoadTestMode());
    }

    @PostMapping("/logging-mode")
    public Map<String, Object> updateLoggingMode(@RequestParam boolean loadTestMode) {
        loggingModeService.setLoadTestMode(loadTestMode);

        return Map.of(
                "status", "Success",
                "loadTestMode", loadTestMode
        );
    }
    @DeleteMapping("/logs")
    public Map<String, String> clearLogs() {
        logRepository.deleteAll();
        return Map.of("status", "Logs cleared");
    }
}