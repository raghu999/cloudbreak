package com.sequenceiq.caas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MockCaasApp {

    public static void main(String[] args) {
        if (args.length == 0) {
            SpringApplication.run(MockCaasApp.class);
        } else {
            SpringApplication.run(MockCaasApp.class, args);
        }
    }

}
