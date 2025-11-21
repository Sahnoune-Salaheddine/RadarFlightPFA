import React, { useState, useEffect } from 'react'
import api from '../services/api'

function CommunicationPanel({ selectedAircraft }) {
  const [communications, setCommunications] = useState([])
  const [newMessage, setNewMessage] = useState('')

  useEffect(() => {
    fetchCommunications()
    const interval = setInterval(fetchCommunications, 3000)
    return () => clearInterval(interval)
  }, [selectedAircraft])

  const fetchCommunications = async () => {
    try {
      let response
      if (selectedAircraft) {
        response = await api.get(`/radar/aircraft/${selectedAircraft.id}/messages`)
        setCommunications(response.data.slice(-10).reverse()) // Dernières 10 communications
      } else {
        // Si pas d'avion sélectionné, ne pas essayer de charger les communications radar
        // (nécessite une authentification radar)
        setCommunications([])
      }
    } catch (error) {
      // Ne pas logger les erreurs 403 (Forbidden) - c'est normal si l'utilisateur n'a pas les permissions
      if (error.response?.status !== 403) {
        console.error('Erreur lors du chargement des communications:', error)
      }
      setCommunications([])
    }
  }

  const sendMessage = async () => {
    if (!newMessage.trim() || !selectedAircraft) return

    try {
      await api.post('/radar/sendMessage', {
        radarCenterId: 1, // TODO: utiliser le radar de l'utilisateur
        receiverType: 'AIRCRAFT',
        receiverId: selectedAircraft.id,
        message: newMessage
      })
      setNewMessage('')
      fetchCommunications()
    } catch (error) {
      console.error('Erreur lors de l\'envoi du message:', error)
    }
  }

  const formatTimestamp = (timestamp) => {
    const date = new Date(timestamp)
    return date.toLocaleTimeString('fr-FR')
  }

  return (
    <div className="bg-white rounded-lg shadow-lg">
      <div className="p-4 border-b">
        <h2 className="text-xl font-semibold text-gray-800">Communications VHF</h2>
        {selectedAircraft && (
          <p className="text-sm text-gray-600 mt-1">Avion: {selectedAircraft.registration}</p>
        )}
      </div>
      <div className="p-4">
        <div className="h-64 overflow-y-auto space-y-2 mb-4">
          {communications.length === 0 ? (
            <p className="text-gray-500 text-sm text-center py-4">Aucune communication</p>
          ) : (
            communications.map(comm => (
              <div key={comm.id} className="border-l-4 border-blue-500 pl-3 py-2 bg-gray-50 rounded">
                <div className="flex justify-between items-start mb-1">
                  <span className="text-xs font-semibold text-gray-700">
                    {comm.senderType} → {comm.receiverType}
                  </span>
                  <span className="text-xs text-gray-500">{formatTimestamp(comm.timestamp)}</span>
                </div>
                <p className="text-sm text-gray-800">{comm.message}</p>
                <p className="text-xs text-gray-500 mt-1">Fréquence: {comm.frequency} MHz</p>
              </div>
            ))
          )}
        </div>
        
        {selectedAircraft && (
          <div className="space-y-2">
            <textarea
              value={newMessage}
              onChange={(e) => setNewMessage(e.target.value)}
              placeholder="Tapez votre message..."
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent text-sm"
              rows="2"
            />
            <button
              onClick={sendMessage}
              className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition duration-200 text-sm"
            >
              Envoyer
            </button>
          </div>
        )}
      </div>
    </div>
  )
}

export default CommunicationPanel
