import api from './api';

class SubscriptionService {
  // Get all subscriptions for current user
  async getUserSubscriptions(userId, page = 0, size = 10) {
    const response = await api.get(`/subscriptions/user/${userId}/paginated`, {
      params: { page, size }
    });
    return response;
  }

  // Get active subscription for user
  async getActiveSubscription(userId) {
    const response = await api.get(`/subscriptions/user/${userId}/active`);
    return response;
  }

  // Check if user has active subscription
  async hasActiveSubscription(userId) {
    const response = await api.get(`/subscriptions/user/${userId}/has-active`);
    return response;
  }

  // Create new subscription
  async createSubscription(subscriptionData) {
    const response = await api.post('/subscriptions', subscriptionData);
    return response;
  }

  // Update subscription settings
  async updateSubscription(subscriptionId, updateData) {
    const response = await api.put(`/subscriptions/${subscriptionId}`, updateData);
    return response;
  }

  // Renew subscription
  async renewSubscription(subscriptionId) {
    const response = await api.post(`/subscriptions/${subscriptionId}/renew`);
    return response;
  }

  // Cancel subscription
  async cancelSubscription(subscriptionId, reason) {
    const response = await api.post(`/subscriptions/${subscriptionId}/cancel`, {
      cancellationReason: reason
    });
    return response;
  }

  // Suspend subscription
  async suspendSubscription(subscriptionId) {
    const response = await api.post(`/subscriptions/${subscriptionId}/suspend`);
    return response;
  }

  // Reactivate subscription
  async reactivateSubscription(subscriptionId) {
    const response = await api.post(`/subscriptions/${subscriptionId}/reactivate`);
    return response;
  }

  // Get subscription usage history
  async getSubscriptionUsage(subscriptionId, page = 0, size = 20) {
    const response = await api.get(`/subscription-usage/subscription/${subscriptionId}/paginated`, {
      params: { page, size }
    });
    return response;
  }

  // Get usage by date range
  async getUsageByDateRange(subscriptionId, startDate, endDate) {
    const response = await api.get(`/subscription-usage/subscription/${subscriptionId}/range`, {
      params: { startDate, endDate }
    });
    return response;
  }

  // Get usage statistics
  async getUsageStats(subscriptionId, startDate, endDate) {
    const response = await api.get(`/subscription-usage/subscription/${subscriptionId}/count`, {
      params: { startDate, endDate }
    });
    return response;
  }

  // Record subscription usage
  async recordUsage(subscriptionId, routeId, busId) {
    const response = await api.post('/subscription-usage/record', null, {
      params: { subscriptionId, routeId, busId }
    });
    return response;
  }
}

export default new SubscriptionService();