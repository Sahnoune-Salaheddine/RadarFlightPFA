import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json'
  },
  timeout: 10000 // 10 secondes de timeout
})

// Intercepteur de requête pour ajouter le token JWT à chaque requête
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Intercepteur pour gérer les erreurs de connexion
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Erreur de connexion réseau (backend non accessible)
    if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK' || 
        (error.message && error.message.includes('Network Error'))) {
      console.error('❌ Erreur de connexion au serveur. Vérifiez que le backend est démarré sur http://localhost:8080')
      // Ne pas rejeter pour permettre au composant de gérer l'erreur gracieusement
    } else if (error.code === 'ECONNABORTED') {
      // Timeout
      console.warn('⏱️ Timeout lors de l\'appel API (10s)')
    } else if (error.response) {
      // Erreur HTTP (4xx, 5xx) - le backend répond mais avec une erreur
      // Ne pas logger en erreur, c'est une erreur métier normale
      if (error.response.status >= 500) {
        console.error('❌ Erreur serveur:', error.response.status, error.response.data)
      } else if (error.response.status === 401) {
        console.warn('🔒 Non authentifié - redirection vers login')
        // Supprimer le token invalide
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        localStorage.removeItem('role')
        delete api.defaults.headers.common['Authorization']
        // Rediriger vers login si on est dans le navigateur
        if (typeof window !== 'undefined') {
          window.location.href = '/login'
        }
      } else if (error.response.status === 403) {
        console.warn('🔒 Accès refusé - vérifiez vos permissions')
        // Si c'est une erreur 403, le token peut être valide mais les permissions insuffisantes
        // Ou le token peut être expiré/invalide
        const errorMessage = error.response.data?.message || error.response.data?.error || 'Accès refusé'
        console.error('Erreur 403:', errorMessage)
      } else if (error.response.status === 404) {
        // 404 est normal pour certaines routes, ne pas logger en erreur
        console.debug('ℹ️ Ressource non trouvée:', error.config.url)
      }
    } else {
      // Autre erreur
      console.error('❌ Erreur inconnue:', error.message)
    }
    return Promise.reject(error)
  }
)

export default api

