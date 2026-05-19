# AutoShield Secure API Gateway

Production-grade Secure API Gateway built with **Spring Boot** + **Spring Cloud Gateway**.

## What’s included (scaffolding only)
- Gateway routing foundation (Spring Cloud Gateway dependency)
- Reactive “web stack” foundation (`spring-boot-starter-webflux`; avoid `spring-boot-starter-web` with Gateway)
- Security foundation (Spring Security dependency; JWT auth to be implemented via gateway filters)
- Redis reactive client (rate limiting + counters foundation)
- Actuator for health/metrics
- Structured JSON logging dependency
- JWT library dependency (JJWT: `jjwt-api` + runtime `jjwt-impl`/`jjwt-jackson`)
- Optional Lombok dependency

## Layout
- `com.autoshield.gateway.config`
- `com.autoshield.gateway.filter`
- `com.autoshield.gateway.controller`
- `com.autoshield.gateway.service`
- `com.autoshield.gateway.util`

## Requirements
- Java 21
- Maven 3.9+

## Run (dev)
Run from `backend/` (this Maven module):

```bash
mvn spring-boot:run
```

