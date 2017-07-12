package com.example.reservationclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import lombok.Data;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@EnableResourceServer
@IntegrationComponentScan
@EnableCircuitBreaker
@EnableFeignClients
@EnableBinding(ProducerChannels.class)
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
}

interface ProducerChannels {

    String OUTPUT = "output";

    @Output(OUTPUT)
    MessageChannel output();
}


@MessagingGateway
interface ReservationWriter {

    @Gateway(requestChannel = ProducerChannels.OUTPUT)
    void write(String rn);
}

@RestController
@RequestMapping("/reservations")
class ReservationApiAdapterRestController {

    private final ReservationReader reservationReader;
    private final ReservationWriter reservationWriter;

    ReservationApiAdapterRestController(ReservationWriter reservationWriter,
                                        ReservationReader reservationReader) {
        this.reservationReader = reservationReader;
        this.reservationWriter = reservationWriter;

    }

    public Collection<String> fallback() {
        return new ArrayList<>();
    }

    @PostMapping
    public void write(@RequestBody Reservation r) {
        this.reservationWriter.write(r.getReservationName());
    }

    @HystrixCommand(fallbackMethod = "fallback")
    @GetMapping("/names")
    public Collection<String> names() {
        return reservationReader
                .read()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }
}


@FeignClient("reservation-service")
interface ReservationReader {

    @RequestMapping(method = RequestMethod.GET, value = "/reservations")
    Collection<Reservation> read();

}

@Data
class Reservation {

    private String reservationName;
}