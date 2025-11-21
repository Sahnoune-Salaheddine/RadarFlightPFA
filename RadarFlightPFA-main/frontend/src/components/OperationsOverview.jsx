import React, { useState, useEffect } from 'react'
import api from '../services/api'
import { Bar, Line, Pie } from 'react-chartjs-2'

function OperationsOverview() {
  const [activeTab, setActiveTab] = useState('traffic')
  const [loading, setLoading] = useState(true)
  
  // Section A: Trafic
  const [trafficData, setTrafficData] = useState(null)
  const [trafficPeriod, setTrafficPeriod] = useState('DAY')
  
  // Section B: Performance KPIs
  const [performanceKPIs, setPerformanceKPIs] = useState(null)
  
  // Section C: Utilisateurs
  const [users, setUsers] = useState([])
  const [userSearch, setUserSearch] = useState('')
  const [userFilter, setUserFilter] = useState('ALL')
  
  // Section D: Systèmes Radar
  const [radarSystems, setRadarSystems] = useState([])
  
  // Section E: Météo globale
  const [weatherData, setWeatherData] = useState(null)
  
  // Section F: Logs
  const [logs, setLogs] = useState([])
  const [logsPage, setLogsPage] = useState(0)
  const [logsTotalPages, setLogsTotalPages] = useState(0)
  const [logsFilters, setLogsFilters] = useState({
    userId: '',
    activityType: '',
    severity: '',
    startDate: '',
    endDate: ''
  })
  
  // Section G: Alertes
  const [alerts, setAlerts] = useState(null)
  
  // Section H: Rapports
  const [reports, setReports] = useState(null)
  const [reportsPeriod, setReportsPeriod] = useState('WEEK')

  useEffect(() => {
    fetchAllData()
    const interval = setInterval(fetchAllData, 30000) // Rafraîchir toutes les 30 secondes
    return () => clearInterval(interval)
  }, [])

  useEffect(() => {
    if (activeTab === 'traffic') {
      fetchTrafficData()
    } else if (activeTab === 'performance') {
      fetchPerformanceKPIs()
    } else if (activeTab === 'users') {
      fetchUsers()
    } else if (activeTab === 'radar') {
      fetchRadarSystems()
    } else if (activeTab === 'weather') {
      fetchWeatherData()
    } else if (activeTab === 'logs') {
      fetchLogs()
    } else if (activeTab === 'alerts') {
      fetchAlerts()
    } else if (activeTab === 'reports') {
      fetchReports()
    }
  }, [activeTab, trafficPeriod, reportsPeriod, logsPage, logsFilters])

  const fetchAllData = async () => {
    setLoading(true)
    try {
      await Promise.all([
        fetchTrafficData(),
        fetchPerformanceKPIs(),
        fetchUsers(),
        fetchRadarSystems(),
        fetchWeatherData(),
        fetchAlerts()
      ])
    } catch (error) {
      console.error('Erreur chargement données:', error)
    } finally {
      setLoading(false)
    }
  }

  const fetchTrafficData = async () => {
    try {
      const response = await api.get(`/admin/operations/traffic?period=${trafficPeriod}`)
      setTrafficData(response.data)
    } catch (error) {
      console.error('Erreur chargement trafic:', error)
    }
  }

  const fetchPerformanceKPIs = async () => {
    try {
      const response = await api.get('/admin/operations/performance')
      setPerformanceKPIs(response.data)
    } catch (error) {
      console.error('Erreur chargement KPIs performance:', error)
    }
  }

  const fetchUsers = async () => {
    try {
      const response = await api.get('/admin/operations/users')
      setUsers(response.data.users || [])
    } catch (error) {
      console.error('Erreur chargement utilisateurs:', error)
    }
  }

  const fetchRadarSystems = async () => {
    try {
      const response = await api.get('/admin/operations/radar-systems')
      setRadarSystems(response.data.systems || [])
    } catch (error) {
      console.error('Erreur chargement systèmes radar:', error)
    }
  }

  const fetchWeatherData = async () => {
    try {
      const response = await api.get('/admin/operations/weather')
      setWeatherData(response.data)
    } catch (error) {
      console.error('Erreur chargement météo:', error)
    }
  }

  const fetchLogs = async () => {
    try {
      const params = new URLSearchParams({
        page: logsPage.toString(),
        size: '50'
      })
      if (logsFilters.userId) params.append('userId', logsFilters.userId)
      if (logsFilters.activityType) params.append('activityType', logsFilters.activityType)
      if (logsFilters.severity) params.append('severity', logsFilters.severity)
      if (logsFilters.startDate) params.append('startDate', logsFilters.startDate)
      if (logsFilters.endDate) params.append('endDate', logsFilters.endDate)
      
      const response = await api.get(`/admin/operations/logs?${params}`)
      setLogs(response.data.logs || [])
      setLogsTotalPages(response.data.totalPages || 0)
    } catch (error) {
      console.error('Erreur chargement logs:', error)
    }
  }

  const fetchAlerts = async () => {
    try {
      const response = await api.get('/admin/operations/alerts')
      setAlerts(response.data)
    } catch (error) {
      console.error('Erreur chargement alertes:', error)
    }
  }

  const fetchReports = async () => {
    try {
      const response = await api.get(`/admin/operations/reports?period=${reportsPeriod}`)
      setReports(response.data)
    } catch (error) {
      console.error('Erreur chargement rapports:', error)
    }
  }

  const exportToCSV = (data, filename) => {
    if (!data || data.length === 0) return
    
    const headers = Object.keys(data[0])
    const csvContent = [
      headers.join(','),
      ...data.map(row => headers.map(header => {
        const value = row[header]
        return typeof value === 'string' ? `"${value}"` : value
      }).join(','))
    ].join('\n')
    
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' })
    const link = document.createElement('a')
    link.href = URL.createObjectURL(blob)
    link.download = filename
    link.click()
  }

  const exportToPDF = () => {
    window.print()
  }

  const getHealthStatusColor = (status) => {
    switch (status) {
      case 'HEALTHY': return 'text-green-400 bg-green-900/30'
      case 'WARNING': return 'text-yellow-400 bg-yellow-900/30'
      case 'CRITICAL': return 'text-red-400 bg-red-900/30'
      default: return 'text-gray-400 bg-gray-800'
    }
  }

  const getSeverityColor = (severity) => {
    switch (severity) {
      case 'HIGH': return 'text-red-400 bg-red-900/30'
      case 'MEDIUM': return 'text-yellow-400 bg-yellow-900/30'
      case 'LOW': return 'text-blue-400 bg-blue-900/30'
      default: return 'text-gray-400 bg-gray-800'
    }
  }

  const filteredUsers = users.filter(user => {
    const matchesSearch = !userSearch || 
      user.username?.toLowerCase().includes(userSearch.toLowerCase()) ||
      user.pilotName?.toLowerCase().includes(userSearch.toLowerCase())
    const matchesFilter = userFilter === 'ALL' || 
      (userFilter === 'ACTIVE' && user.isActive) ||
      (userFilter === 'INACTIVE' && !user.isActive) ||
      (userFilter === user.role)
    return matchesSearch && matchesFilter
  })

  // Graphique trafic par jour
  const trafficChartData = trafficData?.byDay ? {
    labels: Object.keys(trafficData.byDay),
    datasets: [{
      label: 'Vols',
      data: Object.values(trafficData.byDay),
      backgroundColor: 'rgba(59, 130, 246, 0.5)',
      borderColor: 'rgba(59, 130, 246, 1)',
      borderWidth: 1
    }]
  } : null

  // Graphique performance (camembert)
  const performanceChartData = performanceKPIs ? {
    labels: ['À l\'heure', 'Retardés', 'Annulés'],
    datasets: [{
      data: [
        performanceKPIs.onTimeFlights || 0,
        performanceKPIs.delayedFlights || 0,
        performanceKPIs.cancelledFlights || 0
      ],
      backgroundColor: [
        'rgba(34, 197, 94, 0.8)',
        'rgba(234, 179, 8, 0.8)',
        'rgba(239, 68, 68, 0.8)'
      ],
      borderWidth: 0
    }]
  } : null

  const tabs = [
    { id: 'traffic', label: 'A) Trafic', icon: '✈️' },
    { id: 'performance', label: 'B) Performance', icon: '📊' },
    { id: 'users', label: 'C) Utilisateurs', icon: '👥' },
    { id: 'radar', label: 'D) Systèmes Radar', icon: '📡' },
    { id: 'weather', label: 'E) Météo', icon: '🌦️' },
    { id: 'logs', label: 'F) Journal', icon: '📝' },
    { id: 'alerts', label: 'G) Alertes', icon: '⚠️' },
    { id: 'reports', label: 'H) Rapports', icon: '📈' }
  ]

  return (
    <div className="bg-gray-900 text-white">
      {/* Navigation par onglets */}
      <div className="bg-gray-800 border-b border-gray-700 sticky top-0 z-40">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex space-x-1 overflow-x-auto">
            {tabs.map(tab => (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`px-4 py-3 text-sm font-medium whitespace-nowrap border-b-2 transition ${
                  activeTab === tab.id
                    ? 'border-blue-500 text-blue-400'
                    : 'border-transparent text-gray-400 hover:text-gray-300 hover:border-gray-600'
                }`}
              >
                <span className="mr-2">{tab.icon}</span>
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Contenu des sections */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {loading && activeTab !== 'logs' && (
          <div className="text-center py-12 text-gray-400">Chargement...</div>
        )}

        {/* Section A: Trafic */}
        {activeTab === 'traffic' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-bold">A) Nombre total de vols / Trafic</h2>
              <select
                value={trafficPeriod}
                onChange={(e) => setTrafficPeriod(e.target.value)}
                className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white"
              >
                <option value="DAY">Jour</option>
                <option value="WEEK">Semaine</option>
                <option value="MONTH">Mois</option>
              </select>
            </div>

            {trafficData && (
              <>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Total vols</div>
                    <div className="text-3xl font-bold text-blue-400 mt-2">
                      {trafficData.totalFlights || 0}
                    </div>
                    <div className="text-xs text-gray-500 mt-1">
                      Période: {trafficPeriod}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">En cours</div>
                    <div className="text-3xl font-bold text-green-400 mt-2">
                      {trafficData.byStatus?.EN_COURS || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Planifiés</div>
                    <div className="text-3xl font-bold text-yellow-400 mt-2">
                      {trafficData.byStatus?.PLANIFIE || 0}
                    </div>
                  </div>
                </div>

                {trafficChartData && (
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <h3 className="text-lg font-bold mb-4">Évolution du trafic</h3>
                    <div className="h-64">
                      <Line
                        data={trafficChartData}
                        options={{
                          responsive: true,
                          maintainAspectRatio: false,
                          plugins: {
                            legend: { labels: { color: '#fff' } }
                          },
                          scales: {
                            x: { ticks: { color: '#fff' }, grid: { color: '#374151' } },
                            y: { ticks: { color: '#fff' }, grid: { color: '#374151' } }
                          }
                        }}
                      />
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {/* Section B: Performance KPIs */}
        {activeTab === 'performance' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold">B) KPI de Performance</h2>

            {performanceKPIs && (
              <>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Retard moyen (min)</div>
                    <div className="text-3xl font-bold text-orange-400 mt-2">
                      {performanceKPIs.averageDelay || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Retard total (min)</div>
                    <div className="text-3xl font-bold text-red-400 mt-2">
                      {performanceKPIs.totalDelays || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Vols annulés</div>
                    <div className="text-3xl font-bold text-red-400 mt-2">
                      {performanceKPIs.cancelledFlights || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Efficacité opérationnelle</div>
                    <div className="text-3xl font-bold text-green-400 mt-2">
                      {performanceKPIs.operationalEfficiency?.toFixed(1) || 0}%
                    </div>
                  </div>
                </div>

                {performanceChartData && (
                  <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                    <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                      <h3 className="text-lg font-bold mb-4">Répartition des vols</h3>
                      <div className="h-64">
                        <Pie
                          data={performanceChartData}
                          options={{
                            responsive: true,
                            maintainAspectRatio: false,
                            plugins: {
                              legend: { labels: { color: '#fff' } }
                            }
                          }}
                        />
                      </div>
                    </div>
                    <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                      <h3 className="text-lg font-bold mb-4">Détails</h3>
                      <div className="space-y-4">
                        <div className="flex justify-between">
                          <span className="text-gray-400">Vols à l'heure</span>
                          <span className="font-bold text-green-400">
                            {performanceKPIs.onTimeFlights || 0}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-400">Vols retardés</span>
                          <span className="font-bold text-yellow-400">
                            {performanceKPIs.delayedFlights || 0}
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-400">Taux de ponctualité</span>
                          <span className="font-bold">
                            {performanceKPIs.onTimePercentage?.toFixed(1) || 0}%
                          </span>
                        </div>
                      </div>
                    </div>
                  </div>
                )}
              </>
            )}
          </div>
        )}

        {/* Section C: Utilisateurs */}
        {activeTab === 'users' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-bold">C) Utilisateurs / Rôles</h2>
              <div className="flex gap-4">
                <input
                  type="text"
                  placeholder="Rechercher..."
                  value={userSearch}
                  onChange={(e) => setUserSearch(e.target.value)}
                  className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white placeholder-gray-500"
                />
                <select
                  value={userFilter}
                  onChange={(e) => setUserFilter(e.target.value)}
                  className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white"
                >
                  <option value="ALL">Tous</option>
                  <option value="ACTIVE">Actifs</option>
                  <option value="INACTIVE">Inactifs</option>
                  <option value="ADMIN">Admin</option>
                  <option value="PILOTE">Pilote</option>
                  <option value="CENTRE_RADAR">Centre Radar</option>
                </select>
              </div>
            </div>

            <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-700">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Utilisateur</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Rôle</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Statut</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Informations</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-700">
                    {filteredUsers.map((user) => (
                      <tr key={user.id} className="hover:bg-gray-750">
                        <td className="px-4 py-3 whitespace-nowrap">
                          <div className="font-medium">{user.username}</div>
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                            user.role === 'ADMIN' ? 'bg-purple-900/30 text-purple-400' :
                            user.role === 'PILOTE' ? 'bg-blue-900/30 text-blue-400' :
                            'bg-green-900/30 text-green-400'
                          }`}>
                            {user.role}
                          </span>
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full ${
                            user.isActive 
                              ? 'bg-green-900/30 text-green-400' 
                              : 'bg-gray-700 text-gray-400'
                          }`}>
                            {user.isActive ? 'Actif' : 'Inactif'}
                          </span>
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-400">
                          {user.pilotName || user.license || '-'}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* Section D: Systèmes Radar */}
        {activeTab === 'radar' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold">D) Systèmes Radar / Infrastructure</h2>

            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {radarSystems.map((system) => (
                <div key={system.id} className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                  <div className="flex justify-between items-start mb-4">
                    <div>
                      <h3 className="text-lg font-bold">{system.name}</h3>
                      <p className="text-sm text-gray-400">{system.code}</p>
                    </div>
                    <span className={`px-3 py-1 text-xs font-semibold rounded-full ${getHealthStatusColor(system.healthStatus)}`}>
                      {system.healthStatus}
                    </span>
                  </div>
                  
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-gray-400">Aéroport</span>
                      <span className="font-medium">{system.airportCode || '-'}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-400">Avions suivis</span>
                      <span className="font-medium">{system.aircraftTracked || 0}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-400">Charge</span>
                      <span className={`font-medium ${
                        system.load >= 90 ? 'text-red-400' :
                        system.load >= 70 ? 'text-yellow-400' :
                        'text-green-400'
                      }`}>
                        {system.load || 0}%
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-400">Disponibilité</span>
                      <span className={`font-medium ${system.isAvailable ? 'text-green-400' : 'text-red-400'}`}>
                        {system.isAvailable ? 'Disponible' : 'Indisponible'}
                      </span>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Section E: Météo globale */}
        {activeTab === 'weather' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold">E) Météo Globale</h2>

            {weatherData && (
              <>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Stations météo</div>
                    <div className="text-3xl font-bold text-blue-400 mt-2">
                      {weatherData.totalStations || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Alertes actives</div>
                    <div className="text-3xl font-bold text-red-400 mt-2">
                      {weatherData.activeAlerts || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Vents forts</div>
                    <div className="text-3xl font-bold text-yellow-400 mt-2">
                      {weatherData.strongWindsCount || 0}
                    </div>
                  </div>
                </div>

                <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                  <h3 className="text-lg font-bold mb-4">Alertes SIGMET/AIRMET</h3>
                  <div className="space-y-3">
                    {weatherData.alerts && weatherData.alerts.length > 0 ? (
                      weatherData.alerts.map((alert, index) => (
                        <div key={index} className="bg-gray-700 rounded-lg p-4 border border-gray-600">
                          <div className="flex justify-between items-start">
                            <div>
                              <div className="flex items-center gap-2 mb-2">
                                <span className={`px-2 py-1 text-xs font-semibold rounded ${getSeverityColor(alert.severity)}`}>
                                  {alert.alertType}
                                </span>
                                <span className="text-sm font-medium">{alert.airportName}</span>
                                <span className="text-xs text-gray-400">({alert.airportCode})</span>
                              </div>
                              <p className="text-sm text-gray-300">{alert.conditions}</p>
                              <div className="flex gap-4 mt-2 text-xs text-gray-400">
                                <span>Vent: {alert.windSpeed} kt</span>
                                <span>Visibilité: {alert.visibility} km</span>
                                <span>Temp: {alert.temperature}°C</span>
                              </div>
                            </div>
                            <span className={`px-2 py-1 text-xs font-semibold rounded ${getSeverityColor(alert.severity)}`}>
                              {alert.severity}
                            </span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="text-gray-400 text-center py-4">Aucune alerte active</p>
                    )}
                  </div>
                </div>
              </>
            )}
          </div>
        )}

        {/* Section F: Journal / Logs */}
        {activeTab === 'logs' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold">F) Journal / Logs</h2>

            <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
              <div className="grid grid-cols-1 md:grid-cols-5 gap-4 mb-6">
                <input
                  type="text"
                  placeholder="User ID"
                  value={logsFilters.userId}
                  onChange={(e) => setLogsFilters({...logsFilters, userId: e.target.value})}
                  className="bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                />
                <select
                  value={logsFilters.activityType}
                  onChange={(e) => setLogsFilters({...logsFilters, activityType: e.target.value})}
                  className="bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Type d'activité</option>
                  <option value="LOGIN">Connexion</option>
                  <option value="LOGOUT">Déconnexion</option>
                  <option value="FLIGHT_CREATED">Vol créé</option>
                  <option value="FLIGHT_UPDATED">Vol modifié</option>
                  <option value="WEATHER_ALERT">Alerte météo</option>
                </select>
                <select
                  value={logsFilters.severity}
                  onChange={(e) => setLogsFilters({...logsFilters, severity: e.target.value})}
                  className="bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Sévérité</option>
                  <option value="INFO">Info</option>
                  <option value="WARNING">Avertissement</option>
                  <option value="ERROR">Erreur</option>
                  <option value="CRITICAL">Critique</option>
                </select>
                <input
                  type="date"
                  value={logsFilters.startDate}
                  onChange={(e) => setLogsFilters({...logsFilters, startDate: e.target.value})}
                  className="bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                />
                <input
                  type="date"
                  value={logsFilters.endDate}
                  onChange={(e) => setLogsFilters({...logsFilters, endDate: e.target.value})}
                  className="bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                />
              </div>

              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-gray-700">
                    <tr>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Date/Heure</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Utilisateur</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Type</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Description</th>
                      <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Sévérité</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-gray-700">
                    {logs.map((log) => (
                      <tr key={log.id} className="hover:bg-gray-750">
                        <td className="px-4 py-3 whitespace-nowrap text-sm">
                          {new Date(log.timestamp).toLocaleString('fr-FR')}
                        </td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">{log.username || '-'}</td>
                        <td className="px-4 py-3 whitespace-nowrap text-sm">{log.activityType}</td>
                        <td className="px-4 py-3 text-sm">{log.description}</td>
                        <td className="px-4 py-3 whitespace-nowrap">
                          <span className={`px-2 py-1 text-xs font-semibold rounded ${getSeverityColor(log.severity)}`}>
                            {log.severity}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              <div className="flex justify-between items-center mt-4">
                <button
                  onClick={() => setLogsPage(Math.max(0, logsPage - 1))}
                  disabled={logsPage === 0}
                  className="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg disabled:opacity-50"
                >
                  Précédent
                </button>
                <span className="text-gray-400">
                  Page {logsPage + 1} / {logsTotalPages || 1}
                </span>
                <button
                  onClick={() => setLogsPage(logsPage + 1)}
                  disabled={logsPage >= logsTotalPages - 1}
                  className="px-4 py-2 bg-gray-700 hover:bg-gray-600 rounded-lg disabled:opacity-50"
                >
                  Suivant
                </button>
              </div>
            </div>
          </div>
        )}

        {/* Section G: Alertes */}
        {activeTab === 'alerts' && (
          <div className="space-y-6">
            <h2 className="text-2xl font-bold">G) Alertes & Notifications</h2>

            {alerts && (
              <>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Priorité Haute</div>
                    <div className="text-3xl font-bold text-red-400 mt-2">
                      {alerts.highPriority || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Priorité Moyenne</div>
                    <div className="text-3xl font-bold text-yellow-400 mt-2">
                      {alerts.mediumPriority || 0}
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <div className="text-gray-400 text-sm">Priorité Basse</div>
                    <div className="text-3xl font-bold text-blue-400 mt-2">
                      {alerts.lowPriority || 0}
                    </div>
                  </div>
                </div>

                <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                  <h3 className="text-lg font-bold mb-4">Liste des alertes</h3>
                  <div className="space-y-3">
                    {alerts.allAlerts && alerts.allAlerts.length > 0 ? (
                      alerts.allAlerts.map((alert, index) => (
                        <div key={index} className="bg-gray-700 rounded-lg p-4 border border-gray-600">
                          <div className="flex justify-between items-start">
                            <div>
                              <div className="flex items-center gap-2 mb-2">
                                <span className={`px-2 py-1 text-xs font-semibold rounded ${getSeverityColor(alert.severity)}`}>
                                  {alert.type}
                                </span>
                                <span className="text-sm font-medium">{alert.description}</span>
                              </div>
                              {alert.airportName && (
                                <p className="text-xs text-gray-400">Aéroport: {alert.airportName}</p>
                              )}
                              {alert.radarName && (
                                <p className="text-xs text-gray-400">Radar: {alert.radarName}</p>
                              )}
                            </div>
                            <span className={`px-2 py-1 text-xs font-semibold rounded ${getSeverityColor(alert.severity)}`}>
                              {alert.severity}
                            </span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="text-gray-400 text-center py-4">Aucune alerte active</p>
                    )}
                  </div>
                </div>
              </>
            )}
          </div>
        )}

        {/* Section H: Rapports */}
        {activeTab === 'reports' && (
          <div className="space-y-6">
            <div className="flex justify-between items-center">
              <h2 className="text-2xl font-bold">H) Rapports / Analytics</h2>
              <div className="flex gap-4">
                <select
                  value={reportsPeriod}
                  onChange={(e) => setReportsPeriod(e.target.value)}
                  className="bg-gray-800 border border-gray-700 rounded-lg px-4 py-2 text-white"
                >
                  <option value="DAY">Jour</option>
                  <option value="WEEK">Semaine</option>
                  <option value="MONTH">Mois</option>
                </select>
                <button
                  onClick={() => exportToCSV(reports?.trafficStats ? [reports.trafficStats] : [], 'rapport.csv')}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg"
                >
                  Export CSV
                </button>
                <button
                  onClick={exportToPDF}
                  className="px-4 py-2 bg-red-600 hover:bg-red-700 rounded-lg"
                >
                  Export PDF
                </button>
              </div>
            </div>

            {reports && (
              <div className="space-y-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <h3 className="text-lg font-bold mb-4">Statistiques de trafic</h3>
                    <div className="space-y-3">
                      <div className="flex justify-between">
                        <span className="text-gray-400">Total vols</span>
                        <span className="font-bold">{reports.trafficStats?.totalFlights || 0}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-400">Période</span>
                        <span className="font-bold">{reports.period}</span>
                      </div>
                    </div>
                  </div>
                  <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
                    <h3 className="text-lg font-bold mb-4">Tendances</h3>
                    <div className="space-y-3">
                      <div className="flex justify-between">
                        <span className="text-gray-400">Évolution trafic</span>
                        <span className={`font-bold ${
                          (reports.trends?.trafficChange || 0) >= 0 ? 'text-green-400' : 'text-red-400'
                        }`}>
                          {reports.trends?.trafficChange?.toFixed(1) || 0}%
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

export default OperationsOverview

