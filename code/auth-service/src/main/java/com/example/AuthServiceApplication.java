package com.example;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.AuthorityUtils;
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

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
class Account {

	@Id
	@GeneratedValue
	private Long id;

	private String username, password;
	private boolean active = true;

	public Account(String u, String p) {
		this.username = u;
		this.password = p;
	}
}

interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByUsername(String u);
}

@EnableDiscoveryClient
@SpringBootApplication
@EnableResourceServer
public class AuthServiceApplication {

	@RestController
	public static class PrincipalRestController {

		@RequestMapping("/user")
		public Principal principal(Principal p) {
			return p;
		}
	}

	@Component
	public static class AccountInitializer
			implements CommandLineRunner {

		private final AccountRepository accountRepository;

		AccountInitializer(AccountRepository accountRepository) {
			this.accountRepository = accountRepository;
		}

		@Override
		public void run(String... strings) throws Exception {
			Stream.of("jlong,spring", "dsyer,cloud")
					.map(x -> x.split(","))
					.forEach(tpl -> this.accountRepository.save(new Account(tpl[0], tpl[1])));
		}
	}

	@Service
	public static class JpaUserDetailsService
			implements UserDetailsService {

		private final AccountRepository accountRepository;

		JpaUserDetailsService(AccountRepository accountRepository) {
			this.accountRepository = accountRepository;
		}

		@Override
		public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {

			Optional<Account> accountOptional = accountRepository.findByUsername(s);

			return accountOptional
					.map(a -> new User(a.getUsername(), a.getPassword(), a.isActive(), a.isActive(), a.isActive(), a.isActive(),
							AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN")))
					.orElseThrow(() -> new UsernameNotFoundException("couldn't find  " + s + "!"));
		}
	}

	@Configuration
	@EnableAuthorizationServer
	public static class AuthServerConfiguration
			extends AuthorizationServerConfigurerAdapter {

		private final AuthenticationManager authenticationManager;

		AuthServerConfiguration(AuthenticationManager am) {
			this.authenticationManager = am;
		}

		@Override
		public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
			clients.inMemory()
					.withClient("html5").secret("secret").scopes("openid").authorizedGrantTypes("password");
		}

		@Override
		public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
			endpoints.authenticationManager(this.authenticationManager);
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}
}

