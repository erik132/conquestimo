import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { endTurn, getUpkeepPreview } from '../api/gameApi'

const SEASON_EMOJI = { SPRING: '🌱', SUMMER: '☀️', AUTUMN: '🍂', WINTER: '❄️' }

function useCountdown(turnStartedAt, turnTimerSeconds) {
  const [secondsLeft, setSecondsLeft] = useState(null)

  useEffect(() => {
    if (!turnStartedAt || !turnTimerSeconds) return

    const calc = () => {
      const started = new Date(turnStartedAt).getTime()
      const elapsed = Math.floor((Date.now() - started) / 1000)
      return Math.max(0, turnTimerSeconds - elapsed)
    }

    setSecondsLeft(calc())
    const interval = setInterval(() => setSecondsLeft(calc()), 1000)
    return () => clearInterval(interval)
  }, [turnStartedAt, turnTimerSeconds])

  return secondsLeft
}

export default function HudBar({ game, myPlayer, gameId, turnSubmitted, onTurnSubmitted, players }) {
  const navigate = useNavigate()
  const [upkeep, setUpkeep] = useState(null)
  const [ending, setEnding] = useState(false)
  const [error, setError] = useState('')
  const [quitting, setQuitting] = useState(false)

  const secondsLeft = useCountdown(game?.turnStartedAt, game?.turnTimerSeconds)

  useEffect(() => {
    if (!gameId) return
    getUpkeepPreview(gameId)
      .then(setUpkeep)
      .catch(() => {})
  }, [gameId, game?.turnNumber])

  const handleEndTurn = async () => {
    setEnding(true)
    setError('')
    onTurnSubmitted(true)
    try {
      await endTurn(gameId)
    } catch (e) {
      onTurnSubmitted(false)
      setError(e.response?.data?.message || e.message || 'Failed to end turn')
    } finally {
      setEnding(false)
    }
  }

  if (!game || !myPlayer) return null

  const season = game.season || 'SPRING'
  const isWinter = season === 'WINTER'

  return (
    <div style={styles.bar}>
      <div style={styles.group}>
        <div style={styles.label}>Turn</div>
        <div style={styles.value}>{game.turnNumber}</div>
      </div>

      <div style={styles.group}>
        <div style={styles.label}>Season</div>
        <div style={{ ...styles.value, color: isWinter ? '#88ccff' : '#eef' }}>
          {SEASON_EMOJI[season]} {season}
        </div>
      </div>

      {secondsLeft !== null && (
        <div style={styles.group}>
          <div style={styles.label}>Time Left</div>
          <div style={{
            ...styles.value,
            color: secondsLeft < 30 ? '#f66' : secondsLeft < 60 ? '#fa0' : '#9f9',
            fontVariantNumeric: 'tabular-nums',
          }}>
            {Math.floor(secondsLeft / 60)}:{String(secondsLeft % 60).padStart(2, '0')}
          </div>
        </div>
      )}

      <div style={styles.group}>
        <div style={styles.label}>Gold</div>
        <div style={styles.value}>
          {myPlayer.gold ?? '—'}🪙
          {upkeep != null && (
            <span style={{ color: isWinter ? '#f88' : '#aaa', fontSize: 11, marginLeft: 6 }}>
              / {upkeep.required}🪙 winter
            </span>
          )}
        </div>
      </div>

      <div style={styles.group}>
        <div style={styles.label}>Players</div>
        <div style={styles.playerList}>
          {players.map(p => (
            <span
              key={p.id}
              style={{
                display: 'inline-block',
                width: 10, height: 10,
                borderRadius: '50%',
                background: p.color,
                marginRight: 3,
                opacity: p.eliminated ? 0.3 : 1,
              }}
              title={p.username}
            />
          ))}
        </div>
      </div>

      <div style={{ flex: 1 }} />

      {error && <div style={styles.error}>{error}</div>}

      <div style={styles.group}>
        {turnSubmitted ? (
          <div style={styles.waiting}>Waiting for others...</div>
        ) : (
          <button
            style={styles.endBtn}
            onClick={handleEndTurn}
            disabled={ending}
          >
            {ending ? 'Submitting...' : 'End Turn'}
          </button>
        )}
      </div>

      <div style={styles.quitGroup}>
        {quitting ? (
          <div style={styles.quitConfirm}>
            <span style={{ color: '#cde', fontSize: 12, marginRight: 6 }}>Quit game?</span>
            <button style={styles.quitYes} onClick={() => navigate('/lobby')}>Yes</button>
            <button style={styles.quitNo} onClick={() => setQuitting(false)}>No</button>
          </div>
        ) : (
          <button style={styles.quitBtn} onClick={() => setQuitting(true)}>Quit</button>
        )}
      </div>
    </div>
  )
}

const styles = {
  bar: {
    display: 'flex',
    alignItems: 'center',
    background: '#111d2b',
    borderBottom: '1px solid #334',
    padding: '6px 16px',
    gap: 24,
    fontSize: 13,
    color: '#cde',
    height: 48,
    flexShrink: 0,
  },
  group: {
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
    lineHeight: 1.2,
  },
  label: {
    fontSize: 10,
    color: '#778899',
    textTransform: 'uppercase',
    letterSpacing: 1,
  },
  value: {
    fontWeight: 'bold',
  },
  playerList: {
    display: 'flex',
    alignItems: 'center',
    marginTop: 2,
  },
  endBtn: {
    padding: '6px 18px',
    background: '#2a5c2a',
    color: '#9f9',
    border: '1px solid #4a8c4a',
    borderRadius: 4,
    cursor: 'pointer',
    fontWeight: 'bold',
    fontSize: 13,
  },
  waiting: {
    color: '#778899',
    fontStyle: 'italic',
    fontSize: 12,
  },
  error: {
    color: '#f88',
    fontSize: 11,
    background: '#300',
    padding: '2px 8px',
    borderRadius: 3,
  },
  quitGroup: {
    display: 'flex',
    alignItems: 'center',
    marginLeft: 8,
  },
  quitBtn: {
    padding: '4px 12px',
    background: '#2a1a1a',
    color: '#f88',
    border: '1px solid #622',
    borderRadius: 4,
    cursor: 'pointer',
    fontSize: 12,
  },
  quitConfirm: {
    display: 'flex',
    alignItems: 'center',
    gap: 4,
  },
  quitYes: {
    padding: '3px 10px',
    background: '#4a1a1a',
    color: '#f88',
    border: '1px solid #822',
    borderRadius: 3,
    cursor: 'pointer',
    fontSize: 12,
  },
  quitNo: {
    padding: '3px 10px',
    background: '#1a2535',
    color: '#aaa',
    border: '1px solid #334',
    borderRadius: 3,
    cursor: 'pointer',
    fontSize: 12,
  },
}
