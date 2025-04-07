// Authentication state
let currentUser = null;
const TOKEN_KEY = 'auth_token';
const USER_KEY = 'user_data';

// Initialize authentication
document.addEventListener('DOMContentLoaded', function() {
    // Check if user is already logged in
    const token = localStorage.getItem(TOKEN_KEY);
    const userData = localStorage.getItem(USER_KEY);
    
    if (token && userData) {
        currentUser = JSON.parse(userData);
        updateUIForLoggedInUser();
    }

    // Setup login form
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Setup registration form
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegistration);
    }

    // Setup Google login
    const googleLoginBtn = document.getElementById('googleLogin');
    if (googleLoginBtn) {
        googleLoginBtn.addEventListener('click', handleGoogleLogin);
    }

    // Setup forgot password
    const forgotPasswordLink = document.getElementById('forgotPassword');
    if (forgotPasswordLink) {
        forgotPasswordLink.addEventListener('click', handleForgotPassword);
    }
});

// Handle login
async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('email').value; // Using email field for username
    const password = document.getElementById('password').value;
    
    try {
        const response = await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/signin', 'POST', {
            username,
            password
        });

        if (response.token) {
            localStorage.setItem('auth_token', response.token);
            localStorage.setItem('user', JSON.stringify({
                id: response.id,
                username: response.username,
                email: response.email,
                roles: response.roles
            }));
            window.location.href = 'index.html';
        }
    } catch (error) {
        console.error('Login failed:', error);
        alert(error.message || 'Login failed. Please try again.');
    }
}

// Handle registration
async function handleRegistration(event) {
    event.preventDefault();
    
    const username = document.getElementById('fullName').value; // Using fullName field for username
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const role = document.getElementById('role').value;
    
    if (password !== confirmPassword) {
        alert('Passwords do not match');
        return;
    }

    try {
        const response = await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/signup', 'POST', {
            username,
            email,
            password,
            role: [role] // Send role as an array which will be converted to Set on the backend
        });

        if (response.message) {
            alert(response.message);
            window.location.href = 'login.html';
        }
    } catch (error) {
        console.error('Registration failed:', error);
        alert(error.message || 'Registration failed. Please try again.');
    }
}

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

// Update UI for logged in user
function updateUIForLoggedInUser() {
    // Update navigation
    const navLinks = document.querySelectorAll('.nav-link');
    navLinks.forEach(link => {
        if (link.getAttribute('data-auth') === 'true') {
            link.style.display = 'block';
        }
    });

    // Update user menu
    const userMenu = document.querySelector('.user-menu');
    if (userMenu) {
        userMenu.innerHTML = `
            <div class="dropdown">
                <button class="btn btn-link dropdown-toggle" type="button" data-bs-toggle="dropdown">
                    ${currentUser.fullName}
                </button>
                <ul class="dropdown-menu">
                    <li><a class="dropdown-item" href="profile.html">Profile</a></li>
                    <li><a class="dropdown-item" href="orders.html">My Orders</a></li>
                    <li><hr class="dropdown-divider"></li>
                    <li><a class="dropdown-item" href="#" onclick="handleLogout()">Logout</a></li>
                </ul>
            </div>
        `;
    }
}

// Handle logout
function handleLogout() {
    // Clear stored data
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    currentUser = null;

    // Redirect to login page
    window.location.href = 'login.html';
}

// Add token to API calls
function makeApiCall(endpoint, method = 'GET', data = null) {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;
    const options = {
        method,
        headers: {
            ...API_CONFIG.HEADERS
        }
    };

    // Only add Authorization header if we have a token and it's not a login/signup request
    const token = localStorage.getItem(TOKEN_KEY);
    if (token && !endpoint.includes('/login') && !endpoint.includes('/signup')) {
        options.headers['Authorization'] = `Bearer ${token}`;
    }

    if (data) {
        options.body = JSON.stringify(data);
    }

    return fetch(url, options)
        .then(async response => {
            const data = await response.json();
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