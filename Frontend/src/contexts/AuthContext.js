import React, { createContext, useContext, useState, useEffect } from 'react';
import { toast } from 'react-toastify';
import jwtDecode from 'jwt-decode';
import authService from '../services/authService';
import AuthLogger from '../utils/authLogger';

const AuthContext = createContext();

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const initializeAuth = async () => {
      try {
        AuthLogger.log('Initializing authentication...');
        const token = localStorage.getItem('token');
        if (token) {
          AuthLogger.log('Token found, validating...');
          // Verify token is not expired
          const decodedToken = jwtDecode(token);
          const currentTime = Date.now() / 1000;
          
          if (decodedToken.exp > currentTime) {
            // Token is valid, set up axios defaults
            AuthLogger.log('Token valid, setting up user session');
            authService.setAuthToken(token);
            
            // Restore cached user profile to preserve userId
            const cachedUser = localStorage.getItem('user');
            if (cachedUser) {
              setUser(JSON.parse(cachedUser));
            } else {
              setUser({
                id: decodedToken.userId,
                email: decodedToken.sub,
                firstName: decodedToken.firstName,
                lastName: decodedToken.lastName,
                role: decodedToken.role,
              });
            }
            
            // Start monitoring token
            AuthLogger.startMonitoring();
          } else {
            // Token expired, remove it
            AuthLogger.log('Token expired during initialization');
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            authService.setAuthToken(null);
          }
        } else {
          AuthLogger.log('No token found');
        }
      } catch (error) {
        AuthLogger.log('Auth initialization error:', error.message);
        localStorage.removeItem('token');
        authService.setAuthToken(null);
      } finally {
        setLoading(false);
      }
    };

    initializeAuth();
  }, []);

  const login = async (email, password) => {
    try {
      setLoading(true);
      AuthLogger.log(`Login attempt for: ${email}`);
      
      const response = await authService.login(email, password);
      const { token, userId, email: userEmail, role, firstName, lastName } = response.data;

      // Store token
      localStorage.setItem('token', token);
      authService.setAuthToken(token);

      // Set user data from the response
      const userPayload = {
        id: userId,
        email: userEmail,
        firstName,
        lastName,
        role,
      };
      setUser(userPayload);
      localStorage.setItem('user', JSON.stringify(userPayload));

      AuthLogger.log('Login successful, starting monitoring');
      AuthLogger.startMonitoring();
      
      toast.success('Login successful!');
      return { success: true };
    } catch (error) {
      AuthLogger.log('Login failed:', error.response?.data?.message || error.message);
      const message = error.response?.data?.message || 'Login failed. Please try again.';
      toast.error(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };

  const register = async (userData) => {
    try {
      setLoading(true);
      const response = await authService.register(userData);
      
      toast.success('Registration successful! Please login.');
      return { success: true, data: response.data };
    } catch (error) {
      const message = error.response?.data?.message || 'Registration failed. Please try again.';
      toast.error(message);
      return { success: false, error: message };
    } finally {
      setLoading(false);
    }
  };

  const logout = () => {
    AuthLogger.log('Manual logout triggered');
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    authService.setAuthToken(null);
    setUser(null);
    toast.info('You have been logged out.');
  };

  const updateUser = (userData) => {
    setUser(prev => ({ ...prev, ...userData }));
  };

  const value = {
    user,
    loading,
    isAuthenticated: !!user,
    login,
    register,
    logout,
    updateUser,
  };

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
};