package com.smartsplit.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // ** 1. Disable CSRF **
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**") // ** 2. Make auth endpoints public **
                        .permitAll()
                        .anyRequest() // ** 3. Secure all other endpoints **
                        .authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // ** 4. Use stateless sessions **
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // ** 5. Add JWT filter **

        return http.build();
    }
}

// NOTE: You will also need to provide beans for AuthenticationProvider, UserDetailsService,
// and PasswordEncoder elsewhere in your configuration, which are likely already part of your
// user/auth setup.