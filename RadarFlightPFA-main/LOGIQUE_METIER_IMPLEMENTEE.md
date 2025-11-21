# 📋 Logique Métier Implémentée

## 🎯 Vue d'Ensemble

Cette document décrit la logique métier complète implémentée dans le projet RadarFlightPFA.

### Structure Globale

- **4 Aéroports** au total
- **8 Avions** (2 par aéroport)
- **8 Pilotes** (1 par avion, 2 par aéroport)
- **4 Radars** (1 par aéroport)

---

## 📍 1. Aéroports

### Caractéristiques

Chaque aéroport est une entité gérée dans la base de données avec les champs suivants :

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `id` | BIGSERIAL | Identifiant unique | ✅ |
| `name` | VARCHAR(100) | Nom de l'aéroport | ✅ |
| `code_iata` | VARCHAR(3) | Code IATA (ex: CMN) | ✅ |
| `code_icao` | VARCHAR(4) | Code ICAO (ex: GMMN) | ✅ |
| `city` | VARCHAR(100) | Ville | ✅ |
| `country` | VARCHAR(100) | Pays | ✅ |
| `latitude` | DECIMAL(10,8) | Latitude GPS | ✅ |
| `longitude` | DECIMAL(11,8) | Longitude GPS | ✅ |

### Aéroports Initialisés

1. **Casablanca (CMN)**
   - Code ICAO: GMMN
   - Coordonnées: 33.367500, -7.589800
   - Pays: Maroc

2. **Rabat (RBA)**
   - Code ICAO: GMME
   - Coordonnées: 34.051500, -6.751500
   - Pays: Maroc

3. **Marrakech (RAK)**
   - Code ICAO: GMMX
   - Coordonnées: 31.606900, -8.036300
   - Pays: Maroc

4. **Tanger (TNG)**
   - Code ICAO: GMTT
   - Coordonnées: 35.726900, -5.916900
   - Pays: Maroc

### Relations

- **1 → N Avions** : Chaque aéroport possède exactement 2 avions
- **1 → 1 Radar** : Chaque aéroport possède un centre radar unique
- **1 → N Pilotes** : Chaque aéroport possède 2 pilotes

---

## 🛩️ 2. Avions

### Caractéristiques

Chaque avion est assigné à un aéroport et à un pilote.

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `id` | BIGSERIAL | Identifiant unique | ✅ |
| `registration` | VARCHAR(20) | Immatriculation (ex: CN-CMN01) | ✅ |
| `model` | VARCHAR(50) | Modèle (ex: A320, B737) | ✅ |
| `capacity` | INTEGER | Capacité en passagers | ✅ |
| `status` | ENUM | Statut (AU_SOL, EN_VOL, etc.) | ✅ |
| `airport_id` | BIGINT | FK → aéroport | ✅ |
| `pilot_id` | BIGINT | FK → pilote | ✅ |

### Logique d'Assignation

- **Chaque aéroport possède exactement 2 avions**
- **Chaque avion est assigné à un seul pilote**
- **Total : 4 aéroports × 2 avions = 8 avions**

### Nomenclature des Avions

Les avions sont nommés selon le pattern : `CN-{CODE_IATA}{NUMERO}`

Exemples :
- `CN-CMN01` : Premier avion de Casablanca
- `CN-CMN02` : Deuxième avion de Casablanca
- `CN-RBA01` : Premier avion de Rabat
- etc.

### Modèles par Aéroport

- **Premier avion** : A320 (capacité 180 passagers)
- **Deuxième avion** : B737 (capacité 150 passagers)

---

## 👨‍✈️ 3. Pilotes

### Caractéristiques

Chaque pilote est assigné à un avion et à un aéroport.

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `id` | BIGSERIAL | Identifiant unique | ✅ |
| `name` | VARCHAR(100) | Nom complet | ✅ |
| `license` | VARCHAR(50) | Numéro de licence | ✅ |
| `experience_years` | INTEGER | Années d'expérience | ✅ |
| `first_name` | VARCHAR(100) | Prénom | ❌ |
| `last_name` | VARCHAR(100) | Nom de famille | ❌ |
| `airport_id` | BIGINT | FK → aéroport | ✅ |
| `user_id` | BIGINT | FK → utilisateur (authentification) | ❌ |

### Logique d'Assignation

- **Chaque avion est assigné à un seul pilote**
- **Chaque aéroport possède 2 pilotes**
- **Relation : Pilote 1 → 1 Avion**
- **Total : 8 pilotes (1 par avion)**

### Nomenclature des Pilotes

Les pilotes sont nommés selon le pattern : `{CODE_IATA}P{NUMERO}`

Exemples :
- `CMNP1` : Premier pilote de Casablanca
- `CMNP2` : Deuxième pilote de Casablanca
- `RBAP1` : Premier pilote de Rabat
- etc.

### Expérience

- **Premier pilote** : 6 ans d'expérience
- **Deuxième pilote** : 7 ans d'expérience

---

## 📡 4. Radars (Centres Radar)

### Caractéristiques

Chaque aéroport possède un centre radar unique.

| Champ | Type | Description | Obligatoire |
|-------|------|-------------|-------------|
| `id` | BIGSERIAL | Identifiant unique | ✅ |
| `name` | VARCHAR(100) | Nom du radar | ✅ |
| `code` | VARCHAR(20) | Code unique (ex: CMN_RADAR) | ✅ |
| `frequency` | DECIMAL(6,2) | Fréquence VHF en MHz | ✅ |
| `status` | ENUM | Statut (ACTIF, PANNE, MAINTENANCE) | ✅ |
| `range` | DECIMAL(8,2) | Portée en kilomètres | ✅ |
| `airport_id` | BIGINT | FK → aéroport | ✅ |
| `user_id` | BIGINT | FK → utilisateur (authentification) | ❌ |

### Logique d'Assignation

- **Chaque aéroport possède exactement 1 centre radar**
- **Relation : Aéroport 1 → 1 Radar**
- **Total : 4 radars (1 par aéroport)**

### Nomenclature des Radars

Les radars sont nommés selon le pattern : `{CODE_IATA}_RADAR`

Exemples :
- `CMN_RADAR` : Radar de Casablanca
- `RBA_RADAR` : Radar de Rabat
- `RAK_RADAR` : Radar de Marrakech
- `TNG_RADAR` : Radar de Tanger

### Configuration par Défaut

- **Statut** : ACTIF
- **Portée** : 200 km
- **Fréquence** : 121.5 MHz + (ID aéroport)

---

## 🔗 Relations Complètes

### Schéma des Relations

```
Aéroport (4)
├── Avion 1 (8 au total)
│   └── Pilote 1 (8 au total)
├── Avion 2
│   └── Pilote 2
└── Radar (4 au total)
```

### Cardinalités

| Relation | Type | Description |
|----------|------|-------------|
| Aéroport → Avions | 1 → N | 2 avions par aéroport |
| Aéroport → Pilotes | 1 → N | 2 pilotes par aéroport |
| Aéroport → Radar | 1 → 1 | 1 radar par aéroport |
| Avion → Pilote | N → 1 | 1 pilote par avion |

---

## 📊 Résumé des Totaux

| Entité | Quantité | Répartition |
|--------|----------|-------------|
| **Aéroports** | 4 | - |
| **Avions** | 8 | 2 par aéroport |
| **Pilotes** | 8 | 1 par avion, 2 par aéroport |
| **Radars** | 4 | 1 par aéroport |

---

## 🚀 Implémentation

### Fichiers Créés/Modifiés

#### Backend (Java)

1. **Entités Modifiées** :
   - `Airport.java` : Ajout de `codeICAO` et `country`
   - `Aircraft.java` : Ajout de `capacity`
   - `Pilot.java` : Ajout de `airport` (relation ManyToOne)
   - `RadarCenter.java` : Ajout de `status` et `range`
   - `RadarStatus.java` : Nouvel enum (ACTIF, PANNE, MAINTENANCE)

#### Base de Données

1. **Migration SQL** :
   - `MIGRATION_LOGIQUE_METIER.sql` : Script complet de migration
   - Ajoute les colonnes manquantes
   - Initialise les données selon la logique métier

2. **Script PowerShell** :
   - `EXECUTER_MIGRATION_LOGIQUE_METIER.ps1` : Exécution automatique

---

## ✅ Vérification

### Après Migration

Pour vérifier que tout est correct :

```sql
-- Vérifier les aéroports
SELECT code_iata, code_icao, country FROM airports;

-- Vérifier les avions par aéroport
SELECT a.code_iata, COUNT(ac.id) as avions
FROM airports a
LEFT JOIN aircraft ac ON ac.airport_id = a.id
GROUP BY a.code_iata;

-- Vérifier les pilotes par aéroport
SELECT a.code_iata, COUNT(p.id) as pilotes
FROM airports a
LEFT JOIN pilots p ON p.airport_id = a.id
GROUP BY a.code_iata;

-- Vérifier les radars
SELECT a.code_iata, rc.name, rc.status
FROM airports a
LEFT JOIN radar_centers rc ON rc.airport_id = a.id;
```

---

## 📝 Notes Techniques

### Contraintes de Base de Données

- **Unicité** : `code_iata`, `code_icao`, `registration`, `license` sont uniques
- **Relations** : Toutes les FK sont correctement définies avec `ON DELETE SET NULL` ou `ON DELETE CASCADE`
- **Valeurs par Défaut** : Les champs obligatoires ont des valeurs par défaut appropriées

### Logique Métier Appliquée

- ✅ Chaque aéroport a exactement 2 avions
- ✅ Chaque avion est assigné à 1 pilote
- ✅ Chaque aéroport a 2 pilotes
- ✅ Chaque aéroport a 1 radar
- ✅ Toutes les relations sont respectées

---

## 🔄 Prochaines Étapes

1. **Exécuter la migration** :
   ```powershell
   .\EXECUTER_MIGRATION_LOGIQUE_METIER.ps1
   ```

2. **Redémarrer le backend** :
   ```powershell
   cd backend
   mvn spring-boot:run
   ```

3. **Vérifier les données** dans l'interface admin

4. **Tester les fonctionnalités** liées aux aéroports, avions, pilotes et radars

---

## 📚 Documentation Associée

- `ARCHITECTURE_COMPLETE.md` : Architecture générale du projet
- `MIGRATION_LOGIQUE_METIER.sql` : Script SQL complet
- `EXECUTER_MIGRATION_LOGIQUE_METIER.ps1` : Script d'exécution

