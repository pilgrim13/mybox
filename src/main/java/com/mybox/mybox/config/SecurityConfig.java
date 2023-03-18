package com.mybox.mybox.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {

        return http.csrf()
            .disable()
            .build();
    }
//
//    @Bean
//    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
//        log.debug("Initializing the security configuration");
//        return http.authorizeExchange()
//            .pathMatchers("/private").hasRole("USER")
//            .matchers(EndpointRequest.toAnyEndpoint()).hasRole("ADMIN")
//            .anyExchange().permitAll()
//            .and().httpBasic()
//            .and().build();
//    }
//
//    /**
//     * Sample in-memory user details service.
//     */
//    @SuppressWarnings("deprecation") // Removes warning from "withDefaultPasswordEncoder()"
//    @Bean
//    public MapReactiveUserDetailsService userDetailsService() {
//        log.debug("Initializing the user details service");
//        return new MapReactiveUserDetailsService(
//            User.withDefaultPasswordEncoder()
//                .username("user")
//                .password("password")
//                .roles("USER")
//                .build(),
//            User.withDefaultPasswordEncoder()
//                .username("admin")
//                .password("password")
//                .roles("USER,ADMIN")
//                .build());
//    }
}