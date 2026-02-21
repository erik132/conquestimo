import { TERRITORIES, ADJACENCIES, CONTINENT_COLORS, TERRITORY_MAP } from '../data/territories'

const RADIUS = 22

function getRegionColor(region, players, selectedId, myPlayerId) {
  if (!region) return '#555'
  if (region.ownerPlayerId) {
    const player = players.find(p => p.id === region.ownerPlayerId)
    return player ? player.color : '#888'
  }
  return '#555' // neutral
}

export default function MapView({ regions, players, selectedTerritoryId, onSelectTerritory, myPlayerId, movementFrom }) {
  const regionByTerrId = {}
  regions.forEach(r => { regionByTerrId[r.territoryId] = r })

  return (
    <svg
      viewBox="0 0 1000 580"
      style={{ width: '100%', height: '100%', background: '#1a2a3a', display: 'block' }}
    >
      {/* Adjacency lines */}
      {ADJACENCIES.map(([a, b], i) => {
        const ta = TERRITORY_MAP[a]
        const tb = TERRITORY_MAP[b]
        if (!ta || !tb) return null
        return (
          <line
            key={i}
            x1={ta.x} y1={ta.y}
            x2={tb.x} y2={tb.y}
            stroke="#334455"
            strokeWidth="1.5"
          />
        )
      })}

      {/* Territory circles */}
      {TERRITORIES.map(t => {
        const region = regionByTerrId[t.id]
        const isSelected = selectedTerritoryId === t.id
        const isMovementFrom = movementFrom === t.id
        const baseColor = getRegionColor(region, players, selectedTerritoryId, myPlayerId)
        const contColor = CONTINENT_COLORS[t.continent] || '#444'

        return (
          <g key={t.id} onClick={() => onSelectTerritory(t.id)} style={{ cursor: 'pointer' }}>
            {/* Continent background ring */}
            <circle
              cx={t.x} cy={t.y}
              r={RADIUS + 4}
              fill={contColor}
              opacity={0.3}
            />
            {/* Main circle */}
            <circle
              cx={t.x} cy={t.y}
              r={RADIUS}
              fill={baseColor}
              stroke={isSelected ? '#fff' : isMovementFrom ? '#ffd700' : '#223344'}
              strokeWidth={isSelected || isMovementFrom ? 3 : 1.5}
            />
            {/* Army count */}
            {region && region.armyCount > 0 && (
              <text
                x={t.x} y={t.y + 1}
                textAnchor="middle"
                dominantBaseline="middle"
                fontSize="12"
                fontWeight="bold"
                fill="#fff"
              >
                {region.armyCount}
              </text>
            )}
            {/* Territory name below circle */}
            <text
              x={t.x} y={t.y + RADIUS + 11}
              textAnchor="middle"
              dominantBaseline="middle"
              fontSize="9"
              fill="#aabbcc"
            >
              {t.name}
            </text>
            {/* Fortress indicator */}
            {region && region.fortressLevel > 0 && (
              <text
                x={t.x + RADIUS - 4}
                y={t.y - RADIUS + 6}
                fontSize="10"
                fill="#ffd700"
              >
                {'▲'.repeat(Math.min(region.fortressLevel, 3))}
              </text>
            )}
          </g>
        )
      })}
    </svg>
  )
}
