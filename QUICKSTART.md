# Guide de Démarrage Rapide

## 🚀 Démarrage en 5 minutes

### 1. Prérequis
- PostgreSQL installé et démarré
- Java 17+
- Maven 3.6+
- Node.js 18+

### 2. Base de données
```sql
CREATE DATABASE flightradar;
```

### 3. Backend
```bash
cd backend
mvn spring-boot:run
```

### 4. Frontend
```bash
cd frontend
npm install
npm run dev
```

### 5. Accès
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080/api

### 6. Connexion
- **Admin**: `admin` / `admin123`
- **Pilote**: `pilote_cmn1` / `pilote123`
- **Radar**: `radar_cmn` / `radar123`

## ⚙️ Configuration optionnelle

### API Météo
Ajoutez votre clé OpenWeatherMap dans `backend/src/main/resources/application.properties`:
```properties
weather.api.key=votre-cle-api
```

Sans clé API, des données météorologiques par défaut seront utilisées.

## 🐛 Problèmes courants

**Port 8080 déjà utilisé?**
- Changez le port dans `application.properties`: `server.port=8081`

**Erreur de connexion PostgreSQL?**
- Vérifiez que PostgreSQL est démarré
- Vérifiez les identifiants dans `application.properties`

**Frontend ne charge pas les données?**
- Vérifiez que le backend est démarré
- Vérifiez la console du navigateur pour les erreurs CORS

