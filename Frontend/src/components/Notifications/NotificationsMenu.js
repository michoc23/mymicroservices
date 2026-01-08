import React, { useEffect, useState } from 'react';
import {
  IconButton,
  Menu,
  MenuItem,
  ListItemText,
  ListItemSecondaryAction,
  Typography,
  Badge,
  Box,
  Button,
} from '@mui/material';
import { Notifications as NotificationsIcon, MarkEmailRead } from '@mui/icons-material';
import notificationService from '../../services/notificationService';
import { useAuth } from '../../contexts/AuthContext';
import dayjs from 'dayjs';
import relativeTime from 'dayjs/plugin/relativeTime';
dayjs.extend(relativeTime);

const NotificationsMenu = () => {
  const { user } = useAuth();
  const [anchorEl, setAnchorEl] = useState(null);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);

  useEffect(() => {
    if (!user?.id) return;
    fetchNotifications();
    fetchUnreadCount();
  }, [user?.id]);

  const fetchNotifications = async () => {
    try {
      const resp = await notificationService.getUserNotifications(user.id);
      setNotifications(resp.data || []);
    } catch (err) {
      console.error('Failed to load notifications', err);
    }
  };

  const fetchUnreadCount = async () => {
    try {
      const resp = await notificationService.getUnreadCount(user.id);
      setUnreadCount(resp.data || 0);
    } catch (err) {
      console.error('Failed to load unread count', err);
    }
  };

  const handleOpen = (e) => {
    setAnchorEl(e.currentTarget);
    // refresh list when opening
    fetchNotifications();
    fetchUnreadCount();
  };

  const handleClose = () => setAnchorEl(null);

  const handleMarkAsRead = async (id) => {
    try {
      await notificationService.markAsRead(id);
      await fetchNotifications();
      await fetchUnreadCount();
    } catch (err) {
      console.error('Failed to mark as read', err);
    }
  };

  return (
    <>
      <IconButton color="inherit" onClick={handleOpen} aria-label="notifications">
        <Badge badgeContent={unreadCount} color="secondary">
          <NotificationsIcon />
        </Badge>
      </IconButton>

      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={handleClose}
        PaperProps={{ style: { width: 360 } }}
      >
        <Box sx={{ px: 2, py: 1, borderBottom: 1, borderColor: 'divider' }}>
          <Typography variant="subtitle1">Notifications</Typography>
          <Typography variant="caption" color="text.secondary">Recent alerts</Typography>
        </Box>

        {notifications.length === 0 && (
          <MenuItem>
            <ListItemText primary="No notifications" />
          </MenuItem>
        )}

        {notifications.map((n) => (
          <MenuItem key={n.id} divider={true} sx={{ alignItems: 'flex-start' }}>
            <ListItemText
              primary={<strong>{n.title}</strong>}
              secondary={
                <>
                  <Typography variant="body2" color="text.secondary">{n.message}</Typography>
                  <Typography variant="caption" color="text.secondary">{dayjs(n.createdAt).fromNow()}</Typography>
                </>
              }
            />
            <ListItemSecondaryAction>
              {!n.read && (
                <Button size="small" onClick={() => handleMarkAsRead(n.id)} startIcon={<MarkEmailRead />}>
                  Mark
                </Button>
              )}
            </ListItemSecondaryAction>
          </MenuItem>
        ))}

      </Menu>
    </>
  );
};

export default NotificationsMenu;
