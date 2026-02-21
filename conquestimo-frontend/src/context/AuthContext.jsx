import { createContext, useContext, useState } from 'react';
import Cookies from 'js-cookie';
import api from '../api/axios';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const token = Cookies.get('jwt');
    const username = Cookies.get('username');
    return token && username ? { username } : null;
  });

  async function register(username, password) {
    const response = await api.post('/api/auth/register', { username, password });
    Cookies.set('jwt', response.data.token, { expires: 1 });
    Cookies.set('username', response.data.username, { expires: 1 });
    setUser({ username: response.data.username });
  }

  async function login(username, password) {
    const response = await api.post('/api/auth/login', { username, password });
    Cookies.set('jwt', response.data.token, { expires: 1 });
    Cookies.set('username', response.data.username, { expires: 1 });
    setUser({ username: response.data.username });
  }

  function logout() {
    Cookies.remove('jwt');
    Cookies.remove('username');
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
