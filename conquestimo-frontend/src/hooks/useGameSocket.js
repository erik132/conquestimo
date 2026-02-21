import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useGameSocket(gameId, onGameUpdate, onResolution) {
  const clientRef = useRef(null);

  useEffect(() => {
    if (!gameId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:3015/ws'),
      onConnect: () => {
        client.subscribe(`/topic/game/${gameId}`, (message) => {
          const game = JSON.parse(message.body);
          if (onGameUpdate) onGameUpdate(game);
        });
        client.subscribe(`/topic/game/${gameId}/resolution`, (message) => {
          const result = JSON.parse(message.body);
          if (onResolution) onResolution(result);
        });
      },
      reconnectDelay: 3000,
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, [gameId]);
}
