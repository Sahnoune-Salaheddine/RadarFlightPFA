import { useEffect, useRef, useState } from 'react'
import SockJS from 'sockjs-client'
import { Client } from '@stomp/stompjs'

/**
 * Hook personnalisé pour gérer la connexion WebSocket
 * @param {string} topic - Topic à écouter (ex: '/topic/aircraft')
 * @param {function} onMessage - Callback appelé à chaque message reçu
 */
export function useWebSocket(topic, onMessage) {
  const [connected, setConnected] = useState(false)
  const clientRef = useRef(null)

  useEffect(() => {
    // Créer la connexion WebSocket
    const socket = new SockJS('http://localhost:8080/ws')
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        setConnected(true)
        // S'abonner au topic
        client.subscribe(topic, (message) => {
          try {
            const data = JSON.parse(message.body)
            onMessage(data)
          } catch (error) {
            console.error('Erreur parsing message WebSocket:', error)
          }
        })
      },
      onDisconnect: () => {
        setConnected(false)
      },
      onStompError: (frame) => {
        console.error('Erreur STOMP:', frame)
      }
    })

    client.activate()
    clientRef.current = client

    // Nettoyage
    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate()
      }
    }
  }, [topic, onMessage])

  return { connected }
}

