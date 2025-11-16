import React from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Avatar,
  Chip,
  Paper,
  LinearProgress,
} from '@mui/material';
import {
  CardMembership,
  ConfirmationNumber,
  Route,
  TrendingUp,
  DirectionsBus,
  Schedule,
  Payment,
  Star,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';

const DashboardPage = () => {
  const { user } = useAuth();

  // Mock data - replace with real API calls
  const dashboardData = {
    activeSubscription: {
      type: 'Monthly',
      endDate: '2024-02-15',
      daysRemaining: 12,
      status: 'ACTIVE',
    },
    recentTickets: [
      { id: 1, route: 'Line 1', date: '2024-01-10', status: 'Used' },
      { id: 2, route: 'Line 3', date: '2024-01-08', status: 'Used' },
    ],
    stats: {
      totalTrips: 24,
      monthlySavings: 45.50,
      favoriteRoute: 'Line 1',
      avgTripTime: '25 min',
    },
  };

  const StatCard = ({ icon, title, value, subtitle, color = 'primary' }) => (
    <Card sx={{ height: '100%', position: 'relative', overflow: 'visible' }}>
      <CardContent sx={{ pb: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Avatar
            sx={{
              bgcolor: `${color}.light`,
              color: `${color}.contrastText`,
              mr: 2,
            }}
          >
            {icon}
          </Avatar>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 700, lineHeight: 1 }}>
              {value}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {title}
            </Typography>
          </Box>
        </Box>
        {subtitle && (
          <Typography variant="caption" color="text.secondary">
            {subtitle}
          </Typography>
        )}
      </CardContent>
    </Card>
  );

  return (
    <Box>
      {/* Welcome Header */}
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
          Welcome back, {user?.firstName}! 👋
        </Typography>
        <Typography variant="body1" color="text.secondary">
          Here's your urban transport overview
        </Typography>
      </Box>

      <Grid container spacing={3}>
        {/* Quick Stats */}
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<ConfirmationNumber />}
            title="Total Trips"
            value={dashboardData.stats.totalTrips}
            subtitle="This month"
            color="primary"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<Payment />}
            title="Monthly Savings"
            value={`$${dashboardData.stats.monthlySavings}`}
            subtitle="Compared to individual tickets"
            color="success"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<Route />}
            title="Favorite Route"
            value={dashboardData.stats.favoriteRoute}
            subtitle="Most used this month"
            color="info"
          />
        </Grid>
        
        <Grid item xs={12} sm={6} md={3}>
          <StatCard
            icon={<Schedule />}
            title="Avg Trip Time"
            value={dashboardData.stats.avgTripTime}
            subtitle="Based on your routes"
            color="warning"
          />
        </Grid>

        {/* Active Subscription */}
        <Grid item xs={12} md={8}>
          <Card>
            <CardContent>
              <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
                <CardMembership sx={{ mr: 2, color: 'primary.main' }} />
                <Typography variant="h6" sx={{ fontWeight: 600 }}>
                  Active Subscription
                </Typography>
              </Box>

              {dashboardData.activeSubscription ? (
                <Box>
                  <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                    <Chip
                      label={dashboardData.activeSubscription.type}
                      color="primary"
                      variant="outlined"
                      sx={{ mr: 2 }}
                    />
                    <Chip
                      label={dashboardData.activeSubscription.status}
                      color="success"
                      size="small"
                    />
                  </Box>

                  <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                    Valid until {dashboardData.activeSubscription.endDate}
                  </Typography>

                  <Box sx={{ mb: 2 }}>
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                      <Typography variant="body2">
                        Days remaining
                      </Typography>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {dashboardData.activeSubscription.daysRemaining} days
                      </Typography>
                    </Box>
                    <LinearProgress
                      variant="determinate"
                      value={(dashboardData.activeSubscription.daysRemaining / 30) * 100}
                      sx={{ borderRadius: 1 }}
                    />
                  </Box>

                  <Button variant="outlined" size="small">
                    Manage Subscription
                  </Button>
                </Box>
              ) : (
                <Box sx={{ textAlign: 'center', py: 3 }}>
                  <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
                    No active subscription
                  </Typography>
                  <Button variant="contained" startIcon={<CardMembership />}>
                    Subscribe Now
                  </Button>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>

        {/* Quick Actions */}
        <Grid item xs={12} md={4}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>
                Quick Actions
              </Typography>

              <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                <Button
                  variant="outlined"
                  startIcon={<ConfirmationNumber />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  Buy Ticket
                </Button>

                <Button
                  variant="outlined"
                  startIcon={<Route />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  Plan Journey
                </Button>

                <Button
                  variant="outlined"
                  startIcon={<DirectionsBus />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  Track Bus
                </Button>

                <Button
                  variant="outlined"
                  startIcon={<TrendingUp />}
                  fullWidth
                  sx={{ justifyContent: 'flex-start' }}
                >
                  View Analytics
                </Button>
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Recent Activity */}
        <Grid item xs={12}>
          <Card>
            <CardContent>
              <Typography variant="h6" sx={{ fontWeight: 600, mb: 3 }}>
                Recent Activity
              </Typography>

              {dashboardData.recentTickets.length > 0 ? (
                <Box>
                  {dashboardData.recentTickets.map((ticket) => (
                    <Paper
                      key={ticket.id}
                      sx={{
                        p: 2,
                        mb: 2,
                        backgroundColor: 'background.default',
                        border: '1px solid',
                        borderColor: 'divider',
                      }}
                    >
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                        <Box sx={{ display: 'flex', alignItems: 'center' }}>
                          <DirectionsBus sx={{ mr: 2, color: 'primary.main' }} />
                          <Box>
                            <Typography variant="body1" sx={{ fontWeight: 500 }}>
                              {ticket.route}
                            </Typography>
                            <Typography variant="body2" color="text.secondary">
                              {ticket.date}
                            </Typography>
                          </Box>
                        </Box>
                        <Chip
                          label={ticket.status}
                          size="small"
                          color={ticket.status === 'Used' ? 'success' : 'default'}
                        />
                      </Box>
                    </Paper>
                  ))}

                  <Button variant="outlined" fullWidth sx={{ mt: 2 }}>
                    View All Activity
                  </Button>
                </Box>
              ) : (
                <Box sx={{ textAlign: 'center', py: 3 }}>
                  <Typography variant="body1" color="text.secondary">
                    No recent activity
                  </Typography>
                </Box>
              )}
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
};

export default DashboardPage;