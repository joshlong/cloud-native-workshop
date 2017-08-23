package com.example.zipkinservice

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import zipkin.server.EnableZipkinServer

@EnableDiscoveryClient
@EnableZipkinServer
@SpringBootApplication
class ZipkinServiceApplication

fun main(args: Array<String>) {
    SpringApplication.run(ZipkinServiceApplication::class.java, *args)
}
