import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import api from '../services/api'
import OperationsOverview from '../components/OperationsOverview'
import FlightManagement from '../components/FlightManagement'
import PilotsAircraftList from '../components/PilotsAircraftList'
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
} from 'chart.js'
import { Bar, Line, Pie } from 'react-chartjs-2'

// Enregistrer les composants Chart.js
ChartJS.register(
  CategoryScale,
  LinearScale,
  BarElement,
  LineElement,
  PointElement,
  ArcElement,
  Title,
  Tooltip,
  Legend
)

function AdminDashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [dashboardData, setDashboardData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [flights, setFlights] = useState([])
  const [flightsLoading, setFlightsLoading] = useState(true)
  const [showOperationsOverview, setShowOperationsOverview] = useState(false)
  const [showFlightManagement, setShowFlightManagement] = useState(false)
  const [time, setTime] = useState(0)
  const [activeTab, setActiveTab] = useState('dashboard')

  useEffect(() => {
    const timeInterval = setInterval(() => setTime(t => t + 1), 50)
    return () => clearInterval(timeInterval)
  }, [])

  useEffect(() => {
    fetchDashboardData()
    fetchFlights()
    const interval = setInterval(() => {
      fetchDashboardData()
      fetchFlights()
    }, 10000) // Rafraîchir toutes les 10 secondes
    return () => clearInterval(interval)
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const fetchDashboardData = async () => {
    try {
      const response = await api.get('/admin/dashboard')
      setDashboardData(response.data)
      setLoading(false)
    } catch (error) {
      console.error('Erreur chargement dashboard admin:', error)
      setLoading(false)
    }
  }

  const fetchFlights = async () => {
    try {
      const response = await api.get('/flight')
      setFlights(response.data)
      setFlightsLoading(false)
    } catch (error) {
      console.error('Erreur chargement vols:', error)
      setFlightsLoading(false)
    }
  }

  const formatTime = (dateTime) => {
    if (!dateTime) return 'N/A'
    return new Date(dateTime).toLocaleTimeString('fr-FR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    })
  }

  const formatDateTime = (dateTime) => {
    if (!dateTime) return 'N/A'
    return new Date(dateTime).toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'EN_COURS':
        return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50'
      case 'PLANIFIE':
        return 'bg-cyan-500/20 text-cyan-400 border-cyan-500/50'
      case 'TERMINE':
        return 'bg-gray-500/20 text-gray-400 border-gray-500/50'
      case 'ANNULE':
        return 'bg-red-500/20 text-red-400 border-red-500/50'
      case 'RETARDE':
        return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50'
      default:
        return 'bg-gray-500/20 text-gray-400 border-gray-500/50'
    }
  }

  // Aéroports du Maroc
  const airports = [
    { code: 'CMN', name: 'Casablanca', x: 45, y: 52, lat: 33.3675, lon: -7.5898 },
    { code: 'RBA', name: 'Rabat', x: 42, y: 45, lat: 34.0515, lon: -6.7515 },
    { code: 'RAK', name: 'Marrakech', x: 48, y: 65, lat: 31.6069, lon: -8.0363 },
    { code: 'TNG', name: 'Tanger', x: 38, y: 35, lat: 35.7269, lon: -5.9169 },
  ]

  if (loading) {
    return (
      <div className="min-h-screen bg-black text-white flex items-center justify-center relative overflow-hidden">
        {/* Animated Background */}
        <div className="fixed inset-0">
          {[...Array(30)].map((_, i) => (
            <div
              key={i}
              className="absolute w-1 h-1 bg-white rounded-full"
              style={{
                left: `${Math.random() * 100}%`,
                top: `${Math.random() * 100}%`,
                opacity: 0.3 + Math.sin((time + i * 50) * 0.02) * 0.3
              }}
            />
          ))}
        </div>
        <div className="relative z-10 text-cyan-400 text-xl border border-cyan-500/50 px-6 py-3 bg-white/5 backdrop-blur-xl rounded-xl">
          LOADING ADMIN DASHBOARD...
        </div>
      </div>
    )
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-black text-white flex items-center justify-center relative overflow-hidden">
        <div className="relative z-10 text-red-400 text-xl border border-red-500/50 px-6 py-3 bg-white/5 backdrop-blur-xl rounded-xl">
          ERROR LOADING DATA
        </div>
      </div>
    )
  }

  // Préparer les données pour les graphiques
  const trafficByAirport = dashboardData.trafficByAirport || {}
  const airportNames = Object.keys(trafficByAirport)
  const trafficData = {
    labels: airportNames,
    datasets: [
      {
        label: 'En vol',
        data: airportNames.map(name => trafficByAirport[name]?.inFlight || 0),
        backgroundColor: 'rgba(59, 130, 246, 0.5)',
        borderColor: 'rgba(59, 130, 246, 1)',
        borderWidth: 1
      },
      {
        label: 'Au sol',
        data: airportNames.map(name => trafficByAirport[name]?.onGround || 0),
        backgroundColor: 'rgba(34, 197, 94, 0.5)',
        borderColor: 'rgba(34, 197, 94, 1)',
        borderWidth: 1
      }
    ]
  }

  const radarLoadData = {
    labels: (dashboardData.radarCentersStatus || []).map(r => r.name),
    datasets: [
      {
        label: 'Charge (%)',
        data: (dashboardData.radarCentersStatus || []).map(r => r.load || 0),
        backgroundColor: 'rgba(239, 68, 68, 0.5)',
        borderColor: 'rgba(239, 68, 68, 1)',
        borderWidth: 1
      }
    ]
  }

  const safetyScore = dashboardData.safetyIndicators?.safetyScore || 0
  const safetyData = {
    labels: ['Score de sécurité'],
    datasets: [
      {
        data: [safetyScore, 100 - safetyScore],
        backgroundColor: [
          safetyScore >= 80 ? 'rgba(34, 197, 94, 0.8)' : 
          safetyScore >= 60 ? 'rgba(234, 179, 8, 0.8)' : 
          'rgba(239, 68, 68, 0.8)',
          'rgba(107, 114, 128, 0.3)'
        ],
        borderWidth: 0
      }
    ]
  }

  // Vols actifs pour l'affichage
  const activeFlights = flights.filter(f => f.status === 'EN_COURS').slice(0, 3)
  
  // Données pour les communications ATC
  const atcHistory = dashboardData?.atcHistory || []

  return (
    <div className="min-h-screen bg-black text-white overflow-hidden relative">
      {/* Animated Background */}
      <div className="fixed inset-0">
        {/* Stars */}
        {[...Array(50)].map((_, i) => (
          <div
            key={i}
            className="absolute w-1 h-1 bg-white rounded-full pointer-events-none"
            style={{
              left: `${Math.random() * 100}%`,
              top: `${Math.random() * 100}%`,
              opacity: 0.3 + Math.sin((time + i * 50) * 0.02) * 0.3
            }}
          />
        ))}
        
        {/* Gradient Orbs */}
        <div 
          className="absolute w-[600px] h-[600px] rounded-full blur-3xl opacity-20 pointer-events-none"
          style={{
            background: 'radial-gradient(circle, #7c3aed 0%, transparent 70%)',
            left: '60%',
            top: '30%',
            transform: `translate(-50%, -50%) scale(${1 + Math.sin(time * 0.02) * 0.1})`
          }}
        />
        <div 
          className="absolute w-[400px] h-[400px] rounded-full blur-3xl opacity-20 pointer-events-none"
          style={{
            background: 'radial-gradient(circle, #06b6d4 0%, transparent 70%)',
            left: '20%',
            top: '70%',
            transform: `translate(-50%, -50%) scale(${1 + Math.cos(time * 0.02) * 0.1})`
          }}
        />
      </div>

      {/* Glass Header */}
      <header className="relative z-[100] px-8 py-4 bg-white/5 backdrop-blur-xl border-b border-white/10 sticky top-0">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-6">
            {/* Logo */}
            <div className="flex items-center gap-3">
              <div className="relative">
                <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-violet-500 via-purple-500 to-cyan-500 p-0.5">
                  <div className="w-full h-full rounded-2xl bg-black/80 flex items-center justify-center">
                    <svg className="w-8 h-8 text-white" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z"/>
                    </svg>
                  </div>
                </div>
              </div>
            <div>
                <h1 className="text-xl font-bold">RadarFlight</h1>
                <p className="text-xs text-gray-500">Morocco Air Traffic Control</p>
              </div>
            </div>
            {/* Nav Tabs */}
            <nav className="flex items-center gap-1 bg-white/5 backdrop-blur-xl rounded-full p-1 border border-white/10">
              {['Dashboard', 'Flights', 'Radar', 'Weather'].map((tab) => (
              <button
                  key={tab}
                onClick={() => {
                    if (tab === 'Flights') {
                      setShowFlightManagement(!showFlightManagement)
                      setShowOperationsOverview(false)
                    } else if (tab === 'Radar') {
                  setShowOperationsOverview(!showOperationsOverview)
                  setShowFlightManagement(false)
                    }
                    setActiveTab(tab.toLowerCase())
                  }}
                  className={`px-4 py-2 rounded-full text-sm transition-all ${
                    activeTab === tab.toLowerCase() || 
                    (tab === 'Flights' && showFlightManagement) ||
                    (tab === 'Radar' && showOperationsOverview)
                      ? 'bg-gradient-to-r from-violet-500 to-cyan-500 text-white' 
                      : 'text-gray-400 hover:text-white'
                  }`}
                >
                  {tab}
              </button>
              ))}
            </nav>
          </div>
          {/* Right Side */}
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 bg-white/5 backdrop-blur-xl rounded-full px-4 py-2 border border-white/10">
              <div className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"/>
              <span className="text-sm text-gray-300">{dashboardData.aircraftInFlight || 0} vols actifs</span>
            </div>
            <div className="w-10 h-10 rounded-full bg-gradient-to-br from-violet-500 to-cyan-500 flex items-center justify-center text-sm font-bold">
              {user?.username?.charAt(0).toUpperCase() || 'AD'}
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
      <main className="relative z-10 px-8 py-6 grid grid-cols-12 gap-6">
        {showFlightManagement ? (
          <div className="col-span-12">
          <FlightManagement />
          </div>
        ) : showOperationsOverview ? (
          <div className="col-span-12">
          <OperationsOverview />
          </div>
        ) : (
          <>
        {/* Left Stats */}
        <div className="col-span-3 space-y-4 overflow-y-auto max-h-[calc(100vh-120px)]">
          {/* Stats Cards */}
          <div className="grid grid-cols-2 gap-3">
            {[
              { label: 'Vols Actifs', value: dashboardData.aircraftInFlight || 0, color: 'from-violet-500 to-purple-600' },
              { label: 'Au Sol', value: (dashboardData.takeoffsLandingsToday?.takeoffs || 0) - (dashboardData.aircraftInFlight || 0), color: 'from-cyan-500 to-blue-600' },
              { label: 'Alertes', value: dashboardData.safetyIndicators?.potentialConflicts || 0, color: 'from-orange-500 to-red-600' },
              { label: 'Retards', value: dashboardData.delays?.delayedFlights || 0, color: 'from-yellow-500 to-orange-600' },
            ].map((stat, i) => (
              <div
                key={stat.label}
                className="bg-white/5 backdrop-blur-xl rounded-2xl p-4 border border-white/10 hover:border-white/20 transition-all cursor-pointer group"
                style={{
                  transform: `translateY(${Math.sin((time + i * 30) * 0.03) * 3}px)`
                }}
              >
                <div className={`text-3xl font-bold bg-gradient-to-r ${stat.color} bg-clip-text text-transparent`}>
                  {stat.value}
                </div>
                <div className="text-xs text-gray-500 mt-1">{stat.label}</div>
              </div>
            ))}
          </div>

          {/* Flight List */}
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-4">
            <div className="flex items-center justify-between mb-4">
              <h2 className="font-semibold">Vols en cours</h2>
              <button 
                onClick={() => setShowFlightManagement(true)}
                className="text-xs text-violet-400 hover:text-violet-300"
              >
                Voir tout
              </button>
            </div>
            <div className="space-y-3">
              {activeFlights.length === 0 ? (
                <div className="text-gray-400 text-sm text-center py-4">NO ACTIVE FLIGHTS</div>
              ) : (
                activeFlights.map((flight, i) => {
                  const status = flight.status || 'PLANIFIE'
                  const isInFlight = status === 'EN_COURS'
                  return (
                    <div
                      key={flight.id}
                      className="bg-white/5 rounded-xl p-3 border border-white/5 hover:border-violet-500/50 transition-all cursor-pointer"
                      style={{
                        transform: `translateX(${Math.sin((time + i * 20) * 0.02) * 2}px)`
                      }}
                    >
                      <div className="flex items-center justify-between mb-2">
                        <span className="font-mono font-bold text-violet-400">{flight.flightNumber || 'N/A'}</span>
                        <span className={`text-xs px-2 py-0.5 rounded-full border ${
                          isInFlight
                            ? 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50' 
                            : 'bg-cyan-500/20 text-cyan-400 border-cyan-500/50'
                        }`}>
                          {status}
                        </span>
                      </div>
                      <div className="flex items-center justify-between text-sm">
                        <span className="text-gray-400">{flight.departureAirport || 'N/A'} → {flight.arrivalAirport || 'N/A'}</span>
                        <span className="text-gray-500">{formatTime(flight.estimatedArrival) || 'N/A'}</span>
                      </div>
              </div>
                  )
                })
              )}
            </div>
          </div>
        </div>

        {/* Center - 3D Globe/Map */}
        <div className="col-span-6 space-y-4 overflow-y-auto max-h-[calc(100vh-120px)]">
          <div className="bg-white/5 backdrop-blur-xl rounded-3xl border border-white/10 p-6 h-[500px] relative overflow-hidden">
            {/* Globe Effect */}
            <div className="absolute inset-0 flex items-center justify-center">
              {/* Outer Ring */}
              <div 
                className="absolute w-80 h-80 rounded-full border-2 border-violet-500/30"
                style={{
                  transform: `rotateX(75deg) rotateZ(${time * 0.5}deg)`,
                }}
              />
              <div 
                className="absolute w-72 h-72 rounded-full border border-cyan-500/20"
                style={{
                  transform: `rotateX(75deg) rotateZ(${-time * 0.3}deg)`,
                }}
              />
              
              {/* Morocco Map Silhouette */}
              <div className="absolute w-64 h-64 rounded-full bg-gradient-to-br from-violet-900/50 to-cyan-900/50 border border-white/10 overflow-hidden">
                {/* Grid lines */}
                <svg className="absolute inset-0 w-full h-full opacity-30">
                  {[...Array(8)].map((_, i) => (
                    <line key={`h${i}`} x1="0" y1={`${i * 12.5}%`} x2="100%" y2={`${i * 12.5}%`} stroke="white" strokeWidth="0.5"/>
                  ))}
                  {[...Array(8)].map((_, i) => (
                    <line key={`v${i}`} x1={`${i * 12.5}%`} y1="0" x2={`${i * 12.5}%`} y2="100%" stroke="white" strokeWidth="0.5"/>
                  ))}
                </svg>
                
                {/* Airports */}
                {airports.map((airport, i) => (
                  <div
                    key={airport.code}
                    className="absolute group cursor-pointer"
                    style={{ left: `${airport.x}%`, top: `${airport.y}%` }}
                  >
                    <div 
                      className="w-3 h-3 bg-cyan-400 rounded-full shadow-lg shadow-cyan-400/50"
                      style={{
                        animation: `pulse 2s ease-in-out ${i * 0.3}s infinite`
                      }}
                    />
                    <div className="absolute -top-6 left-1/2 -translate-x-1/2 bg-black/80 px-2 py-1 rounded text-xs whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity border border-white/20">
                      {airport.code}
                    </div>
                  </div>
                ))}

                {/* Flight Paths */}
                <svg className="absolute inset-0 w-full h-full">
                  <defs>
                    <linearGradient id="pathGrad" x1="0%" y1="0%" x2="100%" y2="0%">
                      <stop offset="0%" stopColor="#8b5cf6"/>
                      <stop offset="100%" stopColor="#06b6d4"/>
                    </linearGradient>
                  </defs>
                  {/* CMN to RAK */}
                  <path 
                    d="M 45% 52% Q 50% 58% 48% 65%"
                    fill="none"
                    stroke="url(#pathGrad)"
                    strokeWidth="2"
                    strokeDasharray="4 4"
                    style={{
                      strokeDashoffset: -time * 0.5
                    }}
                  />
                  {/* RBA to TNG */}
                  <path 
                    d="M 42% 45% Q 38% 40% 38% 35%"
                    fill="none"
                    stroke="url(#pathGrad)"
                    strokeWidth="2"
                    strokeDasharray="4 4"
                    style={{
                      strokeDashoffset: -time * 0.5
                    }}
                  />
                </svg>

                {/* Moving Aircraft */}
                {activeFlights.slice(0, 2).map((flight, i) => (
                  <div
                    key={flight.id}
                    className="absolute"
                    style={{
                      left: `${45 + Math.sin((time + i * 50) * 0.02) * 3}%`,
                      top: `${52 + ((time + i * 30) * 0.05) % 13}%`,
                    }}
                  >
                    <div className="w-2 h-2 bg-emerald-400 rounded-full shadow-lg shadow-emerald-400/50"/>
                  </div>
                ))}
              </div>

              {/* Rotating ring */}
              <div 
                className="absolute w-96 h-96 rounded-full border border-dashed border-white/10"
                style={{
                  transform: `rotateX(70deg) rotateZ(${time}deg)`,
                }}
              />
            </div>

            {/* Corner Info */}
            <div className="absolute top-4 left-4 text-xs text-gray-500 bg-black/50 px-2 py-1 rounded">
              <div>LAT: 33.5731° N</div>
              <div>LON: 7.5898° W</div>
              </div>
            <div className="absolute top-4 right-4 text-xs text-gray-500 text-right bg-black/50 px-2 py-1 rounded">
              <div>ZOOM: 100%</div>
              <div>MODE: LIVE</div>
            </div>
          </div>

          {/* Charts Section */}
          <div className="grid grid-cols-2 gap-4">
            {/* Trafic par aéroport */}
            <div className="bg-white/5 backdrop-blur-xl rounded-2xl p-4 border border-white/10">
              <h3 className="text-sm font-semibold mb-3 text-violet-400">Trafic par Aéroport</h3>
              <div className="h-48">
              <Bar data={trafficData} options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: { labels: { color: '#fff', font: { size: 10 } } }
                },
                scales: {
                    x: { ticks: { color: '#fff', font: { size: 10 } }, grid: { color: 'rgba(255,255,255,0.1)' } },
                    y: { ticks: { color: '#fff', font: { size: 10 } }, grid: { color: 'rgba(255,255,255,0.1)' } }
                }
              }} />
              </div>
            </div>

            {/* Charge des centres radar */}
            <div className="bg-white/5 backdrop-blur-xl rounded-2xl p-4 border border-white/10">
              <h3 className="text-sm font-semibold mb-3 text-cyan-400">Charge Radar</h3>
              <div className="h-48">
              <Bar data={radarLoadData} options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: { labels: { color: '#fff', font: { size: 10 } } }
                  },
                  scales: {
                    x: { ticks: { color: '#fff', font: { size: 10 } }, grid: { color: 'rgba(255,255,255,0.1)' } },
                    y: { ticks: { color: '#fff', font: { size: 10 } }, grid: { color: 'rgba(255,255,255,0.1)' }, max: 100 }
                  }
                }} />
              </div>
            </div>
          </div>
        </div>

        {/* Right Panel */}
        <div className="col-span-3 space-y-4 overflow-y-auto max-h-[calc(100vh-120px)]">
          {/* Weather Widget */}
          <div className="bg-gradient-to-br from-violet-600/20 to-cyan-600/20 backdrop-blur-xl rounded-2xl border border-white/10 p-4">
            <div className="flex items-center justify-between mb-3">
              <h2 className="font-semibold">Météo CMN</h2>
              <span className="text-xs text-gray-500">Maintenant</span>
            </div>
            <div className="flex items-center gap-4">
              <div className="text-5xl">☀️</div>
              <div>
                <div className="text-3xl font-bold">24°C</div>
                <div className="text-sm text-gray-400">Ensoleillé</div>
              </div>
            </div>
            <div className="grid grid-cols-3 gap-2 mt-4 text-center text-xs">
              <div className="bg-white/10 rounded-lg p-2">
                <div className="text-gray-400">Vent</div>
                <div className="font-semibold">12 km/h</div>
              </div>
              <div className="bg-white/10 rounded-lg p-2">
                <div className="text-gray-400">Visibilité</div>
                <div className="font-semibold">10 km</div>
              </div>
              <div className="bg-white/10 rounded-lg p-2">
                <div className="text-gray-400">Pression</div>
                <div className="font-semibold">1013 hPa</div>
              </div>
            </div>
          </div>

          {/* Safety Score */}
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-4">
            <h2 className="font-semibold mb-3 text-violet-400">Score de Sécurité</h2>
            <div className="h-48 mb-4">
              <Pie data={safetyData} options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: { labels: { color: '#fff', font: { size: 10 } } }
                }
              }} />
            </div>
            <div className="text-center">
              <div className={`text-4xl font-bold ${
                safetyScore >= 80 ? 'text-emerald-400' : 
                safetyScore >= 60 ? 'text-yellow-400' : 
                'text-red-400'
              }`}>
                {safetyScore}%
              </div>
            </div>
          </div>

          {/* Recent Communications */}
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-4">
            <h2 className="font-semibold mb-3">Communications</h2>
            <div className="space-y-2 text-sm max-h-48 overflow-y-auto">
              {atcHistory.length === 0 ? (
                <div className="text-gray-400 text-xs text-center py-4">NO COMMUNICATIONS</div>
              ) : (
                atcHistory.slice(0, 5).map((comm, i) => (
                  <div key={i} className="bg-white/5 rounded-lg p-2 border-l-2 border-violet-500">
                    <div className="flex justify-between text-xs text-gray-500 mb-1">
                      <span className="text-violet-400">{comm.type || 'ATC'}</span>
                      <span>{formatTime(comm.timestamp)}</span>
                    </div>
                    <div className="text-gray-300 text-xs">{comm.message}</div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Quick Actions */}
          <div className="grid grid-cols-2 gap-2">
            <button 
              onClick={() => setShowFlightManagement(true)}
              className="bg-gradient-to-r from-violet-500 to-purple-600 rounded-xl p-3 text-sm font-semibold hover:opacity-90 transition-opacity"
            >
              + Nouveau Vol
            </button>
            <button className="bg-white/10 rounded-xl p-3 text-sm font-semibold hover:bg-white/20 transition-colors border border-white/10">
              Alertes
            </button>
          </div>
        </div>

        {/* Full Width Sections Below */}
        <div className="col-span-12 space-y-4">
          {/* Delays Stats */}
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-6">
            <h2 className="text-lg font-semibold mb-4 text-violet-400">Statistiques de Retards</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-white/5 rounded-xl p-4 border border-white/10">
                <div className="text-gray-400 text-sm">Retard total (minutes)</div>
                <div className="text-2xl font-bold text-red-400 mt-2">
                  {dashboardData.delays?.totalDelayMinutes || 0}
                </div>
              </div>
              <div className="bg-white/5 rounded-xl p-4 border border-white/10">
                <div className="text-gray-400 text-sm">Vols retardés</div>
                <div className="text-2xl font-bold text-yellow-400 mt-2">
                  {dashboardData.delays?.delayedFlights || 0}
                </div>
              </div>
              <div className="bg-white/5 rounded-xl p-4 border border-white/10">
                <div className="text-gray-400 text-sm">Retard moyen (minutes)</div>
                <div className="text-2xl font-bold text-orange-400 mt-2">
                  {dashboardData.delays?.averageDelay || 0}
                </div>
              </div>
            </div>
          </div>

          {/* Flights Table */}
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 overflow-hidden">
            <div className="p-4 border-b border-white/10">
              <h2 className="text-lg font-semibold text-violet-400">Vols en Cours et Planifiés</h2>
            </div>
            {flightsLoading ? (
              <div className="p-8 text-center text-gray-400">LOADING FLIGHTS...</div>
            ) : flights.length === 0 ? (
              <div className="p-8 text-center text-gray-400">NO FLIGHTS FOUND</div>
            ) : (
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-white/5">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        Matricule
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        Numéro de vol
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        Départ
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        Arrivée
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        Heure départ
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        ETA
                      </th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase tracking-wider">
                        Statut
                      </th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-white/10">
                    {flights.map((flight) => (
                      <tr key={flight.id} className="hover:bg-white/5 transition-colors">
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {flight.aircraftRegistration || 'N/A'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm font-mono text-violet-400">
                          {flight.flightNumber || 'N/A'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {flight.departureAirport || 'N/A'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {flight.arrivalAirport || 'N/A'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {formatTime(flight.actualDeparture) || formatTime(flight.scheduledDeparture) || 'N/A'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {formatTime(flight.estimatedArrival) || formatTime(flight.scheduledArrival) || 'N/A'}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full border ${getStatusColor(flight.status)}`}>
                            {flight.status || 'N/A'}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

        {/* Performance ATC */}
          <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-6">
            <h2 className="text-lg font-semibold mb-4 text-cyan-400">Performance ATC</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="bg-white/5 rounded-xl p-4 border border-white/10">
                <div className="text-gray-400 text-sm">Total avions</div>
                <div className="text-2xl font-bold text-blue-400 mt-2">
                  {dashboardData.atcPerformance?.totalAircraft || 0}
                </div>
              </div>
              <div className="bg-white/5 rounded-xl p-4 border border-white/10">
                <div className="text-gray-400 text-sm">Avions en vol</div>
                <div className="text-2xl font-bold text-green-400 mt-2">
                  {dashboardData.atcPerformance?.aircraftInFlight || 0}
                </div>
              </div>
              <div className="bg-white/5 rounded-xl p-4 border border-white/10">
                <div className="text-gray-400 text-sm">Efficacité</div>
                <div className="text-2xl font-bold text-yellow-400 mt-2">
                  {dashboardData.atcPerformance?.efficiency || 0}%
                </div>
              </div>
            </div>
          </div>

          {/* Pilotes et Avions Assignés */}
          <PilotsAircraftList />
        </div>
          </>
        )}
      </main>
      <style>{`
        @keyframes pulse {
          0%, 100% { transform: scale(1); opacity: 1; }
          50% { transform: scale(1.5); opacity: 0.5; }
        }
      `}</style>
    </div>
  )
}

export default AdminDashboard

