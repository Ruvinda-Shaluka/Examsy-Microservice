package lk.ijse.examsy.classservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.classservice", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
public class ExamsyClassServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyClassServiceApplication.class, args);
    }
}
