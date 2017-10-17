package com.example.reservationclient;

import com.google.common.util.concurrent.RateLimiter;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Source;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/*

interface ProducerChannels {

    @Output
    MessageChannel output();
}
*/

@EnableResourceServer
@EnableBinding(Source.class)
@EnableCircuitBreaker
@EnableFeignClients
@EnableDiscoveryClient
@EnableZuulProxy
@SpringBootApplication
public class ReservationClientApplication {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    CommandLineRunner commandLineRunner() {
        return args -> System.out.println("hello, world");
    }

    @Bean
    ApplicationRunner client(DiscoveryClient client) {
        return args ->
                client.getInstances("reservation-service")
                        .forEach((ServiceInstance si) -> System.out.println(si.getHost() + ':' + si.getPort()));
    }

    public static void main(String[] args) {
        SpringApplication.run(ReservationClientApplication.class, args);
    }
}

@RestController
@RequestMapping("/reservations")
class ReservationApiAdapterRestController {
/*
    private final RestTemplate restTemplate;

    ReservationApiAdapterRestController(
            @LoadBalanced RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }*/

    private final MessageChannel out;
    private final ReservationReader reader;

    ReservationApiAdapterRestController(ReservationReader reader,
                                        Source source) {
        this.reader = reader;
        this.out = source.output();
    }

    public Collection<String> fallback() {
        return new ArrayList<>();
    }

    @PostMapping
    public void write(@RequestBody Reservation r) {
        String reservationName = r.getReservationName();
        Message<String> msg = MessageBuilder.withPayload(reservationName)
                .build();
        this.out.send(msg);
    }

    @GetMapping("/names")
    @HystrixCommand(fallbackMethod = "fallback")
    public Collection<String> names() {

   /*     ParameterizedTypeReference<Collection<Reservation>> ptr =
                new ParameterizedTypeReference<Collection<Reservation>>() {
                };

        Collection<Reservation> reservations = this.restTemplate.exchange("http://reservation-service/reservations",
                HttpMethod.GET, null, ptr).getBody();
*/
        return this.reader.read()
                .stream()
                .map(Reservation::getReservationName)
                .collect(Collectors.toList());
    }
}



/*url = "http://localhost:8000", name = "client"*/

@FeignClient("reservation-service")
interface ReservationReader {

    @GetMapping("/reservations")
    Collection<Reservation> read();
}

@AllArgsConstructor
@NoArgsConstructor
@Data
class Reservation {
    private Long id;
    private String reservationName;
}

//@Component
class RateLimitingZuulFilter extends ZuulFilter {

    private RateLimiter rateLimiter = RateLimiter.create(.1);

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext currentContext = RequestContext.getCurrentContext();
        HttpServletResponse response = currentContext.getResponse();

        if (!rateLimiter.tryAcquire()) {
            currentContext.setSendZuulResponse(false);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        }
        return null;
    }
}