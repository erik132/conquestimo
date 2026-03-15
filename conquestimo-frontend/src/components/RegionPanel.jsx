import { useState } from 'react'
import { setRegionTask, queueMovement } from '../api/gameApi'
import { TERRITORY_MAP, ADJACENCIES } from '../data/territories'

const TASKS = [
  { value: 'ARMIES',    label: 'Train Armies' },
  { value: 'GOLD',      label: 'Collect Gold' },
  { value: 'CULTURE',   label: 'Upgrade Culture' },
  { value: 'DIPLOMATS', label: 'Produce Diplomats' },
]

const CULTURE_UPGRADE_COST = { PRIMAL: 10, BASIC: 17, INTERMEDIATE: 28, ADVANCED: null }
const CULTURE_LOYALTY_PER_TURN = { PRIMAL: 2, BASIC: 4, INTERMEDIATE: 6, ADVANCED: 8 }

function getTaskDetail(region) {
  if (!region) return null
  const task = region.currentTask
  const potential = region.regionPotential || 0

  if (task === 'GOLD') {
    const gold = Math.max(1, Math.floor(Math.pow(potential, 1.5)))
    return `${gold} gold/turn`
  }

  if (task === 'ARMIES') {
    const rate = potential / 2
    if (rate <= 0) return null
    if (rate >= 1) {
      return `${rate.toFixed(2)} armies/turn`
    }
    const progress = region.armyProductionProgress || 0
    const remaining = 1 - progress
    const turnsLeft = Math.ceil(remaining / rate)
    return `~${turnsLeft} turn${turnsLeft !== 1 ? 's' : ''} until next army`
  }

  if (task === 'CULTURE') {
    const cost = CULTURE_UPGRADE_COST[region.culture]
    if (!cost) return 'Already max level'
    const progress = region.cultureUpgradeProgress || 0
    const pct = Math.min(100, Math.round((progress / cost) * 100))
    return `${progress.toFixed(1)} / ${cost} pts (${pct}%)`
  }

  if (task === 'BUILDING') {
    let cost = null
    if (region.constructionTarget === 'FARM') {
      cost = region.farmLevel + 6
    } else if (region.constructionTarget === 'FORTRESS') {
      cost = region.fortressLevel * 4 + 3
    }
    if (!cost) return null
    const progress = region.constructionProgress || 0
    const pct = Math.min(100, Math.round((progress / cost) * 100))
    return `${progress.toFixed(1)} / ${cost} pts (${pct}%)`
  }

  if (task === 'DIPLOMATS') {
    const baseLoyalty = CULTURE_LOYALTY_PER_TURN[region.culture] || 0
    const diplomatBonus = Math.floor(potential)
    const total = baseLoyalty + diplomatBonus
    return `+${total} loyalty/turn (base ${baseLoyalty} + ${diplomatBonus} diplomat)`
  }

  return null
}

export default function RegionPanel({
  region,
  gameId,
  myPlayerId,
  onRegionUpdated,
  turnSubmitted,
  allRegions,
  onMovementQueued,
  movementFrom,
  onSetMovementFrom,
  movements,
}) {
  const [moveArmies, setMoveArmies] = useState(1)
  const [error, setError] = useState('')

  if (!region) {
    return (
      <div style={styles.panel}>
        <p style={{ color: '#778899' }}>Select a territory on the map</p>
      </div>
    )
  }

  const isOwned = region.ownerPlayerId === myPlayerId
  const isNeutral = !region.ownerPlayerId
  const tInfo = TERRITORY_MAP[region.territoryId]
  const taskDetail = getTaskDetail(region)

  const handleSetTask = async (task, constructionTarget = null) => {
    setError('')
    try {
      const updated = await setRegionTask(gameId, region.id, task, constructionTarget)
      onRegionUpdated(updated)
    } catch (e) {
      setError(e.response?.data?.message || e.message || 'Failed to set task')
    }
  }

  const handleMoveFrom = () => {
    onSetMovementFrom(region.territoryId)
  }

  const handleMoveTo = async () => {
    if (!movementFrom) return
    setError('')
    const fromRegion = allRegions.find(r => r.territoryId === movementFrom)
    if (!fromRegion) return
    try {
      await queueMovement(gameId, fromRegion.id, region.id, parseInt(moveArmies))
      onMovementQueued()
      onSetMovementFrom(null)
    } catch (e) {
      setError(e.response?.data?.message || e.message || 'Failed to queue movement')
    }
  }

  const canMoveTo = movementFrom && movementFrom !== region.territoryId && tInfo &&
    ADJACENCIES.some(([a, b]) =>
      (a === movementFrom && b === region.territoryId) ||
      (b === movementFrom && a === region.territoryId)
    )

  const fromRegion = movementFrom ? allRegions.find(r => r.territoryId === movementFrom) : null
  const availableArmies = fromRegion ? fromRegion.armyCount -
    movements.filter(m => m.fromRegionId === fromRegion.id).reduce((s, m) => s + m.armyCount, 0) : 0

  return (
    <div style={styles.panel}>
      <h3 style={styles.title}>{tInfo?.name || region.territoryId}</h3>

      <div style={styles.info}>
        <div>Owner: <span style={{ color: region.ownerColor || '#aaa' }}>
          {region.ownerUsername || 'Neutral'}
        </span></div>
        <div>Armies: <b>{region.armyCount}</b></div>
        <div>Farm Level: {region.farmLevel} | Fortress: {region.fortressLevel}</div>
        <div>Culture: {region.culture}</div>
        <div>Loyalty: {region.loyalty}%</div>
        <div>Potential: {region.regionPotential?.toFixed(2)}</div>
        {region.fortressLevel > 0 && (
          <div>Fortress cover: {region.fortressArmyLimit} armies</div>
        )}
      </div>

      {region.currentTask !== 'NONE' && (
        <div style={styles.taskBar}>
          <div>Task: <b>{region.currentTask}</b></div>
          {taskDetail && (
            <div style={styles.taskDetail}>{taskDetail}</div>
          )}
        </div>
      )}

      {error && <div style={styles.error}>{error}</div>}

      {isOwned && !turnSubmitted && (
        <>
          <div style={styles.section}>
            <b>Assign Task:</b>
            <div style={styles.taskGrid}>
              {TASKS.map(t => (
                <button
                  key={t.value}
                  style={{
                    ...styles.taskBtn,
                    background: region.currentTask === t.value ? '#336' : '#223',
                    border: region.currentTask === t.value ? '1px solid #66f' : '1px solid #445',
                  }}
                  onClick={() => handleSetTask(t.value)}
                >
                  {t.label}
                </button>
              ))}
              <button
                style={{
                  ...styles.taskBtn,
                  background: region.currentTask === 'BUILDING' && region.constructionTarget === 'FARM' ? '#336' : '#223',
                  border: region.currentTask === 'BUILDING' && region.constructionTarget === 'FARM' ? '1px solid #66f' : '1px solid #445',
                }}
                onClick={() => handleSetTask('BUILDING', 'FARM')}
              >
                Build Farm
              </button>
              <button
                style={{
                  ...styles.taskBtn,
                  background: region.currentTask === 'BUILDING' && region.constructionTarget === 'FORTRESS' ? '#336' : '#223',
                  border: region.currentTask === 'BUILDING' && region.constructionTarget === 'FORTRESS' ? '1px solid #66f' : '1px solid #445',
                }}
                onClick={() => handleSetTask('BUILDING', 'FORTRESS')}
              >
                Build Fortress
              </button>
            </div>
          </div>

          <div style={styles.section}>
            <b>Army Movement:</b>
            {!movementFrom ? (
              <button style={styles.btn} onClick={handleMoveFrom} disabled={region.armyCount < 2}>
                Move armies FROM here
              </button>
            ) : movementFrom === region.territoryId ? (
              <div style={{ color: '#ffd700', fontSize: 13 }}>
                Selected as source. Click destination.
                <button style={{ ...styles.btn, marginLeft: 8 }} onClick={() => onSetMovementFrom(null)}>
                  Cancel
                </button>
              </div>
            ) : canMoveTo ? (
              <div>
                <div style={{ fontSize: 12, color: '#aaa', marginBottom: 4 }}>
                  Available armies: {availableArmies}
                </div>
                <input
                  type="number"
                  min="1"
                  max={Math.max(1, availableArmies)}
                  value={moveArmies}
                  onChange={e => setMoveArmies(e.target.value)}
                  style={styles.input}
                />
                <button style={styles.btn} onClick={handleMoveTo}>
                  {region.ownerPlayerId === myPlayerId ? 'Move Here' : 'Attack!'}
                </button>
              </div>
            ) : (
              <div style={{ color: '#778899', fontSize: 12 }}>
                Not adjacent to selected source.
              </div>
            )}
          </div>
        </>
      )}

      {!isOwned && !turnSubmitted && movementFrom && canMoveTo && (
        <div style={styles.section}>
          <b>{isNeutral ? 'Capture:' : 'Attack:'}</b>
          <div style={{ fontSize: 12, color: '#aaa', marginBottom: 4 }}>
            Available armies: {availableArmies}
          </div>
          <input
            type="number"
            min="1"
            max={Math.max(1, availableArmies)}
            value={moveArmies}
            onChange={e => setMoveArmies(e.target.value)}
            style={styles.input}
          />
          <button style={{ ...styles.btn, background: isNeutral ? '#334' : '#622' }} onClick={handleMoveTo}>
            {isNeutral ? 'Move In' : 'Attack!'}
          </button>
        </div>
      )}
    </div>
  )
}

const styles = {
  panel: {
    background: '#1a2332',
    borderLeft: '1px solid #334',
    padding: 16,
    overflowY: 'auto',
    color: '#cde',
    fontSize: 13,
    height: '100%',
    boxSizing: 'border-box',
  },
  title: {
    margin: '0 0 10px 0',
    fontSize: 16,
    color: '#eef',
    borderBottom: '1px solid #334',
    paddingBottom: 8,
  },
  info: {
    lineHeight: 1.7,
    marginBottom: 12,
  },
  taskBar: {
    background: '#223',
    border: '1px solid #445',
    borderRadius: 4,
    padding: '4px 8px',
    marginBottom: 10,
    fontSize: 12,
  },
  taskDetail: {
    color: '#9cf',
    marginTop: 2,
    fontSize: 11,
  },
  section: {
    marginTop: 12,
    borderTop: '1px solid #334',
    paddingTop: 10,
  },
  taskGrid: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: 4,
    marginTop: 6,
  },
  taskBtn: {
    padding: '5px 4px',
    color: '#cde',
    cursor: 'pointer',
    borderRadius: 3,
    fontSize: 11,
  },
  btn: {
    marginTop: 6,
    padding: '5px 10px',
    background: '#234',
    color: '#eef',
    border: '1px solid #456',
    borderRadius: 4,
    cursor: 'pointer',
    fontSize: 12,
  },
  input: {
    width: 60,
    padding: '4px 6px',
    background: '#223',
    color: '#eef',
    border: '1px solid #445',
    borderRadius: 3,
    marginRight: 6,
  },
  error: {
    color: '#f88',
    fontSize: 12,
    marginTop: 6,
    background: '#300',
    padding: '4px 8px',
    borderRadius: 3,
  },
}
