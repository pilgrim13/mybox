package com.mybox.mybox;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class })
public class MyboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyboxApplication.class, args);
	}

}
