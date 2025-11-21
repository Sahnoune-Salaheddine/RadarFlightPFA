import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { MapContainer, TileLayer, Marker, Popup, Circle } from 'react-leaflet'
import L from 'leaflet'
import api from '../services/api'

// Fix pour les icônes Leaflet
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

// Icône personnalisée pour les avions
const aircraftIcon = new L.Icon({
  iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-red.png',
  iconSize: [25, 41],
  iconAnchor: [12, 41],
  popupAnchor: [1, -34],
})

function RadarDashboard() {
  const { user, logout } = useAuth()
  const [dashboardData, setDashboardData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [mapCenter, setMapCenter] = useState([33.5731, -7.5898]) // Casablanca par défaut

  useEffect(() => {
    fetchDashboardData()
    const interval = setInterval(fetchDashboardData, 5000) // Rafraîchir toutes les 5 secondes
    return () => clearInterval(interval)
  }, [])

  const fetchDashboardData = async () => {
    try {
      const response = await api.get('/radar/dashboard')
      
      // Vérifier si la réponse contient une erreur
      if (response.data.error) {
        setError(response.data.error)
        setLoading(false)
        return
      }
      
      setDashboardData(response.data)
      setError(null)
      
      // Centrer la carte sur l'aéroport si on a des données
      if (response.data.airport) {
        setMapCenter([response.data.airport.latitude, response.data.airport.longitude])
      } else if (response.data.atis && response.data.atis.latitude) {
        setMapCenter([response.data.atis.latitude, response.data.atis.longitude])
      } else {
        // Utiliser les coordonnées par défaut (Casablanca)
        setMapCenter([33.5731, -7.5898])
      }
      
      setLoading(false)
    } catch (error) {
      console.error('Erreur chargement dashboard radar:', error)
      const errorMessage = error.response?.data?.error || 
                           error.response?.data?.message || 
                           error.message || 
                           'Erreur de connexion au serveur'
      setError(errorMessage)
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-xl">Chargement du dashboard radar...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="bg-gray-800 rounded-lg p-8 border border-gray-700 max-w-md">
          <h2 className="text-2xl font-bold text-red-400 mb-4">Erreur</h2>
          <p className="text-white mb-4">{error}</p>
          {error.includes('aéroport') && (
            <div className="text-gray-400 text-sm space-y-2">
              <p>Pour résoudre ce problème :</p>
              <ol className="list-decimal list-inside space-y-1 ml-2">
                <li>Vérifiez que votre compte a un aéroport associé</li>
                <li>Contactez un administrateur pour associer un aéroport à votre compte</li>
                <li>Ou utilisez l'API : <code className="bg-gray-700 px-2 py-1 rounded">PUT /api/auth/users/{'{id}'}</code></li>
              </ol>
            </div>
          )}
          <button
            onClick={() => {
              setError(null)
              setLoading(true)
              fetchDashboardData()
            }}
            className="mt-4 px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition"
          >
            Réessayer
          </button>
        </div>
      </div>
    )
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-xl">Aucune donnée disponible</div>
      </div>
    )
  }

  const aircraftInSector = dashboardData.aircraftInSector || []
  const atis = dashboardData.atis || {}
  const atcHistory = dashboardData.atcHistory || []

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Header */}
      <header className="bg-gray-800 border-b border-gray-700 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold">Dashboard Centre Radar</h1>
              <p className="text-gray-400 text-sm">Bienvenue, {user?.username}</p>
            </div>
            <button
              onClick={logout}
              className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg transition"
            >
              Déconnexion
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Carte Radar */}
          <div className="lg:col-span-2">
            <div className="bg-gray-800 rounded-lg p-4 border border-gray-700">
              <h2 className="text-xl font-bold mb-4">Carte Radar - Secteur (50 km)</h2>
              <div className="h-96 rounded-lg overflow-hidden">
                <MapContainer
                  center={mapCenter}
                  zoom={10}
                  style={{ height: '100%', width: '100%' }}
                >
                  <TileLayer
                    url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                    attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
                  />
                  
                  {/* Cercle du secteur (50 km) */}
                  <Circle
                    center={mapCenter}
                    radius={50000}
                    pathOptions={{ color: 'yellow', fillOpacity: 0.1, weight: 2 }}
                  />
                  
                  {/* Marqueurs des avions */}
                  {aircraftInSector.map((aircraft) => (
                    <Marker
                      key={aircraft.id}
                      position={[aircraft.latitude, aircraft.longitude]}
                      icon={aircraftIcon}
                    >
                      <Popup>
                        <div className="text-black">
                          <div className="font-bold">{aircraft.registration}</div>
                          <div className="text-sm">{aircraft.model}</div>
                          <div className="text-sm">Altitude: {Math.round(aircraft.altitudeFeet || 0)} ft</div>
                          <div className="text-sm">Vitesse: {Math.round(aircraft.speed || 0)} km/h</div>
                          <div className="text-sm">Cap: {Math.round(aircraft.heading || 0)}°</div>
                          <div className="text-sm">Distance: {Math.round(aircraft.distance || 0)} km</div>
                        </div>
                      </Popup>
                    </Marker>
                  ))}
                </MapContainer>
              </div>
              
              <div className="mt-4 text-sm text-gray-400">
                {aircraftInSector.length} avion(s) dans le secteur
              </div>
            </div>
          </div>

          {/* Panneau ATIS */}
          <div className="lg:col-span-1">
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700 mb-6">
              <h2 className="text-xl font-bold mb-4">ATIS</h2>
              <div className="space-y-3">
                <div>
                  <div className="text-gray-400 text-sm">Vent</div>
                  <div className="text-lg font-bold">{atis.vent || 'N/A'} km/h</div>
                </div>
                <div>
                  <div className="text-gray-400 text-sm">Visibilité</div>
                  <div className="text-lg font-bold">{atis.visibilité || 'N/A'} km</div>
                </div>
                <div>
                  <div className="text-gray-400 text-sm">Pression</div>
                  <div className="text-lg font-bold">{atis.pression || 'N/A'} hPa</div>
                </div>
                <div>
                  <div className="text-gray-400 text-sm">Température</div>
                  <div className="text-lg font-bold">{atis.temperature || 'N/A'} °C</div>
                </div>
                <div>
                  <div className="text-gray-400 text-sm">Conditions</div>
                  <div className="text-lg font-bold">{atis.conditions || 'N/A'}</div>
                </div>
                <div>
                  <div className="text-gray-400 text-sm">Piste en service</div>
                  <div className="text-lg font-bold">{atis.pisteEnService || 'N/A'}</div>
                </div>
              </div>
            </div>

            {/* Liste des avions */}
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <h2 className="text-xl font-bold mb-4">Avions dans le secteur</h2>
              <div className="space-y-2 max-h-64 overflow-y-auto">
                {aircraftInSector.length === 0 ? (
                  <div className="text-gray-400 text-sm">Aucun avion dans le secteur</div>
                ) : (
                  aircraftInSector.map((aircraft) => (
                    <div key={aircraft.id} className="bg-gray-700 rounded p-3">
                      <div className="font-bold">{aircraft.registration}</div>
                      <div className="text-sm text-gray-400">
                        {Math.round(aircraft.altitudeFeet || 0)} ft - {Math.round(aircraft.speed || 0)} km/h
                      </div>
                      <div className="text-xs text-gray-500">
                        Distance: {Math.round(aircraft.distance || 0)} km
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        </div>

        {/* Historique Communications ATC */}
        <div className="mt-6">
          <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
            <h2 className="text-xl font-bold mb-4">Historique Communications ATC</h2>
            <div className="space-y-2 max-h-64 overflow-y-auto">
              {atcHistory.length === 0 ? (
                <div className="text-gray-400 text-sm">Aucune communication</div>
              ) : (
                atcHistory.map((msg) => (
                  <div key={msg.id} className="bg-gray-700 rounded p-3">
                    <div className="flex justify-between items-start">
                      <div className="flex-1">
                        <div className="font-bold text-sm">{msg.type}</div>
                        <div className="text-sm mt-1">{msg.message}</div>
                        <div className="text-xs text-gray-400 mt-1">
                          {new Date(msg.timestamp).toLocaleString('fr-FR')}
                        </div>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default RadarDashboard

