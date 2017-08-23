package com.example.hystrixdashboard

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.hystrix.dashboard.EnableHystrixDashboard

@EnableDiscoveryClient
@EnableHystrixDashboard
@SpringBootApplication
class HystrixDashboardApplication

fun main(args: Array<String>) {
    SpringApplication.run(HystrixDashboardApplication::class.java, *args)
}
