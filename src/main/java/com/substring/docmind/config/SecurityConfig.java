
package com.substring.docmind.config;

import com.substring.docmind.entity.Role;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter authenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity httpSecurity
    ) throws Exception {

        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                .sessionManagement(session ->
                        session.sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .authorizeHttpRequests(auth -> auth

                        // Permit async and error dispatches
                        .dispatcherTypeMatchers(
                                DispatcherType.ASYNC,
                                DispatcherType.ERROR
                        ).permitAll()

                        // Error endpoint
                        .requestMatchers("/error").permitAll()

                        // Public authentication and Swagger endpoints
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/api/health"
                        ).permitAll()

                        // Admin endpoints
                        .requestMatchers("/api/v1/admin/**")
                        .hasRole(Role.ADMIN.toString())

                        // User endpoints
                        .requestMatchers("/api/v1/**")
                        .hasRole(Role.USER.toString())

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                .addFilterAfter(
                        authenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) -> {
                                    if (!response.isCommitted()) {
                                        response.sendError(
                                                HttpStatus.UNAUTHORIZED.value(),
                                                "Unauthorized"
                                        );
                                    }
                                }
                        )
                );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
