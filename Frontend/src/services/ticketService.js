import api from './api';

class TicketService {
  // Get user tickets
  async getUserTickets(userId, page = 0, size = 10) {
    const response = await api.get(`/tickets/user/${userId}`, {
      params: { page, size }
    });
    return response;
  }

  // Get single ticket
  async getTicket(ticketId) {
    const response = await api.get(`/tickets/${ticketId}`);
    return response;
  }

  // Create new order
  async createOrder(orderData) {
    const response = await api.post('/orders', orderData);
    return response;
  }

  // Get user orders
  async getUserOrders(userId, page = 0, size = 10) {
    const response = await api.get(`/orders/user/${userId}`, {
      params: { page, size }
    });
    return response;
  }

  // Get order details
  async getOrder(orderId) {
    const response = await api.get(`/orders/${orderId}`);
    return response;
  }

  // Cancel order
  async cancelOrder(orderId) {
    const response = await api.put(`/orders/${orderId}/cancel`);
    return response;
  }

  // Process payment
  async processPayment(paymentData) {
    const response = await api.post('/payments', paymentData);
    return response;
  }

  // Get payment details
  async getPayment(paymentId) {
    const response = await api.get(`/payments/${paymentId}`);
    return response;
  }

  // Request refund
  async requestRefund(refundData) {
    const response = await api.post('/refunds', refundData);
    return response;
  }

  // Validate ticket
  async validateTicket(ticketId) {
    const response = await api.put(`/tickets/${ticketId}/validate`);
    return response;
  }

  // Cancel ticket
  async cancelTicket(ticketId) {
    const response = await api.put(`/tickets/${ticketId}/cancel`);
    return response;
  }

  // Get ticket types/pricing
  async getTicketTypes() {
    const response = await api.get('/tickets/types');
    return response;
  }

  // Get user ticket statistics
  async getUserTicketStats(userId) {
    const response = await api.get(`/tickets/user/${userId}/stats`);
    return response;
  }
}

export default new TicketService();