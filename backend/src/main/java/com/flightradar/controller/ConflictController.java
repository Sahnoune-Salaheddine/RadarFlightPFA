package com.flightradar.controller;

import com.flightradar.service.ConflictDetectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST pour les alertes de conflit
 */
@RestController
@RequestMapping("/api/conflicts")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class ConflictController {
    
    @Autowired
    private ConflictDetectionService conflictDetectionService;
    
    /**
     * GET /api/conflicts
     * Récupère tous les conflits actifs
     */
    @GetMapping
    public ResponseEntity<List<ConflictDetectionService.ConflictAlert>> getActiveConflicts() {
        return ResponseEntity.ok(conflictDetectionService.getActiveConflicts());
    }
}

