# Cloud Native Java

# Setup  
- you will need JDK 8, Maven, an IDE and Docker in order to follow along. Specify important environment variables before opening any IDEs: `JAVA_HOME`, `DOCKER_IP` and `DOCKER_HOST_IP`.
- Install [the Spring Boot CLI](http://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/getting-started-installing-spring-boot.html#getting-started-installing-the-cli) and [the Spring Cloud CLI](https://github.com/spring-cloud/spring-cloud-cli).
- [Install the Cloud Foundry CLI](https://docs.cloudfoundry.org/devguide/installcf/install-go-cli.html)
- goto the Spring Initializr and specify the latest milestone of Spring Boot 1.3 and then choose EVERY checkbox except those related to AWS, then click generate. In the shell, run `mvn -DskipTests=true clean install` to force the resolution of all those dependencies so you're not stalled later. You may discard this project after you've installed everything.

# "Bootcamp"

- go to the [Spring Initializr](http://start.spring.io) and select JPA, Vaadin, Web. Select the latest Spring Boot 1.3 MILESTONE version. give it an `artifatId` of `reservation-service`.
- Run `mvn clean install` and import it into your favorite IDE using Maven import.
- add a simple entity (`Reservation`) and a repository (`ReservationRepository`)
- add custom Hypermedia links
- write a simple unit test

# Making a Spring Boot application Production Ready
- add `org.springframework.boot`:`spring-boot-starter-actuator`
- customize the `HealthEndpoint` by contributing a custom `HealthIndicator`
- start `./bin/graphite.sh`
- add a `GraphiteMetricsWriter`
- add the Dropwizard Metrics Library
- add a semantic metric using `CounterService` and observe the histogram
- build an executable `.jar` (UNIX-specific) using the `<executable/>` configuration flag
- use the HAL browser

# Use the Config Server
- goto the Spring Initializr, Specify an `artifactId` of `config-server` and check the `Config Server` checkbox.
- point the new module to the configuration in our custom Git repository
- add the Spring Cloud BOM to the `reservation-service`.
- add `org.springframework.cloud`:`spring-cloud-starter-config` to the `reservation-service`.
- create a `MessageRestController` and annotate it with `@RefreshScope`
- trigger a refresh of the message using the `/refresh` endpoint.
- start `./bin/rabbitmq.sh`
- connect the microservice to the event bus using RabbitMQ and by adding the `org.springframework.cloud`:`spring-cloud-starter-bus-amqp` then triggering the refresh using the `/bus/refresh`.

# Service Registration and Discovery
- goto the Spring Initializr and stand up a Eureka server by creating a new module called `Eureka Server` and adding `@EnableEurekaServer`.
- Make sure this module _also_ talks to the Config Server as described in the last lab.
- add `@EnableDiscoveryClient` to the `reservation-service` and restart, and then confirm its appearance in the Eureka Server
- demonstrate using the `DiscoveryClient` API
- EXTRA CREDIT: install Consul and get it to work there :D



# Edge Services: microproxies
# Edge Services: client-side load-balancing 
# Edge Services: API gateways & the circuit breaker pattern
# Streams
# Logging & distributed tracing with ELK and Zipkin
# Security
# to the cloud!
