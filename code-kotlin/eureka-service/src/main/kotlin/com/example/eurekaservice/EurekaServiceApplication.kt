package com.example.eurekaservice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer

@EnableEurekaServer
@SpringBootApplication
class EurekaServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(EurekaServiceApplication::class.java, *args)
}
