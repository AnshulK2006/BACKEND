package com.cvns;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
@SpringBootApplication @EnableScheduling
public class VaccinationApplication { public static void main(String[] args){ SpringApplication.run(VaccinationApplication.class,args); } }
