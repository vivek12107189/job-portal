package com.vivek.jobportal;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class JobPortalApplication {

	public static void main(String[] args) {

		SpringApplication.run(JobPortalApplication.class, args);
	}

}
