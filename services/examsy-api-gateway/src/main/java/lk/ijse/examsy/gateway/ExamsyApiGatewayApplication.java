package lk.ijse.examsy.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Examsy API Gateway:
 * Single public entry-point (Port 8080) for the Examsy platform.
 * Handles:
 * 1. Routing requests to microservices using Eureka service discovery (lb://)
 * 2. Centralized CORS negotiation for the React frontend
 * 3. Actuator health monitoring & metrics
 */
@SpringBootApplication
public class ExamsyApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyApiGatewayApplication.class, args);
    }
}
