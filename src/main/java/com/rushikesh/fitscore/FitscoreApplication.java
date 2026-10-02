package com.rushikesh.fitscore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class FitscoreApplication {

    public static void main(String[] args) {
        // Java on some Windows/India setups sends the old zone name "Asia/Calcutta",
        // which newer PostgreSQL versions reject. UTC is accepted everywhere.
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(FitscoreApplication.class, args);
    }
}