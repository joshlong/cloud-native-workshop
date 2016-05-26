package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
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


/**
 * Easy to retrieve an access token using:
 * {@code curl -X POST -vu acme:acmesecret http://localhost:9191/uaa/oauth/token -H "Accept: application/json" -d "password=spring&username=jlong&grant_type=password&scope=openid&client_secret=acmesecret&client_id=acme"  }
 * <p>
 * Then, send the access token to an OAuth2 secured REST resource using:
 * {@code curl http://localhost:9999/reservations/names -H "Authorization: Bearer _INSERT TOKEN_"}
 *
 * @author Dave Syer (THANK YOU DAVE!)
 * @author Josh Long
 */
@SpringBootApplication
@EnableResourceServer
public class AuthServerApplication {


	public static void main(String[] args) {
		SpringApplication.run(AuthServerApplication.class, args);
	}

}

@Service
class DatabaseUserDetailsService implements UserDetailsService {

	private final AccountRepository accountRepository;

	@Autowired
	public DatabaseUserDetailsService(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return this.accountRepository
				.findByAccountName(username)
				.map(account -> {
					boolean active = account.isActive();
					return new User(account.getAccountName(),
							account.getPassword(),
							active, active, active, active,
							AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN"));
				})
				.orElseThrow(() -> new RuntimeException("couldn't find the user " + username + "!"));
	}
}

@RestController
class PrincipalRestController {

	@RequestMapping("/user")
	Principal user(Principal user) {
		return user;
	}

}

@Component
class AccountCommandLineRunner implements CommandLineRunner {

	private final AccountRepository accountRepository;

	@Autowired
	public AccountCommandLineRunner(AccountRepository accountRepository) {
		this.accountRepository = accountRepository;
	}

	@Override
	public void run(String... args) throws Exception {
		Stream.of("jlong,spring", "pwebb,boot")
				.map(n -> n.split(","))
				.forEach(n -> this.accountRepository.save(new Account(true, n[0], n[1])));
	}
}

interface AccountRepository extends JpaRepository<Account, Long> {
	Optional<Account> findByAccountName(String an);
}

@Entity
class Account {

	@Id
	@GeneratedValue
	private Long id;
	private boolean active;
	private String accountName, password;

	Account() {
	}

	public Account(boolean active, String acccountName, String password) {
		this.active = active;
		this.accountName = acccountName;
		this.password = password;
	}

	public Long getId() {
		return id;
	}

	public boolean isActive() {
		return active;
	}

	public String getAccountName() {
		return accountName;
	}

	public String getPassword() {
		return password;
	}
}

@Configuration
@EnableGlobalAuthentication
class SecurityConfig extends GlobalAuthenticationConfigurerAdapter {

	@Autowired
	private UserDetailsService userDetailsService;

	@Override
	public void init(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(this.userDetailsService);
	}

}

@Configuration
@EnableAuthorizationServer
class OAuth2Config extends AuthorizationServerConfigurerAdapter {

	@Autowired
	private AuthenticationManager authenticationManager;

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints)
			throws Exception {
		endpoints.authenticationManager(authenticationManager);
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients)
			throws Exception {
		clients.inMemory()
				.withClient("acme")
				.secret("acmesecret")
				.authorizedGrantTypes("authorization_code", "refresh_token", "password")
				.scopes("openid");
	}
}
