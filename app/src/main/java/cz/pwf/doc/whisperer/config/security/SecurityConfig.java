package cz.pwf.whisperer.config.security;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.client.RestTemplate;

import java.net.URL;

/**
 * Ústřední třída pro definici bezpečnostních vlastností napříč celou aplikací.
 */
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    public static final String[] EXCLUDED_PATHS = {
            "/swagger-ui/*",
            "/swagger-resources/**",
            "/v3/api-docs",
            "/webjars/**",
            "/error",
            "/actuator/**"
    };

    @Value("${pwf.base-url:}")
    String pwfBaseUrl;

    @Value("${pwf.endpoints.token-verification:/service/api/client/rest/token/verify}")
    String pwfTokenVerificationEndpoint;

    @Value("${pwf.ldap.kpjm-attribute-name:uid}")
    String ldapKpjmAttributeName;

    private final RestTemplate restTemplate;

    @SneakyThrows
    @Override
    protected void configure(HttpSecurity http) {
        URL tokenVerificationEndpointUrl = new URL(new URL(pwfBaseUrl), pwfTokenVerificationEndpoint);

        http
                .cors()
                .and()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .mvcMatchers(EXCLUDED_PATHS).permitAll()
                .anyRequest().authenticated()
                .and()
                .addFilterBefore(new JwtAuthenticationFilter(restTemplate, tokenVerificationEndpointUrl, ldapKpjmAttributeName),
                        UsernamePasswordAuthenticationFilter.class);
    }

}