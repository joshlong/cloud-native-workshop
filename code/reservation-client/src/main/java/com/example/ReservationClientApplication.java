package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.hateoas.Resources;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.MessageChannel;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.stream.Collectors;

@EnableResourceServer
@EnableDiscoveryClient
@SpringBootApplication
@EnableFeignClients
@EnableBinding(ReservationChannels.class)
@IntegrationComponentScan
public class ReservationClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}


interface ReservationChannels {

	@Output
	MessageChannel output();
}

class Reservation {
	private String reservationName;

	public String getReservationName() {
		return reservationName;
	}

	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}
}

@FeignClient("reservation-service")
interface ReservationReader {

	@RequestMapping(method = RequestMethod.GET, value = "/reservations")
	Resources<Reservation> read();
}

@MessagingGateway
interface ReservationWriter {

	@Gateway(requestChannel = "output")
	void write(String rn);
}

@RestController
@RequestMapping("/reservations")
class ReservationApiGatewayRestController {

	private final ReservationReader reservationReader;
	private final ReservationWriter reservationWriter;

	@GetMapping("/names")
	public Collection<String> names() {
		return reservationReader.read()
				.getContent()
				.stream()
				.map(Reservation::getReservationName)
				.collect(Collectors.toList());
	}

	@PostMapping
	public void write(@RequestBody Reservation r) {
		this.reservationWriter.write(r.getReservationName());
	}

	ReservationApiGatewayRestController(
			ReservationReader reservationReader, ReservationWriter reservationWriter) {
		this.reservationReader = reservationReader;
		this.reservationWriter = reservationWriter;
	}
}