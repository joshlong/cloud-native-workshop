package com.example;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.client.EnableOAuth2Sso;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
//import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableResourceServer
@EnableBinding(ReservationChannels.class)
@EnableZuulProxy
@EnableCircuitBreaker
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

	@Bean
	@LoadBalanced
	RestTemplate restTemplate() {
		return new RestTemplate();
	}

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}

interface ReservationChannels {

	@Output
	MessageChannel output();
}

@RestController
class UserInfoRestController {

	@RequestMapping("/user/info")
	Principal principal(Principal p) {
		return p;
	}
}


@RestController
@RequestMapping("/reservations")
class ReservationServiceApiGatewayRestController {

	private final MessageChannel channel;
	private final RestTemplate restTemplate;

	@RequestMapping(method = RequestMethod.POST)
	public void write(@RequestBody Reservation reservation) {
		this.channel.send(
				MessageBuilder.withPayload(reservation.getReservationName()).build()
		);
	}

	@Autowired
	public ReservationServiceApiGatewayRestController(
			@LoadBalanced RestTemplate restTemplate, ReservationChannels channels) {
		this.restTemplate = restTemplate;
		this.channel = channels.output();
	}


	public Collection<String> fallback() {
		return new ArrayList<>();
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET, value = "/names")
	public Collection<String> names() {
		return this
				.restTemplate
				.exchange("http://reservation-service/reservations",
						HttpMethod.GET, null,
						new ParameterizedTypeReference<Resources<Reservation>>() {
						})
				.getBody()
				.getContent()
				.stream()
				.map(Reservation::getReservationName)
				.collect(Collectors.toList());
	}

}


class Reservation {
	private String reservationName;

	public String getReservationName() {
		return reservationName;
	}
}

