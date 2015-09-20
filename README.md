# Workshop

## Building an Application with Spring Boot

## Building a Smart Hypermedia-Aware API

## Code Complete is _not_ Production Ready!

## Velocity through Consistency
 - writing custom auto-configuration & custom starters : we'll create a starter that the rest of our services will include. the starter will do things like define  an `AlwaysSampler` `@Bean`, pull in RabbitMQ for the event bus, pull in the actuator jar, etc.  
 - extending the Spring Initializr

## Centralized Configuration
 - store the configuration in a single place
 - inject it into code
 - demonstrate encryption? (maybe that bit - including the key - could be stored in the custom starter?)


## Service Registration & Discovery
 - Eureka, then Consul (compare & contrast)

## Microproxies

## API Gateways

## Client-Side Load Balancing

 - we'll setup the Eureka `DiscoveryClient` using the `Eureka Server` support
 - we'll setup Consul `DiscoveryClient`. This implies some complexity because of Docker. For the whole thing to work, be sure to add `127.0.0.1	localhost cna`, where `cna` is the host that - inside to the Docker container for Consul - resolves to the host OS (OSX, Linux, whatever) and to our running applications. Consul
 will be able to ping health checks and so on. Externally, this DNS resolves to nothing, and when the `reservation-client` calls the `reservation-service`, Ribbon
 will route to something that it doesn't know about. So restart Intellij to make it see the updated routing table.

## Circuit Breaker

## Tracing

## Messaging with Spring Cloud Stream and Spring Cloud Data Flow
 - `reservation-service` talks to `reservation-client`
 - look at how to [deploy Spring Cloud Data Flow's `admin` and `shell` modules locally](http://docs.spring.io/spring-cloud-dataflow/docs/1.0.0.M1/reference/html/getting-started-deploying-spring-cloud-dataflow.html#_deploying_local).
 - you'll need to run the admin with `--remoteRepositories=/Users/jlong/.m2/repository` to get it to see modules that are deployed locally:
   ```
java -jar spring-cloud-dataflow-admin-1.0.0.BUILD-SNAPSHOT.jar --remoteRepositories=$HOME/.m2/repository
   ```
 - In order to qualify as valid modules, you'll need to `mvn clean install` the Spring Cloud Stream module with the following Spring Boot Maven plugin configuration:

 ```
 <build>
       <plugins>
           <plugin>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-maven-plugin</artifactId>
               <configuration>
                   <classifier>exec</classifier>
               </configuration>
           </plugin>
       </plugins>
   </build>
 ```

- It's important that the module be deployed to the local repository.
- once it's deployed, fire up the `shell` and deploy the module. Here's what my shell session looked like:

```
admin config server http://localhost:$PORT

stream list

module register --name $YOUR_MODULE_NAME --type sink --coordinates $GROUPID:$ARTIFACT:$VERSION


stream create --name http-reservation --definition "http --port=9091 | reservation-sink" --deploy  

http post --data '{ "reservationName": "Ilaya" }'  --target http://localhost:9091

```

- the S-C-S-modules project on GitHub show lots of modules that work just liek the custom one that i developed. Check out `TimeSource.java`, for example. it shows custom common configuration classes like `PeriodicTriggerConfiguration`.


## Security && SSO
 - use Github
 - use Spring Cloud Auth Server

## To the Cloud
