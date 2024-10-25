package com.comp5348.deliveryco;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync //启动异步处理
public class DeliveryApplication {


	public static void main(String[] args) {
		SpringApplication.run(DeliveryApplication.class, args);
	}

}
