import { Routes, Route, Navigate } from 'react-router-dom'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import LobbyPage from './pages/LobbyPage'
import GameLobbyPage from './pages/GameLobbyPage'
import GamePage from './pages/GamePage'
import ProtectedRoute from './components/ProtectedRoute'

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/lobby" element={
        <ProtectedRoute><LobbyPage /></ProtectedRoute>
      } />
      <Route path="/game-lobby/:id" element={
        <ProtectedRoute><GameLobbyPage /></ProtectedRoute>
      } />
      <Route path="/game/:id" element={
        <ProtectedRoute><GamePage /></ProtectedRoute>
      } />
      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}

export default App
