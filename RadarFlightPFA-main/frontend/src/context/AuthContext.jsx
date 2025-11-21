import React, { createContext, useContext, useState, useEffect } from 'react'
import api from '../services/api'

const AuthContext = createContext()

export function useAuth() {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [isAuthenticated, setIsAuthenticated] = useState(false)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const token = localStorage.getItem('token')
    const username = localStorage.getItem('username')
    const role = localStorage.getItem('role')
    
    if (token && username) {
      // Configurer le header d'autorisation avant la requête
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
      
      // Vérifier la validité du token en faisant une requête au backend
      // Si le token est invalide, on nettoie le localStorage
      api.get('/auth/validate')
        .then(() => {
          // Token valide
          setUser({ username, role })
          setIsAuthenticated(true)
          setLoading(false)
        })
        .catch(() => {
          // Token invalide ou expiré, nettoyer le localStorage
          localStorage.removeItem('token')
          localStorage.removeItem('username')
          localStorage.removeItem('role')
          delete api.defaults.headers.common['Authorization']
          setUser(null)
          setIsAuthenticated(false)
          setLoading(false)
        })
    } else {
      setLoading(false)
    }
  }, [])

  const login = async (username, password) => {
    try {
      const response = await api.post('/auth/login', { username, password })
      const { token, role } = response.data
      
      if (!token || !role) {
        return { success: false, error: 'Réponse invalide du serveur' }
      }
      
      localStorage.setItem('token', token)
      localStorage.setItem('username', username)
      localStorage.setItem('role', role)
      
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`
      
      setUser({ username, role })
      setIsAuthenticated(true)
      
      return { success: true }
    } catch (error) {
      // Gestion détaillée des erreurs
      if (error.response) {
        // Le serveur a répondu avec un code d'erreur
        const errorMessage = error.response.data?.error || 
                             error.response.data?.message || 
                             `Erreur ${error.response.status}: ${error.response.statusText}`
        console.error('Erreur de connexion:', errorMessage, error.response.data)
        return { success: false, error: errorMessage }
      } else if (error.request) {
        // La requête a été faite mais aucune réponse n'a été reçue
        console.error('Aucune réponse du serveur:', error.message)
        return { success: false, error: 'Impossible de contacter le serveur. Vérifiez que le backend est démarré sur http://localhost:8080' }
      } else {
        // Une erreur s'est produite lors de la configuration de la requête
        console.error('Erreur de configuration:', error.message)
        return { success: false, error: `Erreur: ${error.message}` }
      }
    }
  }

  const logout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('username')
    localStorage.removeItem('role')
    delete api.defaults.headers.common['Authorization']
    setUser(null)
    setIsAuthenticated(false)
  }

  const value = {
    user,
    isAuthenticated,
    loading,
    login,
    logout
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

