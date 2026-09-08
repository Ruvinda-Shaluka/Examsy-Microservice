package lk.ijse.examsy.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Examsy Auth Service:
 * Manages user authentication, account credential security, password resets,
 * and publishes user lifecycle events (user.registered) to Apache Kafka.
 */
@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.auth", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
public class ExamsyAuthServiceApplication {

    public static void main(String[] args) {
        loadDotEnv();
        SpringApplication.run(ExamsyAuthServiceApplication.class, args);
    }

    /**
     * Automatically loads .env configuration into System properties prior to Spring initialization.
     * Ensures secrets (like Google OAuth2 credentials) are seamlessly available in IDE runs
     * without hardcoding secrets into tracked properties files.
     */
    private static void loadDotEnv() {
        String[] candidatePaths = {
                ".env",
                "../../.env",
                "../.env",
                "D:/Examsy/Examsy-Microservice/.env"
        };
        for (String path : candidatePaths) {
            File envFile = new File(path);
            if (envFile.exists() && envFile.isFile()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        line = line.trim();
                        if (!line.isEmpty() && !line.startsWith("#") && line.contains("=")) {
                            int eqIdx = line.indexOf('=');
                            String key = line.substring(0, eqIdx).trim();
                            String value = line.substring(eqIdx + 1).trim();
                            if (System.getProperty(key) == null && System.getenv(key) == null) {
                                System.setProperty(key, value);
                            }
                        }
                    }
                    break;
                } catch (IOException ignored) {
                }
            }
        }
    }
}
