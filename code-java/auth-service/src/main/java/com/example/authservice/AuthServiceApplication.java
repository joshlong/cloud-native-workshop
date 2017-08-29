package com.example.authservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootApplication
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

    @Bean
    CommandLineRunner init(AccountRepository accountRepository) {
        return args -> {
            Stream.of("jlong,spring", "dsyer,cloud", "pwebb,boot")
                    .map(x -> x.split(","))
                    .forEach(tpl -> accountRepository.save(new Account(tpl[0], tpl[1], true)));
            accountRepository.findAll().forEach(System.out::println);
        };
    }
}

@Configuration
@EnableAuthorizationServer
class AuthServerConfiguration extends AuthorizationServerConfigurerAdapter {

    private final AuthenticationManager authenticationManager;

    AuthServerConfiguration(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("html5")
                    .scopes("openid")
                    .secret("password")
                    .authorizedGrantTypes("password")
                    .and()
                .withClient("uaa")
                    .secret("secret")
                    .scopes("openid")
                    .redirectUris("http://localhost:8080/oauth2/authorize/code/uaa")
                    .authorizedGrantTypes("authorization_code");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.authenticationManager(this.authenticationManager);
    }
}

@Configuration
@EnableResourceServer
class ResourceServerConfiguration extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
            .authorizeRequests()
                .antMatchers("/user").access("#oauth2.hasScope('openid')")
                .anyRequest().authenticated();
    }
}

@RestController
class UserInfoController {

    @RequestMapping("/user")
    public UserInfo userInfo(OAuth2Authentication authentication) {
        return new UserInfo(authentication);
    }

    public class UserInfo {
        private final OAuth2Authentication authentication;

        public UserInfo(OAuth2Authentication authentication) {
            this.authentication = authentication;
        }

        public Collection<? extends GrantedAuthority> getAuthorities() {
            return this.authentication.getAuthorities();
        }

        public String getName() {
            return this.authentication.getName();
        }
    }
}

@Service
class AccountUserDetailsService implements UserDetailsService {

    AccountUserDetailsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        return accountRepository.findByUsername(username)
                .map(account -> new User(account.getUsername(),
                        account.getPassword(),
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")))
                .orElseThrow(() -> new UsernameNotFoundException("oops! couldn't find " + username));
    }

    private final AccountRepository accountRepository;
}

interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUsername(String username);
}

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
class Account {

    public Account(String username, String password, boolean active) {
        this.username = username;
        this.password = password;
        this.active = active;
    }

    @Id
    @GeneratedValue
    private Long id;
    private String username, password;
    private boolean active;
}