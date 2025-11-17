import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Alert,
  LinearProgress,
  Divider,
  Paper,
} from '@mui/material';
import {
  CardMembership,
  Add,
  Cancel,
  Refresh,
  History,
  Payment,
  CheckCircle,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { toast } from 'react-toastify';
import subscriptionService from '../../services/subscriptionService';
import LoadingSpinner from '../../components/Common/LoadingSpinner';

const SubscriptionsPage = () => {
  const { user } = useAuth();
  const [subscriptions, setSubscriptions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [dialogType, setDialogType] = useState(''); // 'create', 'cancel', 'renew'
  const [selectedSubscription, setSelectedSubscription] = useState(null);
  const [formData, setFormData] = useState({
    subscriptionType: 'MONTHLY',
    autoRenewal: true,
    cancellationReason: '',
  });

  // Fetch real subscription data
  useEffect(() => {
    const fetchSubscriptions = async () => {
      console.log('🔄 Fetching subscriptions for user:', user);
      
      if (!user?.id) {
        console.log('❌ No user ID available, stopping subscription fetch');
        setLoading(false);
        return;
      }
      
      try {
        setLoading(true);
        console.log('📡 Calling getUserSubscriptions with userId:', user.id);
        
        const response = await subscriptionService.getUserSubscriptions(user.id, 0, 10);
        console.log('✅ Subscriptions response:', response);
        
        const subscriptionsData = response.data.content || response.data || [];
        console.log('📋 Processed subscriptions data:', subscriptionsData);
        
        setSubscriptions(subscriptionsData);
      } catch (error) {
        console.error('❌ Error fetching subscriptions:', error);
        console.error('Error response:', error.response?.data);
        console.error('Error status:', error.response?.status);
        
        toast.error(`Failed to load subscriptions: ${error.response?.data?.message || error.message}`);
        setSubscriptions([]);
      } finally {
        setLoading(false);
      }
    };

    fetchSubscriptions();
  }, [user?.id]);

  const handleOpenDialog = (type, subscription = null) => {
    setDialogType(type);
    setSelectedSubscription(subscription);
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setSelectedSubscription(null);
    setFormData({
      subscriptionType: 'MONTHLY',
      autoRenewal: true,
      cancellationReason: '',
    });
  };

  const handleCreateSubscription = async () => {
    try {
      const subscriptionData = {
        userId: user.id,
        subscriptionType: formData.subscriptionType,
        autoRenewal: formData.autoRenewal,
      };
      
      await subscriptionService.createSubscription(subscriptionData);
      toast.success('Subscription created successfully!');
      
      // Refresh subscriptions list
      const response = await subscriptionService.getUserSubscriptions(user.id, 0, 10);
      setSubscriptions(response.data.content || response.data || []);
      
      handleCloseDialog();
    } catch (error) {
      console.error('Error creating subscription:', error);
      toast.error(error.response?.data?.message || 'Failed to create subscription');
    }
  };

  const handleCancelSubscription = async () => {
    try {
      await subscriptionService.cancelSubscription(selectedSubscription.id, formData.cancellationReason);
      toast.success('Subscription cancelled successfully!');
      
      // Refresh subscriptions list
      const response = await subscriptionService.getUserSubscriptions(user.id, 0, 10);
      setSubscriptions(response.data.content || response.data || []);
      
      handleCloseDialog();
    } catch (error) {
      console.error('Error cancelling subscription:', error);
      toast.error(error.response?.data?.message || 'Failed to cancel subscription');
    }
  };

  const handleRenewSubscription = async () => {
    try {
      await subscriptionService.renewSubscription(selectedSubscription.id);
      toast.success('Subscription renewed successfully!');
      
      // Refresh subscriptions list
      const response = await subscriptionService.getUserSubscriptions(user.id, 0, 10);
      setSubscriptions(response.data.content || response.data || []);
      
      handleCloseDialog();
    } catch (error) {
      console.error('Error renewing subscription:', error);
      toast.error(error.response?.data?.message || 'Failed to renew subscription');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'success';
      case 'EXPIRED':
        return 'error';
      case 'CANCELLED':
        return 'default';
      case 'SUSPENDED':
        return 'warning';
      default:
        return 'default';
    }
  };

  const SubscriptionCard = ({ subscription }) => (
    <Card sx={{ height: '100%' }}>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <CardMembership sx={{ mr: 1, color: 'primary.main' }} />
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              {subscription.subscriptionType} Plan
            </Typography>
          </Box>
          <Chip
            label={subscription.status}
            color={getStatusColor(subscription.status)}
            size="small"
          />
        </Box>

        <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
          ${subscription.price}
          <Typography component="span" variant="body2" color="text.secondary">
            /{subscription.subscriptionType.toLowerCase()}
          </Typography>
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Valid: {new Date(subscription.startDate).toLocaleDateString()} - {new Date(subscription.endDate).toLocaleDateString()}
        </Typography>

        {subscription.active && (
          <Box sx={{ mb: 2 }}>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
              <Typography variant="body2">Days remaining</Typography>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {subscription.daysRemaining} days
              </Typography>
            </Box>
            <LinearProgress
              variant="determinate"
              value={(subscription.daysRemaining / (subscription.subscriptionType === 'MONTHLY' ? 30 : 365)) * 100}
              sx={{ borderRadius: 1 }}
            />
          </Box>
        )}

        <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
          <Typography variant="body2" color="text.secondary">
            Auto-renewal: {subscription.autoRenewal ? 'Enabled' : 'Disabled'}
          </Typography>
          {subscription.autoRenewal && (
            <CheckCircle sx={{ ml: 1, fontSize: 16, color: 'success.main' }} />
          )}
        </Box>

        <Box sx={{ display: 'flex', gap: 1, flexWrap: 'wrap' }}>
          {subscription.status === 'ACTIVE' && (
            <>
              <Button
                variant="outlined"
                size="small"
                startIcon={<Cancel />}
                onClick={() => handleOpenDialog('cancel', subscription)}
              >
                Cancel
              </Button>
              <Button
                variant="outlined"
                size="small"
                startIcon={<Refresh />}
                onClick={() => handleOpenDialog('renew', subscription)}
              >
                Renew
              </Button>
            </>
          )}
          <Button
            variant="outlined"
            size="small"
            startIcon={<History />}
          >
            Usage History
          </Button>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return <LoadingSpinner message="Loading your subscriptions..." />;
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
            Subscriptions
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your transport subscription plans
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={() => handleOpenDialog('create')}
          disabled={subscriptions.some(sub => sub.status === 'ACTIVE')}
        >
          New Subscription
        </Button>
      </Box>

      {/* Subscription Plans Info */}
      <Paper sx={{ p: 3, mb: 4, backgroundColor: 'primary.light' }}>
        <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.contrastText', mb: 2 }}>
          Available Plans
        </Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} md={6}>
            <Box sx={{ color: 'primary.contrastText' }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Monthly Plan - $29.99
              </Typography>
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                Perfect for regular commuters. Unlimited rides for 30 days.
              </Typography>
            </Box>
          </Grid>
          <Grid item xs={12} md={6}>
            <Box sx={{ color: 'primary.contrastText' }}>
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                Annual Plan - $299.99
              </Typography>
              <Typography variant="body2" sx={{ opacity: 0.9 }}>
                Save $60/year! Best value for daily commuters.
              </Typography>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      {/* Subscriptions List */}
      {subscriptions.length > 0 ? (
        <Grid container spacing={3}>
          {subscriptions.map((subscription) => (
            <Grid item xs={12} md={6} lg={4} key={subscription.id}>
              <SubscriptionCard subscription={subscription} />
            </Grid>
          ))}
        </Grid>
      ) : (
        <Paper sx={{ p: 4, textAlign: 'center' }}>
          <CardMembership sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" sx={{ mb: 2 }}>
            No subscriptions yet
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Start saving on your daily commute with a subscription plan
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={() => handleOpenDialog('create')}
          >
            Get Your First Subscription
          </Button>
        </Paper>
      )}

      {/* Dialogs */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {dialogType === 'create' && 'Create New Subscription'}
          {dialogType === 'cancel' && 'Cancel Subscription'}
          {dialogType === 'renew' && 'Renew Subscription'}
        </DialogTitle>
        
        <DialogContent>
          {dialogType === 'create' && (
            <Box sx={{ pt: 1 }}>
              <FormControl fullWidth margin="normal">
                <InputLabel>Subscription Type</InputLabel>
                <Select
                  value={formData.subscriptionType}
                  label="Subscription Type"
                  onChange={(e) => setFormData({...formData, subscriptionType: e.target.value})}
                >
                  <MenuItem value="MONTHLY">Monthly - $29.99</MenuItem>
                  <MenuItem value="ANNUAL">Annual - $299.99</MenuItem>
                </Select>
              </FormControl>
              
              <FormControl fullWidth margin="normal">
                <InputLabel>Auto Renewal</InputLabel>
                <Select
                  value={formData.autoRenewal}
                  label="Auto Renewal"
                  onChange={(e) => setFormData({...formData, autoRenewal: e.target.value})}
                >
                  <MenuItem value={true}>Enable Auto Renewal</MenuItem>
                  <MenuItem value={false}>Disable Auto Renewal</MenuItem>
                </Select>
              </FormControl>

              <Alert severity="info" sx={{ mt: 2 }}>
                Your subscription will start immediately and give you unlimited access to all transport routes.
              </Alert>
            </Box>
          )}

          {dialogType === 'cancel' && (
            <Box sx={{ pt: 1 }}>
              <Alert severity="warning" sx={{ mb: 2 }}>
                Are you sure you want to cancel your subscription? This action cannot be undone.
              </Alert>
              
              <TextField
                fullWidth
                label="Reason for cancellation (optional)"
                multiline
                rows={3}
                value={formData.cancellationReason}
                onChange={(e) => setFormData({...formData, cancellationReason: e.target.value})}
                margin="normal"
              />
            </Box>
          )}

          {dialogType === 'renew' && (
            <Box sx={{ pt: 1 }}>
              <Alert severity="info" sx={{ mb: 2 }}>
                Renew your subscription to extend your access. The new period will start when your current subscription expires.
              </Alert>
              
              <Typography variant="body2" color="text.secondary">
                Current subscription expires: {selectedSubscription?.endDate && new Date(selectedSubscription.endDate).toLocaleDateString()}
              </Typography>
            </Box>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={() => {
              if (dialogType === 'create') handleCreateSubscription();
              if (dialogType === 'cancel') handleCancelSubscription();
              if (dialogType === 'renew') handleRenewSubscription();
            }}
            variant="contained"
            color={dialogType === 'cancel' ? 'error' : 'primary'}
          >
            {dialogType === 'create' && 'Create Subscription'}
            {dialogType === 'cancel' && 'Cancel Subscription'}
            {dialogType === 'renew' && 'Renew Subscription'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default SubscriptionsPage;