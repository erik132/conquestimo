import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useLobbySocket(onLobbyUpdate) {
  const clientRef = useRef(null);

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:3015/ws'),
      onConnect: () => {
        client.subscribe('/topic/lobby', (message) => {
          const games = JSON.parse(message.body);
          onLobbyUpdate(games);
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
