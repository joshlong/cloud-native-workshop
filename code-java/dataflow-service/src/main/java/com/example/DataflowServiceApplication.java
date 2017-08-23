package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.dataflow.server.EnableDataFlowServer;

@EnableDiscoveryClient
@EnableDataFlowServer
@SpringBootApplication
public class DataflowServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DataflowServiceApplication.class, args);
	}
}
