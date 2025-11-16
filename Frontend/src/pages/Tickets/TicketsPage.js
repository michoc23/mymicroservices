import React, { useState, useEffect } from 'react';
import {
  Box,
  Grid,
  Card,
  CardContent,
  Typography,
  Button,
  Chip,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material';
import {
  ConfirmationNumber,
  Add,
  QrCode,
  Download,
  History,
} from '@mui/icons-material';
import { useAuth } from '../../contexts/AuthContext';
import { toast } from 'react-toastify';
import ticketService from '../../services/ticketService';
import LoadingSpinner from '../../components/Common/LoadingSpinner';

const TicketsPage = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [ticketTypes] = useState([
    { id: 1, name: 'Single Journey', price: 2.50, description: 'Valid for one journey' },
    { id: 2, name: 'Day Pass', price: 8.00, description: 'Unlimited rides for 24 hours' },
    { id: 3, name: 'Week Pass', price: 25.00, description: 'Unlimited rides for 7 days' },
  ]);

  const [newTicket, setNewTicket] = useState({
    ticketTypeId: '',
    quantity: 1,
  });

  // Fetch real ticket data
  useEffect(() => {
    const fetchData = async () => {
      if (!user?.id) return;
      
      try {
        setLoading(true);
        
        // Fetch user tickets
        const ticketsResponse = await ticketService.getUserTickets(user.id, 0, 20);
        const ticketsData = ticketsResponse.data.content || ticketsResponse.data || [];
        setTickets(ticketsData);

        // Fetch user orders
        try {
          const ordersResponse = await ticketService.getUserOrders(user.id, 0, 20);
          const ordersData = ordersResponse.data.content || ordersResponse.data || [];
          setOrders(ordersData);
        } catch (orderError) {
          console.log('No orders found or service unavailable');
          setOrders([]);
        }
        
      } catch (error) {
        console.error('Error fetching tickets:', error);
        toast.error('Failed to load tickets');
        setTickets([]);
        setOrders([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, [user?.id]);

  const handleOpenDialog = () => {
    setOpenDialog(true);
  };

  const handleCloseDialog = () => {
    setOpenDialog(false);
    setNewTicket({ ticketTypeId: '', quantity: 1 });
  };

  const handlePurchaseTicket = async () => {
    try {
      const selectedTicketType = ticketTypes.find(t => t.id === newTicket.ticketTypeId);
      if (!selectedTicketType) {
        toast.error('Please select a ticket type');
        return;
      }

      const orderData = {
        userId: user.id,
        tickets: Array(newTicket.quantity).fill({
          ticketTypeId: newTicket.ticketTypeId,
          price: selectedTicketType.price,
        }),
        totalAmount: selectedTicketType.price * newTicket.quantity,
      };
      
      await ticketService.createOrder(orderData);
      toast.success('Ticket purchased successfully!');
      
      // Refresh tickets and orders
      const [ticketsResponse, ordersResponse] = await Promise.allSettled([
        ticketService.getUserTickets(user.id, 0, 20),
        ticketService.getUserOrders(user.id, 0, 20),
      ]);
      
      if (ticketsResponse.status === 'fulfilled') {
        setTickets(ticketsResponse.value.data.content || ticketsResponse.value.data || []);
      }
      if (ordersResponse.status === 'fulfilled') {
        setOrders(ordersResponse.value.data.content || ordersResponse.value.data || []);
      }
      
      handleCloseDialog();
    } catch (error) {
      console.error('Error purchasing ticket:', error);
      toast.error(error.response?.data?.message || 'Failed to purchase ticket');
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'VALID':
        return 'success';
      case 'USED':
        return 'info';
      case 'EXPIRED':
        return 'error';
      case 'CANCELLED':
        return 'default';
      default:
        return 'default';
    }
  };

  const TicketCard = ({ ticket }) => (
    <Card>
      <CardContent>
        <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
          <Box sx={{ display: 'flex', alignItems: 'center' }}>
            <ConfirmationNumber sx={{ mr: 1, color: 'primary.main' }} />
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              {ticket.ticketType}
            </Typography>
          </Box>
          <Chip
            label={ticket.status}
            color={getStatusColor(ticket.status)}
            size="small"
          />
        </Box>

        <Typography variant="h5" sx={{ fontWeight: 700, mb: 1 }}>
          ${ticket.price}
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
          Purchased: {new Date(ticket.purchaseDate).toLocaleString()}
        </Typography>

        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Valid until: {new Date(ticket.validUntil).toLocaleString()}
        </Typography>

        <Box sx={{ display: 'flex', gap: 1 }}>
          <Button
            variant="outlined"
            size="small"
            startIcon={<QrCode />}
            disabled={ticket.status !== 'VALID' && ticket.status !== 'ACTIVE'}
          >
            Show QR
          </Button>
          <Button
            variant="outlined"
            size="small"
            startIcon={<Download />}
          >
            Download
          </Button>
        </Box>
      </CardContent>
    </Card>
  );

  if (loading) {
    return <LoadingSpinner message="Loading your tickets..." />;
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, mb: 1 }}>
            Tickets
          </Typography>
          <Typography variant="body1" color="text.secondary">
            Manage your transport tickets and travel history
          </Typography>
        </Box>
        <Button
          variant="contained"
          startIcon={<Add />}
          onClick={handleOpenDialog}
        >
          Buy Ticket
        </Button>
      </Box>

      {/* Quick Stats */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h3" sx={{ fontWeight: 700, color: 'primary.main' }}>
              {tickets.filter(t => t.status === 'VALID').length}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Active Tickets
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h3" sx={{ fontWeight: 700, color: 'success.main' }}>
              {tickets.filter(t => t.status === 'USED').length}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Used Tickets
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={4}>
          <Paper sx={{ p: 3, textAlign: 'center' }}>
            <Typography variant="h3" sx={{ fontWeight: 700, color: 'info.main' }}>
              ${tickets.reduce((sum, ticket) => sum + ticket.price, 0).toFixed(2)}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Total Spent
            </Typography>
          </Paper>
        </Grid>
      </Grid>

      {/* Tickets Grid */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
        Your Tickets
      </Typography>
      
      {tickets.length > 0 ? (
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {tickets.map((ticket) => (
            <Grid item xs={12} md={6} lg={4} key={ticket.id}>
              <TicketCard ticket={ticket} />
            </Grid>
          ))}
        </Grid>
      ) : (
        <Paper sx={{ p: 4, textAlign: 'center', mb: 4 }}>
          <ConfirmationNumber sx={{ fontSize: 60, color: 'text.secondary', mb: 2 }} />
          <Typography variant="h6" sx={{ mb: 2 }}>
            No tickets yet
          </Typography>
          <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
            Purchase your first ticket to start using the transport system
          </Typography>
          <Button
            variant="contained"
            startIcon={<Add />}
            onClick={handleOpenDialog}
          >
            Buy Your First Ticket
          </Button>
        </Paper>
      )}

      {/* Order History */}
      <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
        Order History
      </Typography>
      
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Order Number</TableCell>
              <TableCell>Date</TableCell>
              <TableCell>Tickets</TableCell>
              <TableCell>Amount</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {orders.map((order) => (
              <TableRow key={order.id}>
                <TableCell>{order.orderNumber}</TableCell>
                <TableCell>{new Date(order.date).toLocaleDateString()}</TableCell>
                <TableCell>{order.ticketCount}</TableCell>
                <TableCell>${order.totalAmount}</TableCell>
                <TableCell>
                  <Chip label={order.status} color="success" size="small" />
                </TableCell>
                <TableCell>
                  <Button size="small" startIcon={<History />}>
                    View Details
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Purchase Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Purchase Ticket</DialogTitle>
        
        <DialogContent>
          <FormControl fullWidth margin="normal">
            <InputLabel>Ticket Type</InputLabel>
            <Select
              value={newTicket.ticketTypeId}
              label="Ticket Type"
              onChange={(e) => setNewTicket({...newTicket, ticketTypeId: e.target.value})}
            >
              {ticketTypes.map((type) => (
                <MenuItem key={type.id} value={type.id}>
                  {type.name} - ${type.price} ({type.description})
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          
          <FormControl fullWidth margin="normal">
            <InputLabel>Quantity</InputLabel>
            <Select
              value={newTicket.quantity}
              label="Quantity"
              onChange={(e) => setNewTicket({...newTicket, quantity: e.target.value})}
            >
              {[1, 2, 3, 4, 5].map((num) => (
                <MenuItem key={num} value={num}>{num}</MenuItem>
              ))}
            </Select>
          </FormControl>

          {newTicket.ticketTypeId && (
            <Paper sx={{ p: 2, mt: 2, backgroundColor: 'background.default' }}>
              <Typography variant="subtitle2">Order Summary</Typography>
              <Typography variant="body2">
                {ticketTypes.find(t => t.id === newTicket.ticketTypeId)?.name} × {newTicket.quantity}
              </Typography>
              <Typography variant="h6" sx={{ fontWeight: 600 }}>
                Total: ${((ticketTypes.find(t => t.id === newTicket.ticketTypeId)?.price || 0) * newTicket.quantity).toFixed(2)}
              </Typography>
            </Paper>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handlePurchaseTicket}
            variant="contained"
            disabled={!newTicket.ticketTypeId}
          >
            Purchase Ticket
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TicketsPage;