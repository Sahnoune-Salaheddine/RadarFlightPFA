# Vue d'ensemble des Opérations - Documentation

## 📋 Vue d'ensemble

Ce document décrit la nouvelle fonctionnalité **"Vue d'ensemble des Opérations"** ajoutée au dashboard Admin. Cette vue complète offre une vision consolidée de toutes les opérations aéronautiques et météorologiques en temps réel, inspirée des dashboards professionnels internationaux (FlightAware, Eurocontrol, FAA Ops Dashboard).

## 🎯 Objectifs

La vue d'ensemble des opérations permet aux administrateurs de :
- Surveiller le trafic aérien sur différentes périodes (jour/semaine/mois)
- Analyser les performances opérationnelles (retards, annulations, efficacité)
- Gérer les utilisateurs et leurs rôles
- Surveiller l'état des systèmes radar
- Consulter les alertes météorologiques globales (SIGMET/AIRMET)
- Examiner les logs d'activité système
- Visualiser et prioriser les alertes
- Générer des rapports et analytics

## 🏗️ Architecture

### Backend

#### Nouveaux Modèles

**ActivityLog** (`backend/src/main/java/com/flightradar/model/ActivityLog.java`)
- Modèle pour les logs d'activité système
- Types d'activités : LOGIN, LOGOUT, FLIGHT_CREATED, WEATHER_ALERT, etc.
- Niveaux de sévérité : INFO, WARNING, ERROR, CRITICAL

#### Nouveaux Repositories

**ActivityLogRepository** (`backend/src/main/java/com/flightradar/repository/ActivityLogRepository.java`)
- Méthodes de recherche avec filtres (utilisateur, type, sévérité, dates)
- Pagination supportée
- Requêtes optimisées avec index

#### Service Enrichi

**AdminDashboardService** (`backend/src/main/java/com/flightradar/service/AdminDashboardService.java`)

Nouvelles méthodes ajoutées :
- `getTrafficStatistics(String period)` - Statistiques de trafic par période
- `getPerformanceKPIs()` - KPIs de performance détaillés
- `getAllUsersWithStatus()` - Liste complète des utilisateurs avec statut
- `getRadarSystemsStatus()` - Statut détaillé des systèmes radar
- `getGlobalWeather()` - Météo globale avec alertes SIGMET/AIRMET
- `getActivityLogs(...)` - Journal d'activité avec filtres
- `getAllAlerts()` - Alertes consolidées
- `getReportsAnalytics(String period)` - Rapports et analytics

#### Nouveaux Endpoints

**AdminDashboardController** (`backend/src/main/java/com/flightradar/controller/AdminDashboardController.java`)

Tous les endpoints sont protégés par `@PreAuthorize("hasRole('ADMIN')")` :

- `GET /api/admin/operations/traffic?period={DAY|WEEK|MONTH}` - Statistiques de trafic
- `GET /api/admin/operations/performance` - KPIs de performance
- `GET /api/admin/operations/users` - Liste des utilisateurs
- `GET /api/admin/operations/radar-systems` - Statut des systèmes radar
- `GET /api/admin/operations/weather` - Météo globale
- `GET /api/admin/operations/logs?userId=&activityType=&severity=&startDate=&endDate=&page=&size=` - Journal d'activité
- `GET /api/admin/operations/alerts` - Alertes consolidées
- `GET /api/admin/operations/reports?period={DAY|WEEK|MONTH}` - Rapports et analytics

### Frontend

#### Nouveau Composant

**OperationsOverview** (`frontend/src/components/OperationsOverview.jsx`)
- Composant React modulaire avec navigation par onglets
- 8 sections distinctes (A à H)
- Graphiques interactifs avec Chart.js
- Tableaux triables et filtrables
- Pagination pour les logs
- Export CSV/PDF

#### Intégration dans AdminDashboard

Le composant `OperationsOverview` est intégré dans `AdminDashboard.jsx` avec un bouton de bascule entre la vue standard et la vue d'ensemble des opérations.

## 📊 Sections Détaillées

### A) Nombre total de vols / Trafic

**Fonctionnalités :**
- Sélection de période (Jour / Semaine / Mois)
- Affichage du nombre total de vols
- Répartition par statut (EN_COURS, PLANIFIE, TERMINE, etc.)
- Graphique d'évolution du trafic (courbe)

**Données affichées :**
- Total de vols sur la période
- Vols en cours
- Vols planifiés
- Graphique temporel

### B) KPI de Performance

**Fonctionnalités :**
- Retards moyens et totaux
- Nombre de vols annulés
- Efficacité opérationnelle (% vols à l'heure)
- Graphiques synthétiques (camembert, barres)

**KPIs affichés :**
- Retard moyen (minutes)
- Retard total (minutes)
- Vols annulés
- Efficacité opérationnelle (%)
- Taux de ponctualité
- Graphique de répartition (camembert)

### C) Utilisateurs / Rôles

**Fonctionnalités :**
- Liste complète des utilisateurs
- Statut actif/inactif
- Droits d'accès et rôle assigné
- Recherche et filtrage
- Codes couleurs par rôle

**Filtres disponibles :**
- Recherche par nom d'utilisateur
- Filtre par statut (Actif/Inactif)
- Filtre par rôle (ADMIN, PILOTE, CENTRE_RADAR)

### D) Systèmes Radar / Infrastructure

**Fonctionnalités :**
- Statut des radars primaires et secondaires
- Indicateurs de disponibilité/panne
- Charge des systèmes
- Codes couleurs (vert/rouge/orange)

**Informations affichées :**
- Nom et code du radar
- Aéroport associé
- Nombre d'avions suivis
- Charge du système (%)
- Statut de santé (HEALTHY/WARNING/CRITICAL)
- Disponibilité

### E) Météo Globale

**Fonctionnalités :**
- Alertes météo actives par zone
- Indicateurs critiques (vents forts, turbulences, visibilité basse)
- Classification SIGMET/AIRMET
- Codes couleurs standard aviation

**Données affichées :**
- Nombre de stations météo
- Alertes actives
- Vents forts
- Liste des alertes SIGMET/AIRMET avec détails

### F) Journal / Logs

**Fonctionnalités :**
- Logs de connexion et d'accès
- Logs d'actions critiques
- Filtrage par date, utilisateur, type d'action, sévérité
- Pagination
- Codes couleurs d'importance

**Filtres disponibles :**
- User ID
- Type d'activité (LOGIN, LOGOUT, FLIGHT_CREATED, etc.)
- Sévérité (INFO, WARNING, ERROR, CRITICAL)
- Date de début
- Date de fin

### G) Alertes & Notifications

**Fonctionnalités :**
- Problèmes radar
- Anomalies de performance
- Incidents remontés via PIREP ou alertes météo
- Priorisation par criticité (haute, moyenne, basse)
- Codes couleurs

**Types d'alertes :**
- Alertes météo
- Surcharge radar
- Dégradation de performance
- Tri automatique par criticité

### H) Rapports / Analytics

**Fonctionnalités :**
- Rapports d'activité journaliers, hebdomadaires, mensuels
- Tendances (volume de trafic, incidents, retards)
- Graphiques interactifs
- Export CSV/PDF

**Données incluses :**
- Statistiques de trafic
- KPIs de performance
- Tendances et évolutions
- Export au format CSV ou PDF

## 🗄️ Base de Données

### Nouvelle Table

**activity_logs**
```sql
CREATE TABLE activity_logs (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT,
    username VARCHAR(50),
    activity_type VARCHAR(50) NOT NULL,
    description TEXT NOT NULL,
    entity_type VARCHAR(50),
    entity_id BIGINT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    severity VARCHAR(20) NOT NULL DEFAULT 'INFO',
    ip_address VARCHAR(45)
);
```

**Index créés :**
- `idx_activity_logs_timestamp` - Pour les requêtes temporelles
- `idx_activity_logs_user_id` - Pour les filtres par utilisateur
- `idx_activity_logs_activity_type` - Pour les filtres par type
- `idx_activity_logs_severity` - Pour les filtres par sévérité

**Script SQL :** `backend/database/add_activity_logs_table.sql`

## 🔒 Sécurité

- Tous les endpoints sont protégés par `@PreAuthorize("hasRole('ADMIN')")`
- Seuls les utilisateurs avec le rôle ADMIN peuvent accéder à ces fonctionnalités
- Les données sensibles sont filtrées selon les permissions

## 🎨 Design

Le design est inspiré des dashboards internationaux :
- **FlightAware** - Pour la clarté et la modernité
- **Eurocontrol** - Pour les codes couleurs standard aviation
- **FAA Ops Dashboard** - Pour la structure et l'organisation

**Caractéristiques :**
- Interface sombre (bg-gray-900) pour réduire la fatigue visuelle
- Codes couleurs intuitifs :
  - Vert : Opérationnel, Actif, Healthy
  - Jaune/Orange : Avertissement, Retard
  - Rouge : Critique, Erreur, Inactif
  - Bleu : Information, Neutre
- Responsive design (mobile, tablette, desktop)
- Navigation par onglets pour une organisation claire

## 📝 Utilisation

### Accès à la Vue d'ensemble

1. Se connecter en tant qu'administrateur
2. Accéder au dashboard Admin
3. Cliquer sur le bouton **"Vue d'ensemble des Opérations"** dans le header
4. Naviguer entre les sections via les onglets (A à H)

### Export de Données

**Export CSV :**
- Section H (Rapports)
- Cliquer sur "Export CSV"
- Le fichier est téléchargé automatiquement

**Export PDF :**
- Section H (Rapports)
- Cliquer sur "Export PDF"
- Utilise la fonction d'impression du navigateur

### Filtrage des Logs

1. Accéder à la section F (Journal/Logs)
2. Remplir les filtres souhaités :
   - User ID
   - Type d'activité
   - Sévérité
   - Dates (début et fin)
3. Les résultats sont automatiquement filtrés
4. Utiliser la pagination pour naviguer

## 🔄 Rafraîchissement des Données

- Rafraîchissement automatique toutes les 30 secondes
- Rafraîchissement manuel possible en changeant d'onglet
- Les données sont mises à jour en temps réel

## 🚀 Déploiement

### Prérequis

1. Exécuter le script SQL pour créer la table `activity_logs` :
   ```bash
   psql -U postgres -d flightradar -f backend/database/add_activity_logs_table.sql
   ```

2. Redémarrer le backend Spring Boot

3. Le frontend détecte automatiquement les nouveaux endpoints

### Vérification

1. Vérifier que la table `activity_logs` existe dans la base de données
2. Vérifier que les endpoints `/api/admin/operations/*` répondent correctement
3. Tester l'accès à la vue d'ensemble depuis le dashboard Admin

## 📚 Références

- **ICAO Annex 3** - Standards pour météo et alertes aéronautiques (SIGMET/AIRMET)
- **FlightAware** - Dashboard de suivi aérien
- **Eurocontrol** - Dashboard opérationnel européen
- **FAA Ops Dashboard** - Dashboard opérationnel américain

## 🔧 Maintenance

### Ajout de nouveaux types de logs

Pour ajouter un nouveau type d'activité dans les logs :

1. Ajouter la valeur dans l'enum `ActivityLog.ActivityType`
2. Mettre à jour la contrainte CHECK dans la table SQL si nécessaire
3. Utiliser le nouveau type lors de la création de logs

### Ajout de nouvelles métriques

Pour ajouter de nouvelles métriques dans les KPIs :

1. Ajouter la méthode dans `AdminDashboardService`
2. Ajouter l'endpoint dans `AdminDashboardController`
3. Mettre à jour le composant `OperationsOverview` pour afficher la nouvelle métrique

## ⚠️ Notes Importantes

- **Ne pas modifier les fonctionnalités existantes** : La vue d'ensemble est un module séparé qui n'altère pas les fonctionnalités existantes
- **Performance** : Les requêtes sont optimisées avec des index, mais pour de très grandes quantités de données, envisager une pagination plus agressive
- **Logs** : Les logs sont stockés indéfiniment. Envisager une politique de rétention si nécessaire

## 📞 Support

Pour toute question ou problème :
1. Vérifier les logs du backend
2. Vérifier la console du navigateur
3. Vérifier que la table `activity_logs` existe et est accessible
4. Vérifier les permissions ADMIN de l'utilisateur

