import api from './api';

const routeService = {
  // Get all routes
  async getAllRoutes() {
    return await api.get('/routes');
  },

  // Get route by ID
  async getRouteById(id) {
    return await api.get(`/routes/${id}`);
  },

  // Get route with stops
  async getRouteWithStops(routeId) {
    return await api.get(`/routes/${routeId}/stops`);
  },

  // Get all stops
  async getAllStops() {
    return await api.get('/stops');
  },

  // Get stop by ID
  async getStopById(id) {
    return await api.get(`/stops/${id}`);
  },

  // Get nearby stops
  async getNearbyStops(latitude, longitude, radiusKm = 1) {
    return await api.get('/stops/nearby', {
      params: { latitude, longitude, radiusKm }
    });
  },

  // Get next departures for a stop
  async getNextDepartures(stopId, limit = 5) {
    return await api.get(`/stops/${stopId}/next-departures`, {
      params: { limit }
    });
  },

  // Calculate optimal path between two stops
  async calculateOptimalPath(fromStopId, toStopId) {
    return await api.post('/paths/optimal', {
      originStopId: fromStopId,
      destinationStopId: toStopId
    });
  },

  // Get schedules for a route
  async getRouteSchedules(routeId) {
    return await api.get(`/schedules/route/${routeId}`);
  }
};

export default routeService;
