package lk.ijse.examsy.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

/**
 * Examsy Config Server:
 * Centralized configuration service for all Examsy microservices.
 * Reads configurations from the central config-repo using the native filesystem profile.
 */
@SpringBootApplication
@EnableConfigServer
public class ExamsyConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyConfigServerApplication.class, args);
    }
}
