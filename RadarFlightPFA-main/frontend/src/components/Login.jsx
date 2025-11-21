import React, { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

function Login() {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const { login, logout, isAuthenticated, loading, user } = useAuth()
  const navigate = useNavigate()

  // Gérer le paramètre logout dans l'URL
  useEffect(() => {
    // Vérifier si l'utilisateur veut vraiment se déconnecter (paramètre ?logout=true)
    const urlParams = new URLSearchParams(window.location.search)
    const forceLogout = urlParams.get('logout') === 'true'
    
    if (forceLogout && isAuthenticated) {
      // Forcer la déconnexion si le paramètre logout est présent
      logout()
      // Nettoyer l'URL
      window.history.replaceState({}, '', '/login')
    }
    // Ne plus rediriger automatiquement - laisser l'utilisateur voir la page login
    // même s'il est connecté, il peut choisir de se déconnecter
  }, [isAuthenticated, logout])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    
    const result = await login(username, password)
    
    if (result.success) {
      navigate('/')
    } else {
      setError(result.error)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-500 to-blue-700">
      <div className="bg-white p-8 rounded-lg shadow-xl w-full max-w-md">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-800">Flight Radar</h1>
          <p className="text-gray-600 mt-2">Connexion au système</p>
        </div>
        
        {/* Afficher le formulaire seulement si pas connecté */}
        {!loading && !isAuthenticated && (
          <form onSubmit={handleSubmit} className="space-y-6">
          {error && (
            <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
              {error}
            </div>
          )}
          
          <div>
            <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
              Nom d'utilisateur
            </label>
            <input
              id="username"
              type="text"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="admin, pilote_cmn1, radar_cmn, etc."
            />
          </div>
          
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
              Mot de passe
            </label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              placeholder="••••••••"
            />
          </div>
          
          <button
            type="submit"
            className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-200"
          >
            Se connecter
          </button>
        </form>
        )}
        
        {/* Afficher un message et des boutons si déjà connecté */}
        {!loading && isAuthenticated && (
          <div className="mt-4 space-y-3">
            <div className="bg-blue-50 border border-blue-200 text-blue-800 px-4 py-3 rounded">
              <p className="font-semibold mb-1">Vous êtes déjà connecté</p>
              <p className="text-sm">
                Connecté en tant que : <span className="font-semibold">{user?.username}</span> 
                {user?.role && <span> (Rôle: {user.role})</span>}
              </p>
            </div>
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => {
                  // Aller au dashboard selon le rôle
                  if (user?.role?.toUpperCase() === 'PILOTE') {
                    navigate('/pilot')
                  } else if (user?.role?.toUpperCase() === 'ADMIN') {
                    navigate('/admin')
                  } else if (user?.role?.toUpperCase() === 'CENTRE_RADAR') {
                    navigate('/radar')
                  } else {
                    navigate('/')
                  }
                }}
                className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition duration-200"
              >
                Aller au dashboard
              </button>
              <button
                type="button"
                onClick={() => {
                  logout()
                  setUsername('')
                  setPassword('')
                  setError('')
                }}
                className="flex-1 bg-red-600 text-white py-2 px-4 rounded-lg hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 transition duration-200"
              >
                Se déconnecter
              </button>
            </div>
          </div>
        )}
        
        <div className="mt-6 text-sm text-gray-600">
          <p className="font-semibold mb-2">Comptes de test :</p>
          <ul className="space-y-1 text-xs">
            <li>Admin: admin / admin123</li>
            <li>Pilote: pilote_cmn1 / pilote123</li>
            <li>Radar: radar_cmn / radar123</li>
          </ul>
        </div>
      </div>
    </div>
  )
}

export default Login

