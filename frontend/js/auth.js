// Cookie management functions
const cookieManager = {
    // Set a cookie with specified name, value, and expiration days
    setCookie(name, value, days) {
        const date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        const expires = "expires=" + date.toUTCString();
        document.cookie = name + "=" + value + ";" + expires + ";path=/;SameSite=Strict";
    },

    // Get a cookie value by name
    getCookie(name) {
        const nameEQ = name + "=";
        const ca = document.cookie.split(';');
        for (let i = 0; i < ca.length; i++) {
            let c = ca[i];
            while (c.charAt(0) === ' ') c = c.substring(1, c.length);
            if (c.indexOf(nameEQ) === 0) return c.substring(nameEQ.length, c.length);
        }
        return null;
    },

    // Delete a cookie by name
    deleteCookie(name) {
        document.cookie = name + '=; Path=/; Expires=Thu, 01 Jan 1970 00:00:01 GMT;SameSite=Strict';
    }
};

// Authentication state management
const auth = {
    isAuthenticated: false,
    user: null,
    token: null,

    // Initialize auth state
    init() {
        this.loadAuthState();
        this.updateUI();
        this.setupEventListeners();
        this.setupProtectedRoutes();
    },

    // Load auth state from cookies
    loadAuthState() {
        const token = cookieManager.getCookie('token');
        const user = cookieManager.getCookie('user');
        if (token && user) {
            this.token = token;
            this.user = JSON.parse(decodeURIComponent(user));
            this.isAuthenticated = true;
            console.log('Auth state loaded:', { user: this.user, token: this.token });
        }
    },

    // Save auth state to cookies
    saveAuthState(token, user) {
        // Set token cookie to expire in 7 days
        cookieManager.setCookie('token', token, 7);
        // Set user cookie to expire in 7 days
        cookieManager.setCookie('user', encodeURIComponent(JSON.stringify(user)), 7);
        this.token = token;
        this.user = user;
        this.isAuthenticated = true;
        console.log('Auth state saved:', { user: this.user, token: this.token });
    },

    // Clear auth state
    clearAuthState() {
        cookieManager.deleteCookie('token');
        cookieManager.deleteCookie('user');
        this.token = null;
        this.user = null;
        this.isAuthenticated = false;
        console.log('Auth state cleared');
    },

    // Setup protected routes
    setupProtectedRoutes() {
        const protectedRoutes = ['profile.html', 'cart.html', 'delivery.html'];
        const currentPage = window.location.pathname.split('/').pop();
        
        if (protectedRoutes.includes(currentPage) && !this.isAuthenticated) {
            window.location.href = 'login.html';
        }
    },

    // Update UI based on auth state
    updateUI() {
        const loginNavItem = document.getElementById('loginNavItem');
        const registerNavItem = document.getElementById('registerNavItem');
        const profileNavItem = document.getElementById('profileNavItem');
        const logoutNavItem = document.getElementById('logoutNavItem');
        const userGreeting = document.getElementById('userGreeting');

        if (this.isAuthenticated) {
            if (loginNavItem) loginNavItem.style.display = 'none';
            if (registerNavItem) registerNavItem.style.display = 'none';
            if (profileNavItem) profileNavItem.style.display = 'block';
            if (logoutNavItem) logoutNavItem.style.display = 'block';
            if (userGreeting) {
                userGreeting.textContent = `Welcome, ${this.user.username}`;
                userGreeting.style.display = 'block';
            }
        } else {
            if (loginNavItem) loginNavItem.style.display = 'block';
            if (registerNavItem) registerNavItem.style.display = 'block';
            if (profileNavItem) profileNavItem.style.display = 'none';
            if (logoutNavItem) logoutNavItem.style.display = 'none';
            if (userGreeting) userGreeting.style.display = 'none';
        }
    },

    // Setup event listeners
    setupEventListeners() {
        const logoutLink = document.getElementById('logoutLink');
        if (logoutLink) {
            logoutLink.addEventListener('click', (e) => {
                e.preventDefault();
                this.logout();
            });
        }
    },

    // Handle login
    async login(username, password) {
        try {
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/signin', 'POST', {
                username,
                password
            });

            if (response.accessToken) {
                // Format user data according to the new response structure
                const userData = {
                    id: response.id,
                    username: response.username,
                    email: response.email,
                    emailVerified: response.emailVerified,
                    roles: response.roles
                };
                
                // Save the access token and user data
                this.saveAuthState(response.accessToken, userData);
                this.updateUI();
                return { success: true };
            } else {
                throw new Error('Invalid response format');
            }
        } catch (error) {
            console.error('Login error:', error);
            return { success: false, error: error.message };
        }
    },

    // Handle registration
    async register(userData) {
        try {
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/signup', 'POST', {
                username: userData.username,
                email: userData.email,
                password: userData.password,
                roles: [userData.role]
            });

            if (response.success) {
                return { 
                    success: true,
                    message: response.message || 'Registration successful! Please check your email to verify your account.'
                };
            } else {
                throw new Error(response.message || 'Registration failed');
            }
        } catch (error) {
            console.error('Registration error:', error);
            return { success: false, error: error.message };
        }
    },

    // Handle logout
    logout() {
        this.clearAuthState();
        this.updateUI();
        window.location.href = 'index.html';
    },

    // Get auth headers for API calls
    getAuthHeaders() {
        if (this.token) {
            return {
                'Authorization': `Bearer ${this.token}`,
                'Content-Type': 'application/json'
            };
        }
        return {
            'Content-Type': 'application/json'
        };
    }
};

// Initialize auth when the script loads
document.addEventListener('DOMContentLoaded', () => {
    auth.init();
});

// Handle Google login
async function handleGoogleLogin() {
    try {
        // Redirect to Google OAuth endpoint
        window.location.href = API_CONFIG.ENDPOINTS.AUTH + '/oauth2/authorization/google';
    } catch (error) {
        console.error('Google login failed:', error);
        const errorMessage = document.getElementById('errorMessage');
        errorMessage.textContent = 'Google login failed. Please try again.';
        errorMessage.style.display = 'block';
    }
}

// Handle forgot password
async function handleForgotPassword(event) {
    event.preventDefault();
    
    const email = prompt('Please enter your email address:');
    if (!email) return;

    try {
        await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/forgot-password', 'POST', { email });
        alert('Password reset instructions have been sent to your email.');
    } catch (error) {
        console.error('Password reset failed:', error);
        alert('Failed to send password reset instructions. Please try again.');
    }
}

// Add token to API calls
function makeApiCall(endpoint, method = 'GET', data = null) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: {
            ...API_CONFIG.HEADERS
        },
    };

    // Only add Authorization header if we have a token and it's not a /signin or /signup request
    const token = cookieManager.getCookie('token');
    if (token && !endpoint.includes('/signin') && !endpoint.includes('/signup')) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }

    if (data) {
        options.body = JSON.stringify(data);
    }

    return fetch(url, options)
        .then(async response => {
            const data = await response.json();
            console.log(data);
            if (!response.ok) {
                throw new Error(data.message || `HTTP error! status: ${response.status}`);
            }
            return data;
        })
        .catch(error => {
            console.error('API call failed:', error);
            throw error;
        });
} 