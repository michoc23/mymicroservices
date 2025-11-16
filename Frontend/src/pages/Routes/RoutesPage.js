import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  TextField,
  Paper,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Chip,
  Divider,
  InputAdornment,
} from '@mui/material';
import {
  Route,
  Search,
  DirectionsBus,
  Schedule,
  LocationOn,
  MyLocation,
  SwapVert,
  Navigation,
} from '@mui/icons-material';
import LoadingSpinner from '../../components/Common/LoadingSpinner';

const RoutesPage = () => {
  const [routes, setRoutes] = useState([]);
  const [searchQuery, setSearchQuery] = useState({
    from: '',
    to: '',
  });
  const [loading, setLoading] = useState(true);
  const [searchResults, setSearchResults] = useState([]);

  // Mock data - replace with real API calls
  useEffect(() => {
    const fetchRoutes = async () => {
      // Simulate API call
      setTimeout(() => {
        setRoutes([
          {
            id: 1,
            name: 'Line 1',
            origin: 'Downtown Station',
            destination: 'Airport Terminal',
            distance: '25.4 km',
            duration: '45 min',
            frequency: '10 min',
            status: 'ACTIVE',
            stops: ['Downtown', 'University', 'Mall Plaza', 'Business District', 'Airport'],
          },
          {
            id: 2,
            name: 'Line 2',
            origin: 'Central Park',
            destination: 'Industrial Zone',
            distance: '18.2 km',
            duration: '32 min',
            frequency: '15 min',
            status: 'ACTIVE',
            stops: ['Central Park', 'Hospital', 'Shopping Center', 'Residential Area', 'Industrial Zone'],
          },
          {
            id: 3,
            name: 'Line 3',
            origin: 'Beach Front',
            destination: 'Mountain View',
            distance: '31.8 km',
            duration: '55 min',
            frequency: '20 min',
            status: 'ACTIVE',
            stops: ['Beach Front', 'Marina', 'City Center', 'Hills District', 'Mountain View'],
          },
        ]);
        setLoading(false);
      }, 1000);
    };

    fetchRoutes();
  }, []);

  const handleSearch = () => {
    if (searchQuery.from && searchQuery.to) {
      // Simulate search results
      setSearchResults([
        {
          id: 1,
          route: 'Line 1 → Line 2',
          duration: '52 min',
          transfers: 1,
          cost: '$2.50',
          steps: [
            { line: 'Line 1', from: searchQuery.from, to: 'Business District', duration: '25 min' },
            { line: 'Walk', from: 'Business District', to: 'Hospital', duration: '7 min' },
            { line: 'Line 2', from: 'Hospital', to: searchQuery.to, duration: '20 min' },
          ]
        }
      ]);
    }
  };

  const handleSwapLocations = () => {
    setSearchQuery({
      from: searchQuery.to,
      to: searchQuery.from,
    });
  };

  const RouteCard = ({ route }) => (
    <Card sx={{ mb: 2 }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <DirectionsBus sx={{ mr: 1, color: 'primary.main' }} />
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              {route.name}
            </Typography>
          </Box>
          <Chip
            label={route.status}
            color="success"
            size="small"
          />
        </Box>

        <Typography variant="body1" sx={{ mb: 1 }}>
          {route.origin} → {route.destination}
        </Typography>

        <Box sx={{ display: 'flex', gap: 2, mb: 2, flexWrap: 'wrap' }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Route sx={{ mr: 0.5, fontSize: 16, color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary">
              {route.distance}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Schedule sx={{ mr: 0.5, fontSize: 16, color: 'text.secondary' }} />
            <Typography variant="body2" color="text.secondary">
              {route.duration}
            </Typography>
          </Box>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <Typography variant="body2" color="text.secondary">
              Every {route.frequency}
            </Typography>
          </Box>
        </Box>

        <Typography variant="subtitle2" sx={{ mb: 1 }}>
          Stops ({route.stops.length}):
        </Typography>
        <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 0.5 }}>
          {route.stops.map((stop, index) => (
            <Chip
              key={index}
              label={stop}
              size="small"
              variant="outlined"
            />
          ))}
        </Box>

        <Box sx={{ display: 'flex', gap: 1, mt: 2 }}>
          <Button variant="outlined" size="small" startIcon={<Navigation />}>
            Track Bus
          </Button>
          <Button variant="outlined" size="small" startIcon={<Schedule />}>
            Timetable
          </Button>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return <LoadingSpinner message="Loading routes..." />;
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
          Routes & Journey Planner
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Find the best routes for your journey
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Journey Planner */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ position: 'sticky', top: 20 }}>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>
                Plan Your Journey
              </Typography>

              <TextField
                fullWidth
                label="From"
                value={searchQuery.from}
                onChange={(e) => setSearchQuery({ ...searchQuery, from: e.target.value })}
                margin="normal"
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <MyLocation color="action" />
                    </InputAdornment>
                  ),
                }}
              />

              <Box sx={{ display: 'flex', justifyContent: 'center', my: 1 }}>
                <Button
                  onClick={handleSwapLocations}
                  sx={{ minWidth: 'auto', p: 1 }}
                >
                  <SwapVert />
                </Button>
              </Box>

              <TextField
                fullWidth
                label="To"
                value={searchQuery.to}
                onChange={(e) => setSearchQuery({ ...searchQuery, to: e.target.value })}
                margin="normal"
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <LocationOn color="action" />
                    </InputAdornment>
                  ),
                }}
              />

              <Button
                fullWidth
                variant="contained"
                startIcon={<Search />}
                onClick={handleSearch}
                sx={{ mt: 2, mb: 3 }}
                disabled={!searchQuery.from || !searchQuery.to}
              >
                Find Routes
              </Button>

              {/* Search Results */}
              {searchResults.length > 0 && (
                <Box>
                  <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 2 }}>
                    Journey Options
                  </Typography>
                  {searchResults.map((result) => (
                    <Paper key={result.id} sx={{ p: 2, mb: 2, backgroundColor: 'background.default' }}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="subtitle2">{result.route}</Typography>
                        <Typography variant="body2" color="primary" sx={{ fontWeight: 600 }}>
                          {result.cost}
                        </Typography>
                      </Box>
                      <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                        {result.duration} • {result.transfers} transfer(s)
                      </Typography>
                      <Divider sx={{ my: 1 }} />
                      {result.steps.map((step, index) => (
                        <Box key={index} sx={{ display: 'flex', alignItems: 'center', mb: 0.5 }}>
                          <Typography variant="caption" color="text.secondary">
                            {step.line}: {step.from} → {step.to} ({step.duration})
                          </Typography>
                        </Box>
                      ))}
                      <Button size="small" variant="outlined" sx={{ mt: 1 }}>
                        Select Route
                      </Button>
                    </Paper>
                  ))}
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Routes List */}
        <Grid item xs={12} lg={8}>
          <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>
            All Routes
          </Typography>

          {routes.map((route) => (
            <RouteCard key={route.id} route={route} />
          ))}

          {/* Quick Stats */}
          <Paper sx={{ p: 3, mt: 3 }}>
            <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
              Network Statistics
            </Typography>
            <Grid container spacing={2}>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="primary" sx={{ fontWeight: 700 }}>
                    {routes.length}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Active Routes
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="success.main" sx={{ fontWeight: 700 }}>
                    {routes.reduce((total, route) => total + route.stops.length, 0)}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total Stops
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="info.main" sx={{ fontWeight: 700 }}>
                    75.4
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    Total KM
                  </Typography>
                </Box>
              </Grid>
              <Grid item xs={6} sm={3}>
                <Box sx={{ textAlign: 'center' }}>
                  <Typography variant="h4" color="warning.main" sx={{ fontWeight: 700 }}>
                    97%
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    On-Time Rate
                  </Typography>
                </Box>
              </Grid>
            </Grid>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
};

export default RoutesPage;