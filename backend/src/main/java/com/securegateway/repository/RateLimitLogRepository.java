package com.securegateway.repository;

import com.securegateway.model.RateLimitLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RateLimitLogRepository extends JpaRepository<RateLimitLog,Long> {
    List<RateLimitLog> findTop50ByOrderByTimestampDesc();
}
