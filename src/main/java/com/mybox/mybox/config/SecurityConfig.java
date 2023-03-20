package com.mybox.mybox.config;

import com.mybox.mybox.user.domain.constants.Role;
import com.mybox.mybox.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
            .httpBasic()    // Postman 테스트 위해 추가
            .and()
            .csrf().disable()
            .authorizeExchange()
            .pathMatchers("/files/**").hasAnyAuthority(Role.ROLE_USER.name())
            .anyExchange()
            .permitAll()
            .and()
            .formLogin()
            .and()
            .logout()
            .logoutUrl("/logout");
        return http.build();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService() {
        return userRepository::findByUsername;
    }

}