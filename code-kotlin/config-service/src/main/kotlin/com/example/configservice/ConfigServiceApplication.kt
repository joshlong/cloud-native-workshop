package com.example.configservice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.config.server.EnableConfigServer

@EnableConfigServer
@SpringBootApplication
class ConfigServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(ConfigServiceApplication::class.java, *args)
}
