import React, { useState, useEffect } from 'react';
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Grid,
} from '@mui/material';
import { useAuth } from '../../contexts/AuthContext';

const SimpleDashboard = () => {
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);

  return (
    <Box>
      {/* Welcome Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
          Welcome back, {user?.firstName || 'User'}! 👋
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Your Urban Transport Dashboard
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Quick Stats */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Quick Stats
              </Typography>
              <Typography variant="body1">
                • Total Trips: Loading...
              </Typography>
              <Typography variant="body1">
                • Active Subscription: None
              </Typography>
              <Typography variant="body1">
                • Account Status: Active
              </Typography>
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Actions */}
        <Grid item xs={12} md={6}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Quick Actions
              </Typography>
              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Button variant="contained" href="/subscriptions">
                  Manage Subscriptions
                </Button>
                <Button variant="outlined" href="/tickets">
                  Buy Tickets
                </Button>
                <Button variant="outlined" href="/routes">
                  Plan Journey
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Welcome Message */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                Getting Started
              </Typography>
              <Typography variant="body1" sx={{ mb: 2 }}>
                Welcome to Urban Transport! Here's what you can do:
              </Typography>
              <Typography variant="body2">
                1. Create a monthly ($29.99) or annual ($299.99) subscription for unlimited travel
              </Typography>
              <Typography variant="body2">
                2. Purchase individual tickets for single journeys
              </Typography>
              <Typography variant="body2">
                3. Plan your routes and track buses in real-time
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default SimpleDashboard;