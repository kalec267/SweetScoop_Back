package com.sweetscoop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.sweetscoop.order.mapper")
@SpringBootApplication
public class SweetScoopBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(SweetScoopBackApplication.class, args);
    }

}