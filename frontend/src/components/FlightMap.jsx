import React, { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet'
import L from 'leaflet'
import api from '../services/api'

// Fix pour les icônes Leaflet
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.9.4/images/marker-shadow.png',
})

// Icône personnalisée pour les avions
const createAvionIcon = (color = '#FF6B00') => {
  const svg = `<svg width="32" height="32" viewBox="0 0 32 32" xmlns="http://www.w3.org/2000/svg"><path d="M16 2 L20 8 L28 10 L20 14 L16 30 L12 14 L4 10 L12 8 Z" fill="${color}" stroke="#FFF" stroke-width="1"/></svg>`
  return new L.Icon({
    iconUrl: 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svg),
    iconSize: [32, 32],
    iconAnchor: [16, 16],
    popupAnchor: [0, -16]
  })
}

const avionIcon = createAvionIcon()

function FlightMap({ selectedAircraft, onAircraftSelect }) {
  const [aircraft, setAircraft] = useState([])
  const [airports, setAirports] = useState([])

  useEffect(() => {
    fetchData()
    const interval = setInterval(fetchData, 5000)
    return () => clearInterval(interval)
  }, [])

  const fetchData = async () => {
    try {
      const [aircraftRes, airportsRes] = await Promise.all([
        api.get('/aircraft'),
        api.get('/airports')
      ])
      setAircraft(aircraftRes.data)
      setAirports(airportsRes.data)
    } catch (error) {
      console.error('Erreur lors du chargement des données:', error)
    }
  }

  const center = airports.length > 0 
    ? [airports[0].latitude, airports[0].longitude]
    : [33.5731, -7.5898] // Centre du Maroc

  return (
    <div className="bg-white rounded-lg shadow-lg overflow-hidden">
      <div className="p-4 border-b">
        <h2 className="text-xl font-semibold text-gray-800">Carte des vols</h2>
      </div>
      <div className="h-96">
        <MapContainer
          center={center}
          zoom={6}
          style={{ height: '100%', width: '100%' }}
        >
          <TileLayer
            url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
          />
          
          {/* Afficher les aéroports */}
          {airports.map(airport => (
            <Marker
              key={airport.id}
              position={[airport.latitude, airport.longitude]}
            >
              <Popup>
                <div>
                  <h3 className="font-bold">{airport.name}</h3>
                  <p className="text-sm">Code: {airport.codeIATA}</p>
                  <p className="text-sm">Ville: {airport.city}</p>
                </div>
              </Popup>
            </Marker>
          ))}
          
          {/* Afficher les avions */}
          {aircraft.map(ac => {
            const isSelected = selectedAircraft?.id === ac.id
            if (!ac.positionLat || !ac.positionLon) return null
            return (
              <Marker
                key={ac.id}
                position={[ac.positionLat, ac.positionLon]}
                icon={avionIcon}
                eventHandlers={{
                  click: () => onAircraftSelect(ac)
                }}
              >
                <Popup>
                  <div>
                    <h3 className="font-bold">{ac.registration}</h3>
                    <p className="text-sm">Modèle: {ac.model}</p>
                    <p className="text-sm">Altitude: {ac.altitude?.toFixed(0) || 0} m</p>
                    <p className="text-sm">Vitesse: {ac.speed?.toFixed(0) || 0} km/h</p>
                    <p className="text-sm">Direction: {ac.heading?.toFixed(0) || 0}°</p>
                    <p className="text-sm">Statut: {ac.status}</p>
                  </div>
                </Popup>
              </Marker>
            )
          })}
          
          {/* Ligne de trajectoire si un avion est sélectionné */}
          {selectedAircraft && selectedAircraft.airport && (
            <Polyline
              positions={[
                [selectedAircraft.positionLat, selectedAircraft.positionLon],
                [selectedAircraft.airport.latitude, selectedAircraft.airport.longitude]
              ]}
              color="blue"
              dashArray="5, 5"
            />
          )}
        </MapContainer>
      </div>
    </div>
  )
}

export default FlightMap

