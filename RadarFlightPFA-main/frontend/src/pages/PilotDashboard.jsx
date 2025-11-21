import React, { useState, useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet'
import L from 'leaflet'
import api from '../services/api'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

// Fix pour les icônes Leaflet
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

// Style HUD pour la carte Leaflet
const hudMapStyle = {
  filter: 'invert(1) hue-rotate(180deg) saturate(0.5) brightness(0.7) contrast(1.2)',
  mixBlendMode: 'screen'
}

function PilotDashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [dashboardData, setDashboardData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [clearanceStatus, setClearanceStatus] = useState(null)
  const [requestingClearance, setRequestingClearance] = useState(false)
  const [aircraftId, setAircraftId] = useState(null)
  const [airports, setAirports] = useState([])
  const [flightData, setFlightData] = useState(null) // Données de vol en temps réel
  const [simulating, setSimulating] = useState(false)
  const [assignedFlights, setAssignedFlights] = useState([])
  const [flightsLoading, setFlightsLoading] = useState(true)
  const wsClientRef = useRef(null)

  const handleLogout = () => {
    // Déconnecter le WebSocket si actif
    if (wsClientRef.current) {
      wsClientRef.current.deactivate()
      wsClientRef.current = null
    }
    // Appeler la fonction logout du contexte
    logout()
    // Rediriger vers la page de login
    navigate('/login', { replace: true })
  }

  // Récupérer toutes les données du dashboard
  useEffect(() => {
    if (!user?.username) {
      setLoading(false)
      return
    }
    
    fetchDashboardData()
    fetchAirports()
    fetchAssignedFlights()
    const interval = setInterval(fetchDashboardData, 5000) // Rafraîchir toutes les 5 secondes
    return () => clearInterval(interval)
  }, [user?.username])

  // WebSocket pour les mises à jour de vol en temps réel
  useEffect(() => {
    if (!aircraftId) return

    const socket = new SockJS('http://localhost:8080/ws')
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log('WebSocket connecté pour avion', aircraftId)
        // S'abonner aux mises à jour de l'avion
        client.subscribe(`/topic/aircraft/${aircraftId}`, (message) => {
          try {
            const data = JSON.parse(message.body)
            if (data.type === 'flight_update') {
              setFlightData(data)
              // Mettre à jour les données du dashboard avec les nouvelles positions
              setDashboardData(prev => ({
                ...prev,
                latitude: data.latitude,
                longitude: data.longitude,
                altitude: data.altitude,
                altitudeFeet: data.altitude * 3.28084,
                groundSpeed: data.speed,
                airSpeed: data.speed,
                heading: data.heading,
                flightStatus: 'En vol'
              }))
            }
          } catch (error) {
            console.error('Erreur parsing message WebSocket:', error)
          }
        })
      },
      onDisconnect: () => {
        console.log('WebSocket déconnecté')
      },
      onStompError: (frame) => {
        console.error('Erreur STOMP:', frame)
      }
    })

    client.activate()
    wsClientRef.current = client

    return () => {
      if (wsClientRef.current) {
        wsClientRef.current.deactivate()
      }
    }
  }, [aircraftId])

  const fetchDashboardData = async () => {
    if (!user?.username) {
      setLoading(false)
      return
    }
    
    try {
      setError(null)
      const response = await api.get(`/pilots/${user.username}/dashboard`)
      
      if (response.data) {
        setDashboardData(response.data)
        
        // Récupérer l'ID de l'avion si disponible
        if (!aircraftId) {
          try {
            const aircraftResponse = await api.get(`/aircraft/pilot/${user.username}`)
            if (aircraftResponse.data?.id) {
              setAircraftId(aircraftResponse.data.id)
            }
          } catch (err) {
            console.warn('Erreur récupération avion (non bloquant):', err)
            // Ne pas bloquer si on ne peut pas récupérer l'ID de l'avion
          }
        }
      } else {
        setError('Aucune donnée reçue du serveur')
      }
      
      setLoading(false)
    } catch (error) {
      console.error('Erreur chargement dashboard:', error)
      
      // Gérer les différents types d'erreurs
      if (error.response?.status === 404) {
        const errorData = error.response.data
        if (errorData?.code === 'NO_AIRCRAFT_ASSIGNED') {
          setError('NO_AIRCRAFT_ASSIGNED')
        } else {
          setError('Pilote non trouvé ou aucune donnée disponible')
        }
      } else if (error.response?.status === 403) {
        setError('Accès refusé. Vérifiez vos permissions.')
      } else if (error.response?.status === 500) {
        setError('Erreur serveur. Veuillez réessayer plus tard.')
      } else if (error.code === 'ECONNREFUSED' || error.code === 'ERR_NETWORK') {
        setError('Impossible de contacter le serveur. Vérifiez que le backend est démarré.')
      } else {
        setError(error.response?.data?.error || error.message || 'Erreur inconnue')
      }
      
      setLoading(false)
    }
  }

  const fetchAirports = async () => {
    try {
      const response = await api.get('/airports')
      setAirports(response.data)
    } catch (error) {
      console.error('Erreur chargement aéroports:', error)
    }
  }

  const fetchAssignedFlights = async () => {
    if (!user?.username) return
    
    try {
      const response = await api.get(`/flight/pilot/username/${user.username}`)
      setAssignedFlights(response.data.flights || [])
      setFlightsLoading(false)
    } catch (error) {
      console.error('Erreur chargement vols assignés:', error)
      setFlightsLoading(false)
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
      
      // Si autorisation accordée, récupérer l'aircraftId pour le bouton Décoller
      if (response.data.status === 'GRANTED') {
        try {
          const aircraftResponse = await api.get(`/aircraft/pilot/${user.username}`)
          setAircraftId(aircraftResponse.data.id)
        } catch (err) {
          console.error('Erreur récupération avion:', err)
        }
      }
      
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

  const handleTakeoff = async () => {
    if (!aircraftId) {
      alert('ID avion non disponible')
      return
    }
    
    const arrivalAirportId = document.getElementById('arrivalAirport')?.value
    if (!arrivalAirportId) {
      alert('Veuillez sélectionner un aéroport de destination')
      return
    }
    
    // Récupérer l'aéroport de départ depuis les données du dashboard
    const departureAirport = airports.find(ap => 
      dashboardData.departureAirport?.includes(ap.codeIATA)
    )
    
    if (!departureAirport) {
      alert('Aéroport de départ non trouvé')
      return
    }
    
    setSimulating(true)
    
    try {
      const response = await api.post('/flight/simulate-takeoff', {
        aircraftId: aircraftId,
        departureAirportId: departureAirport.id,
        arrivalAirportId: parseInt(arrivalAirportId)
      })
      
      if (response.data.success) {
        setClearanceStatus({
          status: 'FLYING',
          message: 'Vol démarré avec succès',
          flightId: response.data.flightId,
          estimatedArrival: response.data.estimatedArrival
        })
        // Rafraîchir les données
        setTimeout(fetchDashboardData, 1000)
      }
    } catch (error) {
      console.error('Erreur démarrage vol:', error)
      alert('Erreur lors du démarrage du vol: ' + (error.response?.data?.error || error.message))
      setSimulating(false)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'En vol':
      case 'GRANTED':
      case 'FLYING':
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

  const getHUDStatusColor = (status) => {
    switch (status) {
      case 'En vol':
      case 'GRANTED':
      case 'FLYING':
        return 'text-green-400 border-green-400'
      case 'Au sol':
      case 'PENDING':
        return 'text-yellow-400 border-yellow-400'
      case 'Atterrissage':
      case 'REFUSED':
        return 'text-red-400 border-red-400'
      default:
        return 'text-green-500/70 border-green-500/30'
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-950 text-green-400 font-mono flex items-center justify-center relative overflow-hidden">
        {/* Scanline Effect */}
        <div 
          className="fixed inset-0 pointer-events-none z-50 opacity-5"
          style={{
            background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,255,0,0.1) 2px, rgba(0,255,0,0.1) 4px)'
          }}
        />
        <div className="text-green-400 text-xl border border-green-500/50 px-4 py-2">LOADING...</div>
      </div>
    )
  }

  if (!dashboardData && !loading) {
    return (
      <div className="min-h-screen bg-gray-950 text-green-400 font-mono flex items-center justify-center relative overflow-hidden">
        {/* Scanline Effect */}
        <div 
          className="fixed inset-0 pointer-events-none z-50 opacity-5"
          style={{
            background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,255,0,0.1) 2px, rgba(0,255,0,0.1) 4px)'
          }}
        />
        <div className="text-center border border-green-500/50 p-6 max-w-md">
          {error === 'NO_AIRCRAFT_ASSIGNED' ? (
            <>
              <div className="text-green-400 text-xl mb-2">NO AIRCRAFT ASSIGNED</div>
              <div className="text-green-500/70 text-sm mb-4">Aucun avion n'est actuellement assigné à votre compte.</div>
              <div className="text-green-500/70 text-sm">Veuillez contacter l'administrateur pour assigner un avion.</div>
            </>
          ) : error ? (
            <>
              <div className="text-red-400 text-xl mb-2">ERREUR</div>
              <div className="text-green-500/70 text-sm mb-4">{error}</div>
              <button
                onClick={() => {
                  setError(null)
                  setLoading(true)
                  fetchDashboardData()
                }}
                className="border border-green-500/50 px-4 py-2 text-green-400 hover:bg-green-500/20 transition text-sm mt-4"
              >
                RÉESSAYER
              </button>
            </>
          ) : (
            <>
              <div className="text-green-400 text-xl mb-2">AUCUNE DONNÉE</div>
              <div className="text-green-500/70 text-sm mb-4">Impossible de charger les données du dashboard.</div>
              <button
                onClick={() => {
                  setError(null)
                  setLoading(true)
                  fetchDashboardData()
                }}
                className="border border-green-500/50 px-4 py-2 text-green-400 hover:bg-green-500/20 transition text-sm mt-4"
              >
                RÉESSAYER
              </button>
            </>
          )}
        </div>
      </div>
    )
  }

  const mapCenter = dashboardData.latitude && dashboardData.longitude
    ? [dashboardData.latitude, dashboardData.longitude]
    : [33.5731, -7.5898]

  // Trajectoire pour la carte
  const trajectory = dashboardData.trajectory || []

  // Données pour les displays HUD
  const currentAlt = dashboardData.altitudeFeet || 0
  const currentSpeed = dashboardData.groundSpeed || 0
  const currentHeading = dashboardData.heading || 0
  const currentVSpeed = dashboardData.verticalSpeed || 0
  const currentAirSpeed = dashboardData.airSpeed || 0

  return (
    <div className="min-h-screen bg-gray-950 text-green-400 font-mono overflow-hidden relative">
      {/* Scanline Effect */}
      <div 
        className="fixed inset-0 pointer-events-none z-50 opacity-5"
        style={{
          background: 'repeating-linear-gradient(0deg, transparent, transparent 2px, rgba(0,255,0,0.1) 2px, rgba(0,255,0,0.1) 4px)'
        }}
      />
      
      {/* Vignette */}
      <div 
        className="fixed inset-0 pointer-events-none z-40"
        style={{
          background: 'radial-gradient(circle at center, transparent 30%, rgba(0,0,0,0.8) 100%)'
        }}
      />

      {/* Corner Brackets */}
      <svg className="fixed top-4 left-4 w-16 h-16 text-green-500/50 z-30 pointer-events-none">
        <path d="M0,40 L0,0 L40,0" fill="none" stroke="currentColor" strokeWidth="2"/>
      </svg>
      <svg className="fixed top-4 right-4 w-16 h-16 text-green-500/50 z-30 pointer-events-none">
        <path d="M40,0 L80,0 L80,40" fill="none" stroke="currentColor" strokeWidth="2" transform="translate(-40,0)"/>
      </svg>
      <svg className="fixed bottom-4 left-4 w-16 h-16 text-green-500/50 z-30 pointer-events-none">
        <path d="M0,0 L0,40 L40,40" fill="none" stroke="currentColor" strokeWidth="2" transform="translate(0,-40)"/>
      </svg>
      <svg className="fixed bottom-4 right-4 w-16 h-16 text-green-500/50 z-30 pointer-events-none">
        <path d="M40,40 L80,40 L80,0" fill="none" stroke="currentColor" strokeWidth="2" transform="translate(-40,-40)"/>
      </svg>

      {/* Header HUD */}
      <header className="relative z-[100] p-4 border-b border-green-500/30 bg-black/50 sticky top-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="border-2 border-green-500 px-3 py-1">
              <span className="text-lg tracking-widest">RADAR FLIGHT</span>
            </div>
            <div className="text-green-500/70 text-xs">
              PILOT DASHBOARD v1.0
            </div>
            <div className="text-green-500/70 text-xs border-l border-green-500/30 pl-4">
              {dashboardData.flightNumber || 'N/A'} | {dashboardData.airline || 'N/A'} | {dashboardData.aircraftType || 'N/A'}
            </div>
          </div>
          
          <div className="flex items-center gap-8 text-sm">
            <div className="flex items-center gap-2">
              <div className="w-2 h-2 bg-green-400 rounded-full animate-pulse"/>
              <span>SYSTEM ACTIVE</span>
            </div>
            <div className="border border-green-500/50 px-3 py-1">
              {new Date().toLocaleTimeString()} UTC+1
            </div>
            <div className="text-green-500/70 text-xs">
              {user?.username?.toUpperCase() || 'N/A'} | {user?.role || 'N/A'}
            </div>
            <button
              onClick={handleLogout}
              type="button"
              className="border border-red-500/50 px-3 py-1 text-red-400 hover:bg-red-500/20 active:bg-red-500/30 transition text-xs cursor-pointer relative z-[101] pointer-events-auto"
            >
              LOGOUT
            </button>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="relative z-10 p-4 grid grid-cols-12 gap-4 h-[calc(100vh-80px)]">
        {/* Left - Altitude Ladder */}
        <div className="col-span-1 flex flex-col items-center justify-center">
          <div className="text-xs mb-2 text-green-500/70">ALT</div>
          <div className="relative h-80 w-12 border border-green-500/30">
            {/* Altitude marks */}
            {[...Array(11)].map((_, i) => (
              <div 
                key={i}
                className="absolute w-full flex items-center justify-end pr-1"
                style={{ top: `${i * 10}%` }}
              >
                <span className="text-xs text-green-500/70">{(10 - i) * 1000}</span>
                <div className="w-2 h-px bg-green-500/50 ml-1"/>
              </div>
            ))}
            {/* Current altitude indicator */}
            <div 
              className="absolute left-0 w-full h-6 bg-green-500/20 border-y border-green-500 flex items-center justify-center transition-all duration-300"
              style={{ top: `${Math.max(0, Math.min(100, 100 - (currentAlt / 100)))}%`, transform: 'translateY(-50%)' }}
            >
              <span className="text-sm font-bold">{Math.round(currentAlt)}</span>
            </div>
          </div>
          <div className="text-xs mt-2 text-green-500/70">FEET</div>
        </div>

        {/* Main Display Area */}
        <div className="col-span-7 space-y-4 overflow-y-auto">
          {/* Flight Info Panel */}
          <div className="border border-green-500/30 bg-black/50 p-4">
            <div className="text-xs text-green-500/70 mb-3 border-b border-green-500/30 pb-2">FLIGHT INFORMATION</div>
            <div className="grid grid-cols-3 gap-4 text-sm">
              <div>
                <div className="text-green-500/70 text-xs mb-1">FLIGHT NUMBER</div>
                <div className="text-green-400 font-mono font-semibold">{dashboardData.flightNumber || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">AIRLINE</div>
                <div className="text-green-400">{dashboardData.airline || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">AIRCRAFT TYPE</div>
                <div className="text-green-400">{dashboardData.aircraftType || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">DEPARTURE</div>
                <div className="text-green-400">{dashboardData.departureAirport || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">ARRIVAL</div>
                <div className="text-green-400">{dashboardData.arrivalAirport || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">ROUTE</div>
                <div className="text-green-400 font-mono">{dashboardData.route || 'N/A'}</div>
              </div>
            </div>
          </div>

          {/* Map Display */}
          <div className="border border-green-500/30 h-96 relative overflow-hidden bg-black/50">
            <div className="absolute top-2 left-2 z-[1000] text-xs text-green-500/70 border border-green-500/30 bg-black/80 px-2 py-1">
              POSITION & MOVEMENT (ADS-B)
            </div>
            <div className="h-full">
              <MapContainer
                center={mapCenter}
                zoom={dashboardData.latitude && dashboardData.longitude ? 12 : 6}
                style={{ height: '100%', width: '100%', filter: 'invert(1) hue-rotate(180deg) saturate(0.5) brightness(0.7) contrast(1.2)' }}
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
                    color="#00ff00"
                    weight={2}
                  />
                )}
              </MapContainer>
            </div>
            {/* Position Data Overlay */}
            <div className="absolute bottom-0 left-0 right-0 bg-black/80 border-t border-green-500/30 p-2 grid grid-cols-4 gap-2 text-xs">
              <div>
                <div className="text-green-500/70">LAT</div>
                <div className="text-green-400 font-mono">{dashboardData.latitude?.toFixed(6) || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70">LON</div>
                <div className="text-green-400 font-mono">{dashboardData.longitude?.toFixed(6) || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70">ALT</div>
                <div className="text-green-400 font-mono">{Math.round(currentAlt)} FT</div>
              </div>
              <div>
                <div className="text-green-500/70">HDG</div>
                <div className="text-green-400 font-mono">{Math.round(currentHeading)}°</div>
              </div>
            </div>
          </div>

          {/* Flight Status Panel */}
          <div className="border border-green-500/30 bg-black/50 p-4">
            <div className="text-xs text-green-500/70 mb-3 border-b border-green-500/30 pb-2">FLIGHT STATUS</div>
            <div className="grid grid-cols-4 gap-4 text-sm">
              <div>
                <div className="text-green-500/70 text-xs mb-1">STATUS</div>
                <div className={`border px-2 py-1 text-green-400 font-semibold ${getHUDStatusColor(dashboardData.flightStatus)}`}>
                  {dashboardData.flightStatus || 'N/A'}
                </div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">STD</div>
                <div className="text-green-400">{formatTime(dashboardData.scheduledDeparture)}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">ATD</div>
                <div className="text-green-400">{formatTime(dashboardData.actualDeparture)}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">STA</div>
                <div className="text-green-400">{formatTime(dashboardData.scheduledArrival)}</div>
              </div>
              {dashboardData.delayMinutes > 0 && (
                <div className="col-span-4 border border-yellow-500/50 bg-yellow-900/20 p-2">
                  <p className="text-xs text-yellow-400">
                    ⚠️ DELAY: {dashboardData.delayMinutes} MIN
                  </p>
                </div>
              )}
              <div>
                <div className="text-green-500/70 text-xs mb-1">GATE</div>
                <div className="text-green-400">{dashboardData.gate || 'N/A'}</div>
              </div>
              <div>
                <div className="text-green-500/70 text-xs mb-1">RUNWAY</div>
                <div className="text-green-400">{dashboardData.runway || 'N/A'}</div>
              </div>
            </div>
          </div>

          {/* Takeoff Clearance Panel */}
          {dashboardData.flightStatus === 'Au sol' && (
            <div className="border border-green-500/30 bg-black/50 p-4">
              <div className="text-xs text-green-500/70 mb-3 border-b border-green-500/30 pb-2">TAKEOFF CLEARANCE</div>
              <button
                onClick={requestTakeoffClearance}
                disabled={requestingClearance}
                className="w-full border-2 border-green-500 bg-green-500/20 hover:bg-green-500/30 disabled:border-green-500/30 disabled:bg-green-500/10 text-green-400 py-3 px-4 transition font-semibold text-sm uppercase tracking-wider"
              >
                {requestingClearance ? 'REQUESTING...' : '✈️ REQUEST TAKEOFF CLEARANCE'}
              </button>
              
              {clearanceStatus && (
                <div className={`mt-4 p-3 border ${
                  clearanceStatus.status === 'GRANTED' ? 'border-green-500 bg-green-500/20' :
                  clearanceStatus.status === 'REFUSED' ? 'border-red-500 bg-red-500/20' :
                  clearanceStatus.status === 'PENDING' ? 'border-yellow-500 bg-yellow-500/20' :
                  'border-green-500/30 bg-black/50'
                }`}>
                  <p className={`font-semibold mb-1 text-xs ${
                    clearanceStatus.status === 'GRANTED' ? 'text-green-400' :
                    clearanceStatus.status === 'REFUSED' ? 'text-red-400' :
                    clearanceStatus.status === 'PENDING' ? 'text-yellow-400' :
                    'text-green-500/70'
                  }`}>
                    {clearanceStatus.status === 'GRANTED' && '✅ CLEARANCE GRANTED'}
                    {clearanceStatus.status === 'REFUSED' && '❌ CLEARANCE REFUSED'}
                    {clearanceStatus.status === 'PENDING' && '⏳ PENDING'}
                    {clearanceStatus.status === 'ERROR' && '⚠️ ERROR'}
                  </p>
                  <p className="text-xs text-green-400/70 mb-1">{clearanceStatus.message}</p>
                  {clearanceStatus.details && (
                    <p className="text-xs text-green-500/50">{clearanceStatus.details}</p>
                  )}
                </div>
              )}
              
              {/* Takeoff Button after clearance */}
              {clearanceStatus?.status === 'GRANTED' && !simulating && (
                <div className="mt-4">
                  <button
                    onClick={handleTakeoff}
                    className="w-full border-2 border-blue-500 bg-blue-500/20 hover:bg-blue-500/30 text-blue-400 py-3 px-4 transition font-semibold text-sm uppercase tracking-wider"
                  >
                    🚀 TAKEOFF
                  </button>
                  <p className="text-xs text-green-500/70 mt-2 text-center">
                    SELECT DESTINATION AIRPORT BELOW
                  </p>
                  {airports.length > 0 && (
                    <div className="mt-4">
                      <label className="block text-xs text-green-500/70 mb-2">DESTINATION:</label>
                      <select
                        id="arrivalAirport"
                        className="w-full bg-black/50 border border-green-500/30 text-green-400 p-2 text-sm"
                      >
                        <option value="">SELECT...</option>
                        {airports
                          .filter(airport => 
                            dashboardData.departureAirport && 
                            !dashboardData.departureAirport.includes(airport.codeIATA)
                          )
                          .map(airport => (
                            <option key={airport.id} value={airport.id} className="bg-black text-green-400">
                              {airport.name} ({airport.codeIATA})
                            </option>
                          ))}
                      </select>
                    </div>
                  )}
                </div>
              )}
              
              {simulating && (
                <div className="mt-4 p-3 border border-blue-500/50 bg-blue-500/20">
                  <p className="text-blue-400 font-semibold mb-1 text-xs">✈️ FLIGHT IN PROGRESS</p>
                  <p className="text-xs text-blue-400/70">Simulation started</p>
                </div>
              )}
            </div>
          )}
          
          {/* Real-time Flight Info */}
          {flightData && dashboardData.flightStatus === 'En vol' && (
            <div className="border border-green-500/30 bg-black/50 p-4">
              <div className="text-xs text-green-500/70 mb-3 border-b border-green-500/30 pb-2">REAL-TIME FLIGHT DATA</div>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-green-500/70">DISTANCE REMAINING</span>
                  <span className="text-green-400 font-mono font-semibold">
                    {flightData.distanceRemaining?.toFixed(1) || 'N/A'} KM
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">ETA</span>
                  <span className="text-green-400 font-mono font-semibold">
                    {formatTime(flightData.estimatedArrival)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">CURRENT ALT</span>
                  <span className="text-green-400 font-mono">
                    {flightData.altitude ? (flightData.altitude * 3.28084).toFixed(0) : 'N/A'} FT
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">SPEED</span>
                  <span className="text-green-400 font-mono">
                    {flightData.speed?.toFixed(0) || 'N/A'} KM/H
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">HEADING</span>
                  <span className="text-green-400 font-mono">
                    {flightData.heading?.toFixed(0) || 'N/A'}°
                  </span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* Right - Speed Ladder */}
        <div className="col-span-1 flex flex-col items-center justify-center">
          <div className="text-xs mb-2 text-green-500/70">SPD</div>
          <div className="relative h-80 w-12 border border-green-500/30">
            {[...Array(11)].map((_, i) => (
              <div 
                key={i}
                className="absolute w-full flex items-center pl-1"
                style={{ top: `${i * 10}%` }}
              >
                <div className="w-2 h-px bg-green-500/50 mr-1"/>
                <span className="text-xs text-green-500/70">{(10 - i) * 100}</span>
              </div>
            ))}
            <div 
              className="absolute right-0 w-full h-6 bg-green-500/20 border-y border-green-500 flex items-center justify-center transition-all duration-300"
              style={{ top: `${Math.max(0, Math.min(100, 100 - (currentSpeed / 10)))}%`, transform: 'translateY(-50%)' }}
            >
              <span className="text-sm font-bold">{Math.round(currentSpeed)}</span>
            </div>
          </div>
          <div className="text-xs mt-2 text-green-500/70">KM/H</div>
        </div>

        {/* Far Right - Data Panels */}
        <div className="col-span-3 space-y-3 overflow-y-auto">
          {/* Weather Panel */}
          {dashboardData.weather && (
            <div className="border border-green-500/30 p-3 bg-black/50">
              <div className="text-xs text-green-500/70 mb-2 border-b border-green-500/30 pb-1">WEATHER</div>
              <div className="space-y-2 text-xs">
                <div className="flex justify-between">
                  <span className="text-green-500/70">WIND</span>
                  <span className="text-green-400">
                    {dashboardData.weather.windSpeed?.toFixed(0) || 0} KM/H
                    {dashboardData.weather.windDirection && ` @ ${dashboardData.weather.windDirection.toFixed(0)}°`}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">VISIBILITY</span>
                  <span className="text-green-400">
                    {dashboardData.weather.visibility?.toFixed(1) || 0} KM
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">PRECIP</span>
                  <span className="text-green-400">{dashboardData.weather.precipitation || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">TURB</span>
                  <span className="text-green-400">{dashboardData.weather.turbulence || 'N/A'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">TEMP</span>
                  <span className="text-green-400">
                    {dashboardData.weather.temperature?.toFixed(1) || 'N/A'}°C
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-green-500/70">PRESSURE</span>
                  <span className="text-green-400">
                    {dashboardData.weather.pressure?.toFixed(0) || 'N/A'} HPA
                  </span>
                </div>
                {dashboardData.weather.weatherAlerts && dashboardData.weather.weatherAlerts.length > 0 && (
                  <div className="mt-3 p-2 border border-red-500/50 bg-red-500/20">
                    <p className="text-xs font-semibold text-red-400 mb-1">⚠️ WEATHER ALERTS</p>
                    <ul className="text-xs text-red-400/70 space-y-1">
                      {dashboardData.weather.weatherAlerts.map((alert, idx) => (
                        <li key={idx}>• {alert}</li>
                      ))}
                    </ul>
                  </div>
                )}
              </div>
            </div>
          )}

          {/* ATC Communications */}
          <div className="border border-green-500/30 bg-black/50">
            <div className="p-3 border-b border-green-500/30">
              <div className="text-xs text-green-500/70 mb-1 flex items-center gap-2">
                <span className="w-2 h-2 bg-green-400 rounded-full animate-pulse"/>
                ATC COMM
              </div>
              {dashboardData.radarCenterName && (
                <p className="text-xs text-green-500/50">CENTER: {dashboardData.radarCenterName}</p>
              )}
            </div>
            <div className="p-3">
              {dashboardData.lastATCMessage && (
                <div className="mb-3 p-2 border border-blue-500/50 bg-blue-500/20">
                  <p className="text-xs text-blue-400 mb-1">LAST MESSAGE</p>
                  <p className="text-xs text-blue-300">{dashboardData.lastATCMessage}</p>
                </div>
              )}
              
              {dashboardData.currentInstructions && dashboardData.currentInstructions.length > 0 && (
                <div className="mb-3">
                  <p className="text-xs text-green-500/70 mb-2">CURRENT INSTRUCTIONS</p>
                  <ul className="space-y-1">
                    {dashboardData.currentInstructions.map((instruction, idx) => (
                      <li key={idx} className="text-xs p-2 border border-green-500/30 bg-green-500/10">
                        {instruction}
                      </li>
                    ))}
                  </ul>
                </div>
              )}
              
              <div className="h-48 overflow-y-auto space-y-1">
                {dashboardData.atcHistory && dashboardData.atcHistory.length > 0 ? (
                  dashboardData.atcHistory.map((msg, idx) => (
                    <div
                      key={idx}
                      className={`border-l-2 pl-2 py-1 text-xs ${
                        msg.sender === 'ATC' 
                          ? 'border-blue-500 bg-blue-500/10' 
                          : 'border-green-500 bg-green-500/10'
                      }`}
                    >
                      <div className="flex justify-between mb-1">
                        <span className={`font-semibold ${
                          msg.sender === 'ATC' ? 'text-blue-400' : 'text-green-400'
                        }`}>
                          {msg.sender}
                        </span>
                        <span className="text-green-500/50">
                          {formatTime(msg.timestamp)}
                        </span>
                      </div>
                      <p className="text-green-400/70">{msg.message}</p>
                    </div>
                  ))
                ) : (
                  <p className="text-green-500/50 text-xs text-center py-4">NO MESSAGES</p>
                )}
              </div>
            </div>
          </div>

          {/* Security Alerts */}
          {dashboardData.alerts && dashboardData.alerts.length > 0 && (
            <div className="border border-green-500/30 p-3 bg-black/50">
              <div className="text-xs text-green-500/70 mb-2 border-b border-green-500/30 pb-1">SECURITY ALERTS</div>
              <div className="space-y-2">
                {dashboardData.alerts.map((alert, idx) => (
                  <div
                    key={idx}
                    className={`p-2 border text-xs ${
                      alert.severity === 'CRITICAL' ? 'border-red-500 bg-red-500/20' :
                      alert.severity === 'HIGH' ? 'border-orange-500 bg-orange-500/20' :
                      alert.severity === 'MEDIUM' ? 'border-yellow-500 bg-yellow-500/20' :
                      'border-green-500/30 bg-green-500/10'
                    }`}
                  >
                    <div className="flex justify-between mb-1">
                      <span className="font-semibold text-green-400">{alert.type}</span>
                      <span className="text-green-500/50">{formatTime(alert.timestamp)}</span>
                    </div>
                    <p className="text-green-400/70">{alert.message}</p>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* KPIs Panel */}
          {dashboardData.kpis && (
            <div className="border border-green-500/30 p-3 bg-black/50">
              <div className="text-xs text-green-500/70 mb-2 border-b border-green-500/30 pb-1">REAL-TIME KPIs</div>
              <div className="space-y-3 text-xs">
                <div>
                  <div className="text-green-500/50 mb-1">REAL-TIME</div>
                  <div className="space-y-1">
                    <div className="flex justify-between">
                      <span className="text-green-500/70">DIST REMAIN</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.remainingDistance?.toFixed(1) || 'N/A'} KM
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">ETA</span>
                      <span className="text-green-400 font-mono">
                        {formatTime(dashboardData.kpis.estimatedArrival)}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">FUEL EST</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.estimatedFuelConsumption?.toFixed(1) || 'N/A'} L
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">FUEL LVL</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.fuelLevel?.toFixed(0) || 'N/A'}%
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">AVG SPD</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.averageSpeed?.toFixed(0) || 'N/A'} KM/H
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">ALT STABLE</span>
                      <span className={`font-mono ${
                        dashboardData.kpis.stableAltitude ? 'text-green-400' : 'text-red-400'
                      }`}>
                        {dashboardData.kpis.stableAltitude ? 'YES' : 'NO'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">TURBULENCE</span>
                      <span className={`font-mono ${
                        dashboardData.kpis.turbulenceDetected ? 'text-red-400' : 'text-green-400'
                      }`}>
                        {dashboardData.kpis.turbulenceDetected ? 'DETECTED' : 'NONE'}
                      </span>
                    </div>
                  </div>
                </div>

                <div className="pt-3 border-t border-green-500/30">
                  <div className="text-green-500/50 mb-1">RADAR / SECURITY</div>
                  <div className="space-y-1">
                    <div className="flex justify-between">
                      <span className="text-green-500/70">WX SEVERITY</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.weatherSeverity || 0}%
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">TRAJ RISK</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.trajectoryRiskIndex || 0}/100
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">TRAFFIC (30KM)</span>
                      <span className="text-green-400 font-mono">
                        {dashboardData.kpis.trafficDensity30km || 0} ACFT
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-green-500/70">HEALTH SCORE</span>
                      <span className={`font-mono ${
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

          {/* Flight Data Display */}
          <div className="border border-green-500/30 p-3 bg-black/50">
            <div className="text-xs text-green-500/70 mb-2 border-b border-green-500/30 pb-1">FLIGHT DATA</div>
            <div className="space-y-2 text-xs">
              {[
                { label: 'ALTITUDE', value: `${Math.round(currentAlt)} FT`, bar: Math.min(100, currentAlt / 100) },
                { label: 'GROUND SPD', value: `${Math.round(currentSpeed)} KM/H`, bar: Math.min(100, currentSpeed / 10) },
                { label: 'HEADING', value: `${Math.round(currentHeading)}°`, bar: Math.min(100, currentHeading / 3.6) },
                { label: 'VERT SPD', value: `${currentVSpeed > 0 ? '+' : ''}${currentVSpeed.toFixed(1)} M/S`, bar: Math.min(100, Math.abs(currentVSpeed) * 5) },
              ].map((item) => (
                <div key={item.label}>
                  <div className="flex justify-between mb-1">
                    <span className="text-green-500/70">{item.label}</span>
                    <span className="text-green-400 font-mono">{item.value}</span>
                  </div>
                  <div className="h-1 bg-green-500/20">
                    <div 
                      className="h-full bg-green-500 transition-all duration-300"
                      style={{ width: `${item.bar}%` }}
                    />
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Assigned Flights Section - Full Width */}
        <section className="col-span-12 border border-green-500/30 bg-black/50 p-4 mt-4">
        <div className="text-xs text-green-500/70 mb-3 border-b border-green-500/30 pb-2">ASSIGNED FLIGHTS</div>
        
        {flightsLoading ? (
          <div className="border border-green-500/30 p-8 text-center text-green-500/70">
            LOADING FLIGHTS...
          </div>
        ) : assignedFlights.length === 0 ? (
          <div className="border border-green-500/30 p-8 text-center text-green-500/70">
            NO FLIGHTS ASSIGNED
          </div>
        ) : (
          <div className="space-y-3">
            {assignedFlights.map((flight) => (
              <div key={flight.id} className="border border-green-500/30 p-4 bg-black/30">
                <div className="flex justify-between items-start mb-3">
                  <div>
                    <h3 className="text-sm font-bold text-green-400">{flight.flightNumber}</h3>
                    <p className="text-xs text-green-500/70">{flight.airline}</p>
                  </div>
                  <span className={`px-2 py-1 text-xs font-semibold border ${
                    flight.flightStatus === 'EN_COURS' ? 'border-green-500 text-green-400 bg-green-500/20' :
                    flight.flightStatus === 'PLANIFIE' ? 'border-blue-500 text-blue-400 bg-blue-500/20' :
                    flight.flightStatus === 'RETARDE' ? 'border-yellow-500 text-yellow-400 bg-yellow-500/20' :
                    'border-green-500/30 text-green-500/70 bg-green-500/10'
                  }`}>
                    {flight.flightStatus}
                  </span>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs">
                  {/* Flight Plan */}
                  <div>
                    <div className="text-xs text-green-500/70 mb-2 border-b border-green-500/30 pb-1">FLIGHT PLAN</div>
                    <div className="space-y-1">
                      <div className="flex justify-between">
                        <span className="text-green-500/70">DEPART:</span>
                        <span className="text-green-400 font-mono">
                          {flight.departureAirport?.name} ({flight.departureAirport?.codeIATA})
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-green-500/70">ARRIVAL:</span>
                        <span className="text-green-400 font-mono">
                          {flight.arrivalAirport?.name} ({flight.arrivalAirport?.codeIATA})
                        </span>
                      </div>
                      {flight.alternateAirport && (
                        <div className="flex justify-between">
                          <span className="text-green-500/70">ALTERNATE:</span>
                          <span className="text-green-400 font-mono">
                            {flight.alternateAirport.name} ({flight.alternateAirport.codeIATA})
                          </span>
                        </div>
                      )}
                      <div className="flex justify-between">
                        <span className="text-green-500/70">STD:</span>
                        <span className="text-green-400 font-mono">
                          {formatDate(flight.scheduledDeparture)}
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-green-500/70">STA:</span>
                        <span className="text-green-400 font-mono">
                          {formatDate(flight.scheduledArrival)}
                        </span>
                      </div>
                      {flight.estimatedTimeEnroute && (
                        <div className="flex justify-between">
                          <span className="text-green-500/70">ETE:</span>
                          <span className="text-green-400 font-mono">
                            {Math.floor(flight.estimatedTimeEnroute / 60)}H{flight.estimatedTimeEnroute % 60}MIN
                          </span>
                        </div>
                      )}
                      {flight.cruiseAltitude && (
                        <div className="flex justify-between">
                          <span className="text-green-500/70">ALT:</span>
                          <span className="text-green-400 font-mono">{flight.cruiseAltitude} FT</span>
                        </div>
                      )}
                      {flight.cruiseSpeed && (
                        <div className="flex justify-between">
                          <span className="text-green-500/70">SPD:</span>
                          <span className="text-green-400 font-mono">{flight.cruiseSpeed} KT</span>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Weather */}
                  <div>
                    <div className="text-xs text-green-500/70 mb-2 border-b border-green-500/30 pb-1">WEATHER</div>
                    
                    {/* Departure Weather */}
                    {flight.departureAirport?.weather && (
                      <div className="mb-3 p-2 border border-green-500/30 bg-green-500/10">
                        <div className="text-xs font-semibold text-green-500/70 mb-1">
                          DEP: {flight.departureAirport.codeIATA}
                        </div>
                        <div className="space-y-1 text-xs">
                          <div className="flex justify-between">
                            <span className="text-green-500/70">TEMP:</span>
                            <span className="text-green-400">{flight.departureAirport.weather.temperature}°C</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-green-500/70">WIND:</span>
                            <span className="text-green-400">{flight.departureAirport.weather.windSpeed} KT / {flight.departureAirport.weather.windDirection}°</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-green-500/70">VIS:</span>
                            <span className="text-green-400">{flight.departureAirport.weather.visibility} KM</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-green-500/70">COND:</span>
                            <span className="text-green-400">{flight.departureAirport.weather.conditions}</span>
                          </div>
                          {flight.departureAirport.weather.alert && (
                            <div className="mt-1 px-2 py-1 border border-red-500/50 bg-red-500/20 text-red-400 text-xs">
                              ⚠️ WX ALERT
                            </div>
                          )}
                        </div>
                      </div>
                    )}

                    {/* Arrival Weather */}
                    {flight.arrivalAirport?.weather && (
                      <div className="p-2 border border-green-500/30 bg-green-500/10">
                        <div className="text-xs font-semibold text-green-500/70 mb-1">
                          ARR: {flight.arrivalAirport.codeIATA}
                        </div>
                        <div className="space-y-1 text-xs">
                          <div className="flex justify-between">
                            <span className="text-green-500/70">TEMP:</span>
                            <span className="text-green-400">{flight.arrivalAirport.weather.temperature}°C</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-green-500/70">WIND:</span>
                            <span className="text-green-400">{flight.arrivalAirport.weather.windSpeed} KT / {flight.arrivalAirport.weather.windDirection}°</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-green-500/70">VIS:</span>
                            <span className="text-green-400">{flight.arrivalAirport.weather.visibility} KM</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-green-500/70">COND:</span>
                            <span className="text-green-400">{flight.arrivalAirport.weather.conditions}</span>
                          </div>
                          {flight.arrivalAirport.weather.alert && (
                            <div className="mt-1 px-2 py-1 border border-red-500/50 bg-red-500/20 text-red-400 text-xs">
                              ⚠️ WX ALERT
                            </div>
                          )}
                        </div>
                      </div>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
        </section>
      </main>
    </div>
  )
}

export default PilotDashboard
