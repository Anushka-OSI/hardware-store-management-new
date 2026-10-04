package com.hardwarestore.hardwarestoremanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.guruge.hardware", "com.hardwarestore.hardwarestoremanagement"})
@EnableJpaRepositories(basePackages = "com.guruge.hardware.repository")
@EntityScan(basePackages = "com.guruge.hardware.entity")
public class HardwarestoremanagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(HardwarestoremanagementApplication.class, args);
	}

}
