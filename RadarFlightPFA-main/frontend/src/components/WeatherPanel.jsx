import React, { useState, useEffect } from 'react'
import api from '../services/api'

function WeatherPanel() {
  const [weatherData, setWeatherData] = useState({})
  const [airports, setAirports] = useState([])

  useEffect(() => {
    fetchAirports()
    const interval = setInterval(() => {
      airports.forEach(airport => fetchWeather(airport.id))
    }, 600000) // Toutes les 10 minutes
    return () => clearInterval(interval)
  }, [airports])

  const fetchAirports = async () => {
    try {
      const response = await api.get('/airports')
      setAirports(response.data)
      response.data.forEach(airport => fetchWeather(airport.id))
    } catch (error) {
      console.error('Erreur lors du chargement des aéroports:', error)
    }
  }

  const fetchWeather = async (airportId) => {
    try {
      const response = await api.get(`/weather/airport/${airportId}`)
      setWeatherData(prev => ({ ...prev, [airportId]: response.data }))
    } catch (error) {
      console.error(`Erreur lors du chargement de la météo pour l'aéroport ${airportId}:`, error)
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-lg">
      <div className="p-4 border-b">
        <h2 className="text-xl font-semibold text-gray-800">Météo</h2>
      </div>
      <div className="p-4 space-y-4">
        {airports
          .filter(airport => weatherData[airport.id]) // Filtrer avant le map pour éviter les clés dupliquées
          .map(airport => {
            const weather = weatherData[airport.id]
            
            return (
              <div key={`weather-${airport.id}`} className="border rounded-lg p-3">
              <h3 className="font-semibold text-gray-800 mb-2">{airport.name}</h3>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div>
                  <span className="text-gray-600">Température:</span>
                  <span className="ml-2 font-medium">{weather.temperature?.toFixed(1) || 'N/A'}°C</span>
                </div>
                <div>
                  <span className="text-gray-600">Vent:</span>
                  <span className="ml-2 font-medium">{weather.windSpeed?.toFixed(0) || 0} km/h</span>
                </div>
                <div>
                  <span className="text-gray-600">Visibilité:</span>
                  <span className="ml-2 font-medium">{weather.visibility?.toFixed(1) || 0} km</span>
                </div>
                <div>
                  <span className="text-gray-600">Vent travers:</span>
                  <span className="ml-2 font-medium">{weather.crosswind?.toFixed(1) || 0} km/h</span>
                </div>
                <div className="col-span-2">
                  <span className="text-gray-600">Conditions:</span>
                  <span className="ml-2 font-medium">{weather.conditions || 'N/A'}</span>
                </div>
                {weather.alert && (
                  <div className="col-span-2">
                    <span className="px-2 py-1 bg-red-100 text-red-800 text-xs font-semibold rounded">
                      ⚠️ Alerte météo
                    </span>
                  </div>
                )}
              </div>
            </div>
          )
        })}
      </div>
    </div>
  )
}

export default WeatherPanel

