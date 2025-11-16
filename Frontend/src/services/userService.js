import api from './api';

class UserService {
  // Get current user profile
  async getProfile() {
    const response = await api.get('/users/profile');
    return response;
  }

  // Update user profile
  async updateProfile(profileData) {
    const response = await api.put('/users/profile', profileData);
    return response;
  }

  // Get user by ID
  async getUserById(userId) {
    const response = await api.get(`/users/${userId}`);
    return response;
  }

  // Update user information
  async updateUser(userId, userData) {
    const response = await api.put(`/users/${userId}`, userData);
    return response;
  }

  // Deactivate user account
  async deactivateAccount(userId) {
    const response = await api.put(`/users/${userId}/deactivate`);
    return response;
  }

  // Activate user account
  async activateAccount(userId) {
    const response = await api.put(`/users/${userId}/activate`);
    return response;
  }

  // Change password
  async changePassword(passwordData) {
    const response = await api.put('/users/change-password', passwordData);
    return response;
  }

  // Upload profile picture
  async uploadProfilePicture(formData) {
    const response = await api.post('/users/profile/picture', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response;
  }

  // Delete profile picture
  async deleteProfilePicture() {
    const response = await api.delete('/users/profile/picture');
    return response;
  }

  // Get user preferences
  async getPreferences() {
    const response = await api.get('/users/preferences');
    return response;
  }

  // Update user preferences
  async updatePreferences(preferences) {
    const response = await api.put('/users/preferences', preferences);
    return response;
  }

  // Get user activity history
  async getActivityHistory(page = 0, size = 20) {
    const response = await api.get('/users/activity', {
      params: { page, size }
    });
    return response;
  }

  // Get user dashboard data
  async getDashboardData() {
    const response = await api.get('/users/dashboard');
    return response;
  }
}

export default new UserService();