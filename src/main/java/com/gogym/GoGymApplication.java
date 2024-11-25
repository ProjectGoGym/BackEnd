package com.gogym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class GoGymApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoGymApplication.class, args);
	}
}
