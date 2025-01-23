package com.ecommerce.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class AppConfig {

    private final JwtTokenValidator filter;
    private final String frontendBaseUrl;

    public AppConfig(JwtTokenValidator filter,
                     @Qualifier("frontendBaseUrl") String frontendBaseUrl) {
        this.filter = filter;
        this.frontendBaseUrl = frontendBaseUrl;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF protection
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/public/**",
                                "/api/webhook/payment",
                                "/api/payments/callback"
                        ).permitAll() // Public access
                        .requestMatchers("/api/admin/**").hasRole("ADMIN") // Admin role required
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN") // User and admin access
                        .anyRequest().authenticated() // All other requests require authentication
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Stateless sessions (JWT)
                )
                .addFilterBefore(filter, UsernamePasswordAuthenticationFilter.class) // Add JWT token validation before the auth filter
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Enable CORS
                .httpBasic(httpBasic -> {
                }) // Enable basic authentication
                .formLogin(formLogin -> {
                }) // Enable form login (optional)
                .build(); // Build the security configuration
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration corsConfig = new CorsConfiguration();
        corsConfig.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                frontendBaseUrl
        )); // Allowed origins
        corsConfig.setAllowedMethods(Collections.singletonList("*")); // Allow all HTTP methods
        corsConfig.setAllowedHeaders(Collections.singletonList("*")); // Allow all headers
        corsConfig.setExposedHeaders(List.of("Authorization")); // Expose the "Authorization" header
        corsConfig.setAllowCredentials(true); // Allow credentials (cookies, authorization headers, etc.)
        corsConfig.setMaxAge(3600L); // Cache preflight request for 1 hour
        return request -> corsConfig;
    }
}
