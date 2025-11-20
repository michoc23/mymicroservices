import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
  constructor() {
    this.client = null;
    this.subscriptions = new Map();
    this.connected = false;
  }

  connect(onConnected, onError) {
    if (this.connected) {
      console.log('WebSocket already connected');
      return;
    }

    const socket = new SockJS('http://localhost:8082/api/v1/ws');
    
    this.client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      debug: (str) => {
        console.log('STOMP: ' + str);
      },
      onConnect: () => {
        console.log('WebSocket Connected');
        this.connected = true;
        if (onConnected) onConnected();
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame);
        this.connected = false;
        if (onError) onError(frame);
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed');
        this.connected = false;
      }
    });

    this.client.activate();
  }

  disconnect() {
    if (this.client) {
      // Unsubscribe from all subscriptions
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      this.client.deactivate();
      this.connected = false;
      console.log('WebSocket disconnected');
    }
  }

  subscribeToAllBusLocations(callback) {
    if (!this.client || !this.connected) {
      console.error('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe('/topic/locations', (message) => {
      const location = JSON.parse(message.body);
      callback(location);
    });

    this.subscriptions.set('locations', subscription);
    return subscription;
  }

  subscribeToBusLocation(busId, callback) {
    if (!this.client || !this.connected) {
      console.error('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe(`/topic/locations/${busId}`, (message) => {
      const location = JSON.parse(message.body);
      callback(location);
    });

    this.subscriptions.set(`location-${busId}`, subscription);
    return subscription;
  }

  subscribeToAlerts(callback) {
    if (!this.client || !this.connected) {
      console.error('WebSocket not connected');
      return null;
    }

    const subscription = this.client.subscribe('/topic/alerts', (message) => {
      const alert = JSON.parse(message.body);
      callback(alert);
    });

    this.subscriptions.set('alerts', subscription);
    return subscription;
  }

  unsubscribe(key) {
    const subscription = this.subscriptions.get(key);
    if (subscription) {
      subscription.unsubscribe();
      this.subscriptions.delete(key);
    }
  }

  isConnected() {
    return this.connected;
  }
}

// Export singleton instance
const webSocketService = new WebSocketService();
export default webSocketService;
