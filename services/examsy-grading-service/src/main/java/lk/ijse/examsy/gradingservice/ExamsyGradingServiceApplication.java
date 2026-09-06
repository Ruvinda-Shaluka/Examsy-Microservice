package lk.ijse.examsy.gradingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.gradingservice", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
@EnableAsync
public class ExamsyGradingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyGradingServiceApplication.class, args);
    }
}
