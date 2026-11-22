package com.pharmasync.headoffice.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Spring Security for head-office sync endpoints.
 * Per-branch device credentials are loaded from environment variables via
 * {@link BranchSecurityProperties} (BRANCH_DEVICE_CREDENTIALS).
 * Only {@code /api/sync/**} is protected with HTTP Basic + BRANCH role; all other
 * endpoints remain permitAll (status, actuator, public GET endpoints).
 */
@Configuration
public class BranchDeviceSecurityConfig {

    @Bean
    public UserDetailsService branchUserDetailsService(BranchSecurityProperties props) {
        List<UserDetails> users = props.getBranchCredentials().stream()
                .map(this::toUserDetails)
                .toList();
        return new InMemoryUserDetailsManager(users);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain syncSecurityChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/sync/**")
                .authorizeHttpRequests(auth -> auth.anyRequest().hasRole("BRANCH"))
                .httpBasic(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable());
        return http.build();
    }

    private UserDetails toUserDetails(String entry) {
        String[] parts = entry.split(":", 2);
        if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
            throw new IllegalArgumentException("Invalid branch credential entry (expect branchId:secret): " + entry);
        }
        return User.builder()
                .username(parts[0])
                .password("{noop}" + parts[1])
                .roles("BRANCH")
                .build();
    }
}
