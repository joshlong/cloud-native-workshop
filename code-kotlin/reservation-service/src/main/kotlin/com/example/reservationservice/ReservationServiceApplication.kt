package com.example.reservationservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.cloud.client.discovery.EnableDiscoveryClient
import org.springframework.cloud.context.config.annotation.RefreshScope
import org.springframework.cloud.stream.annotation.EnableBinding
import org.springframework.cloud.stream.annotation.StreamListener
import org.springframework.cloud.stream.messaging.Sink
import org.springframework.context.annotation.Bean
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id

@EnableBinding(Sink::class)
@EnableDiscoveryClient
@SpringBootApplication
class ReservationServiceApplication {

    @Bean
    fun intializer(repository: ReservationRepository) =
            ApplicationRunner {
                arrayOf("Josh", "Sheng", "Wei", "Jialin",
                        "Kevin", "Ning", "Liz", "Abel")
                        .forEach { name -> repository.save(Reservation(reservationName = name)) }

                repository.findAll().forEach { println(it) }
            }

    @Bean
    fun health() = HealthIndicator {
        return@HealthIndicator Health.status("I <3 Microsoft!!").build()
    }
}

@RefreshScope
@RestController
class MessageRestController(@Value("\${message}") m: String) {

    var message: String? = m

    @GetMapping("/message")
    fun message() = this.message

}

@Component
class MessageProcessor(val repository: ReservationRepository) {

    @StreamListener("input")
    fun onNewMessagePleaseWriteToDatabaseThankYou(reservationName: String) {
        repository.save(Reservation(reservationName = reservationName))
    }
}


@RestController
class ReservationRestController(val repository: ReservationRepository) {

    @GetMapping("/reservations")
    fun reservations() =
            repository.findAll()
}

fun main(args: Array<String>) {
    SpringApplication.run(ReservationServiceApplication::class.java, *args)
}


interface ReservationRepository : JpaRepository<Reservation, Long>

@Entity
class Reservation(@Id @GeneratedValue var id: Long? = null, var reservationName: String? = null) {
    constructor() : this(null, null)
}