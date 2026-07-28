package com.app.work;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WorkOutsideWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(WorkOutsideWebApplication.class, args);
	}

}
