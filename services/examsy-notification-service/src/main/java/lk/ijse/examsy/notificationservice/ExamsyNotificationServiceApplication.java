package lk.ijse.examsy.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.notificationservice", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
@EnableAsync
@EnableScheduling
public class ExamsyNotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyNotificationServiceApplication.class, args);
    }
}
