package lk.ijse.examsy.adminservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"lk.ijse.examsy.adminservice", "lk.ijse.examsy.common"})
public class ExamsyAdminServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExamsyAdminServiceApplication.class, args);
    }
}
