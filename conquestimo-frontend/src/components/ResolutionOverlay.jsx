const EVENT_ICONS = {
  ATTACK: '⚔️',
  MOVE: '➡️',
  BATTLE: '🏰',
  REBELLION: '⚡',
  UPKEEP: '💰',
  BUILDING_COMPLETED: '🏗️',
  CULTURE_UPGRADED: '✨',
  SEASON_CHANGED: '📅',
}

function formatEvent(event) {
  switch (event.type) {
    case 'ATTACK':
      return `⚔️ Attack: ${event.fromTerritoryId} → ${event.toTerritoryId} (${event.armyCount} armies)`
    case 'MOVE':
      return `➡️ Move: ${event.fromTerritoryId} → ${event.toTerritoryId} (${event.armyCount} armies)`
    case 'BATTLE':
      return event.attackerWon
        ? `🏆 ${event.attackerName} captured ${event.territoryId} from ${event.defenderName}`
        : `🛡️ ${event.defenderName} defended ${event.territoryId} against ${event.attackerName}`
    case 'REBELLION':
      if (event.armyCount === 0)
        return `⚠️ Warning: peasants unhappy in ${event.territoryId} (no rebellion yet)`
      return event.playerWon
        ? `⚡ Rebellion in ${event.territoryId} suppressed (${event.armyCount} rebels)`
        : `💀 Rebellion in ${event.territoryId} — region lost! (${event.armyCount} rebels)`
    case 'UPKEEP':
      return event.armiesLost > 0
        ? `💰 ${event.playerName} paid upkeep: ${event.goldAmount} gold, lost ${event.armiesLost} armies`
        : `💰 ${event.playerName} paid ${event.goldAmount} gold upkeep`
    case 'BUILDING_COMPLETED':
      return `🏗️ ${event.buildingType} level ${event.buildingLevel} built in ${event.territoryId}`
    case 'CULTURE_UPGRADED':
      return `✨ Culture upgraded to ${event.newCulture} in ${event.territoryId}`
    case 'SEASON_CHANGED':
      return `📅 Season changed to ${event.newSeason}`
    default:
      return `${event.type}`
  }
}

export default function ResolutionOverlay({ resolution, onClose }) {
  if (!resolution) return null

  const { events } = resolution

  return (
    <div style={styles.backdrop}>
      <div style={styles.box}>
        <div style={styles.header}>
          <h2 style={styles.title}>Turn Resolution</h2>
          <button style={styles.closeBtn} onClick={onClose}>✕ Close</button>
        </div>

        <div style={styles.eventList}>
          {events.length === 0 && (
            <div style={styles.emptyMsg}>No notable events this turn.</div>
          )}
          {events.map((event, i) => (
            <div
              key={i}
              style={{
                ...styles.event,
                background: getEventBg(event.type),
              }}
            >
              {formatEvent(event)}
            </div>
          ))}
        </div>

        <button style={styles.doneBtn} onClick={onClose}>
          Continue
        </button>
      </div>
    </div>
  )
}

function getEventBg(type) {
  switch (type) {
    case 'BATTLE': return '#1a2a1a'
    case 'REBELLION': return '#2a1a1a'
    case 'UPKEEP': return '#1a1a2a'
    case 'BUILDING_COMPLETED': return '#1a2a2a'
    case 'CULTURE_UPGRADED': return '#2a1a2a'
    default: return '#1a2232'
  }
}

const styles = {
  backdrop: {
    position: 'fixed',
    inset: 0,
    background: 'rgba(0,0,0,0.7)',
    display: 'flex',
    alignItems: 'center',
    justifyContent: 'center',
    zIndex: 1000,
  },
  box: {
    background: '#111d2b',
    border: '1px solid #446',
    borderRadius: 8,
    padding: 24,
    width: 520,
    maxHeight: '80vh',
    display: 'flex',
    flexDirection: 'column',
    gap: 16,
  },
  header: {
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  title: {
    margin: 0,
    color: '#eef',
    fontSize: 18,
  },
  closeBtn: {
    background: 'none',
    border: 'none',
    color: '#778899',
    cursor: 'pointer',
    fontSize: 13,
  },
  eventList: {
    overflowY: 'auto',
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    gap: 6,
  },
  event: {
    padding: '7px 12px',
    borderRadius: 4,
    border: '1px solid #334',
    color: '#cde',
    fontSize: 13,
    lineHeight: 1.5,
  },
  emptyMsg: {
    color: '#556677',
    fontStyle: 'italic',
    textAlign: 'center',
    padding: 24,
  },
  doneBtn: {
    padding: '8px 24px',
    background: '#234',
    color: '#aef',
    border: '1px solid #456',
    borderRadius: 4,
    cursor: 'pointer',
    fontSize: 14,
    alignSelf: 'flex-end',
  },
}
