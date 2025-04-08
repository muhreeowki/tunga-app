const API_CONFIG = {
    BASE_URL: 'http://localhost:8080',
    ENDPOINTS: {
        AUTH: '/api/auth',
        MENU: '/api/menu',
        RESERVATION: '/api/reservations',
        DELIVERY: '/api/delivery'
    },
    HEADERS: {
        'Content-Type': 'application/json'
    }
};

// Make API call with JWT token handling
async function makeApiCall(endpoint, method = 'GET', data = null) {
    const url = API_CONFIG.BASE_URL + endpoint;
    const headers = {
        'Content-Type': 'application/json'
    };

    // Add Authorization header if token exists and it's not an auth endpoint
    const token = localStorage.getItem('token');
    if (token && !endpoint.includes('/auth/')) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const options = {
        method,
        headers
    };

    if (data) {
        options.body = JSON.stringify(data);
    }

    try {
        const response = await fetch(url, options);
        const responseData = await response.json();

        if (!response.ok) {
            // Handle 401 Unauthorized (token expired or invalid)
            if (response.status === 401) {
                localStorage.removeItem('token');
                localStorage.removeItem('user');
                window.location.href = 'login.html';
                throw new Error('Session expired. Please login again.');
            }
            throw new Error(responseData.message || 'An error occurred');
        }

        return responseData;
    } catch (error) {
        console.error('API call error:', error);
        throw error;
    }
} 