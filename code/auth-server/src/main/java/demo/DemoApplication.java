package demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.authentication.configurers.GlobalAuthenticationConfigurerAdapter;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

// https://spring.io/blog/2015/02/03/sso-with-oauth2-angular-js-and-spring-security-part-v

/**
 * Easy to retrieve an access token using:
 * {@code curl -X POST -vu acme:acmesecret http://localhost:9999/uaa/oauth/token -H "Accept: application/json" -d "password=spring&username=jlong&grant_type=password&scope=openid&client_secret=acmesecret&client_id=acme" }
 * <p>
 * Then, send the access token to an OAuth2 secured REST resource using:
 * {@code curl http://localhost:9000/hi -H "Authorization: Bearer _INSERT TOKEN_"}
 *
 * @author Dave Syer (THANK YOU DAVE!)
 * @author Josh Long
 */
@SpringBootApplication
@EnableResourceServer
@RestController
public class DemoApplication {

    @RequestMapping("/user")
    Principal user(Principal user) {
        return user;
    }

    @Bean
    UserDetailsService userDetailsService(JdbcTemplate jdbcTemplate) {
        RowMapper<User> userDetailsRowMapper = (rs, i) -> new User(
                rs.getString("ACCOUNT_NAME"),
                rs.getString("PASSWORD"),
                rs.getBoolean("ENABLED"),
                rs.getBoolean("ENABLED"),
                rs.getBoolean("ENABLED"),
                rs.getBoolean("ENABLED"),
                AuthorityUtils.createAuthorityList("ROLE_USER", "ROLE_ADMIN"));
        return username -> jdbcTemplate.queryForObject(
                "select * from ACCOUNT where ACCOUNT_NAME = ?",
                userDetailsRowMapper, username);
    }

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
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
