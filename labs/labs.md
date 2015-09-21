# Cloud Native Java Workshop

## Setup  

> microservices, for better or for worse, involve a lot of moving parts. Let's make sure we can run all those things in this lab.

- you will need JDK 8, Maven, an IDE and Docker in order to follow along. Specify important environment variables before opening any IDEs: `JAVA_HOME`, `DOCKER_IP` and `DOCKER_HOST_IP`.
- Install [the Spring Boot CLI](http://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/getting-started-installing-spring-boot.html#getting-started-installing-the-cli) and [the Spring Cloud CLI](https://github.com/spring-cloud/spring-cloud-cli).
- [Install the Cloud Foundry CLI](https://docs.cloudfoundry.org/devguide/installcf/install-go-cli.html)
- go to the [Spring Initializr](http://start.spring.io) and specify the latest milestone of Spring Boot 1.3 and then choose EVERY checkbox except those related to AWS, then click generate. In the shell, run `mvn -DskipTests=true clean install` to force the resolution of all those dependencies so you're not stalled later. Then, run `mvn clean install` to force the resolution of the test scoped dependencies. You may discard this project after you've `install`ed everything.
- run each of the `.sh` scripts in the `./bin` directory; run `psql.sh` after you've run `postgresh.sh` and confirm that they all complete and emit no obvious errors

## 1. "Bootcamp"

> in this lab we'll take a look at building a basic Spring Boot application that uses JPA and Spring Data REST. We'll look at how to start a new project, how Spring Boot exposes functionality, and how testing works.

- go to the [Spring Initializr](http://start.spring.io) and select JPA, Vaadin, Web. Select the latest Spring Boot 1.3 MILESTONE version. give it an `artifatId` of `reservation-service`.
- Run `mvn clean install` and import it into your favorite IDE using Maven import.
- add a simple entity (`Reservation`) and a repository (`ReservationRepository`)
- map the repository to the web by adding  `org.springframework.boot`:`spring-boot-starter-data-rest` and then annotating the repository with `@RepositoryRestResource`
- add custom Hypermedia links
- write a simple unit test

## Making a Spring Boot application Production Ready

> Code complete != production ready! If you've ever read Michael Nygard's amazing tome, _Release It!_, then you know that the last mile between being code complete and being to production is _much_ longer than anyone ever anticipates. In this lab, we'll look at how Spring Boot is optimized for the continuous delivery of applications into production.

- add `org.springframework.boot`:`spring-boot-starter-actuator`
- customize the `HealthEndpoint` by contributing a custom `HealthIndicator`
- start `./bin/graphite.sh`
- add a `GraphiteMetricsWriter`
- add the Dropwizard Metrics Library
- add a semantic metric using `CounterService` and observe the histogram
- build an executable `.jar` (UNIX-specific) using the `<executable/>` configuration flag
- use the HAL browser
- add `info.build.artifact=${project.artifactId}` and `info.build.version=${project.version}`  to `application.properties`.
- configure Maven resource filtering and the Git commit ID plugin in the `pom.xml` in all existing and subsequent `pom.xml`s, or extract out a common parent `pom.xml` that all modules may extend.
- introduce a new `@RepositoryEventHandler` and `@Component`. Provide handlers for `@HandleAfterCreate`, `@HandleAfterSave`, and `@HandleAfterDelete`. Extract common counters to a shared method


## Use the Config Server

> the [12 Factor](http://12factor.net/config) manifesto speaks about externalizing that which changes from one environment to another - hosts,  locators, passwords, etc. - from the application itself. Spring Boot readily supports this pattern, but it's not enough. In this lab, we'll loko at how to centralize, externalize, and dynamically update application configuration with the Spring Cloud Config Server.

- go to the Spring Initializr, Specify an `artifactId` of `config-server` and check the `Config Server` checkbox.
- In `application.properties` for the Config Server, point the new module to the configuration in our custom Git repository with the property `spring.cloud.config.server.git.uri`.
- Add `server.port=8888` in `application.properties` to ensure that the Config Server is running on the right port for service to find it.
- add the Spring Cloud BOM to the `reservation-service`.
- add `org.springframework.cloud`:`spring-cloud-starter-config` to the `reservation-service`.
- create a `boostrap.properties` that lives in the same place as `application.properties` and discard the `application.properties` file. Instead, we now need only tell the Spring application where to find the Config Server, with `spring.cloud.config.uri=${config.server:http://localhost:8888}`, and how to identify itself to the Config Server and other services, later, with `spring.application.name`.

> We'll copy and paste  `bootstrap.properties` for each subsequent module, changing only the `spring.application.name` as appropriate.

- In the `reservation-service`, create a `MessageRestController` and annotate it with `@RefreshScope`. Inject the `${message}` key and expose it as a REST endpoint, `/message`.
- trigger a refresh of the message using the `/refresh` endpoint.
- start `./bin/rabbitmq.sh`
- connect the microservice to the event bus using RabbitMQ and by adding the `org.springframework.cloud`:`spring-cloud-starter-bus-amqp` then triggering the refresh using the `/bus/refresh`.

## Service Registration and Discovery

> In the cloud, applications live and die as capacity dictates, they're ephemeral. Applications should not be coupled to the physical location of other services as this state is fleeting. Indeed, even if it were fixed, services may quickly become overwhelmed, so it's very handy to be able to specify how to load balance among the available instances or indeed ask the system to verify that there are instances at all. In this lab, we'll look at the low-level `DiscoveryClient` abstraction at the heart of Spring Cloud's service registration and discovery support.

- go to the Spring Initializr and stand up a Eureka server by creating a new module called `Eureka Server` and adding `@EnableEurekaServer`.
- Make sure this module _also_ talks to the Config Server as described in the last lab.
- identify the service as `eureka-server`.
- add `@EnableDiscoveryClient` to the `reservation-service` and restart, and then confirm its appearance in the Eureka Server
- demonstrate using the `DiscoveryClient` API
- use the Spring Initializr, setup a new module, `reservation-client`, that uses the Config Server, Eureka Discovery, and Web.
- create a `CommandLineRunner` that uses the `DiscoveryClient` to look up other services programatically
- **EXTRA CREDIT**: install [Consul](http://Consul.io) and replace Eureka with Consul. You could use `./bin/consul.sh`, but prepare yourself for some confusion around host resolution if you're running Docker inside a Vagrant VM.

<!--
# CHECKPOINT # 1
- remove the `CommandLineRunner` that injects the `DiscoveryClient` from `reservation-client`.
- The code as it stands at this point will be the starting point for all subsequent exercises. I recommend you create new working directories for each lab and just copy in the `reservation-service`, `eureka-service`, `config-service` and `reservation-client` as they stand now, each time. We _could_ combine everything we're about to look at and just incrementally add everything to one giant example, but this will require quite a few moving parts (not to mention human and computer memory!) It's safer and more approachable to do small focused examples and then - as you need to in real-world development - pick and choose which combinations you need.
-->

## Edge Services: API gateways (circuit breakers, client-side load balancing)
> API gateways are used whenever a client - like a mobile phone or HTML5 client - requires API translation. Perhaps the client requires coarser grained payloads, or transformed views on

- create a client side DTO to hold the `Reservation` data from the service. Do this to avoid being coupled between client and service
- add a REST service called `ReservationNamesRestController` that uses the `@Autowired @LoadBalanced RestTemplate rt` to make a load-balanced call to a service in the registry using Ribbon.
- add `org.springframework.boot`:`spring-boot-starter-hateoas`
- make a call to `http://reservation-service/reservations` using the `RestTemplate#exchange` method, specifying the return value with a `ParameterizedTypeReference<Resources<Reservation>>>` as the final argument to the `RestTemplate#exchange` method.
- take the results of the call and map them from `Reservation` to `Reservation#getReservationName` and return them from a REST endpoint, `/reservations/names`.
- add `@EnableCircuitBreaker` to our configuration class
- add `@HystrixCommand` around any potentially shaky service-to-service calls like the `RestTemplate` call in the last lab.
- go to the [Spring Initializr](http://start.spring.io) and stand up a new service that uses Eureka Discovery, Config Client, and the Hystrix Dashboard.
- identify it is as `hystrix-dashboard` in `bootstrap.properties`
- annotate it with `@EnableHystrixDashboard` and run it.

## Edge Services: microproxies
> proxy requests from an edge-service to mid-tier services with a microproxy. For some classes of clients, a microproxy and security (HTTPS, authentication) might be enough

- take the code from the checkpoint #1 and run everything except the `reservation-client`
- add `org.springframework.cloud`:`spring-cloud-starter-zuul` and   `@EnableZullProxy` to the `reservation-client`, then run it.
- launch a browser and visit the `reservation-client` in the browser under the context path `/reservation-service/reservations`.

## Streams
> while REST is an east, powerful approach to building services, it doesn't provide much in the way of guarantees about state. A failed write needs to be retried, requiring more work of the client. Messaging, on the other hand, guarantees that _eventually_ the intended write will be processed. Eventual consistency works most of the time; even banks don't use distributed transactions! In this lab, we'll look at Spring Cloud Stream which builds atop Spring Integration and the messaging subsystem from Spring XD. Spring Cloud Stream provides the notion of _binders_ that automatically wire up message egress and ingress given a valid connection factory and an agreed upon destination (e.g.: `reservations` or `orders`).

- start `./bin/rabbitmq.sh`

> Sources - like water from a faucet - describe where messages may come from. In our example, messages come from the `reservation-client` that wishes to write messages to the `reservation-service` from the API gateway.

- add `org.springframework.cloud`:`spring-cloud-starter-stream-binder-rabbit`
- add `@EnableBinding(Source.class)` to the `reservation-client` `DemoApplication`
- create a new REST endpoint in the `ReservationNamesRestController` to accept new reservations by reservation-name
- observe that the `Source.class` describes one or more Spring `MessageChannel`s which are themselves annotated with useful qualifiers like `@Output("output")`.
- in the new endpoint, inject the Spring `MessageChannel` and qualify it with `@Output("output")` - the same one as in the `Source.class` definition.
- use the `MessageChannel` to send a message to the `reservation-service`. Connect the two modules through a agreed upon name, which we'll call `reservations`.
- Observe that this is specified in the config server for us in the `reservation-service` module: `spring.cloud.stream.bindings.output=reservations`. `output` is arbitrary and refers to the (arbitrary) channel of the same name described and referenced from the `Source.class` definition.

> Sinks receive messages that flow _to_ this service (like the kitchen sink into which water from the faucet flows).

- add `org.springframework.cloud`:`spring-cloud-starter-stream-binder-rabbit` to the `reservation-service` `DemoApplication`
- add `@EnableBinding(Sink.class)` to the `reservation-service`  `DemoApplication`
- observe that the `Sink.class` describes one or more Spring `MessageChannel`s which are themselves annotated with useful qualifiers like `@Input("input")`.
- create a new `@MessagingEndpoint` that has a `@ServiceActivator`-annotated handler method to receive messages whose payload is of type `String`, the `reservationName` from the `reservation-client`.  
- use the `String` to save new `Reservation`s using an injected `ReservationRepository`
- Observe that this is specified in the config server for us in the `reservation-client` module: `spring.cloud.stream.bindings.input=reservations`. `input` is arbitrary and refers to the (arbitrary) channel of the same name described and referenced from the `Sink.class` definition.


## Logging & distributed tracing with ELK and Zipkin

- run `./bin/zipkin.sh`
- add `org.springframework.cloud`:`spring-cloud-starter-zipkin` to both the `reservation-service` and the `reservation-client`
- configure a `@Bean` of type `AlwaysSampler` for both the `reservation-service` and `reservation-client`.
- observe that as messages flow in and out of the `reservation-client`, you can observe their correspondances and sequences in a waterfall graph in the ZipKin web UI at `http://$DOCKER_IP:8080` by drilling down to the service of choice. You can further drill down to see the headers and nature of the exchange between endpoints.
- run `./bin/elk.sh`
- add `net.logstash.logback`:`logstash-logback-encoder`:`4.2` to the `reservation-service` and `reservation-client`
- add `logback.xml` to each project's `resources` directory. it should be configured to point to the value of `$DOCKER_IP` or some DNS entry
- import `org.slf4j.Logger` and `org.slf4j.LoggerFactory`
- declare a logger: `Logger LOGGER = LoggerFactory.getLogger( DemoApplication.class);`
- in the `reservation-service`, use `LogstashMarker`s to emit interesting semantic logs to be collected by the Kibana UI at `http://$DOCKER_IP:...`

## Security
- add `org.springframework.cloud`:`spring-cloud-starter-oauth2` to the `reservation-client`.
- add `@EnableOAuthSso`
- observe that we've already pointed it to use GitHub for authentication in the config server's `application.properties`
- in the `reservation-client`, create a new REST endpoint called `/user/info` and use it to expose the authenticated principal to the authenticated client.
- confirm this works by launching a new browser in incognito mode and then hitting the protected resource
- switch to a qualified `loadBalancedOauth2RestTemplate` instead of any old `@RestTemplate`.
- **EXTRA CREDIT**: use Spring Security OAuth's Authroization Server instead of GitHub

## Optimize for Velocity and Consistency
- create a parent dependency that in turn defines all the Git Commit ID plugins, the executable jars, etc.
- package up common resources like `logstash.xml`
- create a new stereotypical and task-centric Maven `starter` dependency that in turn brings in commonly used dependencies like `org.springframework.cloud`:`spring-cloud-starter-zipkin`, `org.springframework.cloud`:`spring-cloud-starter-eureka`,
`org.springframework.cloud`:`spring-cloud-starter-config`, `org.springframework.cloud`:`spring-cloud-starter-stream-binder-rabbit`, `org.springframework.boot`:`spring-boot-starter-actuator`, `net.logstash.logback`:`logstash-logback-encoder`:`4.2`,
- extract all the repeated code into auto-configuration: the `AlwaysSampler` bean, `@EnableDiscoveryClient`, the custom `HealthIndicator`s.
- **EXTRA CREDIT**: define a Logger that is in turn a proxy that can only be injected using a custom qualfier (`@Logstash`)


## To The Cloud!
- remove Logstash (you could keep it, but setting up Logstash on AWS is an **EXTRA CREDIT** exercise; besides, Cloud Foundry provides the _loggregator_ which works just fine with tools like Splunk and negates the need for the ELK stack)
- remove Zipkin (you could keep it, but setting up Zipkin on AWS is an **EXTRA CREDIT** exercise)
- `cf login`, and then `cf target` the Pivotal Web Services endpoints
- enable the Spring Boot actuator `/shutdown` endpoint  in the Config Server `application.properties`
- describe each service using `manifest.yml`s
- run `./bin/cf.sh` to deploy the whole suite of services to Pivotal Web Services
- `cf scale -i 4 reservation-service` to scale that single service to 4 instances. Call the `/shutdown` actuator endpoint for `reservation-service`
- observe that `cf apps` records the downed, flagging service and eventually restores it
- observe that the configuration for the various cloud-specific backing services is handled in terms of various configuration files in the Config Server suffixed with `-cloud.properties`.
