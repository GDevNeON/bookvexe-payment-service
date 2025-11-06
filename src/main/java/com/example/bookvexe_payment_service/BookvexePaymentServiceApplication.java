package com.example.bookvexe_payment_service;

import com.example.bookvexe_payment_service.repositories.base.BaseRepositoryImpl;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.example.bookvexe_payment_service.repositories", repositoryBaseClass = BaseRepositoryImpl.class)
@SpringBootApplication
public class BookvexePaymentServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(BookvexePaymentServiceApplication.class, args);
    }

}
