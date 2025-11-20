import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
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
  Route,
  Map,
  Navigation,
  Person,
  Analytics,
  Support,
  Settings,
} from '@mui/icons-material';

const SIDEBAR_WIDTH = 280;

const menuItems = [
  {
    text: 'Dashboard',
    icon: <Dashboard />,
    path: '/dashboard',
    description: 'Overview & stats'
  },
  {
    text: 'Tickets',
    icon: <ConfirmationNumber />,
    path: '/tickets',
    description: 'Manage your tickets'
  },
  {
    text: 'Subscriptions',
    icon: <CardMembership />,
    path: '/subscriptions',
    description: 'Monthly & annual plans',
    badge: 'New'
  },
  {
    text: 'Routes',
    icon: <Route />,
    path: '/routes',
    description: 'Find your journey'
  },
  {
    text: 'Live Map',
    icon: <Map />,
    path: '/live-map',
    description: 'Real-time bus tracking',
    badge: 'Live'
  },
  {
    text: 'Trip Planner',
    icon: <Navigation />,
    path: '/trip-planner',
    description: 'Plan your journey'
  },
];

const secondaryItems = [
  {
    text: 'Profile',
    icon: <Person />,
    path: '/profile',
    description: 'Account settings'
  },
  {
    text: 'Analytics',
    icon: <Analytics />,
    path: '/analytics',
    description: 'Usage insights'
  },
  {
    text: 'Support',
    icon: <Support />,
    path: '/support',
    description: 'Get help'
  },
  {
    text: 'Settings',
    icon: <Settings />,
    path: '/settings',
    description: 'App preferences'
  },
];

const Sidebar = ({ open, onClose, isMobile }) => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleNavigation = (path) => {
    navigate(path);
    if (isMobile) {
      onClose();
    }
  };

  const isActive = (path) => location.pathname === path;

  const renderMenuItems = (items, showDivider = false) => (
    <>
      {showDivider && <Divider sx={{ my: 2 }} />}
      <List sx={{ px: 1 }}>
        {items.map((item) => (
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
};

export default Sidebar;