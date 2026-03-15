import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export function useGameSocket(gameId, onGameUpdate, onResolution) {
  const clientRef = useRef(null);
  const onGameUpdateRef = useRef(onGameUpdate);
  const onResolutionRef = useRef(onResolution);

  onGameUpdateRef.current = onGameUpdate;
  onResolutionRef.current = onResolution;

  useEffect(() => {
    if (!gameId) return;

    const client = new Client({
      webSocketFactory: () => new SockJS(`${import.meta.env.VITE_API_URL || 'http://localhost:3015'}/ws`),
      onConnect: () => {
        client.subscribe(`/topic/game/${gameId}`, (message) => {
          const game = JSON.parse(message.body);
          if (onGameUpdateRef.current) onGameUpdateRef.current(game);
        });
        client.subscribe(`/topic/game/${gameId}/resolution`, (message) => {
          const result = JSON.parse(message.body);
          if (onResolutionRef.current) onResolutionRef.current(result);
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
