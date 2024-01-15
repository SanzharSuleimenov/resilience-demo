package com.parqour.resiliencedemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class ResilienceDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(ResilienceDemoApplication.class, args);
  }

}
