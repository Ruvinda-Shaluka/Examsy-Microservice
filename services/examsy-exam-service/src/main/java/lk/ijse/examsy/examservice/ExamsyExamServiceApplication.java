package lk.ijse.examsy.examservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.examservice", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
public class ExamsyExamServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyExamServiceApplication.class, args);
    }
}
