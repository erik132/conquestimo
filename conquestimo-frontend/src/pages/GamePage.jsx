import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useGameSocket } from '../hooks/useGameSocket'
import { getRegions, getMovements, cancelMovement } from '../api/gameApi'
import { getGame } from '../api/games'
import MapView from '../components/MapView'
import RegionPanel from '../components/RegionPanel'
import HudBar from '../components/HudBar'
import ResolutionOverlay from '../components/ResolutionOverlay'

export default function GamePage() {
  const { id: gameId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [game, setGame] = useState(null)
  const [players, setPlayers] = useState([])
  const [regions, setRegions] = useState([])
  const [movements, setMovements] = useState([])
  const [selectedTerritoryId, setSelectedTerritoryId] = useState(null)
  const [movementFrom, setMovementFrom] = useState(null)
  const [turnSubmitted, setTurnSubmitted] = useState(false)
  const [resolution, setResolution] = useState(null)
  const [loading, setLoading] = useState(true)

  // Load initial game state
  useEffect(() => {
    if (!gameId) return
    Promise.all([getGame(gameId), getRegions(gameId)])
      .then(([gameRes, regionList]) => {
        const gameDetail = gameRes.data
        setGame(gameDetail)
        setPlayers(gameDetail.players || [])
        setRegions(regionList)
        setLoading(false)
      })
      .catch(() => navigate('/lobby'))
  }, [gameId])

  // Load movements when game state is ready
  useEffect(() => {
    if (!gameId || loading) return
    getMovements(gameId).then(setMovements).catch(() => {})
  }, [gameId, loading])

  // WebSocket handlers
  const handleGameUpdate = (updatedGame) => {
    setGame(updatedGame)
    setPlayers(updatedGame.players || [])
    if (updatedGame.state === 'ENDED') return
  }

  const handleResolution = (result) => {
    setRegions(result.updatedRegions || [])
    setMovements([])
    setTurnSubmitted(false)
    setMovementFrom(null)
    setResolution(result)
    if (result.updatedGame) {
      setGame(result.updatedGame)
      setPlayers(result.updatedGame.players || [])
    }
  }

  useGameSocket(gameId, handleGameUpdate, handleResolution)

  const myPlayer = players.find(p => !p.ai && p.username === user?.username)
  const myPlayerId = myPlayer?.id

  const selectedRegion = selectedTerritoryId
    ? regions.find(r => r.territoryId === selectedTerritoryId)
    : null

  const handleRegionUpdated = (updated) => {
    setRegions(prev => prev.map(r => r.id === updated.id ? updated : r))
  }

  const handleMovementQueued = () => {
    getMovements(gameId).then(setMovements).catch(() => {})
  }

  const handleCancelMovement = async (movementId) => {
    try {
      await cancelMovement(gameId, movementId)
      setMovements(prev => prev.filter(m => m.id !== movementId))
    } catch (e) {
      console.error(e)
    }
  }

  if (loading) {
    return (
      <div style={styles.loading}>Loading game...</div>
    )
  }

  if (game?.state === 'ENDED') {
    const activePlayers = players.filter(p => !p.eliminated && !p.ai)
    const winner = activePlayers.length === 1 ? activePlayers[0] : null
    return (
      <div style={styles.endScreen}>
        <div style={styles.endBox}>
          <h1 style={{ color: '#ffd700', marginBottom: 16 }}>Game Over</h1>
          {winner ? (
            <>
              <div style={{ fontSize: 20, color: winner.color }}>
                🏆 {winner.username} wins!
              </div>
              {winner.username === user?.username && (
                <div style={{ color: '#9f9', marginTop: 8 }}>You won!</div>
              )}
            </>
          ) : (
            <div style={{ fontSize: 18, color: '#aaa' }}>Draw — all empires fell!</div>
          )}
          <button style={styles.lobbyBtn} onClick={() => navigate('/lobby')}>
            Return to Lobby
          </button>
        </div>
      </div>
    )
  }

  return (
    <div style={styles.container}>
      <HudBar
        game={game}
        myPlayer={myPlayer}
        gameId={gameId}
        turnSubmitted={turnSubmitted}
        onTurnSubmitted={() => setTurnSubmitted(true)}
        players={players}
      />

      <div style={styles.content}>
        {/* Map area */}
        <div style={styles.mapArea}>
          <MapView
            regions={regions}
            players={players}
            selectedTerritoryId={selectedTerritoryId}
            onSelectTerritory={setSelectedTerritoryId}
            myPlayerId={myPlayerId}
            movementFrom={movementFrom}
          />

          {/* Queued movements panel */}
          {movements.length > 0 && (
            <div style={styles.movementsBar}>
              <b style={{ color: '#aaa', fontSize: 11 }}>Queued movements:</b>
              {movements.map(m => (
                <div key={m.id} style={styles.movementItem}>
                  <span style={{ color: m.attack ? '#f88' : '#8f8' }}>
                    {m.attack ? '⚔' : '➡'} {m.fromTerritoryId} → {m.toTerritoryId} ({m.armyCount})
                  </span>
                  {!turnSubmitted && (
                    <button
                      style={styles.cancelBtn}
                      onClick={() => handleCancelMovement(m.id)}
                    >
                      ✕
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Region detail panel */}
        <div style={styles.sidebar}>
          <RegionPanel
            region={selectedRegion}
            gameId={gameId}
            myPlayerId={myPlayerId}
            onRegionUpdated={handleRegionUpdated}
            turnSubmitted={turnSubmitted}
            allRegions={regions}
            onMovementQueued={handleMovementQueued}
            movementFrom={movementFrom}
            onSetMovementFrom={setMovementFrom}
            movements={movements}
          />
        </div>
      </div>

      {resolution && (
        <ResolutionOverlay
          resolution={resolution}
          onClose={() => setResolution(null)}
        />
      )}
    </div>
  )
}

const styles = {
  container: {
    display: 'flex',
    flexDirection: 'column',
    height: '100vh',
    background: '#0d1821',
    color: '#cde',
  },
  content: {
    display: 'flex',
    flex: 1,
    overflow: 'hidden',
  },
  mapArea: {
    flex: 1,
    position: 'relative',
    overflow: 'hidden',
    display: 'flex',
    flexDirection: 'column',
  },
  sidebar: {
    width: 260,
    flexShrink: 0,
    overflow: 'hidden',
  },
  movementsBar: {
    background: '#111a27',
    borderTop: '1px solid #334',
    padding: '6px 12px',
    display: 'flex',
    flexWrap: 'wrap',
    gap: 6,
    alignItems: 'center',
    fontSize: 12,
  },
  movementItem: {
    display: 'flex',
    alignItems: 'center',
    gap: 4,
    background: '#1a2535',
    border: '1px solid #334',
    borderRadius: 3,
    padding: '2px 6px',
  },
  cancelBtn: {
    background: 'none',
    border: 'none',
    color: '#f88',
    cursor: 'pointer',
    padding: '0 2px',
    fontSize: 11,
  },
  loading: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100vh',
    color: '#778899',
    fontSize: 18,
    background: '#0d1821',
  },
  endScreen: {
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    height: '100vh',
    background: '#0d1821',
  },
  endBox: {
    background: '#111d2b',
    border: '1px solid #446',
    borderRadius: 8,
    padding: 40,
    textAlign: 'center',
    minWidth: 320,
  },
  lobbyBtn: {
    marginTop: 24,
    padding: '10px 24px',
    background: '#234',
    color: '#aef',
    border: '1px solid #456',
    borderRadius: 4,
    cursor: 'pointer',
    fontSize: 14,
  },
}
