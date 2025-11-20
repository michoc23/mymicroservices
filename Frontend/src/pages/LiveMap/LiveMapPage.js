import React, { useState, useEffect, useCallback } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Alert,
  CircularProgress,
  Chip,
  IconButton,
  Drawer,
  List,
  ListItem,
  ListItemText,
  Divider,
  Card,
  CardContent,
  Button,
  Grid
} from '@mui/material';
// import { MapContainer, TileLayer, Marker, Popup, useMap } from 'react-leaflet';
import { DirectionsBus, Warning, Refresh, Close, Map as MapIcon } from '@mui/icons-material';
// import L from 'leaflet';
import busLocationService from '../../services/busLocationService';
import webSocketService from '../../services/webSocketService';
import { toast } from 'react-toastify';
// import 'leaflet/dist/leaflet.css';

// Map functionality temporarily disabled due to React version compatibility
// Will be re-enabled once react-leaflet is updated to support React 18

const LiveMapPage = () => {
  const [busLocations, setBusLocations] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [wsConnected, setWsConnected] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [selectedBus, setSelectedBus] = useState(null);
  const [mapCenter, setMapCenter] = useState([48.8566, 2.3522]); // Paris center
  const [mapZoom, setMapZoom] = useState(13);

  // Load initial data
  useEffect(() => {
    loadInitialData();
    setupWebSocket();

    return () => {
      webSocketService.disconnect();
    };
  }, []);

  const loadInitialData = async () => {
    try {
      setLoading(true);
      
      // Load all buses
      const busResponse = await busLocationService.getAllBuses();
      const buses = busResponse.data?.content || busResponse.data || [];
      
      // Load current locations for all buses
      const locationPromises = buses.map(async (bus) => {
        try {
          const locationResponse = await busLocationService.getBusLocation(bus.id);
          return {
            ...bus,
            location: locationResponse.data
          };
        } catch (error) {
          console.error(`Failed to load location for bus ${bus.id}:`, error);
          return null;
        }
      });

      const locationsData = await Promise.all(locationPromises);
      const validLocations = locationsData.filter(loc => loc !== null && loc.location);
      
      setBusLocations(validLocations);

      // Load alerts
      const alertsResponse = await busLocationService.getActiveAlerts();
      setAlerts(alertsResponse.data?.content || alertsResponse.data || []);

      setLoading(false);
    } catch (error) {
      console.error('Failed to load initial data:', error);
      toast.error('Failed to load bus locations');
      setLoading(false);
    }
  };

  const setupWebSocket = () => {
    webSocketService.connect(
      () => {
        console.log('WebSocket connected successfully');
        setWsConnected(true);
        
        // Subscribe to all bus locations
        webSocketService.subscribeToAllBusLocations((location) => {
          updateBusLocation(location);
        });

        // Subscribe to alerts
        webSocketService.subscribeToAlerts((alert) => {
          setAlerts(prev => [alert, ...prev].slice(0, 10));
          if (alert.alertType === 'DELAY' || alert.alertType === 'OFF_ROUTE') {
            toast.warning(`Alert: ${alert.message}`);
          }
        });
      },
      (error) => {
        console.error('WebSocket connection error:', error);
        setWsConnected(false);
        toast.error('Real-time updates disconnected');
      }
    );
  };

  const updateBusLocation = useCallback((location) => {
    setBusLocations(prev => {
      const index = prev.findIndex(b => b.id === location.busId);
      if (index >= 0) {
        const updated = [...prev];
        updated[index] = {
          ...updated[index],
          location: location
        };
        return updated;
      } else {
        // New bus appeared
        return [...prev, { id: location.busId, location: location }];
      }
    });
  }, []);

  const handleBusClick = (bus) => {
    setSelectedBus(bus);
    setDrawerOpen(true);
    setMapCenter([bus.location.latitude, bus.location.longitude]);
    setMapZoom(15);
  };

  const handleRefresh = () => {
    loadInitialData();
    toast.info('Refreshing bus locations...');
  };

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 2, mb: 2 }}>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
              🗺️ Live Bus Map
            </Typography>
            <Box sx={{ display: 'flex', gap: 1, alignItems: 'center' }}>
              <Chip 
                label={wsConnected ? 'Live' : 'Offline'} 
                color={wsConnected ? 'success' : 'error'} 
                size="small" 
              />
              <Typography variant="body2" color="text.secondary">
                {busLocations.length} buses tracked
              </Typography>
            </Box>
          </Box>
          <IconButton onClick={handleRefresh} color="primary">
            <Refresh />
          </IconButton>
        </Box>

        {alerts.length > 0 && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
              {alerts.length} Active Alert{alerts.length > 1 ? 's' : ''}
            </Typography>
            <Typography variant="body2">
              {alerts[0]?.message}
            </Typography>
          </Alert>
        )}

        <Paper sx={{ height: '70vh', position: 'relative', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          {/* Map placeholder - Interactive map will be enabled when react-leaflet compatibility is resolved */}
          <Box sx={{ 
            flex: 1, 
            display: 'flex', 
            flexDirection: 'column',
            alignItems: 'center', 
            justifyContent: 'center',
            bgcolor: 'grey.100',
            p: 3
          }}>
            <MapIcon sx={{ fontSize: 80, color: 'primary.main', mb: 2 }} />
            <Typography variant="h6" sx={{ mb: 2 }}>
              Live Bus Tracking Map
            </Typography>
            <Alert severity="info" sx={{ maxWidth: 600, mb: 3 }}>
              Interactive map view is being prepared. Real-time bus data is being collected below.
            </Alert>
            
            {/* Bus list view as alternative */}
            <Box sx={{ width: '100%', maxWidth: 800, maxHeight: 400, overflow: 'auto' }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                Active Buses ({busLocations.length})
              </Typography>
              <Grid container spacing={2}>
                {busLocations.map((bus) => (
                  bus.location && (
                    <Grid item xs={12} sm={6} md={4} key={bus.id}>
                      <Card 
                        sx={{ cursor: 'pointer', '&:hover': { boxShadow: 3 } }}
                        onClick={() => handleBusClick(bus)}
                      >
                        <CardContent>
                          <Box sx={{ display: 'flex', alignItems: 'center', mb: 1 }}>
                            <DirectionsBus color="primary" sx={{ mr: 1 }} />
                            <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                              Bus {bus.routeName || bus.id}
                            </Typography>
                          </Box>
                          <Typography variant="caption" display="block">
                            Speed: {bus.location.speed || 0} km/h
                          </Typography>
                          <Typography variant="caption" display="block">
                            Position: {bus.location.latitude.toFixed(4)}, {bus.location.longitude.toFixed(4)}
                          </Typography>
                          <Typography variant="caption" display="block" color="text.secondary">
                            {new Date(bus.location.timestamp).toLocaleTimeString()}
                          </Typography>
                        </CardContent>
                      </Card>
                    </Grid>
                  )
                ))}
              </Grid>
            </Box>
          </Box>
        </Paper>
      </Box>

      {/* Bus Details Drawer */}
      <Drawer
        anchor="right"
        open={drawerOpen}
        onClose={() => setDrawerOpen(false)}
        PaperProps={{ sx: { width: 320, p: 2 } }}
      >
        {selectedBus && (
          <Box>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Bus Details
              </Typography>
              <IconButton onClick={() => setDrawerOpen(false)} size="small">
                <Close />
              </IconButton>
            </Box>

            <Card sx={{ mb: 2 }}>
              <CardContent>
                <Typography variant="subtitle2" color="text.secondary">
                  Bus ID
                </Typography>
                <Typography variant="h6" sx={{ mb: 2 }}>
                  {selectedBus.routeName || selectedBus.id}
                </Typography>

                {selectedBus.location && (
                  <>
                    <Typography variant="subtitle2" color="text.secondary">
                      Current Status
                    </Typography>
                    <List dense>
                      <ListItem>
                        <ListItemText
                          primary="Speed"
                          secondary={`${selectedBus.location.speed || 0} km/h`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Heading"
                          secondary={`${selectedBus.location.heading || 0}°`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Location"
                          secondary={`${selectedBus.location.latitude.toFixed(6)}, ${selectedBus.location.longitude.toFixed(6)}`}
                        />
                      </ListItem>
                      <ListItem>
                        <ListItemText
                          primary="Last Update"
                          secondary={new Date(selectedBus.location.timestamp).toLocaleString()}
                        />
                      </ListItem>
                    </List>
                  </>
                )}
              </CardContent>
            </Card>

            <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
              Recent Alerts
            </Typography>
            <List dense>
              {alerts
                .filter(alert => alert.busId === selectedBus.id)
                .slice(0, 5)
                .map((alert, index) => (
                  <React.Fragment key={index}>
                    <ListItem>
                      <Warning fontSize="small" color="warning" sx={{ mr: 1 }} />
                      <ListItemText
                        primary={alert.alertType}
                        secondary={alert.message}
                      />
                    </ListItem>
                    {index < 4 && <Divider />}
                  </React.Fragment>
                ))}
              {alerts.filter(alert => alert.busId === selectedBus.id).length === 0 && (
                <ListItem>
                  <ListItemText secondary="No recent alerts" />
                </ListItem>
              )}
            </List>
          </Box>
        )}
      </Drawer>
    </Container>
  );
};

export default LiveMapPage;
