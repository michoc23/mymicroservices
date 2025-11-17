// Simple authentication logger - no imports needed
const AuthLogger = {
  log: (message, data = null) => {
    const timestamp = new Date().toISOString().substr(11, 8);
    console.log(`[${timestamp}] 🔐 ${message}`, data || '');
  },

  logTokenStatus: () => {
    const token = localStorage.getItem('token');
    if (!token) {
      AuthLogger.log('No token in localStorage');
      return;
    }

    try {
      // Simple base64 decode without external dependencies
      const payload = JSON.parse(atob(token.split('.')[1]));
      const now = Math.floor(Date.now() / 1000);
      const expiresIn = payload.exp - now;
      
      AuthLogger.log('Token info:', {
        expiresIn: `${Math.floor(expiresIn / 60)}m ${expiresIn % 60}s`,
        email: payload.sub,
        role: payload.role,
        userId: payload.userId
      });
    } catch (e) {
      AuthLogger.log('Failed to decode token:', e.message);
    }
  },

  logApiCall: (url, status) => {
    AuthLogger.log(`API ${status}: ${url}`);
  },

  startMonitoring: () => {
    // Check token status every 30 seconds
    setInterval(() => {
      AuthLogger.logTokenStatus();
    }, 30000);
    
    AuthLogger.log('Auth monitoring started');
    AuthLogger.logTokenStatus();
  }
};

export default AuthLogger;