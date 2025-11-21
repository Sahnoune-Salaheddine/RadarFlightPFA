package com.flightradar.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                // Endpoints publics
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/auth/validate").authenticated() // Nécessite un token valide
                .requestMatchers("/api/auth/register").hasRole("ADMIN") // Seul l'admin peut créer des comptes
                
                // Endpoints ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Endpoints RADAR
                .requestMatchers("/api/radar/**").hasAnyRole("CENTRE_RADAR", "ADMIN")
                
                // Endpoints PILOTE
                .requestMatchers("/api/pilots/**").hasAnyRole("PILOTE", "ADMIN")
                .requestMatchers("/api/atc/**").hasAnyRole("PILOTE", "CENTRE_RADAR", "ADMIN")
                
                // Endpoints publics (lecture seule)
                .requestMatchers("/api/airports/**").permitAll()
                .requestMatchers("/api/aircraft/**").permitAll()
                .requestMatchers("/api/weather/**").permitAll()
                .requestMatchers("/api/flights/**").permitAll()
                .requestMatchers("/api/runways/**").permitAll()
                .requestMatchers("/api/conflicts/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                
                // Tous les autres endpoints nécessitent une authentification
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Autoriser les deux ports possibles pour le frontend (3000 et 3001)
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:3001"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
