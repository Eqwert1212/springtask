package kz.bitlab.G118springfirstapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Local demo: no login; all modifying requests still require a CSRF token.
        http.authorizeHttpRequests(requests -> requests.anyRequest().permitAll())
                .csrf(Customizer.withDefaults());
        return http.build();
    }
}
