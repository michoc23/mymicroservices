import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Paper,
  LinearProgress,
  Chip,
  Divider,
} from '@mui/material';
import {
  Analytics,
  TrendingUp,
  DirectionsBus,
  ConfirmationNumber,
  CardMembership,
  AccessTime,
  Route,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';

const AnalyticsPage = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState({
    totalTrips: 0,
    totalTickets: 0,
    totalSubscriptions: 0,
    averageTripDuration: 0,
    favoriteRoute: null,
    monthlyUsage: [],
  });

  useEffect(() => {
    // Simulate loading analytics data
    setTimeout(() => {
      setStats({
        totalTrips: 127,
        totalTickets: 45,
        totalSubscriptions: 2,
        averageTripDuration: 28,
        favoriteRoute: 'Metro Line 1',
        monthlyUsage: [
          { month: 'Jan', trips: 12, tickets: 5 },
          { month: 'Feb', trips: 18, tickets: 7 },
          { month: 'Mar', trips: 15, tickets: 6 },
          { month: 'Apr', trips: 22, tickets: 8 },
          { month: 'May', trips: 25, tickets: 9 },
          { month: 'Jun', trips: 35, tickets: 10 },
        ],
      });
    }, 500);
  }, []);

  const StatCard = ({ icon, title, value, subtitle, color = 'primary' }) => (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
          <Box>
            <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
              {title}
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 700, color: `${color}.main` }}>
              {value}
            </Typography>
            {subtitle && (
              <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5, display: 'block' }}>
                {subtitle}
              </Typography>
            )}
          </Box>
          <Box
            sx={{
              width: 56,
              height: 56,
              borderRadius: 2,
              backgroundColor: `${color}.light`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: `${color}.main`,
            }}
          >
            {icon}
          </Box>
        </Box>
      </CardContent>
    </Card>
  );

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
          Analytics
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Usage insights and travel statistics
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Statistics Cards */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<DirectionsBus />}
            title="Total Trips"
            value={stats.totalTrips}
            subtitle="All time"
            color="primary"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<ConfirmationNumber />}
            title="Tickets Purchased"
            value={stats.totalTickets}
            subtitle="Single tickets"
            color="success"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<CardMembership />}
            title="Active Subscriptions"
            value={stats.totalSubscriptions}
            subtitle="Current plans"
            color="info"
          />
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<AccessTime />}
            title="Avg. Trip Duration"
            value={`${stats.averageTripDuration} min`}
            subtitle="Per journey"
            color="warning"
          />
        </Grid>

        {/* Usage Overview */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <TrendingUp sx={{ mr: 2, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Monthly Usage Trend
                </Typography>
              </Box>

              <Box sx={{ mb: 3 }}>
                {stats.monthlyUsage.map((data, index) => (
                  <Box key={index} sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2" sx={{ fontWeight: 500 }}>
                        {data.month}
                      </Typography>
                      <Typography variant="body2" color="text.secondary">
                        {data.trips} trips • {data.tickets} tickets
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={(data.trips / 40) * 100}
                      sx={{ height: 8, borderRadius: 1 }}
                    />
                  </Box>
                ))}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Favorite Route */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Route sx={{ mr: 2, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Favorite Route
                </Typography>
              </Box>

              {stats.favoriteRoute ? (
                <Box>
                  <Chip
                    label={stats.favoriteRoute}
                    color="primary"
                    sx={{ mb: 2, fontSize: '1rem', height: 32 }}
                  />
                  <Typography variant="body2" color="text.secondary">
                    Most frequently used route in the last 6 months
                  </Typography>
                </Box>
              ) : (
                <Typography variant="body2" color="text.secondary">
                  No favorite route yet. Start using routes to see your preferences.
                </Typography>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Travel Insights */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <Analytics sx={{ mr: 2, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Travel Insights
                </Typography>
              </Box>

              <Grid container spacing={3}>
                <Grid item xs={12} md={4}>
                  <Paper sx={{ p: 2, backgroundColor: 'background.default' }}>
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                      Peak Travel Time
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 600 }}>
                      8:00 AM - 9:00 AM
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Morning rush hour
                    </Typography>
                  </Paper>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Paper sx={{ p: 2, backgroundColor: 'background.default' }}>
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                      Most Active Day
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 600 }}>
                      Monday
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      Start of the week
                    </Typography>
                  </Paper>
                </Grid>
                <Grid item xs={12} md={4}>
                  <Paper sx={{ p: 2, backgroundColor: 'background.default' }}>
                    <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                      Total Distance
                    </Typography>
                    <Typography variant="h5" sx={{ fontWeight: 600 }}>
                      1,247 km
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      All time distance traveled
                    </Typography>
                  </Paper>
                </Grid>
              </Grid>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default AnalyticsPage;

