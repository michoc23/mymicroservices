import api from './api';

class UserService {
  // Get current user profile
  async getProfile() {
    const response = await api.get('/api/v1/users/profile');
    return response;
  }

  // Update user profile
  async updateProfile(profileData) {
    const response = await api.put('/api/v1/users/profile', profileData);
    return response;
  }

  // Get user by ID
  async getUserById(userId) {
    const response = await api.get(`/api/v1/users/${userId}`);
    return response;
  }

  // Update user information
  async updateUser(userId, userData) {
    const response = await api.put(`/api/v1/users/${userId}`, userData);
    return response;
  }

  // Deactivate user account
  async deactivateAccount(userId) {
    const response = await api.put(`/api/v1/users/${userId}/deactivate`);
    return response;
  }

  // Activate user account
  async activateAccount(userId) {
    const response = await api.put(`/api/v1/users/${userId}/activate`);
    return response;
  }

  // Change password
  async changePassword(passwordData) {
    const response = await api.put('/api/v1/users/change-password', passwordData);
    return response;
  }

  // Upload profile picture
  async uploadProfilePicture(formData) {
    const response = await api.post('/api/v1/users/profile/picture', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response;
  }

  // Delete profile picture
  async deleteProfilePicture() {
    const response = await api.delete('/api/v1/users/profile/picture');
    return response;
  }

  // Get user preferences
  async getPreferences() {
    const response = await api.get('/api/v1/users/preferences');
    return response;
  }

  // Update user preferences
  async updatePreferences(preferences) {
    const response = await api.put('/api/v1/users/preferences', preferences);
    return response;
  }

  // Get user activity history
  async getActivityHistory(page = 0, size = 20) {
    const response = await api.get('/api/v1/users/activity', {
      params: { page, size }
    });
    return response;
  }

  // Get user dashboard data
  async getDashboardData() {
    const response = await api.get('/api/v1/users/dashboard');
    return response;
  }
}

export default new UserService();