
package com.example.reservationservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.SubscribableChannel;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Stream;


@EnableBinding(ConsumerChannels.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(ReservationRepository rr) {
        return args -> {
            Stream.of("Josh", "Heidi", "Cameron", "Saritha",
                    "Balaji", "Soumya", "Steve", "Kelsey")
                    .forEach(name -> rr.save(new Reservation(name)));

            rr.findAll().forEach(System.out::println);
        };
    }

    @Bean
    IntegrationFlow inboundReservationFlow(ConsumerChannels channels,
                                           ReservationRepository rr) {
        return IntegrationFlows
                .from(channels.input())
                .handle((GenericHandler<String>) (reservationName, headers) -> {
                    rr.save(new Reservation(reservationName));
                    return null;
                })
                .get();
    }
}
/*
@Component
class StreamListenerComponent {

    @StreamListener ("input")
    public void on( String reservationName) {

    }
}*/

interface ConsumerChannels {
    @Input
    SubscribableChannel input();
}

@RestController
class ReservationRestController {


    private final ReservationRepository reservationRepository;

    ReservationRestController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @GetMapping("/reservations")
    Collection<Reservation> reservations() {
        return this.reservationRepository.findAll();
    }

}


@RestController
@RefreshScope
class MessageRestController {

    private final String value;

    MessageRestController(@Value("${message}") String value) {
        this.value = value;
    }

    @GetMapping("/message")
    String message() {
        return this.value;
    }
}

@Component
class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.status("I <3 Target!!!").build();
    }
}

interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Collection<Reservation> findByReservationName(String rn);
}

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
class Reservation {

    public Reservation(String reservationName) {
        this.reservationName = reservationName;
    }

    @Id
    @GeneratedValue
    private Long id;

    private String reservationName; // reservation_name
}