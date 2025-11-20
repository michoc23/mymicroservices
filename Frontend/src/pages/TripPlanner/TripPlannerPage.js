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
  Route as RouteIcon
} from '@mui/icons-material';
import routeService from '../../services/routeService';
import { toast } from 'react-toastify';

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

                        {route.steps && route.steps.length > 0 ? (
                          <List>
                            {route.steps.map((step, stepIndex) => (
                              <React.Fragment key={stepIndex}>
                                <ListItem>
                                  <DirectionsBus color="primary" sx={{ mr: 2 }} />
                                  <ListItemText
                                    primary={
                                      <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                                        {step.routeName || `Route ${step.routeId}`}
                                      </Typography>
                                    }
                                    secondary={
                                      <>
                                        <Typography variant="body2" color="text.secondary">
                                          From: {step.fromStop || fromStop.name}
                                        </Typography>
                                        <Typography variant="body2" color="text.secondary">
                                          To: {step.toStop || toStop.name}
                                        </Typography>
                                        {step.duration && (
                                          <Typography variant="body2" color="text.secondary">
                                            Duration: {step.duration} min
                                          </Typography>
                                        )}
                                      </>
                                    }
                                  />
                                </ListItem>
                                {stepIndex < route.steps.length - 1 && <Divider variant="inset" component="li" />}
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
              </Box>
            )}
          </Grid>
        </Grid>
      </Box>
    </Container>
  );
};

export default TripPlannerPage;
