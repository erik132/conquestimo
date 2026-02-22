import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { listGames, createGame, joinGame } from '../api/games';
import { useLobbySocket } from '../hooks/useLobbySocket';

export default function LobbyPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [games, setGames] = useState([]);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [passwordPrompt, setPasswordPrompt] = useState(null);
  const [error, setError] = useState('');

  const [createForm, setCreateForm] = useState({
    name: '',
    timerSeconds: 120,
    movementCap: 5,
    maxPlayers: 5,
    password: '',
  });

  useEffect(() => {
    listGames().then((res) => setGames(res.data)).catch(() => {});
  }, []);

  useLobbySocket((updatedGames) => setGames(updatedGames));

  async function handleCreate(e) {
    e.preventDefault();
    setError('');
    try {
      const payload = { ...createForm };
      if (!payload.password) delete payload.password;
      const res = await createGame(payload);
      setShowCreateModal(false);
      navigate(`/game-lobby/${res.data.id}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create game');
    }
  }

  async function handleJoin(game) {
    if (game.hasPassword) {
      setPasswordPrompt({ game, password: '' });
    } else {
      await doJoin(game.id, '');
    }
  }

  async function handleJoinInProgress(game) {
    if (game.hasPassword) {
      setPasswordPrompt({ game, password: '', inProgress: true });
    } else {
      await doJoin(game.id, '', true);
    }
  }

  async function handlePasswordSubmit(e) {
    e.preventDefault();
    await doJoin(passwordPrompt.game.id, passwordPrompt.password, passwordPrompt.inProgress);
  }

  async function doJoin(gameId, password, inProgress = false) {
    setError('');
    try {
      await joinGame(gameId, { password });
      setPasswordPrompt(null);
      navigate(inProgress ? `/game/${gameId}` : `/game-lobby/${gameId}`);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to join game');
    }
  }

  const stateLabel = { LOBBY: 'Waiting', IN_PROGRESS: 'In Progress', ENDED: 'Ended' };

  return (
    <div className="lobby-container">
      <div className="lobby-header">
        <h1>Conquestimo</h1>
        <div>
          <span>Welcome, {user.username}</span>
          <button onClick={logout} className="btn-secondary">Logout</button>
        </div>
      </div>

      <div className="lobby-actions">
        <h2>Games</h2>
        <button onClick={() => setShowCreateModal(true)} className="btn-primary">
          Create Game
        </button>
      </div>

      {error && <p className="error">{error}</p>}

      <table className="games-table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Status</th>
            <th>Players</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {games.length === 0 && (
            <tr><td colSpan={4}>No games available. Create one!</td></tr>
          )}
          {games.map((game) => (
            <tr key={game.id}>
              <td>
                {game.hasPassword && <span title="Password protected">🔒 </span>}
                {game.name}
              </td>
              <td>{stateLabel[game.state] || game.state}</td>
              <td>{game.playerCount} / {game.maxPlayers}</td>
              <td>
                {game.state === 'LOBBY' && game.playerCount < game.maxPlayers && (
                  <button onClick={() => handleJoin(game)} className="btn-primary btn-sm">
                    Join
                  </button>
                )}
                {game.state === 'IN_PROGRESS' && game.availableAiCount > 0 && (
                  <button onClick={() => handleJoinInProgress(game)} className="btn-primary btn-sm">
                    Join
                  </button>
                )}
                {game.state === 'IN_PROGRESS' && (
                  <button onClick={() => navigate(`/game/${game.id}`)} className="btn-secondary btn-sm">
                    Spectate
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Create Game</h2>
            <form onSubmit={handleCreate}>
              <label>
                Game Name
                <input
                  type="text"
                  value={createForm.name}
                  onChange={(e) => setCreateForm({ ...createForm, name: e.target.value })}
                  required maxLength={100}
                />
              </label>
              <label>
                Max Players (2–5)
                <input
                  type="number"
                  min={2} max={5}
                  value={createForm.maxPlayers}
                  onChange={(e) => setCreateForm({ ...createForm, maxPlayers: +e.target.value })}
                />
              </label>
              <label>
                Turn Timer (seconds)
                <input
                  type="number"
                  min={30} max={600}
                  value={createForm.timerSeconds}
                  onChange={(e) => setCreateForm({ ...createForm, timerSeconds: +e.target.value })}
                />
              </label>
              <label>
                Movement Cap
                <input
                  type="number"
                  min={1} max={10}
                  value={createForm.movementCap}
                  onChange={(e) => setCreateForm({ ...createForm, movementCap: +e.target.value })}
                />
              </label>
              <label>
                Password (optional)
                <input
                  type="password"
                  value={createForm.password}
                  onChange={(e) => setCreateForm({ ...createForm, password: e.target.value })}
                />
              </label>
              {error && <p className="error">{error}</p>}
              <div className="modal-buttons">
                <button type="submit" className="btn-primary">Create</button>
                <button type="button" className="btn-secondary" onClick={() => setShowCreateModal(false)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {passwordPrompt && (
        <div className="modal-overlay">
          <div className="modal">
            <h2>Enter Password</h2>
            <form onSubmit={handlePasswordSubmit}>
              <label>
                Password
                <input
                  type="password"
                  value={passwordPrompt.password}
                  onChange={(e) => setPasswordPrompt({ ...passwordPrompt, password: e.target.value })}
                  autoFocus
                />
              </label>
              {error && <p className="error">{error}</p>}
              <div className="modal-buttons">
                <button type="submit" className="btn-primary">Join</button>
                <button type="button" className="btn-secondary" onClick={() => setPasswordPrompt(null)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
