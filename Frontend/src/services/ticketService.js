import api from './api';

class TicketService {
  // Get user tickets
  async getUserTickets(userId, page = 0, size = 10) {
    const response = await api.get(`/api/v1/tickets/user/${userId}`, {
      params: { page, size }
    });
    return response;
  }

  // Get single ticket
  async getTicket(ticketId) {
    const response = await api.get(`/api/v1/tickets/${ticketId}`);
    return response;
  }

  // Create new order
  async createOrder(orderData) {
    const response = await api.post('/api/v1/orders', orderData);
    return response;
  }

  // Get user orders
  async getUserOrders(userId, page = 0, size = 10) {
    const response = await api.get(`/api/v1/orders/user/${userId}`, {
      params: { page, size }
    });
    return response;
  }

  // Get order details
  async getOrder(orderId) {
    const response = await api.get(`/api/v1/orders/${orderId}`);
    return response;
  }

  // Cancel order
  async cancelOrder(orderId) {
    const response = await api.put(`/api/v1/orders/${orderId}/cancel`);
    return response;
  }

  // Process payment
  async processPayment(paymentData) {
    const response = await api.post('/api/v1/payments', paymentData);
    return response;
  }

  // Get payment details
  async getPayment(paymentId) {
    const response = await api.get(`/api/v1/payments/${paymentId}`);
    return response;
  }

  // Request refund
  async requestRefund(refundData) {
    const response = await api.post('/api/v1/refunds', refundData);
    return response;
  }

  // Validate ticket
  async validateTicket(ticketId) {
    const response = await api.put(`/api/v1/tickets/${ticketId}/validate`);
    return response;
  }

  // Cancel ticket
  async cancelTicket(ticketId) {
    const response = await api.put(`/api/v1/tickets/${ticketId}/cancel`);
    return response;
  }

  // Get ticket types/pricing
  async getTicketTypes() {
    const response = await api.get('/api/v1/tickets/types');
    return response;
  }

  // Get user ticket statistics
  async getUserTicketStats(userId) {
    const response = await api.get(`/api/v1/tickets/user/${userId}/stats`);
    return response;
  }
}

export default new TicketService();