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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
                
                if (username == null || roleStr == null) {
                    log.warn("Token JWT invalide: username ou role manquant. URL: {}", request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }
                
                // Récupérer l'utilisateur depuis la base de données
                Optional<User> userOpt = userRepository.findByUsername(username);
                
                if (userOpt.isEmpty()) {
                    log.warn("Utilisateur non trouvé dans la base de données: {}. URL: {}", username, request.getRequestURI());
                    filterChain.doFilter(request, response);
                    return;
                }
                
                User user = userOpt.get();
                
                try {
                    Role role = Role.valueOf(roleStr);
                    
                    // Vérifier que le rôle correspond
                    if (user.getRole() != role) {
                        log.warn("Rôle dans le token ({}) ne correspond pas au rôle en base ({}) pour l'utilisateur {}. URL: {}", 
                            roleStr, user.getRole(), username, request.getRequestURI());
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            user,
                            null,
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
                        );
                    
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    
                    log.debug("Authentification réussie pour l'utilisateur {} avec le rôle {}. URL: {}", 
                        username, role, request.getRequestURI());
                    
                } catch (IllegalArgumentException e) {
                    log.warn("Rôle invalide dans le token: {}. URL: {}", roleStr, request.getRequestURI());
                }
            } catch (Exception e) {
                // Token invalide ou expiré
                log.warn("Token JWT invalide ou expiré pour l'URL {}: {}", request.getRequestURI(), e.getMessage());
            }
        } else {
            // Pas de token fourni
            if (request.getRequestURI().startsWith("/api/admin") || 
                request.getRequestURI().startsWith("/api/pilots") ||
                request.getRequestURI().startsWith("/api/radar")) {
                log.debug("Aucun token JWT fourni pour l'URL protégée: {}", request.getRequestURI());
            }
        }
        
        filterChain.doFilter(request, response);
    }
}

