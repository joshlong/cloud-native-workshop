package com.example.reservationservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.context.scope.refresh.RefreshScopeRefreshedEvent;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.support.GenericHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.stream.Stream;

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

    @Bean
    IntegrationFlow incomingReservations(Sink sink, ReservationRepository rr) {
        return IntegrationFlows
                .from(sink.input())
                .handle((GenericHandler<String>) (reservationName, headers) -> {
                    rr.save(new Reservation(null, reservationName));
                    return null;
                })
                .get();
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationServiceApplication.class, args);
    }
}

@Component
class SampleDataInitializer implements ApplicationRunner {

    private final ReservationRepository reservationRepository;

    SampleDataInitializer(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Stream.of("Josh", "Jaya", "John", "Danny",
                "Brian", "Quyen", "Dan", "Alex")
                .forEach(name -> reservationRepository.save(new Reservation(null, name)));

        reservationRepository.findAll().forEach(System.out::println);
    }
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

interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Collection<Reservation> findByReservationName(String rn);
}

@RestController
@RefreshScope
class MessageRestController {

    private final String value;

    MessageRestController(@Value("${message}") String value) {
        this.value = value;
    }

    @GetMapping("/message")
    String read() {
        return this.value;
    }
}

@Slf4j
@Component
class Listener {

    @EventListener(RefreshScopeRefreshedEvent.class)
    public void onRefresh(RefreshScopeRefreshedEvent evt) {
        log.info("refresh!");
    }
}

@Component
class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        return Health.status("I <3 Production!!!").build();
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
class Reservation {

    @Id
    @GeneratedValue
    private Long id;

    private String reservationName;
}