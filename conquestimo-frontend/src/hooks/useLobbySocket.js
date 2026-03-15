import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useLobbySocket(onLobbyUpdate) {
  const clientRef = useRef(null);
  const onLobbyUpdateRef = useRef(onLobbyUpdate);

  onLobbyUpdateRef.current = onLobbyUpdate;

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL || 'http://localhost:3015'}/ws`),
      onConnect: () => {
        client.subscribe('/topic/lobby', (message) => {
          const games = JSON.parse(message.body);
          if (onLobbyUpdateRef.current) onLobbyUpdateRef.current(games);
        });
      },
      reconnectDelay: 3000,
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);
}
