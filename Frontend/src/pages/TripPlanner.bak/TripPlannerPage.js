import React from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Alert
} from '@mui/material';

const TripPlannerPage = () => {
  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          🧭 Trip Planner
        </Typography>
        <Paper sx={{ p: 3, mt: 3 }}>
          <Alert severity="info">
            Trip planning tool will be displayed here. This feature integrates with the Route Service
            to help you find the best routes for your journey.
          </Alert>
          <Typography variant="body1" sx={{ mt: 2 }}>
            Features coming soon:
          </Typography>
          <ul>
            <li>Origin and destination search</li>
            <li>Optimal route calculation</li>
            <li>Multiple transport options</li>
            <li>Estimated travel time</li>
            <li>Step-by-step directions</li>
          </ul>
        </Paper>
      </Box>
    </Container>
  );
};

export default TripPlannerPage;
