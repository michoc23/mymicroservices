import React from 'react';
import {
  Box,
  Container,
  Paper,
  Typography,
  Alert
} from '@mui/material';

const LiveMapPage = () => {
  return (
    <Container maxWidth="xl">
      <Box sx={{ mt: 4, mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          🗺️ Live Bus Map
        </Typography>
        <Paper sx={{ p: 3, mt: 3 }}>
          <Alert severity="info">
            Live bus tracking map will be displayed here. This feature integrates with the Bus Geolocation Service
            to show real-time bus positions on an interactive map.
          </Alert>
          <Typography variant="body1" sx={{ mt: 2 }}>
            Features coming soon:
          </Typography>
          <ul>
            <li>Real-time bus location tracking</li>
            <li>Route visualization</li>
            <li>Bus status indicators</li>
            <li>Alert notifications</li>
          </ul>
        </Paper>
      </Box>
    </Container>
  );
};

export default LiveMapPage;
