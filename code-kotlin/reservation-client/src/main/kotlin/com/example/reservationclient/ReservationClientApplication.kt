package com.example.reservationclient

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.netflix.feign.EnableFeignClients
import org.springframework.cloud.netflix.feign.FeignClient
import org.springframework.cloud.netflix.zuul.EnableZuulProxy
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.messaging.Source
import org.springframework.messaging.support.MessageBuilder
import org.springframework.web.bind.annotation.*


@EnableBinding(Source::class)
@EnableCircuitBreaker
@EnableFeignClients
@EnableZuulProxy
@EnableDiscoveryClient
@SpringBootApplication
class ReservationClientApplication

fun main(args: Array<String>) {
    SpringApplication.run(ReservationClientApplication::class.java, *args)
}

@FeignClient("reservation-service")
interface ReservationReader {

    @GetMapping("/reservations")
    fun read(): Array<Reservation>

}

class Reservation(var id: Long? = null, var reservationName: String? = null) {
    constructor() : this(null, null)
}

@RestController
@RequestMapping("/reservations")
class ReservationApiAdapterRestController(
        val reader: ReservationReader,
        val source: Source) {

    @PostMapping
    fun write(@RequestBody reservation: Reservation) {
        // todo send to reservation-service
        val msg = MessageBuilder
                .withPayload(reservation.reservationName)
                .build()
        this.source.output().send(msg)
    }

    fun fallback(): List<String?> = emptyList()

    @HystrixCommand(fallbackMethod = "fallback")
    @GetMapping("/names")
    fun names(): List<String?> = reader.read().map { it.reservationName }


}
