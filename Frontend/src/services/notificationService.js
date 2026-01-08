import api from './api';

class NotificationService {
  async getUserNotifications(userId) {
    const response = await api.get(`/notifications/user/${userId}`);
    return response;
  }

  async getUnreadCount(userId) {
    const response = await api.get(`/notifications/user/${userId}/unread-count`);
    return response;
  }

  async createNotification(payload) {
    const response = await api.post('/notifications', payload);
    return response;
  }

  async markAsRead(notificationId) {
    const response = await api.post(`/notifications/${notificationId}/read`);
    return response;
  }
}

export default new NotificationService();
