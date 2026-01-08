import React, { useState } from 'react';
import {
    Box,
    Container,
    Paper,
    Typography,
    TextField,
    Button,
    Alert,
    Card,
    CardContent,
    Chip,
    Grid,
    CircularProgress,
    IconButton,
    Dialog,
    DialogContent,
} from '@mui/material';
import {
    QrCodeScanner,
    CheckCircle,
    Cancel,
    Info,
    CameraAlt,
    Close,
} from '@mui/icons-material';
import { QrReader } from 'react-qr-reader';
import { validateTicket } from '../../services/ticketValidationService';

const ControllerScannerPage = () => {
    const [qrCode, setQrCode] = useState('');
    const [routeId, setRouteId] = useState('1');
    const [loading, setLoading] = useState(false);
    const [validationResult, setValidationResult] = useState(null);
    const [error, setError] = useState(null);
    const [scannerOpen, setScannerOpen] = useState(false);

    const handleScan = async (code = qrCode) => {
        if (!code || !code.trim()) {
            setError('Please enter or scan a QR code');
            return;
        }

        setLoading(true);
        setError(null);
        setValidationResult(null);

        try {
            const result = await validateTicket(code.trim(), parseInt(routeId));
            setValidationResult(result);
            setScannerOpen(false);
        } catch (err) {
            setError(err.message || 'Failed to validate ticket');
        } finally {
            setLoading(false);
        }
    };

    const handleQrScan = (result) => {
        if (result) {
            setQrCode(result.text);
            handleScan(result.text);
        }
    };

    const handleQrError = (error) => {
        console.error('QR Scanner Error:', error);
        setError('Camera access denied or not available');
    };

    const handleKeyPress = (e) => {
        if (e.key === 'Enter') {
            handleScan();
        }
    };

    const resetScanner = () => {
        setQrCode('');
        setValidationResult(null);
        setError(null);
    };

    const openScanner = () => {
        setScannerOpen(true);
        setError(null);
    };

    return (
        <Container maxWidth="md" sx={{ mt: 4, mb: 4 }}>
            <Paper elevation={3} sx={{ p: 4 }}>
                {/* Header */}
                <Box sx={{ textAlign: 'center', mb: 4 }}>
                    <QrCodeScanner sx={{ fontSize: 60, color: 'primary.main', mb: 2 }} />
                    <Typography variant="h4" gutterBottom>
                        Ticket Validator
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                        Scan QR code with camera or enter manually
                    </Typography>
                </Box>

                {/* Input Section */}
                <Box sx={{ mb: 4 }}>
                    <Grid container spacing={2}>
                        <Grid item xs={12} sm={8}>
                            <TextField
                                fullWidth
                                label="QR Code"
                                variant="outlined"
                                value={qrCode}
                                onChange={(e) => setQrCode(e.target.value)}
                                onKeyPress={handleKeyPress}
                                placeholder="Enter or scan QR code"
                                autoFocus
                                disabled={loading}
                            />
                        </Grid>
                        <Grid item xs={12} sm={4}>
                            <TextField
                                fullWidth
                                label="Route ID"
                                variant="outlined"
                                type="number"
                                value={routeId}
                                onChange={(e) => setRouteId(e.target.value)}
                                disabled={loading}
                            />
                        </Grid>
                    </Grid>

                    <Box sx={{ mt: 2, display: 'flex', gap: 2 }}>
                        <Button
                            variant="contained"
                            color="primary"
                            onClick={() => handleScan()}
                            disabled={loading || !qrCode.trim()}
                            fullWidth
                            size="large"
                        >
                            {loading ? <CircularProgress size={24} /> : 'Validate Ticket'}
                        </Button>
                        <Button
                            variant="contained"
                            color="secondary"
                            onClick={openScanner}
                            disabled={loading}
                            startIcon={<CameraAlt />}
                        >
                            Scan
                        </Button>
                        <Button
                            variant="outlined"
                            onClick={resetScanner}
                            disabled={loading}
                        >
                            Clear
                        </Button>
                    </Box>
                </Box>

                {/* Error Message */}
                {error && (
                    <Alert severity="error" sx={{ mb: 3 }} onClose={() => setError(null)}>
                        {error}
                    </Alert>
                )}

                {/* Validation Result */}
                {validationResult && (
                    <Card
                        sx={{
                            bgcolor: validationResult.valid ? 'success.light' : 'error.light',
                            color: validationResult.valid ? 'success.contrastText' : 'error.contrastText',
                        }}
                    >
                        <CardContent>
                            <Box sx={{ display: 'flex', alignItems: 'center', mb: 2 }}>
                                {validationResult.valid ? (
                                    <CheckCircle sx={{ fontSize: 48, mr: 2 }} />
                                ) : (
                                    <Cancel sx={{ fontSize: 48, mr: 2 }} />
                                )}
                                <Box>
                                    <Typography variant="h5" gutterBottom>
                                        {validationResult.valid ? 'VALID TICKET' : 'INVALID TICKET'}
                                    </Typography>
                                    <Typography variant="body1">
                                        {validationResult.message}
                                    </Typography>
                                </Box>
                            </Box>

                            {/* Ticket Details */}
                            {validationResult.ticket && (
                                <Box sx={{ mt: 3, pt: 2, borderTop: '1px solid rgba(255,255,255,0.3)' }}>
                                    <Typography variant="h6" gutterBottom>
                                        <Info sx={{ verticalAlign: 'middle', mr: 1 }} />
                                        Ticket Details
                                    </Typography>
                                    <Grid container spacing={2} sx={{ mt: 1 }}>
                                        <Grid item xs={6}>
                                            <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                                Type
                                            </Typography>
                                            <Typography variant="body1" fontWeight="bold">
                                                {validationResult.ticket.ticketType}
                                            </Typography>
                                        </Grid>
                                        <Grid item xs={6}>
                                            <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                                Status
                                            </Typography>
                                            <Chip
                                                label={validationResult.ticket.status}
                                                size="small"
                                                sx={{ mt: 0.5 }}
                                            />
                                        </Grid>
                                        <Grid item xs={6}>
                                            <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                                Usage
                                            </Typography>
                                            <Typography variant="body1" fontWeight="bold">
                                                {validationResult.ticket.usageCount} / {validationResult.ticket.maxUsage}
                                            </Typography>
                                        </Grid>
                                        <Grid item xs={6}>
                                            <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                                Valid Until
                                            </Typography>
                                            <Typography variant="body1" fontWeight="bold">
                                                {new Date(validationResult.ticket.validUntil).toLocaleString()}
                                            </Typography>
                                        </Grid>
                                        {validationResult.ticket.passengerName && (
                                            <Grid item xs={12}>
                                                <Typography variant="body2" sx={{ opacity: 0.8 }}>
                                                    Passenger
                                                </Typography>
                                                <Typography variant="body1" fontWeight="bold">
                                                    {validationResult.ticket.passengerName}
                                                </Typography>
                                            </Grid>
                                        )}
                                    </Grid>
                                </Box>
                            )}
                        </CardContent>
                    </Card>
                )}
            </Paper>

            {/* QR Scanner Dialog */}
            <Dialog
                open={scannerOpen}
                onClose={() => setScannerOpen(false)}
                maxWidth="sm"
                fullWidth
            >
                <DialogContent>
                    <Box sx={{ position: 'relative' }}>
                        <IconButton
                            onClick={() => setScannerOpen(false)}
                            sx={{ position: 'absolute', right: 0, top: 0, zIndex: 1 }}
                        >
                            <Close />
                        </IconButton>
                        <Typography variant="h6" gutterBottom sx={{ mb: 2 }}>
                            Scan QR Code
                        </Typography>
                        <QrReader
                            onResult={handleQrScan}
                            onError={handleQrError}
                            constraints={{ facingMode: 'environment' }}
                            style={{ width: '100%' }}
                        />
                        <Typography variant="caption" color="text.secondary" sx={{ mt: 2, display: 'block', textAlign: 'center' }}>
                            Position the QR code within the camera frame
                        </Typography>
                    </Box>
                </DialogContent>
            </Dialog>
        </Container>
    );
};

export default ControllerScannerPage;
