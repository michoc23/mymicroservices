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
import { MapContainer, TileLayer, Marker, Popup } from 'react-leaflet';
import { DirectionsBus, Warning, Refresh, Close, Map as MapIcon } from '@mui/icons-material';
import L from 'leaflet';
import busLocationService from '../../services/busLocationService';
import webSocketService from '../../services/webSocketService';
import { toast } from 'react-toastify';
import 'leaflet/dist/leaflet.css';

// Fix default marker icon issue with webpack
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

// Custom bus icon
const busIcon = L.divIcon({
  html: '<div style="background-color: #1976d2; color: white; border-radius: 50%; width: 30px; height: 30px; display: flex; align-items: center; justify-content: center; border: 2px solid white; box-shadow: 0 2px 4px rgba(0,0,0,0.3);"><svg viewBox="0 0 24 24" width="18" height="18" fill="currentColor"><path d="M4 16c0 .88.39 1.67 1 2.22V20c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1h8v1c0 .55.45 1 1 1h1c.55 0 1-.45 1-1v-1.78c.61-.55 1-1.34 1-2.22V6c0-3.5-3.58-4-8-4s-8 .5-8 4v10zm3.5 1c-.83 0-1.5-.67-1.5-1.5S6.67 14 7.5 14s1.5.67 1.5 1.5S8.33 17 7.5 17zm9 0c-.83 0-1.5-.67-1.5-1.5s.67-1.5 1.5-1.5 1.5.67 1.5 1.5-.67 1.5-1.5 1.5zm1.5-6H6V6h12v5z"/></svg></div>',
  className: 'custom-bus-icon',
  iconSize: [30, 30],
  iconAnchor: [15, 15],
  popupAnchor: [0, -15]
});

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

        <Paper sx={{ height: '70vh', position: 'relative', overflow: 'hidden' }}>
          <MapContainer
            center={mapCenter}
            zoom={mapZoom}
            style={{ height: '100%', width: '100%' }}
          >
            <TileLayer
              attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
              url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />
            
            {/* Render bus markers */}
            {busLocations.map((bus) => (
              bus.location && (
                <Marker
                  key={bus.id}
                  position={[bus.location.latitude, bus.location.longitude]}
                  icon={busIcon}
                >
                  <Popup>
                    <Box sx={{ p: 1, minWidth: 200 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
                        Bus {bus.busNumber || bus.id}
                      </Typography>
                      <Typography variant="caption" display="block">
                        Route: {bus.routeId}
                      </Typography>
                      <Typography variant="caption" display="block">
                        Speed: {(bus.location.speed || 0).toFixed(1)} km/h
                      </Typography>
                      <Typography variant="caption" display="block">
                        Heading: {(bus.location.heading || 0).toFixed(0)}°
                      </Typography>
                      <Typography variant="caption" display="block" color="text.secondary">
                        Status: {bus.status || 'ACTIVE'}
                      </Typography>
                      <Button
                        size="small"
                        variant="outlined"
                        fullWidth
                        sx={{ mt: 1 }}
                        onClick={() => handleBusClick(bus)}
                      >
                        More Details
                      </Button>
                    </Box>
                  </Popup>
                </Marker>
              )
            ))}
          </MapContainer>
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
