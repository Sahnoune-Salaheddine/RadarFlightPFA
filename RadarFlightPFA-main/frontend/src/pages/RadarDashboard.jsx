import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
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
  const navigate = useNavigate()
  const [dashboardData, setDashboardData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [mapCenter, setMapCenter] = useState([33.5731, -7.5898]) // Casablanca par défaut
  const [time, setTime] = useState(0)
  const [selectedAircraft, setSelectedAircraft] = useState(null)

  useEffect(() => {
    const timeInterval = setInterval(() => setTime(t => t + 1), 50)
    return () => clearInterval(timeInterval)
  }, [])

  useEffect(() => {
    fetchDashboardData()
    const interval = setInterval(fetchDashboardData, 5000) // Rafraîchir toutes les 5 secondes
    return () => clearInterval(interval)
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

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
      <div className="min-h-screen bg-gray-950 text-white flex items-center justify-center relative overflow-hidden">
        {/* Animated Grid Background */}
        <div className="absolute inset-0 overflow-hidden">
          <div 
            className="absolute inset-0 opacity-20"
            style={{
              backgroundImage: `
                linear-gradient(rgba(0,255,200,0.1) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0,255,200,0.1) 1px, transparent 1px)
              `,
              backgroundSize: '50px 50px',
            }}
          />
        </div>
        <div className="relative z-10 text-cyan-400 text-xl border border-cyan-500/50 px-6 py-3 bg-gray-900/80 backdrop-blur-xl rounded-xl">
          LOADING RADAR DASHBOARD...
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-950 text-white flex items-center justify-center relative overflow-hidden">
        {/* Animated Grid Background */}
        <div className="absolute inset-0 overflow-hidden">
          <div 
            className="absolute inset-0 opacity-20"
            style={{
              backgroundImage: `
                linear-gradient(rgba(0,255,200,0.1) 1px, transparent 1px),
                linear-gradient(90deg, rgba(0,255,200,0.1) 1px, transparent 1px)
              `,
              backgroundSize: '50px 50px',
            }}
          />
        </div>
        <div className="relative z-10 bg-gray-900/80 backdrop-blur-xl rounded-2xl p-8 border border-red-500/30 max-w-md">
          <h2 className="text-2xl font-bold text-red-400 mb-4">ERROR</h2>
          <p className="text-white mb-4">{error}</p>
          {error.includes('aéroport') && (
            <div className="text-gray-400 text-sm space-y-2">
              <p>Pour résoudre ce problème :</p>
              <ol className="list-decimal list-inside space-y-1 ml-2">
                <li>Vérifiez que votre compte a un aéroport associé</li>
                <li>Contactez un administrateur pour associer un aéroport à votre compte</li>
              </ol>
            </div>
          )}
          <button
            onClick={() => {
              setError(null)
              setLoading(true)
              fetchDashboardData()
            }}
            className="mt-4 px-4 py-2 bg-cyan-600 hover:bg-cyan-700 rounded-lg transition"
          >
            RETRY
          </button>
        </div>
      </div>
    )
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-gray-950 text-white flex items-center justify-center relative overflow-hidden">
        <div className="relative z-10 text-cyan-400 text-xl border border-cyan-500/50 px-6 py-3 bg-gray-900/80 backdrop-blur-xl rounded-xl">
          NO DATA AVAILABLE
        </div>
      </div>
    )
  }

  const aircraftInSector = dashboardData.aircraftInSector || []
  const atis = dashboardData.atis || {}
  const atcHistory = dashboardData.atcHistory || {}

  // Calculer le pourcentage de progression pour chaque avion (basé sur la distance)
  const getAircraftProgress = (aircraft) => {
    if (!aircraft.distance) return 50
    // Plus la distance est grande, moins le pourcentage (simulation)
    return Math.max(10, Math.min(90, 100 - (aircraft.distance / 50) * 100))
  }

  return (
    <div className="min-h-screen bg-gray-950 text-white overflow-hidden relative">
      {/* Animated Grid Background */}
      <div className="absolute inset-0 overflow-hidden">
        <div 
          className="absolute inset-0 opacity-20"
          style={{
            backgroundImage: `
              linear-gradient(rgba(0,255,200,0.1) 1px, transparent 1px),
              linear-gradient(90deg, rgba(0,255,200,0.1) 1px, transparent 1px)
            `,
            backgroundSize: '50px 50px',
            transform: `perspective(500px) rotateX(60deg) translateY(${time % 50}px)`,
            transformOrigin: 'center top'
          }}
        />
      </div>

      {/* Glowing Orb */}
      <div 
        className="absolute top-1/2 left-1/2 w-96 h-96 rounded-full opacity-30 blur-3xl pointer-events-none"
        style={{
          background: 'radial-gradient(circle, rgba(0,255,200,0.4) 0%, transparent 70%)',
          transform: `translate(-50%, -50%) scale(${1 + Math.sin(time * 0.05) * 0.1})`
        }}
      />

      {/* Header */}
      <header className="relative z-[100] p-6 border-b border-cyan-500/30 bg-gray-900/80 backdrop-blur-xl sticky top-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="relative">
              <div className="w-12 h-12 rounded-xl bg-gradient-to-br from-cyan-400 to-emerald-500 flex items-center justify-center shadow-lg shadow-cyan-500/50">
                <svg className="w-7 h-7 text-gray-900" fill="currentColor" viewBox="0 0 24 24">
                  <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                </svg>
              </div>
              <div className="absolute -top-1 -right-1 w-3 h-3 bg-emerald-400 rounded-full animate-pulse"/>
            </div>
            <div>
              <h1 className="text-2xl font-bold bg-gradient-to-r from-cyan-400 to-emerald-400 bg-clip-text text-transparent">
                RADAR FLIGHT
              </h1>
              <p className="text-xs text-cyan-500/70 tracking-widest">AIR TRAFFIC CONTROL SYSTEM</p>
            </div>
          </div>
          
          <div className="flex items-center gap-6">
            <div className="text-right">
              <div className="text-2xl font-mono text-cyan-400">
                {new Date().toLocaleTimeString()}
              </div>
              <div className="text-xs text-gray-500">UTC+1 CASABLANCA</div>
            </div>
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-cyan-500 to-emerald-500 flex items-center justify-center">
              <span className="text-sm font-bold text-gray-900">{user?.username?.charAt(0).toUpperCase() || 'R'}</span>
            </div>
            <button
              onClick={handleLogout}
              type="button"
              className="px-4 py-2 bg-red-600/80 hover:bg-red-700 rounded-lg transition text-sm relative z-[101] pointer-events-auto"
            >
              LOGOUT
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <div className="relative z-10 p-6 grid grid-cols-12 gap-6 h-[calc(100vh-100px)]">
        
        {/* Left Panel - Flight List */}
        <div className="col-span-3 space-y-4 overflow-y-auto">
          <div className="bg-gray-900/80 backdrop-blur-xl rounded-2xl border border-cyan-500/30 p-4 shadow-xl shadow-cyan-500/10">
            <h2 className="text-sm font-semibold text-cyan-400 mb-4 flex items-center gap-2">
              <span className="w-2 h-2 bg-cyan-400 rounded-full animate-pulse"/>
              ACTIVE FLIGHTS ({aircraftInSector.length})
            </h2>
            <div className="space-y-3">
              {aircraftInSector.length === 0 ? (
                <div className="text-gray-400 text-sm text-center py-4">NO AIRCRAFT IN SECTOR</div>
              ) : (
                aircraftInSector.map((aircraft, i) => {
                  const progress = getAircraftProgress(aircraft)
                  return (
                    <div 
                      key={aircraft.id}
                      onClick={() => setSelectedAircraft(aircraft)}
                      className={`p-4 rounded-xl cursor-pointer transition-all duration-300 ${
                        selectedAircraft?.id === aircraft.id 
                          ? 'bg-cyan-500/20 border border-cyan-500/50 shadow-lg shadow-cyan-500/20' 
                          : 'bg-gray-800/50 border border-transparent hover:border-cyan-500/30'
                      }`}
                      style={{
                        transform: `translateX(${Math.sin((time + i * 20) * 0.02) * 2}px)`
                      }}
                    >
                      <div className="flex justify-between items-start mb-2">
                        <span className="font-mono font-bold text-white">{aircraft.registration}</span>
                        <span className="text-xs px-2 py-1 bg-emerald-500/20 text-emerald-400 rounded-full">
                          EN ROUTE
                        </span>
                      </div>
                      <div className="text-sm text-gray-400 mb-3">
                        {aircraft.model || 'N/A'}
                      </div>
                      <div className="h-1.5 bg-gray-700 rounded-full overflow-hidden">
                        <div 
                          className="h-full bg-gradient-to-r from-cyan-500 to-emerald-500 rounded-full transition-all"
                          style={{ width: `${progress}%` }}
                        />
                      </div>
                      <div className="text-xs text-gray-500 mt-2">
                        {Math.round(aircraft.distance || 0)} km | {Math.round(aircraft.altitudeFeet || 0)} ft
                      </div>
                    </div>
                  )
                })
              )}
            </div>
          </div>

          {/* Weather Card */}
          <div className="bg-gray-900/80 backdrop-blur-xl rounded-2xl border border-cyan-500/30 p-4">
            <h2 className="text-sm font-semibold text-cyan-400 mb-3">MÉTÉO {atis.codeIATA || 'CMN'}</h2>
            <div className="grid grid-cols-2 gap-3">
              <div className="bg-gray-800/50 rounded-xl p-3 text-center">
                <div className="text-2xl font-bold text-white">{atis.temperature || 'N/A'}°C</div>
                <div className="text-xs text-gray-500">Température</div>
              </div>
              <div className="bg-gray-800/50 rounded-xl p-3 text-center">
                <div className="text-2xl font-bold text-cyan-400">{atis.vent || 'N/A'}</div>
                <div className="text-xs text-gray-500">Vent (km/h)</div>
              </div>
              <div className="bg-gray-800/50 rounded-xl p-3 text-center">
                <div className="text-2xl font-bold text-emerald-400">{atis.visibilité || 'N/A'}</div>
                <div className="text-xs text-gray-500">Visibilité (km)</div>
              </div>
              <div className="bg-gray-800/50 rounded-xl p-3 text-center">
                <div className="text-2xl font-bold text-white">{atis.pression || 'N/A'}</div>
                <div className="text-xs text-gray-500">Pression (hPa)</div>
              </div>
            </div>
            {atis.pisteEnService && (
              <div className="mt-3 pt-3 border-t border-cyan-500/30">
                <div className="text-xs text-gray-500">Piste en service</div>
                <div className="text-sm font-bold text-cyan-400">{atis.pisteEnService}</div>
              </div>
            )}
          </div>
        </div>

        {/* Center - 3D Radar Display */}
        <div className="col-span-6">
          <div className="bg-gray-900/80 backdrop-blur-xl rounded-2xl border border-cyan-500/30 p-6 h-full relative overflow-hidden">
            {/* Radar circles */}
            <div className="absolute inset-0 flex items-center justify-center">
              {[1, 2, 3, 4].map((ring) => (
                <div
                  key={ring}
                  className="absolute rounded-full border border-cyan-500/20"
                  style={{
                    width: `${ring * 22}%`,
                    height: `${ring * 22}%`,
                  }}
                />
              ))}
              
              {/* Scanning line */}
              <div 
                className="absolute w-1/2 h-0.5 bg-gradient-to-r from-cyan-500 to-transparent origin-left"
                style={{
                  transform: `rotate(${time * 2}deg)`,
                }}
              />
              
              {/* Aircraft dots */}
              {aircraftInSector.map((aircraft, i) => {
                // Calculer la position sur le radar basée sur les coordonnées réelles
                const latDiff = aircraft.latitude - mapCenter[0]
                const lonDiff = aircraft.longitude - mapCenter[1]
                const distance = Math.sqrt(latDiff * latDiff + lonDiff * lonDiff) * 111 // Conversion approximative en km
                const angle = Math.atan2(latDiff, lonDiff) * 180 / Math.PI
                const radius = Math.min(40, (distance / 50) * 40) // Limiter à 40% du rayon
                
                return (
                  <div
                    key={aircraft.id}
                    className="absolute cursor-pointer group"
                    onClick={() => setSelectedAircraft(aircraft)}
                    style={{
                      left: `${50 + Math.cos((angle + 90) * Math.PI / 180) * radius}%`,
                      top: `${50 + Math.sin((angle + 90) * Math.PI / 180) * radius}%`,
                      transform: 'translate(-50%, -50%)'
                    }}
                  >
                    <div className="relative">
                      <div className={`w-4 h-4 rounded-full shadow-lg animate-pulse ${
                        selectedAircraft?.id === aircraft.id 
                          ? 'bg-cyan-400 shadow-cyan-400/50' 
                          : 'bg-emerald-400 shadow-emerald-400/50'
                      }`}/>
                      <div className="absolute -top-8 left-1/2 -translate-x-1/2 bg-gray-900/90 px-2 py-1 rounded text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity border border-cyan-500/50">
                        {aircraft.registration}
                      </div>
                      {/* Trail */}
                      <div 
                        className="absolute w-12 h-0.5 bg-gradient-to-l from-emerald-400 to-transparent -z-10"
                        style={{
                          transform: `rotate(${angle + 90}deg)`,
                          transformOrigin: 'left center'
                        }}
                      />
                    </div>
                  </div>
                );
              })}
              
              {/* Center point */}
              <div className="w-3 h-3 bg-cyan-400 rounded-full shadow-lg shadow-cyan-400/50"/>
            </div>

            {/* Compass */}
            <div className="absolute top-4 left-1/2 -translate-x-1/2 text-cyan-400 font-bold">N</div>
            <div className="absolute bottom-4 left-1/2 -translate-x-1/2 text-cyan-400/50">S</div>
            <div className="absolute left-4 top-1/2 -translate-y-1/2 text-cyan-400/50">W</div>
            <div className="absolute right-4 top-1/2 -translate-y-1/2 text-cyan-400/50">E</div>

            {/* Sector info */}
            <div className="absolute bottom-4 left-4 text-xs text-cyan-400/70">
              SECTOR: 50 KM | ACFT: {aircraftInSector.length}
            </div>
          </div>
        </div>

        {/* Right Panel - Flight Details */}
        <div className="col-span-3 space-y-4 overflow-y-auto">
          <div className="bg-gray-900/80 backdrop-blur-xl rounded-2xl border border-cyan-500/30 p-4">
            <h2 className="text-sm font-semibold text-cyan-400 mb-4">
              {selectedAircraft ? `AIRCRAFT ${selectedAircraft.registration}` : 'SELECT AIRCRAFT'}
            </h2>
            {selectedAircraft && (
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-3">
                  <div className="bg-gray-800/50 rounded-xl p-3">
                    <div className="text-xs text-gray-500 mb-1">ALTITUDE</div>
                    <div className="text-xl font-mono text-white">{Math.round(selectedAircraft.altitudeFeet || 0).toLocaleString()}</div>
                    <div className="text-xs text-cyan-400">feet</div>
                  </div>
                  <div className="bg-gray-800/50 rounded-xl p-3">
                    <div className="text-xs text-gray-500 mb-1">SPEED</div>
                    <div className="text-xl font-mono text-white">{Math.round(selectedAircraft.speed || 0)}</div>
                    <div className="text-xs text-cyan-400">km/h</div>
                  </div>
                  <div className="bg-gray-800/50 rounded-xl p-3">
                    <div className="text-xs text-gray-500 mb-1">HEADING</div>
                    <div className="text-xl font-mono text-white">{Math.round(selectedAircraft.heading || 0)}°</div>
                    <div className="text-xs text-cyan-400">degrees</div>
                  </div>
                  <div className="bg-gray-800/50 rounded-xl p-3">
                    <div className="text-xs text-gray-500 mb-1">DISTANCE</div>
                    <div className="text-xl font-mono text-white">{Math.round(selectedAircraft.distance || 0)}</div>
                    <div className="text-xs text-cyan-400">km</div>
                  </div>
                </div>
                
                <div className="bg-gray-800/50 rounded-xl p-3">
                  <div className="text-xs text-gray-500 mb-2">MODEL</div>
                  <div className="text-sm font-mono text-white">{selectedAircraft.model || 'N/A'}</div>
                </div>

                {/* Altitude Graph */}
                <div className="bg-gray-800/50 rounded-xl p-3">
                  <div className="text-xs text-gray-500 mb-2">ALTITUDE PROFILE</div>
                  <svg className="w-full h-20" viewBox="0 0 200 60">
                    <path
                      d="M0,50 Q30,50 50,20 L150,20 Q170,20 200,50"
                      fill="none"
                      stroke="url(#altGradient)"
                      strokeWidth="2"
                    />
                    <defs>
                      <linearGradient id="altGradient" x1="0%" y1="0%" x2="100%" y2="0%">
                        <stop offset="0%" stopColor="#06b6d4"/>
                        <stop offset="100%" stopColor="#10b981"/>
                      </linearGradient>
                    </defs>
                    {/* Current position */}
                    <circle 
                      cx={getAircraftProgress(selectedAircraft) * 2} 
                      cy={getAircraftProgress(selectedAircraft) < 25 ? 50 - getAircraftProgress(selectedAircraft) * 1.2 : getAircraftProgress(selectedAircraft) > 75 ? 20 + (getAircraftProgress(selectedAircraft) - 75) * 1.2 : 20}
                      r="4"
                      fill="#10b981"
                      className="animate-pulse"
                    />
                  </svg>
                </div>
              </div>
            )}
          </div>

          {/* Communications */}
          <div className="bg-gray-900/80 backdrop-blur-xl rounded-2xl border border-cyan-500/30 p-4 flex-1">
            <h2 className="text-sm font-semibold text-cyan-400 mb-3 flex items-center gap-2">
              <span className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"/>
              COMMUNICATIONS ATC
            </h2>
            <div className="space-y-2 text-sm max-h-64 overflow-y-auto">
              {atcHistory.length === 0 ? (
                <div className="text-gray-400 text-xs text-center py-4">NO COMMUNICATIONS</div>
              ) : (
                atcHistory.map((msg) => (
                  <div key={msg.id} className="bg-gray-800/50 rounded-lg p-2">
                    <span className="text-cyan-400">{msg.type || 'ATC'}:</span>
                    <span className="text-gray-300 ml-2">{msg.message}</span>
                    <div className="text-xs text-gray-500 mt-1">
                      {new Date(msg.timestamp).toLocaleTimeString('fr-FR')}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default RadarDashboard

