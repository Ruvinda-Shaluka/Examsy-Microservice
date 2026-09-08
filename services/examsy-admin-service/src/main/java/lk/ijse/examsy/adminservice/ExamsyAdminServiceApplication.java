package lk.ijse.examsy.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication(scanBasePackages = {"lk.ijse.examsy.adminservice", "lk.ijse.examsy.common"})
@EnableDiscoveryClient
public class ExamsyAdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyAdminServiceApplication.class, args);
    }
}
