document.addEventListener('DOMContentLoaded', function() {
    const reservationForm = document.getElementById('reservationForm');
    const dateInput = document.getElementById('date');
    const timeInput = document.getElementById('time');

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
    reservationForm.addEventListener('submit', function(e) {
        e.preventDefault();

        // Generate a random token number
        const tokenNumber = 'TUNGA-' + Math.floor(100000 + Math.random() * 900000);

        // Create reservation object
        const reservation = {
            restaurant: document.getElementById('restaurant').value,
            date: dateInput.value,
            time: timeInput.value,
            guests: document.getElementById('guests').value,
            name: document.getElementById('name').value,
            email: document.getElementById('email').value,
            phone: document.getElementById('phone').value,
            specialRequests: document.getElementById('specialRequests').value,
            token: tokenNumber
        };

        // Here you would typically send this data to your backend API
        console.log('Reservation submitted:', reservation);

        // Show success message with token number
        alert(`Reservation successful! Your token number is: ${tokenNumber}`);

        // Reset form
        reservationForm.reset();
    });
});

// Helper function to format time
Date.prototype.toHoursMinutes = function() {
    return this.getHours().toString().padStart(2, '0') + ':' + 
           this.getMinutes().toString().padStart(2, '0');
}; 