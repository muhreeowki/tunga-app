package com.funnfood.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FunNFoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(FunNFoodApplication.class, args);
	}
}
