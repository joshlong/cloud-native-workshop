# Cloud Native Java Workshop - 1 Day

The accompanying code for this workshop is [on Github](http://github.com/joshlong/cloud-native-workshop)

## Setup  

> microservices, for better or for worse, involve a lot of moving parts. Let's make sure we can run all those things in this lab.

- you will need JDK 8, Git, Maven, an IDE and [Docker Toolbox](https://www.docker.com/docker-toolbox) in order to follow along.
- Specify important environment variables before opening any IDEs. Set `JAVA_HOME` to where the JDK is. If you're on OSX you can have it set with:
  ```
  export JAVA_HOME=`/usr/libexec/java_home`
  ```
- create an environment variable called `DOCKER_HOST` that points to your Docker Toolbox machine. On OSX and Windows this will resolve to a virtual machine running in VirtualBox. If you're on OSX you can have it set with:
  ```
  export DOCKER_HOME=`docker-machine ip default`
  ```

- [Install the Cloud Foundry CLI](https://docs.cloudfoundry.org/devguide/installcf/install-go-cli.html)
- go to the [Spring Initializr](http://start.spring.io) and specify the latest stable (non-`SNAPSHOT`) version of Spring Boot, click the link _Switch to the full version_, and then choose EVERY checkbox, then click _Generate Project_.
- In the shell, run `mvn -DskipTests=true clean install` to force the resolution of all those dependencies so you're not stalled later. Then, run `mvn clean install` to force the resolution of the test scoped dependencies. You may discard this project after you've `install`ed everything.
- run each of the `.sh` scripts in the `./bin` directory of the cloned Git repository for this workshop. Run `bin/psql.sh` after you've run `bin/postgresh.sh` and confirm that they all complete and emit no obvious errors
- **VERY IMPORTANT** - go to [Pivotal Web Services](http://run.pivotal.io) and sign up for a Free Trial! You'll need an account to see this through to the end! No credit card is required, though you'll need a valid phone number!

<!-- TODO which things should they run!  -->

## 1. "Bootcamp"

> in this lab we'll take a look at building a basic Spring Boot application that uses JPA and Spring Data REST. We'll look at how to start a new project, how Spring Boot exposes functionality, and how testing works.

- go to the [Spring Initializr](http://start.spring.io) and select the H2, Actuator, REST Repositories, JPA, and Web checkboxes. Select the latest Spring Boot 1.3 version. give it an `artifactId` of `reservation-service`.
- Run `mvn clean install` and import it into your favorite IDE using Maven import.
- add a simple JPA entity (`Reservation`) and a Spring Data repository (`ReservationRepository`)
- map the repository to the web by adding  `org.springframework.boot`:`spring-boot-starter-data-rest` and then annotating the repository with `@RepositoryRestResource`
- add custom Hypermedia links
- write a simple unit test

### Questions:
- what is Spring? Spring, fundamentally, is a dependency injection container. This detail is unimportant. What is important is that once Spring is aware of all the objects - _beans_ - in an application, it can provide services to them to support different use cases like persistence, web services, web applications, messaging and integration, etc.
- why `.jar`s and not `.war`s? We've found that many organizations deploy only one, not many, application to one Tomcat/Jetty/whatever. They need to configure things like SSL, or GZIP compression, so they end up doing that in the container itself and - because they don't want the versioned configuration for the server to drift out of sync with the code, they end up version controlling the application server artifacts as well as the application itself! This implies a needless barrier between dev and ops which we struggle in every other place to remove.   
- how do I access the `by-name` search endpoint? Follow the links! visit `http://localhost:8080/reservations` and scroll down and you'll see _link_s that connect you to related resources. You'll see one for `search`. Follow it, find the relevant finder method, and then follow its link.



## 2. Making a Spring Boot application Production Ready

> Code complete != production ready! If you've ever read Michael Nygard's amazing tome, _Release It!_, then you know that the last mile between being code complete and being to production is _much_ longer than anyone ever anticipates. In this lab, we'll look at how Spring Boot is optimized for the continuous delivery of applications into production.

- add `org.springframework.boot`:`spring-boot-starter-actuator`
- customize the `HealthEndpoint` by contributing a custom `HealthIndicator`
- start `./bin/graphite.sh`  
- configure two environment variables `GRAPHITE_HOST` (`export GRAPHITE_HOST="$DOCKER_IP"`) and `GRAPHITE_PORT` (`2003`) (you may need to restart your IDE to _see_ these new environment variables)
- add a `GraphiteReporter` bean
- add `io.dropwizard.metrics`:`metrics-graphite`
- build an executable `.jar` (UNIX-specific) using the `<executable/>` configuration flag
- add the HAL browser - `org.springframework.data`:`spring-data-rest-hal-browser` and view the Actuator endpoints using that
- configure Maven resource filtering and the Git commit ID plugin in the `pom.xml` in all existing and subsequent `pom.xml`s, or extract out a common parent `pom.xml` that all modules may extend. Make sure to `git init`, `git add . `, and `git commit -a -m yolo` the directory in which the module lives
- add `info.build.artifact=${project.artifactId}` and `info.build.version=${project.version}`  to `application.properties`.
- introduce a new `@RepositoryEventHandler` and `@Component`. Provide handlers for `@HandleAfterCreate`, `@HandleAfterSave`, and `@HandleAfterDelete`. Extract common counters to a shared method
- add a semantic metric using `CounterService` and observe the histogram in Graphite



##  3. the Config Server

> the [12 Factor](http://12factor.net/config) manifesto speaks about externalizing that which changes from one environment to another - hosts,  locators, passwords, etc. - from the application itself. Spring Boot readily supports this pattern, but it's not enough. In this lab, we'll loko at how to centralize, externalize, and dynamically update application configuration with the Spring Cloud Config Server.

- go to the Spring Initializr, choose the latest milestone of Spring Boot 1.3, specify an `artifactId` of `config-service` and add `Config Server` from the list of dependencies.
- you should `git clone` the [Git repository for this workshop - https://github.com/joshlong/bootiful-microservices-config](`https://github.com/joshlong/bootiful-microservices-config.git`)
- In the Config Server's `application.properties`, specify that it should run on port 8888 (`server.port=8888`) and that it should manage the Git repository of configuration that lives in the root directory of the `git clone`'d  configuration. (`spring.cloud.config.server.git.uri=...`).
- add `@EnableConfigServer` to the `config-service` `DemoApplication`
- Add `server.port=8888` to the `application.properties` to ensure that the Config Server is running on the right port for service to find it.
- add the Spring Cloud BOM (you can copy it from the Config Server) to the `reservation-service`.

Add this to your `pom.xml` of the `reservation-service` from step #1.

    <dependencyManagement>
        <dependencies>
          <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-parent</artifactId>
            <version>Brixton.M3</version>
            <type>pom</type>
            <scope>import</scope>
          </dependency>
        </dependencies>
    </dependencyManagement>

- add `org.springframework.cloud`:`spring-cloud-starter-config` to the `reservation-service`.
- create a `bootstrap.properties` that lives in the same place as `application.properties` and discard the `application.properties` file. Instead, we now need only tell the Spring application where to find the Config Server, with `spring.cloud.config.uri=${config.server:http://localhost:8888}`, and how to identify itself to the Config Server and other services, later, with `spring.application.name`.
- Run the Config Server

> We'll copy and paste  `bootstrap.properties` for each subsequent module, changing only the `spring.application.name` as appropriate.

- In the `reservation-service`, create a `MessageRestController` and annotate it with `@RefreshScope`. Inject the `${message}` key and expose it as a REST endpoint, `/message`.
- trigger a refresh of the message using the `/refresh` endpoint.
- **EXTRA CREDIT**: start `./bin/rabbitmq.sh` and connect the microservice to the event bus using RabbitMQ and by adding the `org.springframework.cloud`:`spring-cloud-starter-bus-amqp` then triggering the refresh using the `/bus/refresh`.

## 4. Service Registration and Discovery

> In the cloud, applications live and die as capacity dictates, they're ephemeral. Applications should not be coupled to the physical location of other services as this state is fleeting. Indeed, even if it were fixed, services may quickly become overwhelmed, so it's very handy to be able to specify how to load balance among the available instances or indeed ask the system to verify that there are instances at all. In this lab, we'll look at the low-level `DiscoveryClient` abstraction at the heart of Spring Cloud's service registration and discovery support.

- go to the Spring Initializr, select the `Eureka Server` (this brings in `org.springframework.cloud`:`spring-cloud-starter-eureka-server`) checkbox, name it `eureka-service` and then add `@EnableEurekaServer` to the `DemoApplication` class.
- Make sure this module _also_ talks to the Config Server as described in the last lab by adding the `org.springframework.cloud`:`spring-cloud-starter-config`.
- add `org.springframework.cloud`:`spring-cloud-starter-eureka` to the `reservation-service`
- add `@EnableDiscoveryClient` to the `reservation-service`'s `DemoApplication` and restart the process, and then confirm its appearance in the Eureka Server at `http://localhost:8761`
- demonstrate using the `DiscoveryClient` API
- use the Spring Initializr, setup a new module, `reservation-client`, that uses the Config Server (`org.springframework.cloud`:`spring-cloud-starter-config`), Eureka Discovery (`org.springframework.cloud`:`spring-cloud-starter-eureka`), and Web (`org.springframework.boot`:`spring-boot-starter-web`).
- create a `bootstrap.properties`, just as with the other modules, but name this one `reservation-client`.
- create a `CommandLineRunner` that uses the `DiscoveryClient` to look up other services programatically

## 5. Edge Services: API gateways (circuit breakers, client-side load balancing)
> Edge services sit as intermediaries between the clients (smart phones, HTML5 applications, etc) and the service. An edge service is a logical place to insert any client-specific requirements (security, API translation, protocol translation) and keep the mid-tier services free of this burdensome logic (as well as free from associated redeploys!)

> Proxy requests from an edge-service to mid-tier services with a _microproxy_. For some classes of clients, a microproxy and security (HTTPS, authentication) might be enough.

- add `org.springframework.cloud`:`spring-cloud-starter-zuul` and `@EnableZuulProxy` to the `reservation-client`, then run it.
- launch a browser and visit the `reservation-client` at `http://localhost:9999/reservation-service/reservations`. This is proxying your request to `http://localhost:8000/reservations`.

> API gateways are used whenever a client - like a mobile phone or HTML5 client - requires API translation. Perhaps the client requires coarser grained payloads, or transformed views on the data

- In the `reservation-service`, create a client side DTO - named `Reservation`, perhaps? - to hold the `Reservation` data from the service. Do this to avoid being coupled between client and service
- add `org.springframework.boot`:`spring-boot-starter-hateoas`
- add a REST service called `ReservationApiGatewayRestController` that uses the `@Autowired @LoadBalanced RestTemplate rt` to make a load-balanced call to a service in the registry using Ribbon.
- map the controller itself to `/reservations` and then create a new controller handler method, `getReservationNames`, that's mapped to `/names`.
- in the `getReservationNames` handler, make a call to `http://reservation-service/reservations` using the `RestTemplate#exchange` method, specifying the return value with a `ParameterizedTypeReference<Resources<Reservation>>>` as the final argument to the `RestTemplate#exchange` method.
- take the results of the call and map them from `Reservation` to `Reservation#getReservationName`. Then, confirm that `http://localhost:9999/reservations/names` returns the names.

> the code works, but it assumes that the `reservation-service` will always be up and responding to requests. We need to be a bit more defensive in any code that clients will connect to. We'll use a circuit-breaker to ensure that the `reservation-client` does something useful as a _fallback_ when it can't connect to the `reservation-service`.

- add `org.springframework.boot`:`spring-boot-starter-actuator` and `org.springframework.cloud`:`spring-cloud-starter-hystrix` to the `reservation-client`
- add `@EnableCircuitBreaker` to our `DemoApplication` configuration class
- add `@HystrixCommand` around any potentially shaky service-to-service calls, like `getReservationNames`, specifying a fallback method that returns an empty collection.
- test that everything works by killing the `reservation-service` and revisiting the `/reservations/names` endpoint
- go to the [Spring Initializr](http://start.spring.io) and stand up a new service - with an `artifactId` of `hystrix-dashboard` - that uses Eureka Discovery, Config Client, and the Hystrix Dashboard.
- identify it is as `hystrix-dashboard` in `bootstrap.properties` and point it to config server.
- annotate it with `@EnableHystrixDashboard` and run it. You should be able to load it at `http://localhost:8010/hystrix.html`. It will expect a heartbeat stream from any of the services with a circuit breaker in them. Give it the address from the `reservation-client`: `http://localhost:9999/hystrix.stream`



## 6. to the Cloud!

> Spring Cloud helps you develop services that are resilient to failure - they're _fault tolerant_. If a service goes down, they'll degrade gracefully, and correctly expand to accommodate the available capacity. But who starts and stops these services? You need a platform for that. In this lab, we'll use a free trial account at Pivotal Web Services to demonstrate how to deploy, scale and heal our services.

- do a `mvn clean install` to get binaries for each of the modules
- comment out the `GraphiteReporter` bean in `reservation-service`
- remove the `<executable/>` attribute from your Maven build plugin
- make sure that each Maven build defines a `<finalName>` element as: `<finalName>${project.artifactId}</finalName>` so that you can consistently refer to the built artifact from each Cloud Foundry manifest.
- sign up for a free trial [account at Pivotal Web Services](http://run.pivotal.io/).
- change the various `bootstrap.properties` files to load configuration from an environment property, `vcap.services.config-service.credentials.uri`, _or_, if that's not available, `http://localhost:8888`: `spring.cloud.config.uri=${vcap.services.config-service.credentials.uri:http://localhost:8888}`
- `cf login` the Pivotal Web Services endpoint, `api.run.pivotal.io` and then enter your credentials for the free trial account you've signed up for.
- enable the Spring Boot actuator `/shutdown` endpoint  in the Config Server `application.properties`: `endpoints.shutdown.enabled=true`
- describe each service - its RAM, DNS `route`, and required services - using a `manifest.yml` file collocated with each binary

> you may generate the `manifest.yml` manually or you may use a tool like Spring Tool Suite's Spring Boot Dashboard which will, on deploy, prompt you to save the deployment configuration as a `manifest.yml`.

- run `cf.sh` in the `labs/6` folder to deploy the whole suite of services to [Pivotal Web Services](http://run.pivotal.io), **OR**:
- follow the steps in `cf-simple.sh`. This will `cf push` the `eureka-service` and `config-service`. It will use `cf cups` to create services that are available to `reservation-service` and `reservation-client` as environment variables, just like any other standard service. Then, it will `cf push` `reservation-client` and `reservation-service`, binding to those services. See `cf-simple.sh` for details and comments - you should be able to follow along on Windows as well.

> As you push new instances, you'll get new routes because of the configuration in the `manifest.yml` which specifies host is "...-${random-word}". When creating the user-provided-services (`cf cups ..`) be sure to choose only the first route. To delete orphaned routes, use `cf delete-orphaned-routes`

> if you're running the `cf cups` commands, remember to quote and escape correctly, e.g.: `cf cups "{ \"uri":\"..\" }"`

- `cf scale -i 4 reservation-service` to scale that single service to 4 instances. Call the `/shutdown` actuator endpoint for `reservation-client`: `curl -d{} http://_RESERVATION_CLIENT_ROUTE_/shutdown`, replacing `_RESERVATION_CLIENT_ROUTE_`.
- observe that `cf apps` records the downed, _flapping_ service and eventually restores it.
- observe that the configuration for the various cloud-specific backing services is handled in terms of various configuration files in the Config Server suffixed with `-cloud.properties`.

> if you need to delete an application, you can use `cf d _APP_NAME_`, where `_APP_NAME_` is your application's logical name. If you want to delete a service, use `cf ds _SERVICE_NAME_` where `_SERVICE_NAME_` is a logical name for the service. Use `-f` to force the deletion without confirmation.
