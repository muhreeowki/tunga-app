document.addEventListener('DOMContentLoaded', function() {
    const reservationForm = document.getElementById('reservationForm');
    const dateInput = document.getElementById('date');
    const timeInput = document.getElementById('time');
    const submitButton = document.getElementById('submitReservation');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');

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
        
        // Show loading state
        submitButton.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        errorMessage.style.display = 'none';

        try {
            // Create reservation object
            const reservation = {
                restaurant: document.getElementById('restaurant').value,
                date: dateInput.value,
                time: timeInput.value,
                guests: document.getElementById('guests').value,
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value,
                specialRequests: document.getElementById('specialRequests').value
            };

            // Make API call
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.RESERVATION, 'POST', reservation);
            
            // Show success message with token number
            alert(`Reservation successful! Your token number is: ${response.token}`);
            
            // Reset form
            reservationForm.reset();
        } catch (error) {
            console.error('Reservation failed:', error);
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