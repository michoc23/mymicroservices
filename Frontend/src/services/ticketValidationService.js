import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8082';

// Validate a ticket by QR code
export const validateTicket = async (qrCode, routeId) => {
    try {
        const response = await axios.post(
            `${API_BASE_URL}/api/v1/tickets/validate`,
            {
                qrCode,
                routeId,
            },
            {
                headers: {
                    'Content-Type': 'application/json',
                    Authorization: `Bearer ${localStorage.getItem('token')}`,
                },
            }
        );
        return response.data;
    } catch (error) {
        throw error.response?.data || error.message;
    }
};

// Get ticket by QR code
export const getTicketByQR = async (qrCode) => {
    try {
        const response = await axios.get(
            `${API_BASE_URL}/api/v1/tickets/qr/${qrCode}`,
            {
                headers: {
                    Authorization: `Bearer ${localStorage.getItem('token')}`,
                },
            }
        );
        return response.data;
    } catch (error) {
        throw error.response?.data || error.message;
    }
};
