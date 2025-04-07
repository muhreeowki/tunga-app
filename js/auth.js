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
    
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const rememberMe = document.getElementById('rememberMe').checked;
    
    const loginButton = document.getElementById('loginButton');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');

    try {
        // Show loading state
        loginButton.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        errorMessage.style.display = 'none';

        // Make API call
        const response = await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/login', 'POST', {
            email,
            password
        });

        // Store token and user data
        localStorage.setItem(TOKEN_KEY, response.token);
        localStorage.setItem(USER_KEY, JSON.stringify(response.user));
        currentUser = response.user;

        // Update UI
        updateUIForLoggedInUser();

        // Redirect to home page
        window.location.href = 'index.html';
    } catch (error) {
        console.error('Login failed:', error);
        errorMessage.textContent = 'Invalid email or password';
        errorMessage.style.display = 'block';
    } finally {
        // Hide loading state
        loginButton.disabled = false;
        loadingSpinner.style.display = 'none';
    }
}

// Handle registration
async function handleRegistration(event) {
    event.preventDefault();
    
    const fullName = document.getElementById('fullName').value;
    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;
    const phone = document.getElementById('phone').value;
    const role = document.getElementById('role').value;
    
    const registerButton = document.getElementById('registerButton');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');

    // Validate password
    if (password !== confirmPassword) {
        errorMessage.textContent = 'Passwords do not match';
        errorMessage.style.display = 'block';
        return;
    }

    // Validate role
    if (!role) {
        errorMessage.textContent = 'Please select an account type';
        errorMessage.style.display = 'block';
        return;
    }

    try {
        // Show loading state
        registerButton.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        errorMessage.style.display = 'none';

        // Make API call with roles as a Set
        const response = await makeApiCall(API_CONFIG.ENDPOINTS.AUTH + '/signup', 'POST', {
            fullName,
            email,
            password,
            phone,
            roles: [role] // Send role as an array which will be converted to Set on the backend
        });

        // Show success message
        alert('Registration successful! Please check your email to verify your account.');
        
        // Redirect to login page
        window.location.href = 'login.html';
    } catch (error) {
        console.error('Registration failed:', error);
        errorMessage.textContent = error.message || 'Registration failed. Please try again.';
        errorMessage.style.display = 'block';
    } finally {
        // Hide loading state
        registerButton.disabled = false;
        loadingSpinner.style.display = 'none';
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