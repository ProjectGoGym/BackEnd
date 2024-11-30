package com.gogym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EnableAspectJAutoProxy
@EnableScheduling
public class GoGymApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoGymApplication.class, args);
	}
}
