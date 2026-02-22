import { TERRITORIES, ADJACENCIES, CONTINENT_COLORS, TERRITORY_MAP } from '../data/territories'

const RADIUS = 22

function getRegionColor(region, players) {
  if (!region) return '#555'
  if (region.ownerPlayerId) {
    const player = players.find(p => p.id === region.ownerPlayerId)
    return player ? player.color : '#888'
  }
  return '#555'
}

export default function MapView({
  regions,
  players,
  selectedTerritoryId,
  onSelectTerritory,
  myPlayerId,
  movementFrom,
  movements = [],
  onArrowClick,
  turnSubmitted,
}) {
  const regionByTerrId = {}
  regions.forEach(r => { regionByTerrId[r.territoryId] = r })

  return (
    <svg
      viewBox="0 0 1000 580"
      style={{ width: '100%', height: '100%', background: '#1a2a3a', display: 'block' }}
    >
      <defs>
        <marker id="arrow-friendly" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto">
          <path d="M0,0 L8,4 L0,8 Z" fill="#4f4" />
        </marker>
        <marker id="arrow-attack" markerWidth="8" markerHeight="8" refX="7" refY="4" orient="auto">
          <path d="M0,0 L8,4 L0,8 Z" fill="#f84" />
        </marker>
      </defs>

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

      {/* Movement arrows */}
      {movements.map(m => {
        const fromPos = TERRITORY_MAP[m.fromTerritoryId]
        const toPos = TERRITORY_MAP[m.toTerritoryId]
        if (!fromPos || !toPos) return null

        const dx = toPos.x - fromPos.x
        const dy = toPos.y - fromPos.y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist === 0) return null

        const nx = dx / dist
        const ny = dy / dist
        const x1 = fromPos.x + nx * (RADIUS + 4)
        const y1 = fromPos.y + ny * (RADIUS + 4)
        const x2 = toPos.x - nx * (RADIUS + 10)
        const y2 = toPos.y - ny * (RADIUS + 10)

        const color = m.attack ? '#f84' : '#4f4'
        const markerId = m.attack ? 'arrow-attack' : 'arrow-friendly'
        const clickable = !turnSubmitted && onArrowClick

        return (
          <g key={m.id}>
            <line
              x1={x1} y1={y1} x2={x2} y2={y2}
              stroke={color}
              strokeWidth="2.5"
              strokeOpacity="0.9"
              markerEnd={`url(#${markerId})`}
              style={{ pointerEvents: 'none' }}
            />
            {clickable && (
              <line
                x1={x1} y1={y1} x2={x2} y2={y2}
                stroke="transparent"
                strokeWidth="14"
                style={{ cursor: 'pointer' }}
                onClick={(e) => { e.stopPropagation(); onArrowClick(m.id) }}
              />
            )}
          </g>
        )
      })}

      {/* Territory circles */}
      {TERRITORIES.map(t => {
        const region = regionByTerrId[t.id]
        const isSelected = selectedTerritoryId === t.id
        const isMovementFrom = movementFrom === t.id
        const baseColor = getRegionColor(region, players)
        const contColor = CONTINENT_COLORS[t.continent] || '#444'

        return (
          <g key={t.id} onClick={() => onSelectTerritory(t.id)} style={{ cursor: 'pointer' }}>
            <circle
              cx={t.x} cy={t.y}
              r={RADIUS + 4}
              fill={contColor}
              opacity={0.3}
            />
            <circle
              cx={t.x} cy={t.y}
              r={RADIUS}
              fill={baseColor}
              stroke={isSelected ? '#fff' : isMovementFrom ? '#ffd700' : '#223344'}
              strokeWidth={isSelected || isMovementFrom ? 3 : 1.5}
            />
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
            <text
              x={t.x} y={t.y + RADIUS + 11}
              textAnchor="middle"
              dominantBaseline="middle"
              fontSize="9"
              fill="#aabbcc"
            >
              {t.name}
            </text>
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
