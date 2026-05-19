package com.securegateway.service;

import org.springframework.stereotype.Service;

@Service
public class RateLimitConfigService {

    private volatile Integer globalCapacityOverride = null;
    private volatile Integer globalRefillRateOverride = null;

    public void setOverrides(Integer capacity, Integer refillRate) {
        this.globalCapacityOverride = capacity;
        this.globalRefillRateOverride = refillRate;
    }

    public Integer getGlobalCapacityOverride() {
        return globalCapacityOverride;
    }

    public Integer getGlobalRefillRateOverride() {
        return globalRefillRateOverride;
    }
}
