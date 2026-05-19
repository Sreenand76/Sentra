package com.securegateway.service;

import com.securegateway.exception.RateLimitException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class TokenBucketService {
    // Map to store tokens for each client (Thread-safe)
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final RateLimitConfigService configService;

    public TokenBucketService(RateLimitConfigService configService) {
        this.configService = configService;
    }

    public void consume(String clientId, int capacity, int refillRate) {
        int finalCapacity = configService.getGlobalCapacityOverride() != null ? configService.getGlobalCapacityOverride() : capacity;
        int finalRefillRate = configService.getGlobalRefillRateOverride() != null ? configService.getGlobalRefillRateOverride() : refillRate;

        // Get the bucket for this client, or create a new one if it doesn't exist
        Bucket bucket = buckets.computeIfAbsent(clientId, k -> new Bucket(finalCapacity, finalRefillRate));
        
        // Ensure bucket config is updated if changed dynamically
        bucket.updateConfig(finalCapacity, finalRefillRate);

        if (!bucket.tryConsume()) {
            throw new RateLimitException("Too many requests! Limit: " + finalCapacity + " per window.");
        }
    }
    
    public void resetBuckets() {
        buckets.clear();
    }
    
    // Inner class representing a single Bucket
    private static class Bucket {
        private volatile long capacity;
        private volatile long refillRate;
        private final AtomicLong tokens;
        private long lastRefillTimestamp;

        public Bucket(long capacity, long refillRate) {
            this.capacity = capacity;
            this.refillRate = refillRate;
            this.tokens = new AtomicLong(capacity);
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        public synchronized void updateConfig(long newCapacity, long newRefillRate) {
            if (this.capacity != newCapacity || this.refillRate != newRefillRate) {
                this.capacity = newCapacity;
                this.refillRate = newRefillRate;
                // Reset tokens when config changes
                this.tokens.set(newCapacity); 
            }
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens.get() > 0) {
                tokens.decrementAndGet();
                return true;
            }
            return false;
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long timePassed = now - lastRefillTimestamp;

            // Calculate how many tokens to add based on time passed
            long tokensToAdd = (timePassed / 1000) * refillRate;

            if (tokensToAdd > 0) {
                long currentTokens = tokens.get();
                // Ensure we don't exceed capacity
                tokens.set(Math.min(capacity, currentTokens + tokensToAdd));
                lastRefillTimestamp = now;
            }
        }
    }
}
