package com.smartsplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SmartsplitApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartsplitApplication.class, args);
	}

}
