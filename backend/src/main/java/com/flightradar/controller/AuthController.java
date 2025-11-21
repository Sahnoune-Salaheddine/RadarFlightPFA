package com.flightradar.controller;

import com.flightradar.model.Role;
import com.flightradar.model.User;
import com.flightradar.repository.UserRepository;
import com.flightradar.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@Slf4j
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");
        
        Optional<User> userOpt = authService.authenticate(username, password);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String token = authService.generateToken(username, user.getRole());
            
            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("username", username);
            response.put("role", user.getRole().toString());
            response.put("airportId", user.getAirportId());
            response.put("pilotId", user.getPilotId());
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.status(401).body(Map.of("error", "Identifiants invalides"));
    }
    
    /**
     * POST /api/auth/register
     * Créer un nouveau compte (ADMIN seulement)
     */
    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> userData) {
        try {
            String username = (String) userData.get("username");
            String password = (String) userData.get("password");
            String roleStr = (String) userData.get("role");
            Long airportId = userData.get("airportId") != null ? 
                Long.valueOf(userData.get("airportId").toString()) : null;
            Long pilotId = userData.get("pilotId") != null ? 
                Long.valueOf(userData.get("pilotId").toString()) : null;
            
            if (username == null || password == null || roleStr == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Données manquantes"));
            }
            
            // Vérifier si l'utilisateur existe déjà
            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Nom d'utilisateur déjà utilisé"));
            }
            
            Role role = Role.valueOf(roleStr.toUpperCase());
            
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setRole(role);
            user.setAirportId(airportId);
            user.setPilotId(pilotId);
            
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("message", "Compte créé avec succès", "userId", user.getId()));
        } catch (Exception e) {
            log.error("Erreur lors de la création du compte", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * GET /api/auth/users
     * Liste tous les utilisateurs (ADMIN seulement)
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        try {
            List<Map<String, Object>> users = userRepository.findAll().stream()
                .map(user -> {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("id", user.getId());
                    userData.put("username", user.getUsername());
                    userData.put("role", user.getRole().toString());
                    userData.put("airportId", user.getAirportId());
                    userData.put("pilotId", user.getPilotId());
                    return userData;
                })
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Erreur lors de la récupération des utilisateurs", e);
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * PUT /api/auth/users/{id}
     * Modifier un utilisateur (ADMIN seulement)
     */
    @PutMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> userData) {
        try {
            Optional<User> userOpt = userRepository.findById(id);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            User user = userOpt.get();
            
            if (userData.containsKey("username")) {
                user.setUsername((String) userData.get("username"));
            }
            if (userData.containsKey("password")) {
                user.setPassword(passwordEncoder.encode((String) userData.get("password")));
            }
            if (userData.containsKey("role")) {
                user.setRole(Role.valueOf(((String) userData.get("role")).toUpperCase()));
            }
            if (userData.containsKey("airportId")) {
                user.setAirportId(userData.get("airportId") != null ? 
                    Long.valueOf(userData.get("airportId").toString()) : null);
            }
            if (userData.containsKey("pilotId")) {
                user.setPilotId(userData.get("pilotId") != null ? 
                    Long.valueOf(userData.get("pilotId").toString()) : null);
            }
            
            userRepository.save(user);
            
            return ResponseEntity.ok(Map.of("message", "Utilisateur modifié avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la modification de l'utilisateur", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * DELETE /api/auth/users/{id}
     * Supprimer un utilisateur (ADMIN seulement)
     */
    @DeleteMapping("/users/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            if (!userRepository.existsById(id)) {
                return ResponseEntity.notFound().build();
            }
            
            userRepository.deleteById(id);
            
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
        } catch (Exception e) {
            log.error("Erreur lors de la suppression de l'utilisateur", e);
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}

