import api from './axios';

export const listGames = () => api.get('/api/games');
export const getGame = (id) => api.get(`/api/games/${id}`);
export const createGame = (data) => api.post('/api/games', data);
export const joinGame = (id, data) => api.post(`/api/games/${id}/join`, data);
export const leaveGame = (id) => api.post(`/api/games/${id}/leave`);
export const startGame = (id) => api.post(`/api/games/${id}/start`);
