# Cloud Native Java Workshop

The accompanying code for this workshop is [on Github](http://github.com/joshlong/cloud-native-workshop)

## Setup

> microservices, for better or for worse, involve a lot of moving parts. Let's make sure we can run all those things in this lab.

- In this workshop you'll need the latest Java version. Java 8 is the baseline for this workshop.
- You'll need a newer, 3.1, version of Apache Maven installed.
- You'll need an IDE installed. Something like Apache NetBeans, Eclipse, or IntelliJ IDEA.
- You might want to use the [the Spring Boot CLI](http://docs.spring.io/autorepo/docs/spring-boot/current/reference/html/getting-started-installing-spring-boot.html#getting-started-installing-the-cli) and [the Spring Cloud CLI](https://github.com/spring-cloud/spring-cloud-cli). Neither is required but you could use them to replace a lot of code, later.
- [Install the Cloud Foundry CLI](https://docs.cloudfoundry.org/devguide/installcf/install-go-cli.html)
- Go to the [Spring Initializr](http://start.spring.io) and use the latest stable version of Spring Boot. If you are doing this in a workshop setting where internet connectivity is constrained, you'll want to pre-cache the Maven dependencies before starting. Go to the Spring Initializr and   choose EVERY checkbox except those related to AWS, Zookeeper, or Consul, then click _Generate_. In the shell, run `mvn -DskipTests=true clean install` to force the resolution of all those dependencies so you're not stalled later. Then, run `mvn clean install` to force the resolution of the test scoped dependencies. You may discard this project after you've run the commands. This will download whatever artifacts are most current to your local Maven repository (usually, `.m2/repository`).
- _For multi-day workshops only_: Run each of the `.sh` scripts in the `./bin` directory; run `psql.sh` after you've run `postgresh.sh` and confirm that they all complete and emit no obvious errors


## 1. "Bootcamp"

> In this lab we'll take a look at building a basic Spring Boot application that uses JPA and Spring Data REST. We'll look at how to start a new project, how Spring Boot exposes functionality, and how testing works.

- Go to the [Spring Initializr](http://start.spring.io) and select `Web`, `JPA`, `H2`, `Actuator`, `Config Client`, `Eureka Discovery`, `Lombok`, `Zipkin Client`, `Stream Rabbit`, `Cloud Contract Verifier` and `Integration`. Specify an `artifactId` of `reservation-service`.
- Click `Generate` and then unzip the archive. Change into the directory of the unzipped project and then run `mvn clean install`.
- Open the project in your favorite IDE using Maven import.
- Open `pom.xml` and comment out dependencies that we don't need, for the moment, including: `org.springframework.cloud`:`spring-cloud-starter-stream-rabbit`, `org.springframework.cloud`:`spring-cloud-starter-config`, `org.springframework.cloud`:`spring-cloud-starter-zipkin`, and `org.springframework.cloud`:`spring-cloud-starter-eureka`.
- Add a simple entity (`Reservation`) with an `id` field and a `reservationName` field. Use Lombok to synthesize getters/setters, all-argument and no-argument constructors.
- create a new JPA repository (`ReservationRepository`)
- Observe that we have a Maven wrapper (`./mvnw`) in the build to support reproducible builds

### Questions:
- What is Spring? Spring, fundamentally, is a dependency injection container. This detail is unimportant. What is important is that once Spring is aware of all the objects - _beans_ - in an application, it can provide services to them to support different use cases like persistence, web services, web applications, messaging and integration, etc.
- Why `.jar`s and not `.war`s? We've found that many organizations deploy only one, not many, application to one Tomcat/Jetty/whatever. They need to configure things like SSL, or GZIP compression, so they end up doing that in the container itself and - because they don't want the versioned configuration for the server to drift out of sync with the code, they end up version controlling the application server artifacts as well as the application itself! This implies a needless barrier between dev and ops which we struggle in every other place to remove.
- How do I access the `by-name` search endpoint? Follow the links! visit `http://localhost:8080/reservations` and scroll down and you'll see _links_ that connect you to related resources. You'll see one for `search`. Follow it, find the relevant finder method, and then follow its link.



## 2. Making a Spring Boot application Production Ready

(_Multi-day workshops only_)

> Code complete != production ready! If you've ever read Michael Nygard's amazing tome, _Release It!_, then you know that the last mile between being code complete and being to production is _much_ longer than anyone ever anticipates. In this lab, we'll look at how Spring Boot is optimized for the continuous delivery of applications into production.

- Add `org.springframework.boot`:`spring-boot-starter-actuator`
- customize the `HealthEndpoint` by contributing a custom `HealthIndicator`
- Start `./bin/graphite.sh`
- Configure two environment variables `GRAPHITE_HOST` (`export GRAPHITE_HOST="$DOCKER_IP"`) and `GRAPHITE_PORT` (`2003`) (you may need to restart your IDE to _see_ these new environment variables)
- Add a `GraphiteReporter` bean
- Add `io.dropwizard.metrics`:`metrics-graphite`
- Build an executable `.jar` (UNIX-specific) using the `<executable/>` configuration flag
- Add the HAL browser - `org.springframework.data`:`spring-data-rest-hal-browser` and view the Actuator endpoints using that
- Configure Maven resource filtering and the Git commit ID plugin in the `pom.xml` in all existing and subsequent `pom.xml`s, or extract out a common parent `pom.xml` that all modules may extend.
- Add `info.build.artifact=@project.artifactId@` and `info.build.version=@project.version@`  to `application.properties`.
- Introduce a new `@RepositoryEventHandler` and `@Component`. Provide handlers for `@HandleAfterCreate`, `@HandleAfterSave`, and `@HandleAfterDelete`. Extract common counters to a shared method
- Add a semantic metric using `CounterService` and observe the histogram in Graphite



##  3. The Config Server

> The [12 Factor](http://12factor.net/config) manifesto talks about externalizing that which changes from one environment to another - hosts,  locators, passwords, etc. - from the application itself. Spring Boot readily supports this pattern, but it's not enough. In this lab, we'll look at how to centralize, externalize, and dynamically update application configuration with the Spring Cloud Config Server.

- Go to the Spring Initializr, choose the latest milestone of Spring Boot 1.3.x, specify an `artifactId` of `config-service` and add `Config Server` from the list of dependencies.
- You should `git clone` the [Git repository for this workshop - https://github.com/joshlong/bootiful-microservices-config](`https://github.com/joshlong/bootiful-microservices-config.git`)
- In the Config Server's `application.properties`, specify that it should run on port 8888 (`server.port=8888`) and that it should manage the Git repository of configuration that lives in the root directory of the `git clone`'d  configuration. (`spring.cloud.config.server.git.uri=...`).
- Add `@EnableConfigServer` to the `config-service`'s root application
- Add `server.port=8888` to the `application.properties` to ensure that the Config Server is running on the right port for service to find it.
- Add the Spring Cloud BOM (you can copy it from the Config Server) to the `reservation-service`.

We will need to modify the `reservation-service`'s `pom.xml` in order to make it a config client. To do this, add the following to your `pom.xml` of the `reservation-service` from step #1.

Example:

    <dependencyManagement>
        <dependencies>
          ...
          <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-parent</artifactId>
            <version>Brixton.RELEASE</version>
            <type>pom</type>
            <scope>import</scope>
          </dependency>
        </dependencies>
    </dependencyManagement>

**IMPORTANT**: Make sure that you make this modification in the `</dependencyManagement>` block. There are two places where Maven dependencies are added in a `pom.xml` and some people tend to get confused at this step. If you need help, please raise your hand and flag down an instructor.

Next, add `org.springframework.cloud`:`spring-cloud-starter-config` to the `reservation-service`. We'll add this dependency declaration to the general dependencies tag, unlike the previous modification to `</dependencyManagement>`.

Example:

    <dependencies>
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-config</artifactId>
      </dependency>
    ..
    </dependencies>

Next:

Create a `boostrap.properties` that lives in the same place as `application.properties` and discard the `application.properties` file. Now we need only to tell the Spring application where to find the Config Server, with the property `spring.cloud.config.uri=@config.server:http://localhost:8888@`, and how to identify itself to the Config Server and other services, later, with `spring.application.name`.

Now, run the Config Server:

> We'll copy and paste  `bootstrap.properties` for each subsequent module, changing only the `spring.application.name` as appropriate.

In the `reservation-service`, create a `MessageRestController` and annotate it with `@RefreshScope`. Inject the `${message}` key and expose it as a REST endpoint, `/message`.

Trigger a refresh of the message using the `/refresh` endpoint.

**EXTRA CREDIT**: Install RabbitMQ server and connect the microservice to the the Spring Cloud Stream-based event bus and then triggering the refresh using the `/bus/refresh`.

## 4. Service Registration and Discovery

> In the cloud, services are often ephemeral and it's important to be able to talk to these services abstractly, without worrying about the host and ports for these services. At first blush, this seems like a use-case for DNS, but DNS fails in several key situations. How do we know if there's a service waiting on the other end of a DNS-mapped service that can respond? How do we support more sophisticated load-balancing than DNS + a typical loadbalancer can handle (e.g.: round-robin)? How do we avoid the extra hop outside of most cloud environments required to resolve DNS? For all of these and more, we want the effect of DNS - a dispatch table - without being coupled to DNS. We'll use a service registry and Spring Cloud's `DiscoveryClient` abstraction.

- Go to the Spring Initializr, select the `Eureka Server` (this brings in `org.springframework.cloud`:`spring-cloud-starter-eureka-server`) checkbox, name it `eureka-service` and then add `@EnableEurekaServer` to the `DemoApplication` class.
- Make sure this module _also_ talks to the Config Server as described in the last lab by adding the `org.springframework.cloud`:`spring-cloud-starter-config`.
- add `org.springframework.cloud`:`spring-cloud-starter-eureka` to the `reservation-service`
- Add `@EnableDiscoveryClient` to the `reservation-service`'s `DemoApplication` and restart the process, and then confirm its appearance in the Eureka Server at `http://localhost:8761`

> you have a service registry and now you have a single service registered and advertising its presence. Let's take advantage of that in an edge service, which we'll call `reservation-client`.

- Use the Spring Initializr, setup a new module, `reservation-client`, with the `Web`, `Lombok`, `Feign`, `Zuul`, `Hystrix`, `Stream Rabbit`, `Eureka Discovery`, `Config Client`, `Cloud OAuth2`, and `Zipkin Client` dependencies.
- open `pom.xml` and comment out the dependencies that are neither needed nor workable without some extra configuration (which we'll get to later): `org.springframework.cloud`:`spring-cloud-starter-oauth2`, `org.springframework.cloud`:`spring-cloud-starter-stream-rabbit`, and  `org.springframework.cloud`:`spring-cloud-starter-zipkin`.
- Create a `bootstrap.properties`, just as with the other modules, but name this one `reservation-client`.
- Create a `CommandLineRunner` that uses the `DiscoveryClient` to look up other services programmatically




## 5. Edge Services: API gateways (circuit breakers, client-side load balancing)
> Edge services sit as intermediaries between the clients (smart phones, HTML5 applications, etc) and the service. An edge service is a logical place to insert any client-specific requirements (security, API translation, protocol translation) and keep the mid-tier services free of this burdensome logic (as well as free from associated redeploys!)

> Proxy requests from an edge-service to mid-tier services with a _microproxy_. For some classes of clients, a microproxy and security (HTTPS, authentication) might be enough.

- Add `org.springframework.cloud`:`spring-cloud-starter-zuul` and `@EnableZuulProxy` to the `reservation-client`, then run it.
- Launch a browser and visit the `reservation-client` at `http://localhost:9999/reservation-service/reservations`. This is proxying your request to `http://localhost:8000/reservations`.

> API gateways are used whenever a client - like a mobile phone or HTML5 client - requires API translation. Perhaps the client requires coarser grained payloads, or transformed views on the data

- In the `reservation-client`, create a client side DTO - named `Reservation`, perhaps? - to hold the `Reservation` data from the service. Do this to avoid being coupled between client and service
- Add `org.springframework.boot`:`spring-boot-starter-hateoas`
- Add a REST service called `ReservationApiGatewayRestController` that uses the `@Autowired @LoadBalanced RestTemplate rt` to make a load-balanced call to a service in the registry using Ribbon.
- Map the controller itself to `/reservations` and then create a new controller handler method, `getReservationNames`, that's mapped to `/names`.
- In the `getReservationNames` handler, make a call to `http://reservation-service/reservations` using the `RestTemplate#exchange` method, specifying the return value with a `ParameterizedTypeReference<Resources<Reservation>>>` as the final argument to the `RestTemplate#exchange` method.
- Take the results of the call and map them from `Reservation` to `Reservation#getReservationName`. Then, confirm that `http://localhost:9999/reservations/names` returns the names.

> The code works, but it assumes that the `reservation-service` will always be up and responding to requests. We need to be a bit more defensive in any code that clients will connect to. We'll use a circuit-breaker to ensure that the `reservation-client` does something useful as a _fallback_ when it can't connect to the `reservation-service`.

- Add `org.springframework.boot`:`spring-boot-starter-actuator` and `org.springframework.cloud`:`spring-cloud-starter-hystrix` to the `reservation-client`
- Add `@EnableCircuitBreaker` to our `DemoApplication` configuration class
- Add `@HystrixCommand` around any potentially shaky service-to-service calls, like `getReservationNames`, specifying a fallback method that returns an empty collection.
- Test that everything works by killing the `reservation-service` and revisiting the `/reservations/names` endpoint
- Go to the [Spring Initializr](http://start.spring.io) and stand up a new service - with an `artifactId` of `hystrix-dashboard` - that uses Eureka Discovery, Config Client, and the Hystrix Dashboard.
- Identify it is as `hystrix-dashboard` in `bootstrap.properties` and point it to config server.
- Annotate it with `@EnableHystrixDashboard` and run it. You should be able to load it at `http://localhost:8010/hystrix.html`. It will expect a heartbeat stream from any of the services with a circuit breaker in them. Give it the address from the `reservation-client`: `http://localhost:9999/hystrix.stream`

## 6. To the Cloud!

> Spring Cloud helps you develop services that are resilient to failure - they're _fault tolerant_. If a service goes down, they'll degrade gracefully, and correctly expand to accommodate the available capacity. But who starts and stops these services? You need a platform for that. In this lab, we'll use a free trial account at Pivotal Web Services to demonstrate how to deploy, scale and heal our services.

- Do a `mvn clean install` to get binaries for each of the modules
- _Optionally_: Comment out the `GraphiteReporter` bean in `reservation-service`
- Remove the `<executable/>` attribute from your Maven build plugin
- Make sure that each Maven build defines a `<finalName>` element as: `<finalName>${project.artifactId}</finalName>` so that you can consistently refer to the built artifact from each Cloud Foundry manifest.
- Sign up for a free trial [account at Pivotal Web Services](http://run.pivotal.io/).
- Change the various `bootstrap.properties` files to load configuration from an environment property, `vcap.services.config-service.credentials.uri`, _or_, if that's not available, `http://localhost:8888`: `spring.cloud.config.uri=${vcap.services.config-service.credentials.uri:http://localhost:8888}`
- `cf login` the Pivotal Web Services endpoint, `api.run.pivotal.io` and then enter your credentials for the free trial account you've signed up for.
- Enable the Spring Boot actuator `/shutdown` endpoint  in the Config Server `application.properties`: `endpoints.shutdown.enabled=true`
- Describe each service - its RAM, DNS `route`, and required services - using a `manifest.yml` file collocated with each binary

> You may generate the `manifest.yml` manually or you may use a tool like Spring Tool Suite's Spring Boot Dashboard which will, on deploy, prompt you to save the deployment configuration as a `manifest.yml`.

_Multi-day workshop_:

- run `cf.sh` in the `labs/6` folder to deploy the whole suite of services to [Pivotal Web Services](http://run.pivotal.io), **OR**:
- follow the steps in `cf-simple.sh`. This will `cf push` the `eureka-service` and `config-service`. It will use `cf cups` to create services that are available to `reservation-service` and `reservation-client` as environment variables, just like any other standard service. Then, it will `cf push` `reservation-client` and `reservation-service`, binding to those services. See `cf-simple.sh` for details and comments - you should be able to follow along on Windows as well.

> As you push new instances, you'll get new routes because of the configuration in the `manifest.yml` which specifies host is "...-${random-word}". When creating the user-provided-services (`cf cups ..`) be sure to choose only the first route. To delete orphaned routes, use `cf delete-orphaned-routes`

> if you're running the `cf cups` commands, remember to quote and escape correctly, e.g.: `cf cups "{ \"uri":\"..\" }"`

- `cf scale -i 4 reservation-service` to scale that single service to 4 instances. Call the `/shutdown` actuator endpoint for `reservation-client`: `curl -d{} http://_RESERVATION_CLIENT_ROUTE_/shutdown`, replacing `_RESERVATION_CLIENT_ROUTE_`.
- observe that `cf apps` records the downed, _flapping_ service and eventually restores it.
- observe that the configuration for the various cloud-specific backing services is handled in terms of various configuration files in the Config Server suffixed with `-cloud.properties`.

> if you need to delete an application, you can use `cf d _APP_NAME_`, where `_APP_NAME_` is your application's logical name. If you want to delete a service, use `cf ds _SERVICE_NAME_` where `_SERVICE_NAME_` is a logical name for the service. Use `-f` to force the deletion without confirmation.

## 7. Streams
> while REST is an easy, powerful approach to building services, it doesn't provide much in the way of guarantees about state. A failed write needs to be retried, requiring more work of the client. Messaging, on the other hand, guarantees that _eventually_ the intended write will be processed. Eventual consistency works most of the time; even banks don't use distributed transactions! In this lab, we'll look at Spring Cloud Stream which builds atop Spring Integration and the messaging subsystem from Spring XD. Spring Cloud Stream provides the notion of _binders_ that automatically wire up message egress and ingress given a valid connection factory and an agreed upon destination (e.g.: `reservations` or `orders`).

- start `./bin/rabbitmq.sh`.
> This will install a RabbitMQ instance that is available at `$DOCKER_IP`. You'll also be able to access the console, which is available `http://$DOCKER_IP:15672`. The username and password to access the console are `guest`/`guest`.

- add `org.springframework.cloud`:`spring-cloud-starter-stream-rabbit` to both the `reservation-client` and `reservation-service`.

> Sources - like water from a faucet - describe where messages may come from. In our example, messages come from the `reservation-client` that wishes to write messages to the `reservation-service` from the API gateway.

- add `@EnableBinding(Source.class)` to the `reservation-client` `DemoApplication`
- create a new REST endpoint - a `POST` endpoint that accepts a `@RequestBody Reservation reservation` - in the `ReservationApiGatewayRestController` to accept new reservations
- observe that the `Source.class` describes one or more Spring `MessageChannel`s which are themselves annotated with useful qualifiers like `@Output("output")`.
- in the new endpoint, inject the Spring `MessageChannel` and qualify it with `@Output("output")` - the same one as in the `Source.class` definition.
- use the `MessageChannel` to send a message to the `reservation-service`. Connect the two modules through a agreed upon name, which we'll call `reservations`.
- Observe that this is specified in the config server for us in the `reservation-service` module: `spring.cloud.stream.bindings.output=reservations`. `output` is arbitrary and refers to the (arbitrary) channel of the same name described and referenced from the `Source.class` definition.

> Sinks receive messages that flow _to_ this service (like the kitchen sink into which water from the faucet flows).

- add `@EnableBinding(Sink.class)` to the `reservation-service`  `DemoApplication`
- observe that the `Sink.class` describes one or more Spring `MessageChannel`s which are themselves annotated with useful qualifiers like `@Input("input")`.
- create a new `@MessagingEndpoint` that has a `@ServiceActivator`-annotated handler method to receive messages whose payload is of type `String`, the `reservationName` from the `reservation-client`.
- use the `String` to save new `Reservation`s using an injected `ReservationRepository`
- Observe that this is specified in the config server for us in the `reservation-client` module: `spring.cloud.stream.bindings.input=reservations`. `input` is arbitrary and refers to the (arbitrary) channel of the same name described in the `Sink.class` definition.



## 8. Distributed Tracing with Zipkin

> Distributed tracing lets us trace the path of a request from one service to another. It's very useful in understanding where a failure is occurring in a complex chain of calls.

- Go to the [Spring Initializr](http://start.spring.io) and select Zipkin UI, Zipkin Server, Eureka Discovery and Config Client, then Generate a new project. Open it in your IDE.
- point the project to the config service and give it a `spring.application.name` of `zipkin-service`, as discussed earlier. Add `@EnableDiscoveryClient` to have it participate in service registration and discovery.
- Add `@EnableZipkinServer` to the `zipkin-service` main class.
- start the `zipkin-service`

> Now, let's connect our services to the Zipkin service.

- add `org.springframework.cloud`:`spring-cloud-starter-zipkin` to both the `reservation-service` and the `reservation-client`
- observe that as messages flow in and out of the `reservation-client`, you can observe their correspondences and sequences in a waterfall graph in the ZipKin web UI at `http://localhost:9411` by drilling down to the service of choice. You can further drill down to see the headers and nature of the exchange between endpoints. The `Dependencies` view in Zipkin shows you the topology of the cluster.


## 9. Consumer Driven Contract Testing

> we've built a trivial API with an even more trivial client (thanks to the `RestTemplate` or `Feign`). We've done a good job on day one of our journey. What happens on day two or at any point down the line after the API has changed but the client that uses it has updated accordingly? What happens when the producer of the API changes the API? Does this break the client? It's important that we capture such breaking changes as early and often as possible. In a monolithic application the incompatible updates to the producer of an API would be caught on the first compile. Refactoring would help us prevent these problems, as well. In a distributed systems world, these incompatible changes are harder to catch. They get caught in the integration tests. integration tests are among the slowest of the tests you should have in your system. They're towards the top of the testing pyramid because they're _expensive_ - both in terms of time and computational resources. In order to run the tests we'd need to run both client and service and all supporting infrastructure. This is a worst-case scenario; organizations move to microservices to accelerate feedback (which in turn yields learning and improvement), _not_ to reduce it! What we need is some way to capture breaking changes that keeps both producer and consumer in sync _and_ that doesn't constrain velocity of feedback. Spring Cloud Contract, and consumer driven contracts and consumer driven contract testing, make this work easier. The idea is that contract definitions are used to capture the expected behavior of an API for a particular client. This may include all the quirks of particular clients, and it may incluhde older clients using older APIs. A producer may capture as many contract scenarios as needed. These contracts are enforced bilaterally. On the producer side, the Spring Cloud Contract verifier turns the contract into a Spring MVC Test Framework test that fails if the actual API doesn't work as the contract stipulates. On the consumer, clients can run test against actual HTTP (or messaging-based) APIs that are themselves stubs. These stubs are _stubs_ - that is, there's no real business logic behind them. Just preconfigured responses defined by the contracts. As the stub is defined entirely by the contract, it is trivially cheap to run the stub APIs and exercise clients against them. As the stubs are only ever available _if_ the producer passes all its tests, this ensures that the client is building and testing against a reflection of the latest and actual API, _not_ the understanding of the API implied when the client test was originally written.

- first we'll define a contract for our producer. create a `src/test/resources/contracts` directory in the `reservation-service`.
- then, define a contract to capture a scenario, `src/test/resources/contracts/shouldReturnAllReservations.groovy`. In our service, the scenario is that we want to view the collection of `Reservation` records when we hit the `/reservations` endpoint with an HTTP `GET` call.
-  the contract should also define what a valid response looks like. It's fine to put some dummy data in the response. remember, the contract will be translated into a unit test. One that is executed against the Spring MVC API, and that tests that the API does what the contract expects it to do. The generated tests will need some sort of initialization. We'll specify which base class the tests should extend and in that base class we can stub out the mock data.   
-  a contract is no good if there is no enforcement. Configure the Spring Cloud Contract Maven Verifier plugin on the producer API build. You'll need to specify what base class (or classes) should be used to setup the tests, too. This plugin is what translates our contract into a test on compilation. If the test generated by the contract doesn't pass, then the build will fail. Our contract will prevent us from shipping an API that violates the assumptions of the contract!

   ```

   <plugin>
       <groupId>org.springframework.cloud</groupId>
       <artifactId>spring-cloud-contract-maven-plugin</artifactId>
       <version>1.1.1.RELEASE</version>
       <extensions>true</extensions>
       <configuration>
           <baseClassForTests>
               com.example.reservationservice.BaseClass
           </baseClassForTests>
       </configuration>
   </plugin>

   ```  
  -  `com.example.reservationservice.BaseClass` should setup the usual Spring Boot test machinery, specify the `SpringRunner` for the JUnit runner, and using the `@SpringBootTest` annotation to configure which application context class we want to work with. Mock out the repository that returns our results  with `@MockBean` and mockito.  
  - Now when you run `mvn clean install` the contract will ensure that your API hasn't changed in an unexpected way.
  - When a build succeeds, with `mvn clean install`, the build contributes a `.pom`, `.jar` and, thanks to the Maven plugin we just configured, an artifact ending in `-stub.jar`. This last artifact contains the definition from our contract that we care about. It is this stub that we'll use with our client.
  - in the client code, create a new test and test the Feign interface by injecting it and asserting that the client returns data that we've stubbed out in the contract.
  - configure the client test with `@AutoconfgureStubRunner(..)`, pointing the client test to the Maven coordinates for the contract.
  - As configured, the client test will spin up a WireMock-based API that's pre-programmed to respond according to the contract. In this case it'll return the two names we specifed in the contract definition when somebody visits `/reservations`. It'll run for the life of the client test, and no longer. It's an actual HTTP API, against which we can make client-side invocations. It does not, however, need all the supporting infrastructure to work. This makes it markedly cheaper, computationally and clock-time wise, to run. 
  - the only fly in the ointment is that, so far, we *still* need the service registry, Eureka. Let's stub that out, as well. In the `reservation-client/src/test/resources` directory, create an `application.properties` property file. In the property file, disable Eureka (`eureka.client.enabled=false`).
  - We'll also need to stub out the `DiscoveryClient` that our code depends on. This is easy enough using `stubrunner.ids-to-service-ids.reservation-service=reservation-service`. Thusly configured, we map the service (as it's registered in the registry) to the `-stub.jar` artifact ID.  
  - if we run the tests on the client, everything should be green, and quick.

> Spring Cloud Contract supports clients and services written with Spring in mind but can they help us when developing clients in other languages?

 - use the `stub-runner-boot.jar` (which you can download on the project website) to specify the stub ids (the `groupId` and `artifactId`) and whether to consult the local `.m2/repository` and then have it stand up an API that your browser, mobile, or other clients can talk to, even if they're not using Spring and Java.


## 10. Security

> in a distributed systems world, multiple clients might access multiple services and it becomes very important to have an easy-to-scale answer to the question: which clients may access which resources? The solution for this problem is single signon: all requests to a given resource present a token that may be redeemed with a centralized authentication service. We'll build an OAuth 2-powered authorization service and that secure our edge service to talk to it.

- add `org.springframework.cloud`:`spring-cloud-starter-oauth2` to the `reservation-client`.
- add `@EnableResourceServer` to the `reservation-client`'s main class.

> your application will need to talk to an authentication service that understands OAuth. You could use any of a number of valid services, like Github, Facebook, Google, or even an API Gateway product like Apigee. In our case, we'll connect to a custom Spring Security OAuth-powered `auth-service`.

- Go to the [Spring Initializr](http://start.spring.io) and select H2, the Config Client, Eureka Discovery, Web support, and name the project `auth-service`.
- in the resulting Maven `pom.xml`, add `org.springframework.cloud`:`spring-cloud-starter-oauth2`
- make sure that you redefine the Maven property `spring-security.version` so that Spring Security is _at least_ `4.1.0.RELEASE`
- add `@EnableResourceServer` to the main class for the application
- create a new REST controller, `PrincipalRestController`, that - when asked - returns the current authenticated `javax.security.Principal`.
- create an implementation of the `org.springframework.security.core.userdetails.UserDetailsService` contract. A simple implementation might defer to a table of records in a database. In this example we use a simple `Account` JPA entity to map this information from a SQL database. We use a simple Spring Data JPA _repository_ implementation to read JPA records from the database.
- Finally, we need to provide an implementation of `AuthorizationServerConfigurerAdapter` and override two of the `configure(..)` methods.
- the first override, `AuthorizationServerConfigurerAdapter#configure(AuthorizationServerEndpointsConfigurer)`, should provide an injected `AuthenticationManager` to the `AuthorizationServerEndpointsConfigurer#authenticationManager(AuthenticationManager)` method.
- the second override, `AuthorizationServerConfigurerAdapter#configure(ClientDetailsServiceConfigurer)`, should define OAuth clients. In OAuth, identity is composed of some notion of a user, `bob`, for example, _and_ a client (`bob`'s HTML5 client, `bob`'s iPhone, `bob`'s Android tablet, etc). Different clients can make differnet guarantees about the amount of security they can support. Our example will define a simple client, `acme`, with a secret password, `acmesecret`, three authorized grant types (`authorization_code`, `refresh_token`, `password`) and a single scope (`openid`)
- You should be able to generate a new token using
    ```
    curl -X POST -vu acme:acmesecret http://localhost:9191/uaa/oauth/token -H "Accept: application/json" -d "password=spring&username=jlong&grant_type=password&scope=openid&client_secret=acmesecret&client_id=acme"
    ```
    Then, send the access token to an OAuth2 secured REST resource using:

    ```
    curl http://localhost:9999/reservations/names -H "Authorization: Bearer _INSERT TOKEN_"
    ```

##  11. Optimize for Velocity and Consistency

> Thus far we've looked at building applications with Spring Boot and Spring Cloud, layering in the various technologies as we've learned about them and needed them. It's been a fun process of discovery (hopefully!), but this shouldn't be required for _every_ developer. Instead, you should package up best-practices as Spring Boot starter dependencies and auto-configurations. Codifying these best practices helps get past the endless list of non-functional requirements required to go to production.

- create a parent dependency that in turn defines all the Git Commit ID plugins, the executable jars, etc.
- package up common resources like `logstash.xml`
- create a new stereotypical and task-centric Maven `starter` dependency that in turn brings in commonly used dependencies like `org.springframework.cloud`:`spring-cloud-starter-zipkin`, `org.springframework.cloud`:`spring-cloud-starter-eureka`,
`org.springframework.cloud`:`spring-cloud-starter-config`, `org.springframework.cloud`:`spring-cloud-starter-stream-binder-rabbit`, `org.springframework.boot`:`spring-boot-starter-actuator`, `net.logstash.logback`:`logstash-logback-encoder`:`4.2`,
- extract all the repeated code into auto-configuration: the `AlwaysSampler` bean, `@EnableDiscoveryClient`, the custom `HealthIndicator`s.
- **EXTRA CREDIT**: define a Logger that is in turn a bean defined using Spring Framework's support for `InjectionPoint`s. You can qualify this bean with a custom qualifier (`@Logger`).
- **EXTRA CREDIT**: customize the Spring Initializr. The Spring Initializr is itself an open-source project. You can find the code for [the Spring Initializr on Github](https://github.com/spring-io/initializr). It is itself an auto-configuration. build and install the Spring Initializr and then create a new Spring Boot application. Add the Initializr dependency to your new Spring Boot application and then configure which checkboxes are shown by overriding the configuration in `application.properties` or, more likely, `appication.yml`. Now you have your own Spring Initializr, with your own checkboxes and auto-configurations. Host this on Cloud Foundry (or anywhere, really) and point people in your organization to it for all their new-project needs.

## 12. Log Aggregation and Analysis with ELK

> For all the fancy new technologies we have today, the venerable log file still reigns supreme. Modern logs, are more than strings blurted out by code in the dead of night. They _should_ be structured data. In this exercise we'll write our logs using Logstash and then publish them to an ElasticSearch cluster.

- run `./bin/elk.sh`
- add `net.logstash.logback`:`logstash-logback-encoder`:`4.2` to the `reservation-service` and `reservation-client`
- add `logback.xml` to each project's `resources` directory. it should be configured to point to the value of `$DOCKER_HOST` or some DNS entry
- import `org.slf4j.Logger` and `org.slf4j.LoggerFactory`
- declare a logger: `Logger LOGGER = LoggerFactory.getLogger( DemoApplication.class);`
- in the `reservation-service`, use `LogstashMarker`s to emit interesting semantic logs to be collected by the Kibana UI at `http://$DOCKER_HOST:...`

## 13. Cloud Native Data Processing with Spring Cloud Data Flow
