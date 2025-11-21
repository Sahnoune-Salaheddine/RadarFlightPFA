import React, { useState, useEffect } from 'react'
import api from '../services/api'

function AircraftList({ selectedAircraft, onAircraftSelect }) {
  const [aircraft, setAircraft] = useState([])

  useEffect(() => {
    fetchAircraft()
    const interval = setInterval(fetchAircraft, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchAircraft = async () => {
    try {
      const response = await api.get('/aircraft')
      setAircraft(response.data)
    } catch (error) {
      console.error('Erreur lors du chargement des avions:', error)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'EN_VOL':
        return 'bg-green-100 text-green-800'
      case 'ATTERRISSAGE':
        return 'bg-yellow-100 text-yellow-800'
      case 'DECOLLAGE':
        return 'bg-blue-100 text-blue-800'
      case 'AU_SOL':
        return 'bg-gray-100 text-gray-800'
      case 'EN_ATTENTE':
        return 'bg-orange-100 text-orange-800'
      default:
        return 'bg-gray-100 text-gray-800'
    }
  }

  return (
    <div className="bg-white rounded-lg shadow-lg">
      <div className="p-4 border-b">
        <h2 className="text-xl font-semibold text-gray-800">Liste des avions</h2>
      </div>
      <div className="overflow-y-auto max-h-96">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Immatriculation</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Modèle</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Altitude</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Vitesse</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase">Statut</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {aircraft.map(ac => (
              <tr
                key={ac.id}
                onClick={() => onAircraftSelect(ac)}
                className={`cursor-pointer hover:bg-gray-50 ${
                  selectedAircraft?.id === ac.id ? 'bg-blue-50' : ''
                }`}
              >
                <td className="px-4 py-3 text-sm font-medium text-gray-900">{ac.registration}</td>
                <td className="px-4 py-3 text-sm text-gray-500">{ac.model}</td>
                <td className="px-4 py-3 text-sm text-gray-500">{ac.altitude?.toFixed(0) || 0} m</td>
                <td className="px-4 py-3 text-sm text-gray-500">{ac.speed?.toFixed(0) || 0} km/h</td>
                <td className="px-4 py-3">
                  <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(ac.status)}`}>
                    {ac.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default AircraftList

