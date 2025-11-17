import axios from 'axios';
import { toast } from 'react-toastify';

// Simple logging function to avoid circular imports
const logApiEvent = (message, data = null) => {
  const timestamp = new Date().toISOString().substr(11, 8);
  console.log(`[${timestamp}] 🌐 API: ${message}`, data || '');
};

// Create axios instance with default config
const api = axios.create({
  baseURL: '/api/v1', // Use API Gateway proxy
  timeout: 15000, // Increased timeout to prevent premature failures
  headers: {
    'Content-Type': 'application/json',
  },
});

// Fallback API instance for direct service calls when needed
export const directUserAPI = axios.create({
  baseURL: 'http://localhost:8081/api/v1',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Setup interceptors for direct API as well
directUserAPI.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Request interceptor to add auth token
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
api.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    // Handle different types of errors
    if (error.response) {
      const { status, data } = error.response;
      
      switch (status) {
        case 401:
          // Only logout if we're not already on login page and token exists
          const currentPath = window.location.pathname;
          const hasToken = localStorage.getItem('token');
          
          logApiEvent(`401 Unauthorized on ${error.config?.url}`, {
            currentPath,
            hasToken: !!hasToken,
            willLogout: hasToken && currentPath !== '/login'
          });
          
          if (hasToken && currentPath !== '/login') {
            logApiEvent('Forcing logout due to 401');
            localStorage.removeItem('token');
            window.location.href = '/login';
            toast.error('Session expired. Please login again.');
          }
          break;
        case 403:
          toast.error('Access denied. You do not have permission to perform this action.');
          break;
        case 404:
          toast.error(data.message || 'Resource not found.');
          break;
        case 500:
          toast.error('Server error. Please try again later.');
          break;
        default:
          toast.error(data.message || 'An unexpected error occurred.');
      }
    } else if (error.request) {
      // Network error
      logApiEvent('Network error occurred', {
        url: error.config?.url,
        timeout: error.code === 'ECONNABORTED'
      });
      toast.error('Network error. Please check your connection.');
    } else {
      // Request setup error
      logApiEvent('Request setup error:', error.message);
      toast.error('Request failed. Please try again.');
    }

    return Promise.reject(error);
  }
);

export default api;