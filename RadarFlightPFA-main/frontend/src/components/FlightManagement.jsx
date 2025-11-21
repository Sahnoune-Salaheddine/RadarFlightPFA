import React, { useState, useEffect } from 'react'
import api from '../services/api'

function FlightManagement() {
  const [flights, setFlights] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingFlight, setEditingFlight] = useState(null)
  const [aircrafts, setAircrafts] = useState([])
  const [airports, setAirports] = useState([])
  const [pilots, setPilots] = useState([])
  
  const [formData, setFormData] = useState({
    flightNumber: '',
    airline: '',
    aircraftId: '',
    departureAirportId: '',
    arrivalAirportId: '',
    alternateAirportId: '',
    scheduledDeparture: '',
    scheduledArrival: '',
    cruiseAltitude: '',
    cruiseSpeed: '',
    flightType: 'COMMERCIAL',
    pilotId: '',
    flightStatus: 'PLANIFIE'
  })

  useEffect(() => {
    fetchFlights()
    fetchAircrafts()
    fetchAirports()
    fetchPilots()
  }, [])

  const fetchFlights = async () => {
    try {
      const response = await api.get('/flight')
      setFlights(response.data)
      setLoading(false)
    } catch (error) {
      console.error('Erreur chargement vols:', error)
      setLoading(false)
    }
  }

  const fetchAircrafts = async () => {
    try {
      const response = await api.get('/aircraft')
      setAircrafts(response.data)
    } catch (error) {
      console.error('Erreur chargement avions:', error)
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

  const fetchPilots = async () => {
    try {
      const response = await api.get('/auth/users')
      const pilotsList = response.data.filter(u => u.role === 'PILOTE')
      setPilots(pilotsList)
    } catch (error) {
      console.error('Erreur chargement pilotes:', error)
    }
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
    
    // Calculer ETE si les deux dates sont présentes
    if (name === 'scheduledDeparture' || name === 'scheduledArrival') {
      const dep = name === 'scheduledDeparture' ? value : formData.scheduledDeparture
      const arr = name === 'scheduledArrival' ? value : formData.scheduledArrival
      
      if (dep && arr) {
        const depDate = new Date(dep)
        const arrDate = new Date(arr)
        const diffMinutes = Math.round((arrDate - depDate) / (1000 * 60))
        if (diffMinutes > 0) {
          // L'ETE sera calculé automatiquement côté backend
        }
      }
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    
    try {
      const data = {
        ...formData,
        aircraftId: parseInt(formData.aircraftId),
        departureAirportId: parseInt(formData.departureAirportId),
        arrivalAirportId: parseInt(formData.arrivalAirportId),
        alternateAirportId: formData.alternateAirportId ? parseInt(formData.alternateAirportId) : null,
        cruiseAltitude: formData.cruiseAltitude ? parseInt(formData.cruiseAltitude) : null,
        cruiseSpeed: formData.cruiseSpeed ? parseInt(formData.cruiseSpeed) : null,
        pilotId: formData.pilotId ? parseInt(formData.pilotId) : null
      }
      
      console.log('=== DONNÉES ENVOYÉES AU SERVEUR ===')
      console.log('Données complètes:', JSON.stringify(data, null, 2))
      console.log('formData original:', formData)
      
      if (editingFlight) {
        await api.put(`/flight/manage/${editingFlight.id}`, data)
        alert('Vol modifié avec succès')
      } else {
        await api.post('/flight/manage', data)
        alert('Vol créé avec succès')
      }
      
      setShowForm(false)
      setEditingFlight(null)
      resetForm()
      fetchFlights()
      
    } catch (error) {
      console.error('=== ERREUR LORS DE LA SAUVEGARDE ===')
      console.error('Erreur complète:', error)
      console.error('Status:', error.response?.status)
      console.error('Status Text:', error.response?.statusText)
      console.error('Headers:', error.response?.headers)
      console.error('Réponse complète:', error.response)
      console.error('Données de la réponse:', error.response?.data)
      console.error('Message d\'erreur:', error.message)
      
      // Extraire le message d'erreur
      let errorMessage = 'Erreur inconnue'
      if (error.response?.data) {
        if (typeof error.response.data === 'string') {
          errorMessage = error.response.data
        } else if (error.response.data.error) {
          errorMessage = error.response.data.error
        } else if (error.response.data.message) {
          errorMessage = error.response.data.message
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      const errorType = error.response?.data?.type || 'UNKNOWN'
      const errorDetails = error.response?.data?.details || ''
      
      let fullMessage = errorMessage
      if (errorDetails) {
        fullMessage += '\n\nDétails techniques: ' + errorDetails
      }
      
      console.error('Message d\'erreur final:', fullMessage)
      console.error('Type d\'erreur:', errorType)
      console.error('Détails complets de l\'erreur:', error.response?.data)
      
      // Construire un message d'erreur détaillé
      let displayMessage = errorMessage
      if (errorDetails) {
        displayMessage += '\n\nDétails techniques:\n' + errorDetails
      }
      if (error.response?.data?.details) {
        displayMessage += '\n\nDétails supplémentaires:\n' + error.response.data.details
      }
      
      // Afficher une alerte avec le message d'erreur
      alert('❌ Erreur lors de la sauvegarde:\n\n' + displayMessage + '\n\nVérifiez la console (F12) pour plus de détails.')
    }
  }

  const handleEdit = (flight) => {
    if (flight.status === 'EN_COURS') {
      alert('Impossible de modifier un vol en cours')
      return
    }
    
    setEditingFlight(flight)
    setFormData({
      flightNumber: flight.flightNumber || '',
      airline: flight.airline || '',
      aircraftId: flight.aircraft?.id || '',
      departureAirportId: flight.departureAirport?.id || '',
      arrivalAirportId: flight.arrivalAirport?.id || '',
      alternateAirportId: flight.alternateAirport?.id || '',
      scheduledDeparture: flight.scheduledDeparture ? new Date(flight.scheduledDeparture).toISOString().slice(0, 16) : '',
      scheduledArrival: flight.scheduledArrival ? new Date(flight.scheduledArrival).toISOString().slice(0, 16) : '',
      cruiseAltitude: flight.cruiseAltitude || '',
      cruiseSpeed: flight.cruiseSpeed || '',
      flightType: flight.flightType || 'COMMERCIAL',
      pilotId: flight.pilot?.id || '',
      flightStatus: flight.status || 'PLANIFIE'
    })
    setShowForm(true)
  }

  const handleDelete = async (flightId, flightNumber) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir supprimer le vol ${flightNumber} ?`)) {
      return
    }
    
    try {
      await api.delete(`/flight/manage/${flightId}`)
      alert('Vol supprimé avec succès')
      fetchFlights()
    } catch (error) {
      console.error('Erreur suppression vol:', error)
      alert('Erreur: ' + (error.response?.data?.error || error.message))
    }
  }

  const resetForm = () => {
    setFormData({
      flightNumber: '',
      airline: '',
      aircraftId: '',
      departureAirportId: '',
      arrivalAirportId: '',
      alternateAirportId: '',
      scheduledDeparture: '',
      scheduledArrival: '',
      cruiseAltitude: '',
      cruiseSpeed: '',
      flightType: 'COMMERCIAL',
      pilotId: '',
      flightStatus: 'PLANIFIE'
    })
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'EN_COURS': return 'bg-green-100 text-green-800'
      case 'PLANIFIE': return 'bg-blue-100 text-blue-800'
      case 'TERMINE': return 'bg-gray-100 text-gray-800'
      case 'ANNULE': return 'bg-red-100 text-red-800'
      case 'RETARDE': return 'bg-yellow-100 text-yellow-800'
      default: return 'bg-gray-100 text-gray-800'
    }
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

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <h2 className="text-2xl font-bold">Gestion des Vols</h2>
        <button
          onClick={() => {
            setShowForm(!showForm)
            setEditingFlight(null)
            resetForm()
          }}
          className="px-4 py-2 bg-blue-600 hover:bg-blue-700 rounded-lg transition"
        >
          {showForm ? 'Annuler' : '+ Nouveau Vol'}
        </button>
      </div>

      {/* Formulaire de création/modification */}
      {showForm && (
        <div className="bg-gray-800 rounded-lg p-6 border border-gray-700">
          <h3 className="text-xl font-bold mb-4">
            {editingFlight ? 'Modifier le vol' : 'Créer un nouveau vol'}
          </h3>
          
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Numéro de vol / Callsign *
                </label>
                <input
                  type="text"
                  name="flightNumber"
                  value={formData.flightNumber}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                  placeholder="AT123"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Compagnie aérienne *
                </label>
                <input
                  type="text"
                  name="airline"
                  value={formData.airline}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                  placeholder="Royal Air Maroc"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Avion *
                </label>
                <select
                  name="aircraftId"
                  value={formData.aircraftId}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Sélectionner un avion</option>
                  {aircrafts.map(ac => (
                    <option key={ac.id} value={ac.id}>
                      {ac.registration} - {ac.model}
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Pilote assigné
                </label>
                <select
                  name="pilotId"
                  value={formData.pilotId}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Aucun pilote assigné</option>
                  {pilots.map(pilot => (
                    <option key={pilot.id} value={pilot.pilotId || ''}>
                      {pilot.username}
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Aéroport de départ *
                </label>
                <select
                  name="departureAirportId"
                  value={formData.departureAirportId}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Sélectionner un aéroport</option>
                  {airports.map(airport => (
                    <option key={airport.id} value={airport.id}>
                      {airport.name} ({airport.codeIATA})
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Aéroport d'arrivée *
                </label>
                <select
                  name="arrivalAirportId"
                  value={formData.arrivalAirportId}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Sélectionner un aéroport</option>
                  {airports.map(airport => (
                    <option key={airport.id} value={airport.id}>
                      {airport.name} ({airport.codeIATA})
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Aéroport alternatif
                </label>
                <select
                  name="alternateAirportId"
                  value={formData.alternateAirportId}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="">Aucun</option>
                  {airports.map(airport => (
                    <option key={airport.id} value={airport.id}>
                      {airport.name} ({airport.codeIATA})
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Type de vol *
                </label>
                <select
                  name="flightType"
                  value={formData.flightType}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="COMMERCIAL">Commercial</option>
                  <option value="CARGO">Cargo</option>
                  <option value="PRIVATE">Privé</option>
                  <option value="MILITARY">Militaire</option>
                  <option value="TRAINING">Entraînement</option>
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  STD (Heure départ prévue) *
                </label>
                <input
                  type="datetime-local"
                  name="scheduledDeparture"
                  value={formData.scheduledDeparture}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  STA (Heure arrivée prévue) *
                </label>
                <input
                  type="datetime-local"
                  name="scheduledArrival"
                  value={formData.scheduledArrival}
                  onChange={handleInputChange}
                  required
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Altitude de croisière (pieds)
                </label>
                <input
                  type="number"
                  name="cruiseAltitude"
                  value={formData.cruiseAltitude}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                  placeholder="35000"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Vitesse de croisière (nœuds)
                </label>
                <input
                  type="number"
                  name="cruiseSpeed"
                  value={formData.cruiseSpeed}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                  placeholder="450"
                />
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-300 mb-1">
                  Statut initial
                </label>
                <select
                  name="flightStatus"
                  value={formData.flightStatus}
                  onChange={handleInputChange}
                  className="w-full bg-gray-700 border border-gray-600 rounded-lg px-4 py-2 text-white"
                >
                  <option value="PLANIFIE">Planifié</option>
                  <option value="RETARDE">Retardé</option>
                </select>
              </div>
            </div>
            
            <div className="flex gap-4 pt-4">
              <button
                type="submit"
                className="px-6 py-2 bg-green-600 hover:bg-green-700 rounded-lg transition"
              >
                {editingFlight ? 'Modifier' : 'Créer'}
              </button>
              <button
                type="button"
                onClick={() => {
                  setShowForm(false)
                  setEditingFlight(null)
                  resetForm()
                }}
                className="px-6 py-2 bg-gray-600 hover:bg-gray-700 rounded-lg transition"
              >
                Annuler
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Liste des vols */}
      <div className="bg-gray-800 rounded-lg border border-gray-700 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-400">Chargement...</div>
        ) : flights.length === 0 ? (
          <div className="p-8 text-center text-gray-400">Aucun vol trouvé</div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead className="bg-gray-700">
                <tr>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Vol</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Compagnie</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Départ</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Arrivée</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">STD</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">STA</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Statut</th>
                  <th className="px-4 py-3 text-left text-xs font-medium text-gray-300 uppercase">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-700">
                {flights.map((flight) => (
                  <tr key={flight.id} className="hover:bg-gray-750">
                    <td className="px-4 py-3 whitespace-nowrap font-mono">{flight.flightNumber}</td>
                    <td className="px-4 py-3 whitespace-nowrap">{flight.airline || 'N/A'}</td>
                    <td className="px-4 py-3 whitespace-nowrap">{flight.departureAirport || 'N/A'}</td>
                    <td className="px-4 py-3 whitespace-nowrap">{flight.arrivalAirport || 'N/A'}</td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm">
                      {formatDateTime(flight.scheduledDeparture)}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap text-sm">
                      {formatDateTime(flight.scheduledArrival)}
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <span className={`px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(flight.status)}`}>
                        {flight.status}
                      </span>
                    </td>
                    <td className="px-4 py-3 whitespace-nowrap">
                      <div className="flex gap-2">
                        <button
                          onClick={() => handleEdit(flight)}
                          disabled={flight.status === 'EN_COURS'}
                          className="px-3 py-1 bg-blue-600 hover:bg-blue-700 rounded text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Modifier
                        </button>
                        <button
                          onClick={() => handleDelete(flight.id, flight.flightNumber)}
                          disabled={flight.status === 'EN_COURS'}
                          className="px-3 py-1 bg-red-600 hover:bg-red-700 rounded text-sm disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                          Supprimer
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  )
}

export default FlightManagement

