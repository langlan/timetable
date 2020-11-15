package com.github.langlan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class AccessingDataRestApplication {

	public static void main(String[] args) {
		SpringApplication.run(AccessingDataRestApplication.class, args);
	}
	
	@RequestMapping("/")
	public String index() {
		return "{\"msg\": \"hello world \"}";
	}
}
