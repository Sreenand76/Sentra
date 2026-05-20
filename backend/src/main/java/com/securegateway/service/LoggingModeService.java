package com.securegateway.service;

import org.springframework.stereotype.Service;

@Service
public class LoggingModeService {
    private boolean loadTestMode = false;

    public boolean isLoadTestMode() {
        return loadTestMode;
    }

    public void setLoadTestMode(boolean loadTestMode) {
        this.loadTestMode = loadTestMode;
    }
}