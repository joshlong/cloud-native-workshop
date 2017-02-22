package com.example;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.messaging.SubscribableChannel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableDiscoveryClient
@SpringBootApplication
@EnableBinding(ReservationChannels.class)
@IntegrationComponentScan
public class ReservationServiceApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}

	private final ReservationRepository reservationRepository;

	public ReservationServiceApplication(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		Stream.of("Josh,Ray,Matt,Dave,Spencer".split(","))
				.forEach(name -> reservationRepository.save(new Reservation(name)));
		reservationRepository.findAll().forEach(System.out::println);
	}
}

interface ReservationChannels {

	@Input
	SubscribableChannel input();
}

@MessageEndpoint
class ReservationProcessor {

	@StreamListener(value = "input")
	public void process(String rn) {
		this.reservationRepository.save(new Reservation(rn));
	}

	private final ReservationRepository reservationRepository;

	public ReservationProcessor(ReservationRepository reservationRepository) {
		this.reservationRepository = reservationRepository;
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long> {

	@RestResource(path = "by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@Entity
@Data
@NoArgsConstructor
class Reservation {

	@Id
	@GeneratedValue
	private Long id;

	private String reservationName;

	public Reservation(String rn) {
		this.reservationName = rn;
	}
}