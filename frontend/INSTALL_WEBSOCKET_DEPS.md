# Installation des dépendances WebSocket

Pour utiliser WebSocket dans le frontend, installer les dépendances suivantes :

```bash
cd frontend
npm install sockjs-client @stomp/stompjs
```

Ces dépendances sont nécessaires pour :
- `sockjs-client` : Client WebSocket compatible avec Spring WebSocket
- `@stomp/stompjs` : Client STOMP pour la messagerie

---

**Alternative** : Les dashboards fonctionnent aussi avec polling (toutes les 5 secondes) si WebSocket n'est pas configuré.

