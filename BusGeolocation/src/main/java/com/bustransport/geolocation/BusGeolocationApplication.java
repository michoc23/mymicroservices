package com.bustransport.geolocation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BusGeolocationApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusGeolocationApplication.class, args);
    }
}

