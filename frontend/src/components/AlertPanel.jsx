import React, { useState, useEffect } from 'react'
import api from '../services/api'

function AlertPanel() {
  const [weatherAlerts, setWeatherAlerts] = useState([])
  const [conflictAlerts, setConflictAlerts] = useState([])

  useEffect(() => {
    fetchAlerts()
    const interval = setInterval(fetchAlerts, 5000) // Toutes les 5 secondes
    return () => clearInterval(interval)
  }, [])

  const fetchAlerts = async () => {
    try {
      // Récupérer les alertes météo
      const weatherResponse = await api.get('/weather/alerts')
      setWeatherAlerts(weatherResponse.data || [])
      
      // Récupérer les alertes de conflit
      const conflictResponse = await api.get('/conflicts')
      setConflictAlerts(conflictResponse.data || [])
    } catch (error) {
      // Ignorer les erreurs silencieusement pour ne pas polluer la console
      if (error.response?.status !== 404) {
        console.error('Erreur lors du chargement des alertes:', error)
      }
    }
  }

  const allAlerts = [...weatherAlerts, ...conflictAlerts]

  if (allAlerts.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow-lg">
        <div className="p-4 border-b">
          <h2 className="text-xl font-semibold text-gray-800">Alertes</h2>
        </div>
        <div className="p-4">
          <p className="text-gray-500 text-sm">Aucune alerte active</p>
        </div>
      </div>
    )
  }

  return (
    <div className="bg-white rounded-lg shadow-lg">
      <div className="p-4 border-b bg-red-50">
        <h2 className="text-xl font-semibold text-red-800">⚠️ Alertes</h2>
      </div>
      <div className="p-4 space-y-3 max-h-96 overflow-y-auto">
        {/* Alertes météo */}
        {weatherAlerts.map(alert => (
          <div key={`weather-${alert.id}`} className="border-l-4 border-red-500 pl-3 py-2 bg-red-50 rounded">
            <h3 className="font-semibold text-red-800">🌤️ Météo - {alert.airport?.name || 'Aéroport inconnu'}</h3>
            <p className="text-sm text-red-700 mt-1">
              Conditions: {alert.conditions} | 
              Visibilité: {alert.visibility?.toFixed(1) || 0} km | 
              Vent: {alert.windSpeed?.toFixed(0) || 0} km/h
            </p>
          </div>
        ))}
        
        {/* Alertes de conflit */}
        {conflictAlerts.map((conflict, idx) => {
          const severity = conflict.conflictInfo?.severity || conflict.severity || 'MEDIUM'
          const severityColor = severity === 'CRITICAL' ? 'border-red-600' : 
                               severity === 'HIGH' ? 'border-orange-500' : 
                               'border-yellow-500'
          // Utiliser l'ID du conflit ou un identifiant unique
          const conflictKey = conflict.id || `conflict-${conflict.aircraft1?.id}-${conflict.aircraft2?.id}-${idx}`
          return (
            <div key={conflictKey} className={`border-l-4 ${severityColor} pl-3 py-2 bg-orange-50 rounded`}>
              <h3 className="font-semibold text-orange-800">
                ⚠️ Conflit de Trajectoire - {severity}
              </h3>
              <p className="text-sm text-orange-700 mt-1">
                Avion 1: {conflict.aircraft1?.registration || conflict.aircraft1?.id} | 
                Avion 2: {conflict.aircraft2?.registration || conflict.aircraft2?.id}
              </p>
              <p className="text-xs text-orange-600 mt-1">
                Distance: {(conflict.conflictInfo?.distance || conflict.distanceKm)?.toFixed(1) || 'N/A'} km | 
                Altitude diff: {(conflict.conflictInfo?.altitudeDiff || conflict.altitudeDifferenceMeters)?.toFixed(0) || 'N/A'} m
              </p>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default AlertPanel
