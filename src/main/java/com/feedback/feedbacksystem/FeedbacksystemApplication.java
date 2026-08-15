package com.feedback.feedbacksystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class FeedbacksystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(FeedbacksystemApplication.class, args);
	}

}
