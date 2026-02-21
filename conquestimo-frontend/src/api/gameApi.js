import api from './axios'

export const getRegions = (gameId) =>
  api.get(`/api/games/${gameId}/regions`).then(r => r.data)

export const setRegionTask = (gameId, regionId, task, constructionTarget = null) =>
  api.post(`/api/games/${gameId}/regions/${regionId}/task`, { task, constructionTarget }).then(r => r.data)

export const getMovements = (gameId) =>
  api.get(`/api/games/${gameId}/movements`).then(r => r.data)

export const queueMovement = (gameId, fromRegionId, toRegionId, armyCount) =>
  api.post(`/api/games/${gameId}/movements`, { fromRegionId, toRegionId, armyCount }).then(r => r.data)

export const cancelMovement = (gameId, movementId) =>
  api.delete(`/api/games/${gameId}/movements/${movementId}`).then(r => r.data)

export const endTurn = (gameId) =>
  api.post(`/api/games/${gameId}/turn/end`).then(r => r.data)

export const getTurnStatus = (gameId) =>
  api.get(`/api/games/${gameId}/turn/status`).then(r => r.data)

export const getUpkeepPreview = (gameId) =>
  api.get(`/api/games/${gameId}/turn/upkeep-preview`).then(r => r.data)
