import api from './api';

class AuthService {
  async login(email, password) {
    const response = await api.post('/auth/login', { email, password });
    return response;
  }

  async register(userData) {
    const response = await api.post('/auth/register', userData);
    return response;
  }

  async refreshToken() {
    const response = await api.post('/auth/refresh');
    return response;
  }

  async logout() {
    try {
      await api.post('/auth/logout');
    } catch (error) {
      // Log error but don't throw - logout should always succeed locally
      console.error('Logout error:', error);
    }
  }

  async getCurrentUser() {
    const response = await api.get('/auth/me');
    return response;
  }

  async updateProfile(userData) {
    const response = await api.put('/auth/profile', userData);
    return response;
  }

  async changePassword(currentPassword, newPassword) {
    const response = await api.put('/auth/change-password', {
      currentPassword,
      newPassword,
    });
    return response;
  }

  setAuthToken(token) {
    if (token) {
      api.defaults.headers.common['Authorization'] = `Bearer ${token}`;
    } else {
      delete api.defaults.headers.common['Authorization'];
    }
  }

  getToken() {
    return localStorage.getItem('token');
  }

  isAuthenticated() {
    const token = this.getToken();
    return !!token;
  }
}

export default new AuthService();