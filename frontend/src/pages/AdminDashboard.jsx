import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import api from '../services/api'
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
  const [dashboardData, setDashboardData] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchDashboardData()
    const interval = setInterval(fetchDashboardData, 10000) // Rafraîchir toutes les 10 secondes
    return () => clearInterval(interval)
  }, [])

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

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-xl">Chargement du dashboard admin...</div>
      </div>
    )
  }

  if (!dashboardData) {
    return (
      <div className="min-h-screen bg-gray-900 flex items-center justify-center">
        <div className="text-white text-xl">Erreur de chargement des données</div>
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

  return (
    <div className="min-h-screen bg-gray-900 text-white">
      {/* Header */}
      <header className="bg-gray-800 border-b border-gray-700 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold">Dashboard Administrateur</h1>
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
        {/* KPIs Temps Réel */}
        <section className="mb-8">
          <h2 className="text-xl font-bold mb-4">KPIs Temps Réel</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <div className="text-gray-400 text-sm">Avions en vol</div>
              <div className="text-3xl font-bold text-blue-400 mt-2">
                {dashboardData.aircraftInFlight || 0}
              </div>
            </div>
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <div className="text-gray-400 text-sm">Pilotes connectés</div>
              <div className="text-3xl font-bold text-green-400 mt-2">
                {dashboardData.pilotsConnected || 0}
              </div>
            </div>
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <div className="text-gray-400 text-sm">Décollages aujourd'hui</div>
              <div className="text-3xl font-bold text-yellow-400 mt-2">
                {dashboardData.takeoffsLandingsToday?.takeoffs || 0}
              </div>
            </div>
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <div className="text-gray-400 text-sm">Atterrissages aujourd'hui</div>
              <div className="text-3xl font-bold text-purple-400 mt-2">
                {dashboardData.takeoffsLandingsToday?.landings || 0}
              </div>
            </div>
          </div>
        </section>

        {/* Graphiques */}
        <section className="mb-8">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Trafic par aéroport */}
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <h3 className="text-lg font-bold mb-4">Trafic par Aéroport</h3>
              <Bar data={trafficData} options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: { labels: { color: '#fff' } }
                },
                scales: {
                  x: { ticks: { color: '#fff' }, grid: { color: '#374151' } },
                  y: { ticks: { color: '#fff' }, grid: { color: '#374151' } }
                }
              }} />
            </div>

            {/* Charge des centres radar */}
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <h3 className="text-lg font-bold mb-4">Charge des Centres Radar</h3>
              <Bar data={radarLoadData} options={{
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                  legend: { labels: { color: '#fff' } }
                },
                scales: {
                  x: { ticks: { color: '#fff' }, grid: { color: '#374151' } },
                  y: { ticks: { color: '#fff' }, grid: { color: '#374151' }, max: 100 }
                }
              }} />
            </div>
          </div>
        </section>

        {/* Retards */}
        <section className="mb-8">
          <h2 className="text-xl font-bold mb-4">Statistiques de Retards</h2>
          <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <div className="text-gray-400 text-sm">Retard total (minutes)</div>
                <div className="text-2xl font-bold text-red-400 mt-2">
                  {dashboardData.delays?.totalDelayMinutes || 0}
                </div>
              </div>
              <div>
                <div className="text-gray-400 text-sm">Vols retardés</div>
                <div className="text-2xl font-bold text-yellow-400 mt-2">
                  {dashboardData.delays?.delayedFlights || 0}
                </div>
              </div>
              <div>
                <div className="text-gray-400 text-sm">Retard moyen (minutes)</div>
                <div className="text-2xl font-bold text-orange-400 mt-2">
                  {dashboardData.delays?.averageDelay || 0}
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Indicateurs de sécurité */}
        <section className="mb-8">
          <h2 className="text-xl font-bold mb-4">Indicateurs de Sécurité</h2>
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <h3 className="text-lg font-bold mb-4">Score de Sécurité</h3>
              <div className="h-64">
                <Pie data={safetyData} options={{
                  responsive: true,
                  maintainAspectRatio: false,
                  plugins: {
                    legend: { labels: { color: '#fff' } }
                  }
                }} />
              </div>
              <div className="mt-4 text-center">
                <div className="text-4xl font-bold text-green-400">
                  {safetyScore}%
                </div>
              </div>
            </div>
            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <h3 className="text-lg font-bold mb-4">Détails</h3>
              <div className="space-y-3">
                <div className="flex justify-between">
                  <span className="text-gray-400">Conflits potentiels</span>
                  <span className="font-bold">
                    {dashboardData.safetyIndicators?.potentialConflicts || 0}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-400">Avions en vol</span>
                  <span className="font-bold">
                    {dashboardData.safetyIndicators?.aircraftInFlight || 0}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </section>

        {/* Performance ATC */}
        <section className="mb-8">
          <h2 className="text-xl font-bold mb-4">Performance ATC</h2>
          <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <div className="text-gray-400 text-sm">Total avions</div>
                <div className="text-2xl font-bold text-blue-400 mt-2">
                  {dashboardData.atcPerformance?.totalAircraft || 0}
                </div>
              </div>
              <div>
                <div className="text-gray-400 text-sm">Avions en vol</div>
                <div className="text-2xl font-bold text-green-400 mt-2">
                  {dashboardData.atcPerformance?.aircraftInFlight || 0}
                </div>
              </div>
              <div>
                <div className="text-gray-400 text-sm">Efficacité</div>
                <div className="text-2xl font-bold text-yellow-400 mt-2">
                  {dashboardData.atcPerformance?.efficiency || 0}%
                </div>
              </div>
            </div>
          </div>
        </section>
      </main>
    </div>
  )
}

export default AdminDashboard

