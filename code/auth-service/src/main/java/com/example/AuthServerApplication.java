package com.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
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

@SpringBootApplication
@EnableResourceServer
public class AuthServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}
}

@RestController
class PrincipalRestController {

	@RequestMapping ("/user")
	Principal principal (Principal principal) {
		return principal;
	}

}

@Configuration
@EnableAuthorizationServer
class OAuthConfiguration extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager ;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients
				.inMemory()
				.withClient("acme")
				.secret("acmesecret")
				.scopes("openid")
				.authorizedGrantTypes(
						"authorization_code", "refresh_token", "password");
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
	 endpoints.authenticationManager(this.authenticationManager);
	}

}

@Service
class AccountUserDetailsService implements UserDetailsService {

	private final AccountRepository accountRepository;

	@Autowired
	public AccountUserDetailsService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws
			UsernameNotFoundException {

		return this.accountRepository.findByUsername(username)
				.map(account -> new User(
						account.getUsername(),
						account.getPassword(),
						account.isActive(),
						account.isActive(),
						account.isActive(),
						account.isActive(),
						AuthorityUtils.createAuthorityList("ROLE_ADMIN", "ROLE_USER")))
				.orElseThrow(() -> new UsernameNotFoundException("couldn't find the user " +
						"" + username + "!"));

	}
}

@Component
class SampleAccountDataCLR implements CommandLineRunner {

	private final AccountRepository accountRepository;

	@Autowired
	public SampleAccountDataCLR(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public void run(String... strings) throws Exception {

		Stream.of("jlong,spring", "pwebb,boot", "dsyer,cloud")
				.map(t -> t.split(","))
				.forEach(tuple ->
						accountRepository.save(new Account(
								tuple[0],
								tuple[1],
								true
						))
				);


	}
}

interface AccountRepository extends JpaRepository<Account, Long> {

	Optional<Account> findByUsername(String username);
}

@Entity
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

	public String getUsername() {
		return username;
	}

	Account() {// why JPA why??
	}

	public String getPassword() {
		return password;
	}

	public boolean isActive() {
		return active;
	}
}
