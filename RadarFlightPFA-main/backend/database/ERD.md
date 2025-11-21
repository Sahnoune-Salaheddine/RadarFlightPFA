# Diagramme Entité-Relation (ERD) - Flight Radar

## 📊 Vue d'ensemble

```
┌─────────────┐
│    Users    │
│─────────────│
│ id (PK)     │
│ username    │
│ password    │
│ role        │
└──────┬──────┘
       │
       │ 1:1
       │
┌──────▼──────────┐      ┌──────────────┐
│    Pilots       │      │ RadarCenters │
│─────────────────│      │──────────────│
│ id (PK)         │      │ id (PK)      │
│ name            │      │ name         │
│ license         │      │ code         │
│ experience_years│      │ frequency    │
│ user_id (FK)    │      │ airport_id   │
└──────┬──────────┘      │ user_id (FK) │
       │                └──────┬───────┘
       │ 1:N                   │ 1:1
       │                       │
┌──────▼──────────┐      ┌──────▼──────────┐
│   Aircraft      │      │   Airports     │
│─────────────────│      │────────────────│
│ id (PK)         │      │ id (PK)        │
│ model           │      │ name           │
│ registration    │      │ city           │
│ status          │      │ code_iata      │
│ airport_id (FK) │◄─────┤ latitude       │
│ pilot_id (FK)   │      │ longitude      │
│ position_lat    │      └──────┬─────────┘
│ position_lon    │             │ 1:N
│ altitude        │      ┌──────▼──────────┐
│ speed           │      │    Runways      │
│ heading         │      │─────────────────│
└──────┬──────────┘      │ id (PK)         │
       │ 1:N            │ name            │
       │                │ orientation     │
┌──────▼──────────┐      │ length_meters   │
│    Flights      │      │ width_meters    │
│─────────────────│      │ airport_id (FK) │
│ id (PK)         │      └─────────────────┘
│ flight_number   │
│ aircraft_id (FK)│      ┌──────────────┐
│ departure_id    │      │ WeatherData  │
│ arrival_id      │      │──────────────│
│ flight_status   │      │ id (PK)      │
└─────────────────┘      │ airport_id   │
                         │ wind_speed   │
┌─────────────────┐      │ wind_dir     │
│ Communications  │      │ visibility   │
│─────────────────│      │ temperature  │
│ id (PK)         │      │ humidity     │
│ sender_type     │      │ conditions   │
│ sender_id       │      │ crosswind    │
│ receiver_type   │      │ alert        │
│ receiver_id     │      │ timestamp    │
│ message         │      └──────────────┘
│ frequency       │
│ timestamp       │
└─────────────────┘
```

## 🔗 Relations détaillées

### 1. Users ↔ Pilots (1:1)
- Un utilisateur peut être un pilote
- Un pilote a un compte utilisateur (optionnel)

### 2. Users ↔ RadarCenters (1:1)
- Un utilisateur peut être un opérateur radar
- Un centre radar a un compte utilisateur (optionnel)

### 3. Airports ↔ Runways (1:N)
- Un aéroport a plusieurs pistes
- Une piste appartient à un seul aéroport

### 4. Airports ↔ RadarCenters (1:1)
- Un aéroport a un seul centre radar
- Un centre radar appartient à un seul aéroport

### 5. Airports ↔ Aircraft (1:N)
- Un aéroport peut avoir plusieurs avions
- Un avion est basé à un aéroport

### 6. Pilots ↔ Aircraft (1:N)
- Un pilote peut piloter plusieurs avions (historique)
- Un avion a un pilote assigné

### 7. Aircraft ↔ Flights (1:N)
- Un avion peut effectuer plusieurs vols
- Un vol utilise un seul avion

### 8. Airports ↔ Flights (N:M via departure/arrival)
- Un aéroport peut être départ ou arrivée de plusieurs vols
- Un vol a un aéroport de départ et un d'arrivée

### 9. Airports ↔ WeatherData (1:N)
- Un aéroport a plusieurs enregistrements météo (historique)
- Une donnée météo appartient à un seul aéroport

### 10. Communications (Relations polymorphes)
- sender_type + sender_id : peut être RADAR, AIRCRAFT, ou AIRPORT
- receiver_type + receiver_id : peut être RADAR, AIRCRAFT, ou AIRPORT

## 📐 Cardinalités

| Relation | Type | Description |
|----------|------|-------------|
| Users → Pilots | 1:1 | Optionnel |
| Users → RadarCenters | 1:1 | Optionnel |
| Airports → Runways | 1:N | Obligatoire (au moins 1 piste) |
| Airports → RadarCenters | 1:1 | Obligatoire |
| Airports → Aircraft | 1:N | Optionnel |
| Pilots → Aircraft | 1:N | Optionnel |
| Aircraft → Flights | 1:N | Optionnel |
| Airports → WeatherData | 1:N | Historique temporel |
| Communications | Polymorphe | Relations flexibles |

## 🔑 Clés primaires et étrangères

### Clés primaires
- Toutes les tables ont un `id BIGSERIAL` comme clé primaire

### Clés étrangères importantes
- `aircraft.airport_id` → `airports.id`
- `aircraft.pilot_id` → `pilots.id`
- `flights.aircraft_id` → `aircraft.id`
- `flights.departure_airport_id` → `airports.id`
- `flights.arrival_airport_id` → `airports.id`
- `runways.airport_id` → `airports.id`
- `radar_centers.airport_id` → `airports.id`
- `weather_data.airport_id` → `airports.id`
- `pilots.user_id` → `users.id`
- `radar_centers.user_id` → `users.id`

## 📊 Contraintes d'intégrité

1. **Cascade de suppression** :
   - Supprimer un aéroport supprime ses pistes et son centre radar
   - Supprimer un avion supprime ses vols

2. **RESTRICT de suppression** :
   - Impossible de supprimer un aéroport utilisé comme départ/arrivée d'un vol actif

3. **SET NULL** :
   - Supprimer un utilisateur met à NULL les références dans pilots/radar_centers

4. **Contraintes CHECK** :
   - Status des avions : valeurs énumérées
   - Heading : 0-360 degrés
   - Wind direction : 0-360 degrés
   - Humidity : 0-100%
   - Runway orientation : 0-360 degrés

