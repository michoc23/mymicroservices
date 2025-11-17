import api from './api';

// Create subscription API instance pointing to port 8084
const subscriptionAPI = api;

class SubscriptionService {
  // Get all subscriptions for current user
  async getUserSubscriptions(userId, page = 0, size = 10) {
    const response = await subscriptionAPI.get(`/api/v1/subscriptions/user/${userId}/paginated`, {
      params: { page, size }
    });
    return response;
  }

  // Get active subscription for user
  async getActiveSubscription(userId) {
    const response = await subscriptionAPI.get(`/api/v1/subscriptions/user/${userId}/active`);
    return response;
  }

  // Check if user has active subscription
  async hasActiveSubscription(userId) {
    const response = await api.get(`/api/v1/subscriptions/user/${userId}/has-active`);
    return response;
  }

  // Create new subscription
  async createSubscription(subscriptionData) {
    const response = await api.post('/api/v1/subscriptions', subscriptionData);
    return response;
  }

  // Update subscription settings
  async updateSubscription(subscriptionId, updateData) {
    const response = await api.put(`/api/v1/subscriptions/${subscriptionId}`, updateData);
    return response;
  }

  // Renew subscription
  async renewSubscription(subscriptionId) {
    const response = await api.post(`/api/v1/subscriptions/${subscriptionId}/renew`);
    return response;
  }

  // Cancel subscription
  async cancelSubscription(subscriptionId, reason) {
    const response = await api.post(`/api/v1/subscriptions/${subscriptionId}/cancel`, {
      cancellationReason: reason
    });
    return response;
  }

  // Suspend subscription
  async suspendSubscription(subscriptionId) {
    const response = await api.post(`/api/v1/subscriptions/${subscriptionId}/suspend`);
    return response;
  }

  // Reactivate subscription
  async reactivateSubscription(subscriptionId) {
    const response = await api.post(`/api/v1/subscriptions/${subscriptionId}/reactivate`);
    return response;
  }

  // Get subscription usage history
  async getSubscriptionUsage(subscriptionId, page = 0, size = 20) {
    const response = await api.get(`/api/v1/subscription-usage/subscription/${subscriptionId}/paginated`, {
      params: { page, size }
    });
    return response;
  }

  // Get usage by date range
  async getUsageByDateRange(subscriptionId, startDate, endDate) {
    const response = await api.get(`/api/v1/subscription-usage/subscription/${subscriptionId}/range`, {
      params: { startDate, endDate }
    });
    return response;
  }

  // Get usage statistics
  async getUsageStats(subscriptionId, startDate, endDate) {
    const response = await api.get(`/api/v1/subscription-usage/subscription/${subscriptionId}/count`, {
      params: { startDate, endDate }
    });
    return response;
  }

  // Record subscription usage
  async recordUsage(subscriptionId, routeId, busId) {
    const response = await api.post('/api/v1/subscription-usage/record', null, {
      params: { subscriptionId, routeId, busId }
    });
    return response;
  }
}

export default new SubscriptionService();