const API_CONFIG = {
    BASE_URL: 'http://localhost:8080/api',
    ENDPOINTS: {
        RESERVATIONS: '/reservations',
        DELIVERIES: '/deliveries',
        MENU: '/menu',
        AUTH: '/auth',
        PROFILE: '/profile',
        ORDERS: '/orders'
    },
    HEADERS: {
        'Content-Type': 'application/json'
    }
};

// Helper function to make API calls
async function makeApiCall(endpoint, method = 'GET', data = null) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: API_CONFIG.HEADERS
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
} 