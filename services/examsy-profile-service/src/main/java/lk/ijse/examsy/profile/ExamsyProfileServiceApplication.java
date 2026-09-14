package lk.ijse.examsy.profile;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.profile", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
public class ExamsyProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyProfileServiceApplication.class, args);
    }
}
