const cookieManager = {
    setCookie(name, value, days) {
        const expires = new Date(Date.now() + days * 864e5).toUTCString();
        document.cookie = `${name}=${value}; expires=${expires}; path=/`;
    },
    getCookie(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    },
    deleteCookie(name) {
        document.cookie = `${name}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/`;
    }
};



async function makeApiCall2(endpoint, method = 'GET', data = null) {
    const url = API_CONFIG.BASE_URL + endpoint;
    const headers = {
        'Content-Type': 'application/json'
    };

    // Get token from cookies instead of localStorage
    const token = cookieManager.getCookie('token');
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
            if (response.status === 401) {
                // Clear cookies instead of localStorage
                cookieManager.deleteCookie('token');
                cookieManager.deleteCookie('user');
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


document.addEventListener('DOMContentLoaded', function() {
    const reservationForm = document.getElementById('reservationForm');
    const dateInput = document.getElementById('date');
    const timeInput = document.getElementById('time');
    const submitButton = document.getElementById('submitReservation');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const guests = document.getElementById('guests');

    // Set minimum date (today) and maximum date (1 month from today)
    const today = new Date();
    const maxDate = new Date();
    maxDate.setMonth(maxDate.getMonth() + 1);

    dateInput.min = today.toISOString().split('T')[0];
    dateInput.max = maxDate.toISOString().split('T')[0];

    // Set minimum time (6 hours from now) for today's reservations
    if (dateInput.value === today.toISOString().split('T')[0]) {
        const minTime = new Date();
        minTime.setHours(minTime.getHours() + 6);
        timeInput.min = minTime.toHoursMinutes();
    }

    // Update time input when date changes
    dateInput.addEventListener('change', function() {
        if (this.value === today.toISOString().split('T')[0]) {
            const minTime = new Date();
            minTime.setHours(minTime.getHours() + 6);
            timeInput.min = minTime.toHoursMinutes();
        } else {
            timeInput.min = '00:00';
        }
    });

    // Handle form submission
    reservationForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        const reservationDateTime = new Date(`${dateInput.value}T${timeInput.value}`);
        
        // Show loading state
        submitButton.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        errorMessage.style.display = 'none';

        try {
            // Create reservation object
            const reservation = {
                restaurant: document.getElementById('restaurant').value,
                reservationDateTime: reservationDateTime.toISOString(),
                numberOfGuests: document.getElementById('guests').value,
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value,
                specialRequests: document.getElementById('specialRequests').value
            };

            // Make API call
            const response = await makeApiCall2(API_CONFIG.ENDPOINTS.RESERVATION, 'POST', reservation);
            
            // Show success message with token number
            alert(`Reservation successful! Your token number is: ${response.token}`);
            
            // Reset form
            reservationForm.reset();
        } catch (error) {
            console.error('Reservation failed:', error);
            console.log(reservationDateTime.toISOString());
            errorMessage.textContent = 'Failed to make reservation. Please try again.';
            errorMessage.style.display = 'block';
        } finally {
            // Hide loading state
            submitButton.disabled = false;
            loadingSpinner.style.display = 'none';
        }
    });
});

// Helper function to format time
Date.prototype.toHoursMinutes = function() {
    return this.getHours().toString().padStart(2, '0') + ':' + 
           this.getMinutes().toString().padStart(2, '0');
}; 