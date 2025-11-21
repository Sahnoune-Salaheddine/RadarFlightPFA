import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet'
import L from 'leaflet'
import api from '../services/api'

// Fix pour les icônes Leaflet
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

function PilotDashboard() {
  const { user, logout } = useAuth()
  const [dashboardData, setDashboardData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [clearanceStatus, setClearanceStatus] = useState(null)
  const [requestingClearance, setRequestingClearance] = useState(false)

  // Récupérer toutes les données du dashboard
  useEffect(() => {
    if (!user?.username) {
      setLoading(false)
      return
    }
    
    fetchDashboardData()
    const interval = setInterval(fetchDashboardData, 5000) // Rafraîchir toutes les 5 secondes
    return () => clearInterval(interval)
  }, [user?.username])

  const fetchDashboardData = async () => {
    if (!user?.username) return
    
    try {
      const response = await api.get(`/pilots/${user.username}/dashboard`)
      setDashboardData(response.data)
      setLoading(false)
    } catch (error) {
      console.error('Erreur chargement dashboard:', error)
      if (error.response?.status === 404) {
        console.log('Aucun avion assigné à ce pilote')
      }
      setLoading(false)
    }
  }

  const requestTakeoffClearance = async () => {
    if (!dashboardData || requestingClearance) return
    
    setRequestingClearance(true)
    setClearanceStatus(null)
    
    try {
      // Récupérer l'ID de l'avion depuis les données du dashboard
      // On doit faire une requête pour obtenir l'avion
      const aircraftResponse = await api.get(`/aircraft/pilot/${user.username}`)
      const aircraftId = aircraftResponse.data.id
      
      const response = await api.post('/atc/request-takeoff-clearance', {
        aircraftId: aircraftId
      })
      
      setClearanceStatus(response.data)
      
      // Rafraîchir les données après la demande
      setTimeout(fetchDashboardData, 1000)
    } catch (error) {
      console.error('Erreur demande décollage:', error)
      setClearanceStatus({
        status: 'ERROR',
        message: 'Erreur lors de la demande d\'autorisation',
        details: error.response?.data?.message || 'Erreur inconnue'
      })
    } finally {
      setRequestingClearance(false)
    }
  }

  const formatTime = (dateTime) => {
    if (!dateTime) return 'N/A'
    return new Date(dateTime).toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }

  const formatDate = (dateTime) => {
    if (!dateTime) return 'N/A'
    return new Date(dateTime).toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'En vol':
      case 'GRANTED':
        return 'text-green-400'
      case 'Au sol':
      case 'PENDING':
        return 'text-yellow-400'
      case 'Atterrissage':
      case 'REFUSED':
        return 'text-red-400'
      default:
        return 'text-gray-400'
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-xl">Chargement...</div>
      </div>
    )
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-center">
          <div className="text-white text-xl mb-2">Aucun avion assigné</div>
          <div className="text-gray-400 text-sm">Contactez l'administrateur pour assigner un avion</div>
        </div>
      </div>
    )
  }

  const mapCenter = dashboardData.latitude && dashboardData.longitude
    ? [dashboardData.latitude, dashboardData.longitude]
    : [33.5731, -7.5898]

  // Trajectoire pour la carte
  const trajectory = dashboardData.trajectory || []

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Header */}
      <header className="bg-gray-800 border-b border-gray-700 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold">Dashboard Pilote</h1>
              <p className="text-sm text-gray-400">
                {dashboardData.flightNumber} - {dashboardData.airline} | {dashboardData.aircraftType}
              </p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm font-medium">{user?.username}</p>
                <p className="text-xs text-gray-400">{user?.role}</p>
              </div>
              <button
                onClick={logout}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition"
              >
                Déconnexion
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Colonne 1 : Carte et Informations principales */}
          <div className="lg:col-span-2 space-y-6">
            {/* ========== 1. Informations générales du vol ========== */}
            <div className="bg-gray-800 rounded-lg shadow-lg p-6">
              <h2 className="text-xl font-semibold mb-4 border-b border-gray-700 pb-2">
                Informations Générales du Vol
              </h2>
              <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div>
                  <p className="text-xs text-gray-400 mb-1">Numéro de vol</p>
                  <p className="text-lg font-mono font-semibold">{dashboardData.flightNumber || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Compagnie</p>
                  <p className="text-lg font-semibold">{dashboardData.airline || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Type d'avion</p>
                  <p className="text-lg font-semibold">{dashboardData.aircraftType || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Départ</p>
                  <p className="text-sm font-semibold">{dashboardData.departureAirport || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Arrivée</p>
                  <p className="text-sm font-semibold">{dashboardData.arrivalAirport || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Route</p>
                  <p className="text-sm font-mono text-green-400">{dashboardData.route || 'N/A'}</p>
                </div>
              </div>
            </div>

            {/* ========== 2. Position & Mouvement (ADS-B) ========== */}
            <div className="bg-gray-800 rounded-lg shadow-lg overflow-hidden">
              <div className="p-4 border-b border-gray-700">
                <h2 className="text-xl font-semibold">Position & Mouvement (ADS-B)</h2>
              </div>
              <div className="h-96">
                <MapContainer
                  center={mapCenter}
                  zoom={dashboardData.latitude && dashboardData.longitude ? 12 : 6}
                  style={{ height: '100%', width: '100%' }}
                >
                  <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; OpenStreetMap'
                  />
                  {dashboardData.latitude && dashboardData.longitude && (
                    <Marker position={[dashboardData.latitude, dashboardData.longitude]}>
                      <Popup>
                        <div className="text-black">
                          <h3 className="font-bold">{dashboardData.flightNumber}</h3>
                          <p>Altitude: {dashboardData.altitudeFeet?.toFixed(0) || 0} ft</p>
                          <p>Vitesse sol: {dashboardData.groundSpeed?.toFixed(0) || 0} km/h</p>
                          <p>Vitesse air: {dashboardData.airSpeed?.toFixed(0) || 0} km/h</p>
                          <p>Cap: {dashboardData.heading?.toFixed(0) || 0}°</p>
                          <p>Montée/Descente: {dashboardData.verticalSpeed?.toFixed(1) || 0} m/s</p>
                        </div>
                      </Popup>
                    </Marker>
                  )}
                  {trajectory.length > 1 && (
                    <Polyline
                      positions={trajectory.map(p => [p.latitude, p.longitude])}
                      color="#3b82f6"
                      weight={2}
                    />
                  )}
                </MapContainer>
              </div>
              <div className="p-4 grid grid-cols-2 md:grid-cols-4 gap-4 bg-gray-850">
                <div>
                  <p className="text-xs text-gray-400 mb-1">Latitude</p>
                  <p className="text-sm font-mono">{dashboardData.latitude?.toFixed(6) || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Longitude</p>
                  <p className="text-sm font-mono">{dashboardData.longitude?.toFixed(6) || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Altitude</p>
                  <p className="text-sm font-mono text-green-400">
                    {dashboardData.altitudeFeet?.toFixed(0) || 0} ft
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Cap</p>
                  <p className="text-sm font-mono text-green-400">
                    {dashboardData.heading?.toFixed(0) || 0}°
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Vitesse sol</p>
                  <p className="text-sm font-mono text-green-400">
                    {dashboardData.groundSpeed?.toFixed(0) || 0} km/h
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Vitesse air</p>
                  <p className="text-sm font-mono text-green-400">
                    {dashboardData.airSpeed?.toFixed(0) || 0} km/h
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Montée/Descente</p>
                  <p className={`text-sm font-mono ${
                    dashboardData.verticalSpeed > 0 ? 'text-green-400' : 
                    dashboardData.verticalSpeed < 0 ? 'text-red-400' : 'text-gray-400'
                  }`}>
                    {dashboardData.verticalSpeed?.toFixed(1) || 0} m/s
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Transpondeur</p>
                  <p className="text-sm font-mono text-blue-400">
                    {dashboardData.transponderCode || 'N/A'}
                  </p>
                </div>
              </div>
            </div>

            {/* ========== 3. Statut du vol ========== */}
            <div className="bg-gray-800 rounded-lg shadow-lg p-6">
              <h2 className="text-xl font-semibold mb-4 border-b border-gray-700 pb-2">Statut du Vol</h2>
              <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                <div>
                  <p className="text-xs text-gray-400 mb-1">Statut</p>
                  <p className={`text-lg font-semibold ${getStatusColor(dashboardData.flightStatus)}`}>
                    {dashboardData.flightStatus || 'N/A'}
                  </p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Départ prévu</p>
                  <p className="text-sm">{formatTime(dashboardData.scheduledDeparture)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Départ réel</p>
                  <p className="text-sm">{formatTime(dashboardData.actualDeparture)}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Arrivée prévue</p>
                  <p className="text-sm">{formatTime(dashboardData.scheduledArrival)}</p>
                </div>
                {dashboardData.delayMinutes > 0 && (
                  <div className="col-span-2 md:col-span-4">
                    <div className="bg-yellow-900 border border-yellow-700 rounded p-3">
                      <p className="text-sm text-yellow-200">
                        ⚠️ Retard: {dashboardData.delayMinutes} minutes
                      </p>
                    </div>
                  </div>
                )}
                <div>
                  <p className="text-xs text-gray-400 mb-1">Porte</p>
                  <p className="text-sm">{dashboardData.gate || 'N/A'}</p>
                </div>
                <div>
                  <p className="text-xs text-gray-400 mb-1">Piste</p>
                  <p className="text-sm">{dashboardData.runway || 'N/A'}</p>
                </div>
              </div>
            </div>

            {/* Bouton Demander Autorisation */}
            {dashboardData.flightStatus === 'Au sol' && (
              <div className="bg-gray-800 rounded-lg shadow-lg p-6">
                <h2 className="text-xl font-semibold mb-4">Autorisation de Décollage</h2>
                <button
                  onClick={requestTakeoffClearance}
                  disabled={requestingClearance}
                  className="w-full bg-green-600 hover:bg-green-700 disabled:bg-gray-600 text-white py-3 px-4 rounded-lg transition font-semibold text-lg"
                >
                  {requestingClearance ? 'Envoi en cours...' : '✈️ Demander Autorisation de Décollage'}
                </button>
                
                {clearanceStatus && (
                  <div className={`mt-4 p-4 rounded-lg border ${
                    clearanceStatus.status === 'GRANTED' ? 'bg-green-900 border-green-700' :
                    clearanceStatus.status === 'REFUSED' ? 'bg-red-900 border-red-700' :
                    clearanceStatus.status === 'PENDING' ? 'bg-yellow-900 border-yellow-700' :
                    'bg-gray-800 border-gray-700'
                  }`}>
                    <p className={`font-semibold mb-2 ${
                      clearanceStatus.status === 'GRANTED' ? 'text-green-200' :
                      clearanceStatus.status === 'REFUSED' ? 'text-red-200' :
                      clearanceStatus.status === 'PENDING' ? 'text-yellow-200' :
                      'text-gray-200'
                    }`}>
                      {clearanceStatus.status === 'GRANTED' && '✅ Autorisation Accordée'}
                      {clearanceStatus.status === 'REFUSED' && '❌ Autorisation Refusée'}
                      {clearanceStatus.status === 'PENDING' && '⏳ En Attente'}
                      {clearanceStatus.status === 'ERROR' && '⚠️ Erreur'}
                    </p>
                    <p className="text-sm mb-1">{clearanceStatus.message}</p>
                    {clearanceStatus.details && (
                      <p className="text-xs opacity-75">{clearanceStatus.details}</p>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>

          {/* Colonne 2 : Panneaux latéraux */}
          <div className="space-y-6">
            {/* ========== 4. Météo du vol ========== */}
            {dashboardData.weather && (
              <div className="bg-gray-800 rounded-lg shadow-lg p-6">
                <h2 className="text-xl font-semibold mb-4 border-b border-gray-700 pb-2">Météo du Vol</h2>
                <div className="space-y-3">
                  <div className="flex justify-between">
                    <span className="text-gray-400">Vent</span>
                    <span className="font-semibold">
                      {dashboardData.weather.windSpeed?.toFixed(0) || 0} km/h
                      {dashboardData.weather.windDirection && ` à ${dashboardData.weather.windDirection.toFixed(0)}°`}
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-400">Visibilité</span>
                    <span className="font-semibold">
                      {dashboardData.weather.visibility?.toFixed(1) || 0} km
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-400">Précipitations</span>
                    <span className="font-semibold">{dashboardData.weather.precipitation || 'N/A'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-400">Turbulence</span>
                    <span className="font-semibold">{dashboardData.weather.turbulence || 'N/A'}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-400">Température</span>
                    <span className="font-semibold">
                      {dashboardData.weather.temperature?.toFixed(1) || 'N/A'}°C
                    </span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-400">Pression</span>
                    <span className="font-semibold">
                      {dashboardData.weather.pressure?.toFixed(0) || 'N/A'} hPa
                    </span>
                  </div>
                  {dashboardData.weather.weatherAlerts && dashboardData.weather.weatherAlerts.length > 0 && (
                    <div className="mt-4 p-3 bg-red-900 border border-red-700 rounded">
                      <p className="text-sm font-semibold text-red-200 mb-2">⚠️ Alertes Météo</p>
                      <ul className="text-xs text-red-300 space-y-1">
                        {dashboardData.weather.weatherAlerts.map((alert, idx) => (
                          <li key={idx}>• {alert}</li>
                        ))}
                      </ul>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* ========== 5. Communications ATC ========== */}
            <div className="bg-gray-800 rounded-lg shadow-lg">
              <div className="p-4 border-b border-gray-700">
                <h2 className="text-xl font-semibold">Communications ATC</h2>
                {dashboardData.radarCenterName && (
                  <p className="text-xs text-gray-400 mt-1">Centre: {dashboardData.radarCenterName}</p>
                )}
              </div>
              <div className="p-4">
                {dashboardData.lastATCMessage && (
                  <div className="mb-4 p-3 bg-blue-900 border border-blue-700 rounded">
                    <p className="text-xs text-blue-300 mb-1">Dernier message ATC</p>
                    <p className="text-sm">{dashboardData.lastATCMessage}</p>
                  </div>
                )}
                
                {dashboardData.currentInstructions && dashboardData.currentInstructions.length > 0 && (
                  <div className="mb-4">
                    <p className="text-xs text-gray-400 mb-2">Instructions en cours</p>
                    <ul className="space-y-2">
                      {dashboardData.currentInstructions.map((instruction, idx) => (
                        <li key={idx} className="text-sm p-2 bg-gray-700 rounded">
                          {instruction}
                        </li>
                      ))}
                    </ul>
                  </div>
                )}
                
                <div className="h-64 overflow-y-auto space-y-2">
                  {dashboardData.atcHistory && dashboardData.atcHistory.length > 0 ? (
                    dashboardData.atcHistory.map((msg, idx) => (
                      <div
                        key={idx}
                        className={`border-l-4 pl-3 py-2 rounded ${
                          msg.sender === 'ATC' 
                            ? 'border-blue-500 bg-blue-900 bg-opacity-30' 
                            : 'border-green-500 bg-green-900 bg-opacity-30'
                        }`}
                      >
                        <div className="flex justify-between mb-1">
                          <span className={`text-xs font-semibold ${
                            msg.sender === 'ATC' ? 'text-blue-300' : 'text-green-300'
                          }`}>
                            {msg.sender}
                          </span>
                          <span className="text-xs text-gray-400">
                            {formatTime(msg.timestamp)}
                          </span>
                        </div>
                        <p className="text-sm">{msg.message}</p>
                      </div>
                    ))
                  ) : (
                    <p className="text-gray-500 text-sm text-center py-4">Aucun message</p>
                  )}
                </div>
              </div>
            </div>

            {/* ========== 6. Sécurité / Suivi ADS-B ========== */}
            {dashboardData.alerts && dashboardData.alerts.length > 0 && (
              <div className="bg-gray-800 rounded-lg shadow-lg p-6">
                <h2 className="text-xl font-semibold mb-4 border-b border-gray-700 pb-2">Alertes Sécurité</h2>
                <div className="space-y-2">
                  {dashboardData.alerts.map((alert, idx) => (
                    <div
                      key={idx}
                      className={`p-3 rounded border ${
                        alert.severity === 'CRITICAL' ? 'bg-red-900 border-red-700' :
                        alert.severity === 'HIGH' ? 'bg-orange-900 border-orange-700' :
                        alert.severity === 'MEDIUM' ? 'bg-yellow-900 border-yellow-700' :
                        'bg-gray-700 border-gray-600'
                      }`}
                    >
                      <div className="flex justify-between mb-1">
                        <span className="text-xs font-semibold">{alert.type}</span>
                        <span className="text-xs opacity-75">{formatTime(alert.timestamp)}</span>
                      </div>
                      <p className="text-sm">{alert.message}</p>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* ========== 7. KPIs ========== */}
            {dashboardData.kpis && (
              <div className="bg-gray-800 rounded-lg shadow-lg p-6">
                <h2 className="text-xl font-semibold mb-4 border-b border-gray-700 pb-2">KPIs Temps Réel</h2>
                <div className="space-y-4">
                  {/* KPIs Temps Réel */}
                  <div>
                    <p className="text-xs text-gray-400 mb-2">Temps Réel</p>
                    <div className="space-y-2">
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Distance restante</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.remainingDistance?.toFixed(1) || 'N/A'} km
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">ETA</span>
                        <span className="text-sm font-semibold">
                          {formatTime(dashboardData.kpis.estimatedArrival)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Carburant estimé</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.estimatedFuelConsumption?.toFixed(1) || 'N/A'} L
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Niveau carburant</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.fuelLevel?.toFixed(0) || 'N/A'}%
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Vitesse moyenne</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.averageSpeed?.toFixed(0) || 'N/A'} km/h
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Altitude stable</span>
                        <span className={`text-sm font-semibold ${
                          dashboardData.kpis.stableAltitude ? 'text-green-400' : 'text-red-400'
                        }`}>
                          {dashboardData.kpis.stableAltitude ? 'Oui' : 'Non'}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Turbulence</span>
                        <span className={`text-sm font-semibold ${
                          dashboardData.kpis.turbulenceDetected ? 'text-red-400' : 'text-green-400'
                        }`}>
                          {dashboardData.kpis.turbulenceDetected ? 'Détectée' : 'Aucune'}
                        </span>
                      </div>
                    </div>
                  </div>

                  {/* KPIs Radar / Sécurité */}
                  <div className="pt-4 border-t border-gray-700">
                    <p className="text-xs text-gray-400 mb-2">Radar / Sécurité</p>
                    <div className="space-y-2">
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Sévérité météo</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.weatherSeverity || 0}%
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Risque trajectoire</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.trajectoryRiskIndex || 0}/100
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Densité trafic (30km)</span>
                        <span className="text-sm font-semibold">
                          {dashboardData.kpis.trafficDensity30km || 0} avions
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-sm text-gray-400">Score santé avion</span>
                        <span className={`text-sm font-semibold ${
                          (dashboardData.kpis.aircraftHealthScore || 0) >= 80 ? 'text-green-400' :
                          (dashboardData.kpis.aircraftHealthScore || 0) >= 50 ? 'text-yellow-400' :
                          'text-red-400'
                        }`}>
                          {dashboardData.kpis.aircraftHealthScore || 0}/100
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  )
}

export default PilotDashboard
