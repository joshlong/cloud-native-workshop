package com.example.authservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.security.Principal;
import java.util.Optional;
import java.util.stream.Stream;

@EnableResourceServer
@EnableDiscoveryClient
@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}

@RestController
class PrincipalRestController {

    @RequestMapping("/user")
    Principal p(Principal p) {
        return p;
    }
}

@Configuration
@EnableAuthorizationServer
class OAuthConfiguration extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;

    OAuthConfiguration(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
                .inMemory()
                .withClient("html5").authorizedGrantTypes("password").scopes("openid").secret("password")
                .and()
                .withClient("batch-job-client").authorizedGrantTypes("client_credentials").secret("password").scopes("openid");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(this.authenticationManager);
    }
}

@Service
class AccountUserDetailsService implements UserDetailsService {

    private final AccountRepository accountRepository;

    AccountUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.accountRepository.findByUsername(username)
                .map(account ->
                        User.withUsername(account.getUsername())
                                .password(account.getPassword()).roles("USER")
                                .build())

                .orElseThrow(() -> new UsernameNotFoundException("couldn't find " + username + "!"));
    }
}

@Component
class AccountInitializer implements ApplicationRunner {

    private final AccountRepository accountRepository;

    AccountInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(ApplicationArguments applicationArguments) throws Exception {
        Stream.of("jlong,spring", "dsyer,cloud", "pwebb,boot")
                .map(x -> x.split(","))
                .forEach(tuple -> accountRepository.save(new Account(null, tuple[0], tuple[1], true)));

    }
}

interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);
}

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
class Account {

    @Id
    @GeneratedValue
    private Long id;

    private String username, password;
    private boolean active;
}