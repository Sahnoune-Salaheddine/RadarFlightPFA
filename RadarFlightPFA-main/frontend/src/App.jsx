import React from 'react'
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom'
import Login from './components/Login'
import Dashboard from './components/Dashboard'
import PilotDashboard from './pages/PilotDashboard'
import AdminDashboard from './pages/AdminDashboard'
import RadarDashboard from './pages/RadarDashboard'
import { AuthProvider, useAuth } from './context/AuthContext'

// Composants qui utilisent useAuth - doivent être dans AuthProvider
function ProtectedRoute({ children }) {
  const { isAuthenticated, loading } = useAuth()
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-600">Chargement...</div>
      </div>
    )
  }
  
  return isAuthenticated ? children : <Navigate to="/login" />
}

function RoleBasedRoute() {
  const { user, loading } = useAuth()
  
  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-600">Chargement...</div>
      </div>
    )
  }
  
  // Normaliser le rôle pour la comparaison (en majuscules)
  const userRole = user?.role?.toUpperCase()
  
  // Debug: afficher le rôle pour diagnostiquer
  console.log('User role:', userRole, 'User:', user)
  
  if (userRole === 'PILOTE') {
    return <Navigate to="/pilot" replace />
  } else if (userRole === 'CENTRE_RADAR') {
    return <Navigate to="/radar" replace />
  } else if (userRole === 'ADMIN') {
    return <Navigate to="/admin" replace />
  } else {
    return <Dashboard />
  }
}

// AppRoutes doit être défini à l'intérieur de App pour avoir accès au contexte
// Composant pour gérer la route login (peut être affichée même si connecté)
function LoginRoute() {
  const { isAuthenticated, loading } = useAuth()
  
  // Toujours afficher le login, même si connecté
  // L'utilisateur peut se déconnecter depuis le login si nécessaire
  return <Login />
}

function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginRoute />} />
      <Route 
        path="/" 
        element={
          <ProtectedRoute>
            <RoleBasedRoute />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/pilot" 
        element={
          <ProtectedRoute>
            <PilotDashboard />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/radar" 
        element={
          <ProtectedRoute>
            <RadarDashboard />
          </ProtectedRoute>
        } 
      />
      <Route 
        path="/admin" 
        element={
          <ProtectedRoute>
            <AdminDashboard />
          </ProtectedRoute>
        } 
      />
    </Routes>
  )
}

function App() {
  return (
    <AuthProvider>
      <Router>
        <AppRoutes />
      </Router>
    </AuthProvider>
  )
}

export default App

