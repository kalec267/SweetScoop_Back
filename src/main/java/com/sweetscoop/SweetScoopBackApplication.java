package com.sweetscoop;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan({
    "com.sweetscoop.order.repository",
    "com.sweetscoop.menu.repository",
    "com.sweetscoop.size.repository",
    "com.sweetscoop.cup.repository"
})
public class SweetScoopBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(SweetScoopBackApplication.class, args);
    }

}