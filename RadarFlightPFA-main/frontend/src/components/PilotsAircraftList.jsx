import React, { useState, useEffect } from 'react'
import api from '../services/api'

function PilotsAircraftList() {
  const [pilotsData, setPilotsData] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    fetchPilotsAndAircraft()
    const interval = setInterval(() => {
      fetchPilotsAndAircraft()
    }, 10000) // Rafraîchir toutes les 10 secondes
    return () => clearInterval(interval)
  }, [])

  const fetchPilotsAndAircraft = async () => {
    try {
      // Récupérer tous les avions (qui contiennent les infos du pilote)
      const aircraftResponse = await api.get('/aircraft')
      const aircrafts = aircraftResponse.data

      // Récupérer tous les utilisateurs pilotes (essayer d'abord l'endpoint admin qui a plus d'infos)
      let pilotUsers = []
      try {
        const usersResponse = await api.get('/admin/operations/users')
        pilotUsers = (usersResponse.data.users || []).filter(u => u.role === 'PILOTE')
      } catch (error) {
        // Fallback sur l'endpoint auth si admin n'est pas disponible
        const usersResponse = await api.get('/auth/users')
        pilotUsers = usersResponse.data.filter(u => u.role === 'PILOTE')
      }

      // Créer une map des pilotes avec leurs avions
      const pilotsMap = new Map()

      // Pour chaque utilisateur pilote
      pilotUsers.forEach(user => {
        const pilotInfo = {
          id: user.id,
          username: user.username,
          pilotId: user.pilotId,
          name: user.pilotName || user.name || user.username,
          license: user.license || null,
          experienceYears: user.experienceYears || 0,
          aircrafts: []
        }
        pilotsMap.set(user.username, pilotInfo)
      })

      // Assigner les avions aux pilotes
      aircrafts.forEach(aircraft => {
        if (aircraft.pilot) {
          const pilotUsername = aircraft.pilot.user?.username || aircraft.usernamePilote
          if (pilotUsername && pilotsMap.has(pilotUsername)) {
            const pilotInfo = pilotsMap.get(pilotUsername)
            pilotInfo.aircrafts.push({
              id: aircraft.id,
              registration: aircraft.registration,
              model: aircraft.model,
              status: aircraft.status,
              airport: aircraft.airport?.name || aircraft.airport?.codeIATA || 'N/A',
              positionLat: aircraft.positionLat,
              positionLon: aircraft.positionLon,
              altitude: aircraft.altitude,
              speed: aircraft.speed
            })
            // Mettre à jour les infos du pilote si disponibles
            if (aircraft.pilot.license) pilotInfo.license = aircraft.pilot.license
            if (aircraft.pilot.experienceYears) pilotInfo.experienceYears = aircraft.pilot.experienceYears
            if (aircraft.pilot.name) pilotInfo.name = aircraft.pilot.name
          }
        } else if (aircraft.usernamePilote) {
          // Si l'avion a un usernamePilote mais pas de relation pilot
          const pilotUsername = aircraft.usernamePilote
          if (!pilotsMap.has(pilotUsername)) {
            pilotsMap.set(pilotUsername, {
              id: null,
              username: pilotUsername,
              pilotId: null,
              name: pilotUsername,
              license: null,
              experienceYears: 0,
              aircrafts: []
            })
          }
          const pilotInfo = pilotsMap.get(pilotUsername)
          pilotInfo.aircrafts.push({
            id: aircraft.id,
            registration: aircraft.registration,
            model: aircraft.model,
            status: aircraft.status,
            airport: aircraft.airport?.name || aircraft.airport?.codeIATA || 'N/A',
            positionLat: aircraft.positionLat,
            positionLon: aircraft.positionLon,
            altitude: aircraft.altitude,
            speed: aircraft.speed
          })
        }
      })

      // Convertir la map en tableau
      const pilotsArray = Array.from(pilotsMap.values())
      setPilotsData(pilotsArray)
      setLoading(false)
    } catch (error) {
      console.error('Erreur chargement pilotes et avions:', error)
      setLoading(false)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'EN_VOL':
        return 'bg-emerald-500/20 text-emerald-400 border-emerald-500/50'
      case 'AU_SOL':
        return 'bg-cyan-500/20 text-cyan-400 border-cyan-500/50'
      case 'DECOLLAGE':
        return 'bg-yellow-500/20 text-yellow-400 border-yellow-500/50'
      case 'ATTERRISSAGE':
        return 'bg-orange-500/20 text-orange-400 border-orange-500/50'
      case 'EN_ATTENTE':
        return 'bg-gray-500/20 text-gray-400 border-gray-500/50'
      default:
        return 'bg-gray-500/20 text-gray-400 border-gray-500/50'
    }
  }

  const formatStatus = (status) => {
    const statusMap = {
      'EN_VOL': 'En vol',
      'AU_SOL': 'Au sol',
      'DECOLLAGE': 'Décollage',
      'ATTERRISSAGE': 'Atterrissage',
      'EN_ATTENTE': 'En attente'
    }
    return statusMap[status] || status
  }

  if (loading) {
    return (
      <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 p-6">
        <div className="text-center text-gray-400">Chargement des pilotes et avions...</div>
      </div>
    )
  }

  return (
    <div className="bg-white/5 backdrop-blur-xl rounded-2xl border border-white/10 overflow-hidden">
      <div className="p-4 border-b border-white/10">
        <h2 className="text-lg font-semibold text-violet-400">Pilotes et Avions Assignés</h2>
      </div>
      
      {pilotsData.length === 0 ? (
        <div className="p-8 text-center text-gray-400">Aucun pilote avec avion assigné</div>
      ) : (
        <div className="divide-y divide-white/10">
          {pilotsData.map((pilot) => (
            <div key={pilot.username} className="p-4 hover:bg-white/5 transition-colors">
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="font-semibold text-white text-lg">{pilot.name}</h3>
                  <div className="text-sm text-gray-400 mt-1">
                    <span>Username: {pilot.username}</span>
                    {pilot.license && (
                      <span className="ml-4">Licence: {pilot.license}</span>
                    )}
                    {pilot.experienceYears > 0 && (
                      <span className="ml-4">{pilot.experienceYears} ans d'expérience</span>
                    )}
                  </div>
                </div>
                <div className="text-right">
                  <div className="text-sm text-gray-400">Avions assignés</div>
                  <div className="text-2xl font-bold text-violet-400">{pilot.aircrafts.length}</div>
                </div>
              </div>
              
              {pilot.aircrafts.length > 0 ? (
                <div className="mt-3 space-y-2">
                  {pilot.aircrafts.map((aircraft) => (
                    <div
                      key={aircraft.id}
                      className="bg-white/5 rounded-lg p-3 border border-white/10"
                    >
                      <div className="flex items-center justify-between">
                        <div className="flex items-center gap-3">
                          <div>
                            <div className="font-mono font-bold text-cyan-400">
                              {aircraft.registration}
                            </div>
                            <div className="text-sm text-gray-400">
                              {aircraft.model} • {aircraft.airport}
                            </div>
                          </div>
                        </div>
                        <div className="flex items-center gap-3">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-full border ${getStatusColor(aircraft.status)}`}>
                            {formatStatus(aircraft.status)}
                          </span>
                          {aircraft.status === 'EN_VOL' && (
                            <div className="text-xs text-gray-400 text-right">
                              <div>Alt: {aircraft.altitude ? Math.round(aircraft.altitude) : 0}m</div>
                              <div>Vit: {aircraft.speed ? Math.round(aircraft.speed) : 0} km/h</div>
                            </div>
                          )}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div className="mt-3 text-sm text-gray-500 italic">
                  Aucun avion assigné
                </div>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

export default PilotsAircraftList

