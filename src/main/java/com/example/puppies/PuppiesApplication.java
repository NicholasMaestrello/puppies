package com.example.puppies;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@EnableCaching
@SpringBootApplication
public class PuppiesApplication {

	public static void main(String[] args) {
		SpringApplication.run(PuppiesApplication.class, args);
	}

}
