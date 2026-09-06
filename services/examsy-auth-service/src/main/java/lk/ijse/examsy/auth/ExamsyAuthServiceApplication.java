package lk.ijse.examsy.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Examsy Auth Service:
 * Manages user authentication, account credential security, password resets,
 * and publishes user lifecycle events (user.registered) to Apache Kafka.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class ExamsyAuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyAuthServiceApplication.class, args);
    }
}
