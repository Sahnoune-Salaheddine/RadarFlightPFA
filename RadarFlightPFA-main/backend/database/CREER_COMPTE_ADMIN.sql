-- =====================================================
-- Script SQL - Créer un Compte Admin
-- =====================================================
-- Ce script crée un compte administrateur si aucun n'existe
-- =====================================================

-- Vérifier les comptes existants
SELECT '=== COMPTES EXISTANTS ===' as info;

SELECT id, username, role FROM users ORDER BY role, username;

-- Créer un compte admin si aucun n'existe
DO $$
DECLARE
    admin_count INTEGER;
    admin_password VARCHAR(255);
BEGIN
    -- Compter les admins existants
    SELECT COUNT(*) INTO admin_count FROM users WHERE role = 'ADMIN';
    
    IF admin_count = 0 THEN
        -- Hasher le mot de passe "admin123" avec BCrypt
        -- Le hash BCrypt pour "admin123" est : $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
        admin_password := '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';
        
        -- Créer le compte admin
        INSERT INTO users (username, password, role)
        VALUES ('admin', admin_password, 'ADMIN')
        ON CONFLICT (username) DO NOTHING;
        
        RAISE NOTICE 'Compte admin cree: username=admin, password=admin123';
    ELSE
        RAISE NOTICE 'Un compte admin existe deja';
    END IF;
END $$;

-- Vérifier le résultat
SELECT '=== RESULTAT ===' as info;

SELECT 
    id,
    username,
    role,
    CASE 
        WHEN role = 'ADMIN' THEN 'OK - Compte admin disponible'
        ELSE 'Autre role'
    END as statut
FROM users
WHERE role = 'ADMIN';

-- Afficher tous les comptes
SELECT '=== TOUS LES COMPTES ===' as info;

SELECT id, username, role FROM users ORDER BY role, username;

SELECT '=== INSTRUCTIONS ===' as info;
SELECT 
    'Pour vous connecter en tant qu''admin:' as instruction,
    'Username: admin' as username,
    'Password: admin123' as password;

