package lk.ijse.examsy.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Examsy Eureka Server:
 * Service registry and dynamic discovery backbone for the Examsy platform.
 * Microservices register their active network addresses (host, port) here,
 * and the API Gateway queries this registry to load-balance incoming requests.
 */
@SpringBootApplication
@EnableEurekaServer
public class ExamsyEurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyEurekaServerApplication.class, args);
    }
}
