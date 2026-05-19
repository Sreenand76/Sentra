package com.securegateway.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rate_limit_logs")
public class RateLimitLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String clientIp;
    private LocalDateTime timestamp;
    private boolean blocked;

    public RateLimitLog() {
    }

    public RateLimitLog(String clientIp, LocalDateTime timestamp, boolean blocked) {
        this.clientIp = clientIp;
        this.timestamp = timestamp;
        this.blocked = blocked;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }
}
