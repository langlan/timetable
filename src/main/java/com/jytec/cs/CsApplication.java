package com.jytec.cs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class CsApplication extends SpringBootServletInitializer{

	public static void main(String[] args) {
		SpringApplication.run(CsApplication.class, args);
	}
	
	@Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        // return application.sources(...);
		System.out.println("...");
		return builder;
    }

	@RequestMapping("/t")
	public String index() {
		return "{\"msg\": \"hello world \"}";
	}
}
