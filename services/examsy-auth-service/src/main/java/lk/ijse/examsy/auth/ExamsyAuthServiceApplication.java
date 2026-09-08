package lk.ijse.examsy.auth;

import lk.ijse.examsy.common.security.JwtTokenProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

/**
 * Examsy Auth Service:
 * Manages user authentication, account credential security, password resets,
 * and publishes user lifecycle events (user.registered) to Apache Kafka.
 */
@SpringBootApplication
@EnableDiscoveryClient
@Import(JwtTokenProvider.class)
public class ExamsyAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyAuthServiceApplication.class, args);
    }
}
