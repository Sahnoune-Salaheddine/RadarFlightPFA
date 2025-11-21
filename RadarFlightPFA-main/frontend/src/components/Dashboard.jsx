import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import FlightMap from './FlightMap'
import AircraftList from './AircraftList'
import WeatherPanel from './WeatherPanel'
import CommunicationPanel from './CommunicationPanel'
import AlertPanel from './AlertPanel'

function Dashboard() {
  const { user, logout } = useAuth()
  const [selectedAircraft, setSelectedAircraft] = useState(null)
  const [refreshKey, setRefreshKey] = useState(0)

  useEffect(() => {
    const interval = setInterval(() => {
      setRefreshKey(prev => prev + 1)
    }, 5000) // Rafraîchir toutes les 5 secondes

    return () => clearInterval(interval)
  }, [])

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Header */}
      <header className="bg-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-4">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-bold text-gray-800">Flight Radar</h1>
              <p className="text-sm text-gray-600">Suivi en temps réel des avions</p>
            </div>
            <div className="flex items-center space-x-4">
              <div className="text-right">
                <p className="text-sm font-medium text-gray-700">{user?.username}</p>
                <p className="text-xs text-gray-500">{user?.role}</p>
              </div>
              <button
                onClick={logout}
                className="px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition duration-200"
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
          {/* Carte principale */}
          <div className="lg:col-span-2 space-y-6">
            <FlightMap 
              key={refreshKey}
              selectedAircraft={selectedAircraft}
              onAircraftSelect={setSelectedAircraft}
            />
            <AircraftList 
              key={refreshKey}
              selectedAircraft={selectedAircraft}
              onAircraftSelect={setSelectedAircraft}
            />
          </div>

          {/* Panneaux latéraux */}
          <div className="space-y-6">
            <AlertPanel key={refreshKey} />
            <WeatherPanel key={refreshKey} />
            <CommunicationPanel 
              key={refreshKey}
              selectedAircraft={selectedAircraft}
            />
          </div>
        </div>
      </main>
    </div>
  )
}

export default Dashboard
