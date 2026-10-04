package com.example.xGallery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.xGallery.mapper")
public class XGalleryApplication {

	public static void main(String[] args) {
		SpringApplication.run(XGalleryApplication.class, args);
	}

}
