import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getGame, leaveGame, startGame } from '../api/games';
import { useGameSocket } from '../hooks/useGameSocket';

export default function GameLobbyPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [game, setGame] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    getGame(id)
      .then((res) => {
        const g = res.data;
        if (g.state === 'IN_PROGRESS') {
          navigate(`/game/${id}`, { replace: true });
        } else if (g.state === 'ENDED') {
          navigate('/lobby', { replace: true });
        } else {
          setGame(g);
        }
      })
      .catch(() => navigate('/lobby', { replace: true }));
  }, [id]);

  useGameSocket(id, (updatedGame) => {
    setGame(updatedGame);
    if (updatedGame.state === 'IN_PROGRESS') {
      navigate(`/game/${id}`, { replace: true });
    }
  });

  async function handleLeave() {
    try {
      await leaveGame(id);
      navigate('/lobby');
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to leave game');
    }
  }

  async function handleStart() {
    setError('');
    try {
      await startGame(id);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to start game');
    }
  }

  if (!game) {
    return <div className="loading">Loading...</div>;
  }

  const isCreator = game.createdByUsername === user.username;
  const humanPlayers = game.players.filter((p) => !p.ai).length;
  const canStart = isCreator && humanPlayers >= 1;

  return (
    <div className="game-lobby-container">
      <div className="game-lobby-header">
        <h1>{game.name}</h1>
        <span className="lobby-status">Waiting for players...</span>
      </div>

      <div className="game-lobby-info">
        <span>Max Players: {game.maxPlayers}</span>
        <span>Turn Timer: {game.turnTimerSeconds}s</span>
        <span>Movement Cap: {game.movementCap}</span>
      </div>

      <div className="player-list">
        <h2>Players ({game.players.length} / {game.maxPlayers})</h2>
        {game.players.map((player) => (
          <div key={player.id} className="player-row">
            <span
              className="player-color-dot"
              style={{ backgroundColor: player.color }}
            />
            <span className="player-name">{player.username}</span>
            {player.username === game.createdByUsername && (
              <span className="creator-badge">Host</span>
            )}
          </div>
        ))}
        {Array.from({ length: game.maxPlayers - game.players.length }).map((_, i) => (
          <div key={`empty-${i}`} className="player-row player-row-empty">
            <span className="player-color-dot" style={{ backgroundColor: '#555' }} />
            <span className="player-name">Empty slot (will be filled by AI)</span>
          </div>
        ))}
      </div>

      {error && <p className="error">{error}</p>}

      <div className="game-lobby-actions">
        {isCreator ? (
          <>
            <button
              onClick={handleStart}
              className="btn-primary"
              disabled={!canStart}
              title={!canStart ? 'Need at least 1 human player' : ''}
            >
              Start Game
            </button>
            <button onClick={handleLeave} className="btn-secondary">
              Cancel Game
            </button>
          </>
        ) : (
          <button onClick={handleLeave} className="btn-secondary">
            Leave Game
          </button>
        )}
      </div>

      {isCreator && (
        <p className="lobby-hint">
          Empty slots will be filled with AI players when you start.
        </p>
      )}
    </div>
  );
}
