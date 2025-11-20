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
  Divider,
  Stack,
  CircularProgress,
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
import { QRCodeCanvas } from 'qrcode.react';
import jsPDF from 'jspdf';

const TicketsPage = () => {
  const { user } = useAuth();
  const [tickets, setTickets] = useState([]);
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [openDialog, setOpenDialog] = useState(false);
  const [qrDialog, setQrDialog] = useState({ open: false, ticket: null });
  const [orderDialog, setOrderDialog] = useState({
    open: false,
    loading: false,
    order: null,
  });
  const [ticketTypes] = useState([
    { id: 1, name: 'Single Journey', type: 'SINGLE', price: 2.50, description: 'Valid for one journey' },
    { id: 2, name: 'Day Pass', type: 'DAY_PASS', price: 8.00, description: 'Unlimited rides for 24 hours' },
    { id: 3, name: 'Week Pass', type: 'MULTI_RIDE', price: 25.00, description: 'Unlimited rides for 7 days' },
  ]);

  const [newTicket, setNewTicket] = useState({
    ticketTypeId: '',
    quantity: 1,
  });

  // Fetch real ticket data
  useEffect(() => {
    const fetchData = async () => {
      if (!user?.id) {
        // No authenticated user yet – avoid leaving spinner forever
        setTickets([]);
        setOrders([]);
        setLoading(false);
        return;
      }
      
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

      // Calculate valid dates
      const now = new Date();
      const validFrom = new Date(now);
      validFrom.setHours(now.getHours() + 1, 0, 0, 0); // Start 1 hour from now
      
      let validUntil = new Date(validFrom);
      // Set validity based on ticket type
      if (selectedTicketType.type === 'SINGLE') {
        validUntil.setHours(validFrom.getHours() + 2); // 2 hours for single
      } else if (selectedTicketType.type === 'DAY_PASS') {
        validUntil.setDate(validFrom.getDate() + 1); // 24 hours
      } else if (selectedTicketType.type === 'MULTI_RIDE') {
        validUntil.setDate(validFrom.getDate() + 30); // 30 days
      }

      const orderData = {
        userId: user.id,
        tickets: Array(newTicket.quantity).fill(null).map(() => ({
          ticketType: selectedTicketType.type,
          routeId: 1, // Default route ID - TODO: allow user to select route
          validFrom: validFrom.toISOString(),
          validUntil: validUntil.toISOString(),
        })),
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
      const errorMessage = error.response?.data?.message || error.response?.data?.details?.join(', ') || 'Failed to purchase ticket';
      toast.error(errorMessage);
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

  const handleShowQR = (ticket) => {
    if (!ticket) {
      toast.error('Ticket not available');
      return;
    }
    setQrDialog({ open: true, ticket });
  };

  const handleCloseQrDialog = () => setQrDialog({ open: false, ticket: null });

  const handleDownload = (ticket) => {
    if (!ticket) {
      toast.error('Ticket not available');
      return;
    }

    try {
      const doc = new jsPDF();
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();
      const margin = 20;
      let yPos = margin;

      // Header
      doc.setFontSize(20);
      doc.setFont('helvetica', 'bold');
      doc.text('Urban Transport Ticket', pageWidth / 2, yPos, { align: 'center' });
      yPos += 15;

      // Divider
      doc.setLineWidth(0.5);
      doc.line(margin, yPos, pageWidth - margin, yPos);
      yPos += 10;

      // Ticket Information
      doc.setFontSize(12);
      doc.setFont('helvetica', 'normal');
      
      const formatDate = (dateString) => {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleString();
      };

      doc.setFont('helvetica', 'bold');
      doc.text('Ticket Details', margin, yPos);
      yPos += 8;
      
      doc.setFont('helvetica', 'normal');
      doc.text(`Ticket ID: ${ticket.id}`, margin, yPos);
      yPos += 7;
      
      doc.text(`Type: ${ticket.ticketType || 'N/A'}`, margin, yPos);
      yPos += 7;
      
      doc.text(`Status: ${ticket.status || 'N/A'}`, margin, yPos);
      yPos += 7;
      
      doc.text(`Price: $${ticket.price || '0.00'}`, margin, yPos);
      yPos += 7;
      
      doc.text(`Purchase Date: ${formatDate(ticket.purchaseDate)}`, margin, yPos);
      yPos += 7;
      
      doc.text(`Valid From: ${formatDate(ticket.validFrom)}`, margin, yPos);
      yPos += 7;
      
      doc.text(`Valid Until: ${formatDate(ticket.validUntil)}`, margin, yPos);
      yPos += 7;
      
      if (ticket.routeId) {
        doc.text(`Route ID: ${ticket.routeId}`, margin, yPos);
        yPos += 7;
      }

      yPos += 5;
      
      // QR Code Section
      if (ticket.qrCode) {
        doc.setFont('helvetica', 'bold');
        doc.text('QR Code:', margin, yPos);
        yPos += 8;
        
        doc.setFont('helvetica', 'normal');
        doc.setFontSize(10);
        doc.text(ticket.qrCode, margin, yPos, { maxWidth: pageWidth - 2 * margin });
        yPos += 10;
      }

      // Footer
      yPos = pageHeight - 30;
      doc.setLineWidth(0.5);
      doc.line(margin, yPos, pageWidth - margin, yPos);
      yPos += 10;
      
      doc.setFontSize(8);
      doc.setFont('helvetica', 'italic');
      doc.text('This is an official ticket for Urban Transport System.', pageWidth / 2, yPos, { align: 'center' });
      yPos += 5;
      doc.text('Please present this ticket when boarding.', pageWidth / 2, yPos, { align: 'center' });

      // Save PDF
      doc.save(`ticket-${ticket.id}.pdf`);
      toast.success('Ticket PDF downloaded successfully!');
    } catch (error) {
      console.error('Error generating PDF:', error);
      toast.error('Failed to generate PDF');
    }
  };

  const handleViewOrderDetails = async (order) => {
    if (!order?.id) {
      toast.error('Order not found');
      return;
    }
    setOrderDialog({ open: true, loading: true, order: null });

    try {
      const response = await ticketService.getOrder(order.id);
      setOrderDialog({
        open: true,
        loading: false,
        order: response.data || response,
      });
    } catch (error) {
      console.error('Failed to load order details:', error);
      toast.error('Unable to load order details');
      setOrderDialog((prev) => ({ ...prev, loading: false }));
    }
  };

  const handleCloseOrderDialog = () =>
    setOrderDialog({ open: false, loading: false, order: null });

  const formatDateTime = (value, options = {}) => {
    if (!value) return 'N/A';
    const date = new Date(value);
    return date.toLocaleString(undefined, {
      dateStyle: 'medium',
      timeStyle: 'short',
      ...options,
    });
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
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              handleShowQR(ticket);
            }}
          >
            Show QR
          </Button>
          <Button
            variant="outlined"
            size="small"
            startIcon={<Download />}
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              handleDownload(ticket);
            }}
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
                <TableCell>
                  {order.createdAt 
                    ? new Date(order.createdAt).toLocaleDateString() 
                    : 'N/A'}
                </TableCell>
                <TableCell>{order.tickets?.length || 0}</TableCell>
                <TableCell>${order.totalAmount || 0}</TableCell>
                <TableCell>
                  <Chip 
                    label={order.status || 'PENDING'} 
                    color={order.status === 'PAID' ? 'success' : 'default'} 
                    size="small" 
                  />
                </TableCell>
                <TableCell>
                  <Button 
                    size="small" 
                    startIcon={<History />}
                    onClick={(e) => {
                      e.preventDefault();
                      e.stopPropagation();
                      handleViewOrderDetails(order);
                    }}
                  >
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

      {/* QR Code Dialog */}
      <Dialog open={qrDialog.open} onClose={handleCloseQrDialog} maxWidth="xs" fullWidth>
        <DialogTitle>Ticket QR Code</DialogTitle>
        <DialogContent>
          {qrDialog.ticket ? (
            <Stack spacing={2} alignItems="center" sx={{ py: 1 }}>
              {qrDialog.ticket.qrCode ? (
                <QRCodeCanvas
                  value={qrDialog.ticket.qrCode}
                  size={220}
                  includeMargin
                />
              ) : (
                <Typography color="text.secondary">
                  QR code not available for this ticket.
                </Typography>
              )}
              <Divider flexItem />
              <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                {qrDialog.ticket.ticketType}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Valid until {formatDateTime(qrDialog.ticket.validUntil, { dateStyle: 'medium', timeStyle: 'short' })}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Status: {qrDialog.ticket.status}
              </Typography>
            </Stack>
          ) : (
            <Typography>Ticket not found.</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseQrDialog}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Order Details Dialog */}
      <Dialog open={orderDialog.open} onClose={handleCloseOrderDialog} maxWidth="md" fullWidth>
        <DialogTitle>Order Details</DialogTitle>
        <DialogContent dividers>
          {orderDialog.loading ? (
            <Box sx={{ display: 'flex', justifyContent: 'center', py: 4 }}>
              <CircularProgress />
            </Box>
          ) : orderDialog.order ? (
            <Stack spacing={3}>
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                  {orderDialog.order.orderNumber}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Created: {formatDateTime(orderDialog.order.createdAt)}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Status:{' '}
                  <Chip
                    size="small"
                    label={orderDialog.order.status}
                    color={orderDialog.order.status === 'PAID' ? 'success' : 'default'}
                  />
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Total Amount: ${Number(orderDialog.order.totalAmount || 0).toFixed(2)}
                </Typography>
              </Box>
              <Divider />
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 600, mb: 1 }}>
                  Tickets
                </Typography>
                {orderDialog.order.tickets?.length ? (
                  <Stack spacing={2}>
                    {orderDialog.order.tickets.map((ticket) => (
                      <Paper
                        key={ticket.id}
                        variant="outlined"
                        sx={{ p: 2, display: 'flex', flexDirection: 'column', gap: 0.5 }}
                      >
                        <Typography sx={{ fontWeight: 600 }}>
                          {ticket.ticketType} — ${ticket.price}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Valid: {formatDateTime(ticket.validFrom)} → {formatDateTime(ticket.validUntil)}
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          Status:{' '}
                          <Chip
                            size="small"
                            label={ticket.status}
                            color={ticket.status === 'ACTIVE' ? 'success' : 'default'}
                          />
                        </Typography>
                        <Typography variant="body2" color="text.secondary">
                          QR Code: {ticket.qrCode || 'N/A'}
                        </Typography>
                      </Paper>
                    ))}
                  </Stack>
                ) : (
                  <Typography variant="body2" color="text.secondary">
                    No tickets found for this order.
                  </Typography>
                )}
              </Box>
            </Stack>
          ) : (
            <Typography>Order details unavailable.</Typography>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseOrderDialog}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default TicketsPage;