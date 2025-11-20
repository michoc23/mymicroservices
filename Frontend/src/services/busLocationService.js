import api from './api';

const busLocationService = {
  // Get all active buses
  async getAllBuses() {
    return await api.get('/buses');
  },

  // Get bus by ID
  async getBusById(busId) {
    return await api.get(`/buses/${busId}`);
  },

  // Get current location of a bus
  async getBusLocation(busId) {
    return await api.get(`/buses/${busId}/location`);
  },

  // Get location history
  async getLocationHistory(busId, hours = 1) {
    return await api.get(`/locations/bus/${busId}`, {
      params: { hours }
    });
  },

  // Get all buses on a route
  async getBusesOnRoute(routeId) {
    return await api.get('/buses/route', {
      params: { routeId }
    });
  },

  // Get active alerts
  async getActiveAlerts() {
    return await api.get('/alerts/active');
  },

  // Get alerts for a bus
  async getBusAlerts(busId) {
    return await api.get(`/alerts/bus/${busId}`);
  },

  // Get alerts for a route
  async getRouteAlerts(routeId) {
    return await api.get(`/alerts/route/${routeId}`);
  }
};

export default busLocationService;
