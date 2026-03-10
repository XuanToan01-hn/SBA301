package com.buildings;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JavaL1LxtApplication {

    public static void main(String[] args) {
        SpringApplication.run(JavaL1LxtApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void openSwaggerUI() {
        String url = "http://localhost:8080/building-management/swagger-ui/index.html";
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", url});
        } catch (Exception e) {
            System.out.println("Could not open browser: " + e.getMessage());
        }
    }

}
