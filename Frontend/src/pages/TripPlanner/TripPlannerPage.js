import React, { useState, useEffect } from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  TextField,
  Button,
  Grid,
  Card,
  CardContent,
  Chip,
  List,
  ListItem,
  ListItemText,
  Divider,
  IconButton,
  CircularProgress,
  Alert,
  Autocomplete
} from '@mui/material';
import {
  MyLocation,
  LocationOn,
  SwapVert,
  Search,
  DirectionsBus,
  Schedule,
  Route as RouteIcon,
  DirectionsWalk
} from '@mui/icons-material';
import { MapContainer, TileLayer, Marker, Popup, Polyline } from 'react-leaflet';
import L from 'leaflet';
import routeService from '../../services/routeService';
import { toast } from 'react-toastify';
import 'leaflet/dist/leaflet.css';

// Fix default marker icon issue with webpack
delete L.Icon.Default.prototype._getIconUrl;
L.Icon.Default.mergeOptions({
  iconRetinaUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon-2x.png',
  iconUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-icon.png',
  shadowUrl: 'https://cdnjs.cloudflare.com/ajax/libs/leaflet/1.7.1/images/marker-shadow.png',
});

const TripPlannerPage = () => {
  const [stops, setStops] = useState([]);
  const [routes, setRoutes] = useState([]);
  const [fromStop, setFromStop] = useState(null);
  const [toStop, setToStop] = useState(null);
  const [searchResults, setSearchResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [loadingData, setLoadingData] = useState(true);
  const [nearbyStops, setNearbyStops] = useState([]);

  useEffect(() => {
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      setLoadingData(true);

      // Load all stops
      const stopsResponse = await routeService.getAllStops();
      setStops(stopsResponse.data?.content || stopsResponse.data || []);

      // Load all routes
      const routesResponse = await routeService.getAllRoutes();
      setRoutes(routesResponse.data?.content || routesResponse.data || []);

      setLoadingData(false);
    } catch (error) {
      console.error('Failed to load data:', error);
      toast.error('Failed to load stops and routes');
      setLoadingData(false);
    }
  };

  const handleSwapStops = () => {
    const temp = fromStop;
    setFromStop(toStop);
    setToStop(temp);
  };

  const handleFindNearby = async () => {
    if (!navigator.geolocation) {
      toast.error('Geolocation is not supported by your browser');
      return;
    }

    navigator.geolocation.getCurrentPosition(
      async (position) => {
        try {
          const { latitude, longitude } = position.coords;
          const response = await routeService.getNearbyStops(latitude, longitude, 1);
          setNearbyStops(response.data || []);
          toast.success(`Found ${response.data?.length || 0} nearby stops`);
        } catch (error) {
          console.error('Failed to find nearby stops:', error);
          toast.error('Failed to find nearby stops');
        }
      },
      (error) => {
        console.error('Geolocation error:', error);
        toast.error('Failed to get your location');
      }
    );
  };

  const handleSearch = async () => {
    if (!fromStop || !toStop) {
      toast.warning('Please select both origin and destination');
      return;
    }

    if (fromStop.id === toStop.id) {
      toast.warning('Origin and destination cannot be the same');
      return;
    }

    try {
      setLoading(true);
      const response = await routeService.calculateOptimalPath(fromStop.id, toStop.id);
      setSearchResults(response.data);
      setLoading(false);
    } catch (error) {
      console.error('Failed to calculate route:', error);
      toast.error('Failed to calculate route. The route service may not be available.');
      setLoading(false);
    }
  };

  if (loadingData) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="80vh">
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
          🧭 Trip Planner
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          Plan your journey and find the best routes
        </Typography>

        <Grid container spacing={3}>
          {/* Search Panel */}
          <Grid item xs={12} lg={4}>
            <Paper sx={{ p: 3, position: 'sticky', top: 20 }}>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>
                Plan Your Journey
              </Typography>

              <Autocomplete
                value={fromStop}
                onChange={(event, newValue) => setFromStop(newValue)}
                options={stops}
                getOptionLabel={(option) => option.name || ''}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="From"
                    placeholder="Select origin stop"
                    fullWidth
                    margin="normal"
                    InputProps={{
                      ...params.InputProps,
                      startAdornment: (
                        <>
                          <MyLocation color="action" sx={{ mr: 1 }} />
                          {params.InputProps.startAdornment}
                        </>
                      ),
                    }}
                  />
                )}
              />

              <Box sx={{ display: 'flex', justifyContent: 'center', my: 1 }}>
                <IconButton onClick={handleSwapStops} color="primary">
                  <SwapVert />
                </IconButton>
              </Box>

              <Autocomplete
                value={toStop}
                onChange={(event, newValue) => setToStop(newValue)}
                options={stops}
                getOptionLabel={(option) => option.name || ''}
                renderInput={(params) => (
                  <TextField
                    {...params}
                    label="To"
                    placeholder="Select destination stop"
                    fullWidth
                    margin="normal"
                    InputProps={{
                      ...params.InputProps,
                      startAdornment: (
                        <>
                          <LocationOn color="action" sx={{ mr: 1 }} />
                          {params.InputProps.startAdornment}
                        </>
                      ),
                    }}
                  />
                )}
              />

              <Button
                fullWidth
                variant="contained"
                startIcon={<Search />}
                onClick={handleSearch}
                disabled={!fromStop || !toStop || loading}
                sx={{ mt: 3, mb: 2 }}
              >
                {loading ? 'Searching...' : 'Find Routes'}
              </Button>

              <Button
                fullWidth
                variant="outlined"
                startIcon={<MyLocation />}
                onClick={handleFindNearby}
              >
                Find Nearby Stops
              </Button>

              {nearbyStops.length > 0 && (
                <Box sx={{ mt: 3 }}>
                  <Typography variant="subtitle2" sx={{ mb: 1, fontWeight: 600 }}>
                    Nearby Stops
                  </Typography>
                  <List dense>
                    {nearbyStops.slice(0, 5).map((stop) => (
                      <ListItem
                        key={stop.id}
                        button
                        onClick={() => setFromStop(stop)}
                      >
                        <ListItemText
                          primary={stop.name}
                          secondary={`${stop.distance ? stop.distance.toFixed(2) + ' km away' : ''}`}
                        />
                      </ListItem>
                    ))}
                  </List>
                </Box>
              )}
            </Paper>
          </Grid>

          {/* Results Panel */}
          <Grid item xs={12} lg={8}>
            {!searchResults && !loading && (
              <Paper sx={{ p: 4, textAlign: 'center' }}>
                <RouteIcon sx={{ fontSize: 64, color: 'action.disabled', mb: 2 }} />
                <Typography variant="h6" color="text.secondary">
                  Select origin and destination to find routes
                </Typography>
              </Paper>
            )}

            {loading && (
              <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
                <CircularProgress />
              </Box>
            )}

            {searchResults && !loading && (
              <Box>
                <Typography variant="h5" sx={{ fontWeight: 600, mb: 3 }}>
                  Journey Options
                </Typography>

                {searchResults.routes && searchResults.routes.length > 0 ? (
                  searchResults.routes.map((route, index) => (
                    <Card key={index} sx={{ mb: 2 }}>
                      <CardContent>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 2 }}>
                          <Typography variant="h6" sx={{ fontWeight: 600 }}>
                            Option {index + 1}
                          </Typography>
                          <Box sx={{ display: 'flex', gap: 1 }}>
                            <Chip
                              icon={<Schedule />}
                              label={`${route.duration || 'N/A'} min`}
                              color="primary"
                              size="small"
                            />
                            <Chip
                              icon={<DirectionsBus />}
                              label={`${route.transfers || 0} transfer(s)`}
                              size="small"
                            />
                          </Box>
                        </Box>

                        <Divider sx={{ my: 2 }} />

                        {route.segments && route.segments.length > 0 ? (
                          <List>
                            {route.segments.map((segment, segIndex) => (
                              <React.Fragment key={segIndex}>
                                <ListItem alignItems="flex-start">
                                  <Box sx={{ mr: 2, display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
                                    {segment.type === 'transit' ? (
                                      <DirectionsBus sx={{ color: segment.routeColor || 'primary.main' }} />
                                    ) : (
                                      <DirectionsWalk color="action" />
                                    )}
                                    {segIndex < route.segments.length - 1 && (
                                      <Box sx={{ width: '2px', height: '40px', bgcolor: 'divider', my: 1 }} />
                                    )}
                                  </Box>
                                  <ListItemText
                                    primary={
                                      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                                        {segment.type === 'transit' ?
                                          `${segment.routeName || `Route ${segment.routeNumber}`}` :
                                          'Walking'}
                                      </Typography>
                                    }
                                    secondary={
                                      <>
                                        <Typography variant="body2" color="text.primary" sx={{ fontWeight: 500 }}>
                                          {segment.instructions}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                          {segment.distance ? `${segment.distance.toFixed(2)} km` : ''} • {segment.duration} min
                                        </Typography>
                                        {segment.intermediateStops && segment.intermediateStops.length > 0 && (
                                          <Box sx={{ mt: 1, pl: 1, borderLeft: '2px dashed', borderColor: 'divider' }}>
                                            <Typography variant="caption" color="text.secondary">
                                              Passing through {segment.intermediateStops.length} stops
                                            </Typography>
                                          </Box>
                                        )}
                                      </>
                                    }
                                  />
                                </ListItem>
                              </React.Fragment>
                            ))}
                          </List>
                        ) : (
                          <Typography color="text.secondary">
                            Route details not available
                          </Typography>
                        )}

                        <Button
                          variant="outlined"
                          fullWidth
                          sx={{ mt: 2 }}
                        >
                          Select This Route
                        </Button>
                      </CardContent>
                    </Card>
                  ))
                ) : (
                  <Alert severity="info">
                    No direct routes found between these stops. The route service may be building the route database.
                  </Alert>
                )}

                {/* Show available routes info */}
                <Paper sx={{ p: 3, mt: 3 }}>
                  <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                    Available Routes in Network
                  </Typography>
                  <Grid container spacing={1}>
                    {routes.slice(0, 6).map((route) => (
                      <Grid item key={route.id}>
                        <Chip
                          label={route.name}
                          variant="outlined"
                          color="primary"
                        />
                      </Grid>
                    ))}
                  </Grid>
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 2 }}>
                    Total {routes.length} routes available • {stops.length} stops in network
                  </Typography>
                </Paper>

                {/* Map showing stops */}
                {stops.length > 0 && (
                  <Paper sx={{ p: 3, mt: 3 }}>
                    <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                      Network Map
                    </Typography>
                    <Box sx={{ height: '400px', borderRadius: 1, overflow: 'hidden' }}>
                      <MapContainer
                        center={fromStop ? [fromStop.latitude, fromStop.longitude] : [48.8566, 2.3522]}
                        zoom={12}
                        style={{ height: '100%', width: '100%' }}
                      >
                        <TileLayer
                          attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                          url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
                        />

                        {/* Show all stops */}
                        {stops.map((stop) => (
                          <Marker
                            key={stop.id}
                            position={[stop.latitude, stop.longitude]}
                          >
                            <Popup>
                              <Box sx={{ p: 1 }}>
                                <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>
                                  {stop.name}
                                </Typography>
                                <Typography variant="caption" display="block">
                                  Code: {stop.stopCode}
                                </Typography>
                                <Typography variant="caption" display="block">
                                  Type: {stop.stopType}
                                </Typography>
                              </Box>
                            </Popup>
                          </Marker>
                        ))}

                        {/* Draw route from search results if available */}
                        {searchResults && (searchResults.segments || searchResults.routes?.[0]?.segments) && (
                          <>
                            {(searchResults.segments || searchResults.routes[0].segments).map((segment, segIndex) => {
                              if (!segment.path || segment.path.length === 0) return null;

                              const positions = segment.path.map(coord => [
                                coord.lat || coord.latitude,
                                coord.lon || coord.longitude
                              ]);

                              const isTransit = segment.type === 'transit';
                              const color = isTransit ? (segment.routeColor || '#1976d2') : '#757575';
                              const weight = isTransit ? 6 : 4;
                              const opacity = isTransit ? 0.9 : 0.6;

                              return (
                                <Polyline
                                  key={segIndex}
                                  positions={positions}
                                  pathOptions={{
                                    color: color,
                                    weight: weight,
                                    opacity: opacity,
                                    lineCap: 'round',
                                    lineJoin: 'round',
                                    dashArray: isTransit ? null : '5, 10'
                                  }}
                                  smoothFactor={1}
                                />
                              );
                            })}
                          </>
                        )}

                        {/* Fallback: Draw simple line between selected stops if no search results */}
                        {!searchResults && fromStop && toStop && (
                          <Polyline
                            positions={[
                              [fromStop.latitude, fromStop.longitude],
                              [toStop.latitude, toStop.longitude]
                            ]}
                            color="#1976d2"
                            weight={3}
                            opacity={0.7}
                            lineCap="round"
                            lineJoin="round"
                            dashArray="10, 10"
                          />
                        )}
                      </MapContainer>
                    </Box>
                  </Paper>
                )}
              </Box>
            )}
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default TripPlannerPage;
