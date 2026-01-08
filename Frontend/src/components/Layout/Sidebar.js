import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import {
  Drawer,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Divider,
  Box,
  Typography,
  Chip,
} from '@mui/material';
import {
  Dashboard,
  ConfirmationNumber,
  CardMembership,
  Map,
  Navigation,
  Person,
  Analytics,
  Support,
  Settings,
  QrCodeScanner,
} from '@mui/icons-material';

const SIDEBAR_WIDTH = 280;

const menuItems = [
  {
    text: 'Dashboard',
    icon: <Dashboard />,
    path: '/dashboard',
    description: 'Overview & stats',
    roles: ['PASSENGER', 'DRIVER', 'CONTROLLER', 'ADMIN']
  },
  {
    text: 'Tickets',
    icon: <ConfirmationNumber />,
    path: '/tickets',
    description: 'Manage your tickets',
    roles: ['PASSENGER', 'ADMIN']
  },
  {
    text: 'Subscriptions',
    icon: <CardMembership />,
    path: '/subscriptions',
    description: 'Monthly & annual plans',
    badge: 'New',
    roles: ['PASSENGER', 'ADMIN']
  },
  {
    text: 'Live Map',
    icon: <Map />,
    path: '/live-map',
    description: 'Real-time bus tracking',
    badge: 'Live',
    roles: ['PASSENGER', 'DRIVER', 'CONTROLLER', 'ADMIN']
  },
  {
    text: 'Trip Planner',
    icon: <Navigation />,
    path: '/trip-planner',
    description: 'Plan your journey',
    roles: ['PASSENGER', 'ADMIN']
  },
  {
    text: 'Controller',
    icon: <QrCodeScanner />,
    path: '/controller',
    description: 'Scan & validate tickets',
    badge: 'Controller',
    roles: ['CONTROLLER', 'ADMIN']
  },
];

const secondaryItems = [
  {
    text: 'Profile',
    icon: <Person />,
    path: '/profile',
    description: 'Account settings',
    roles: ['PASSENGER', 'DRIVER', 'CONTROLLER', 'ADMIN']
  },
  {
    text: 'Analytics',
    icon: <Analytics />,
    path: '/analytics',
    description: 'Usage insights',
    roles: ['PASSENGER', 'ADMIN']
  },
  {
    text: 'Support',
    icon: <Support />,
    path: '/support',
    description: 'Get help',
    roles: ['PASSENGER', 'DRIVER', 'CONTROLLER', 'ADMIN']
  },
  {
    text: 'Settings',
    icon: <Settings />,
    path: '/settings',
    description: 'App preferences',
    roles: ['PASSENGER', 'DRIVER', 'CONTROLLER', 'ADMIN']
  },
];

const Sidebar = ({ open, onClose, isMobile }) => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();

  const handleNavigation = (path) => {
    navigate(path);
    if (isMobile) {
      onClose();
    }
  };

  const isActive = (path) => location.pathname === path;

  // Filter menu items based on user role
  const filterByRole = (items) => {
    if (!user || !user.role) return items;
    return items.filter(item => !item.roles || item.roles.includes(user.role));
  };

  const renderMenuItems = (items, showDivider = false) => {
    const filteredItems = filterByRole(items);

    return (
      <>
        {showDivider && <Divider sx={{ my: 2 }} />}
        <List sx={{ px: 1 }}>
          {filteredItems.map((item) => (
            <ListItem key={item.text} disablePadding sx={{ mb: 0.5 }}>
              <ListItemButton
                onClick={() => handleNavigation(item.path)}
                sx={{
                  borderRadius: 2,
                  backgroundColor: isActive(item.path) ? 'primary.light' : 'transparent',
                  color: isActive(item.path) ? 'primary.contrastText' : 'text.primary',
                  '&:hover': {
                    backgroundColor: isActive(item.path)
                      ? 'primary.main'
                      : 'action.hover',
                  },
                  py: 1.5,
                  px: 2,
                }}
              >
                <ListItemIcon
                  sx={{
                    color: isActive(item.path) ? 'primary.contrastText' : 'primary.main',
                    minWidth: 40,
                  }}
                >
                  {item.icon}
                </ListItemIcon>
                <Box sx={{ flexGrow: 1 }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <ListItemText
                      primary={item.text}
                      primaryTypographyProps={{
                        variant: 'body2',
                        fontWeight: isActive(item.path) ? 600 : 500,
                      }}
                    />
                    {item.badge && (
                      <Chip
                        label={item.badge}
                        size="small"
                        color="secondary"
                        sx={{ height: 20, fontSize: '0.7rem' }}
                      />
                    )}
                  </Box>
                  <Typography
                    variant="caption"
                    sx={{
                      color: isActive(item.path)
                        ? 'rgba(255, 255, 255, 0.8)'
                        : 'text.secondary',
                      display: 'block',
                      lineHeight: 1,
                    }}
                  >
                    {item.description}
                  </Typography>
                </Box>
              </ListItemButton>
            </ListItem>
          ))}
        </List>
      </>
    );
  };

  const drawerContent = (
    <Box
      sx={{
        height: '100%',
        display: 'flex',
        flexDirection: 'column',
        backgroundColor: 'background.paper',
      }}
    >
      {/* Header */}
      <Box sx={{ p: 3, pt: 4 }}>
        <Typography variant="h6" sx={{ fontWeight: 700, color: 'primary.main' }}>
          Navigation
        </Typography>
        <Typography variant="caption" color="text.secondary">
          Quick access to all features
        </Typography>
      </Box>

      {/* Main Navigation */}
      {renderMenuItems(menuItems)}

      {/* Secondary Navigation */}
      {renderMenuItems(secondaryItems, true)}

      {/* Footer */}
      <Box sx={{ mt: 'auto', p: 3 }}>
        <Box
          sx={{
            p: 2,
            borderRadius: 2,
            backgroundColor: 'primary.light',
            textAlign: 'center',
          }}
        >
          <Typography variant="body2" sx={{ color: 'primary.contrastText', mb: 1 }}>
            Need Help?
          </Typography>
          <Typography variant="caption" sx={{ color: 'rgba(255, 255, 255, 0.8)' }}>
            Contact our support team for assistance
          </Typography>
        </Box>
      </Box>
    </Box>
  );

  return (
    <Drawer
      variant={isMobile ? 'temporary' : 'persistent'}
      anchor="left"
      open={open}
      onClose={onClose}
      ModalProps={{
        keepMounted: true, // Better open performance on mobile
      }}
      sx={{
        width: SIDEBAR_WIDTH,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: SIDEBAR_WIDTH,
          boxSizing: 'border-box',
          borderRight: 'none',
          boxShadow: isMobile ? 'none' : '0 0 10px rgba(0,0,0,0.1)',
        },
      }}
    >
      {drawerContent}
    </Drawer>
  );
}

export default Sidebar;