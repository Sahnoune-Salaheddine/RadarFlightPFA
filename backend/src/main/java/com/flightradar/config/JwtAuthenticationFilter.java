package com.flightradar.config;

import com.flightradar.model.Role;
import com.flightradar.model.User;
import com.flightradar.repository.UserRepository;
import com.flightradar.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Filtre JWT pour valider les tokens sur chaque requête
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                Claims claims = jwtService.parseToken(token);
                
                String username = claims.getSubject();
                String roleStr = claims.get("role", String.class);
                
                // Récupérer l'utilisateur depuis la base de données
                Optional<User> userOpt = userRepository.findByUsername(username);
                
                if (userOpt.isPresent() && roleStr != null) {
                    User user = userOpt.get();
                    Role role = Role.valueOf(roleStr);
                    
                    // Vérifier que le rôle correspond
                    if (user.getRole() == role) {
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(
                                user,
                                null,
                                Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                            );
                        
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                // Token invalide - continuer sans authentification
                logger.debug("Token JWT invalide: " + e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

