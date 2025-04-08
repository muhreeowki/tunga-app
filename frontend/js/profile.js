document.addEventListener('DOMContentLoaded', () => {
    // Check if user is logged in
    const token = localStorage.getItem('token');
    if (!token) {
        window.location.href = 'login.html';
        return;
    }

    // Load user information
    loadUserInfo();
    loadAddresses();
    loadReservations();

    // Event listeners
    document.getElementById('editProfileBtn').addEventListener('click', showEditProfileForm);
    document.getElementById('cancelEditBtn').addEventListener('click', hideEditProfileForm);
    document.getElementById('editProfileForm').addEventListener('submit', updateProfile);
    document.getElementById('changePasswordBtn').addEventListener('click', showChangePasswordModal);
    document.getElementById('changePasswordForm').addEventListener('submit', changePassword);
    document.getElementById('addAddressBtn').addEventListener('click', showAddAddressForm);
    document.getElementById('cancelAddressBtn').addEventListener('click', hideAddAddressForm);
    document.getElementById('newAddressForm').addEventListener('submit', addNewAddress);
});

async function loadUserInfo() {
    try {
        const response = await fetch('/api/user/profile', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load user information');
        }

        const userData = await response.json();
        document.getElementById('username').textContent = userData.username;
        document.getElementById('email').textContent = userData.email;
        document.getElementById('emailVerified').textContent = userData.emailVerified ? 'Yes' : 'No';
        
        // Pre-fill edit form
        document.getElementById('editUsername').value = userData.username;
        document.getElementById('editEmail').value = userData.email;
    } catch (error) {
        console.error('Error loading user information:', error);
        alert('Failed to load user information');
    }
}

async function loadAddresses() {
    try {
        const response = await fetch('/api/user/addresses', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load addresses');
        }

        const addresses = await response.json();
        const addressesList = document.getElementById('addressesList');
        addressesList.innerHTML = '';

        addresses.forEach(address => {
            const addressCard = document.createElement('div');
            addressCard.className = 'card mb-3';
            addressCard.innerHTML = `
                <div class="card-body">
                    <h6 class="card-title">${address.addressLine1}</h6>
                    ${address.addressLine2 ? `<p class="card-text">${address.addressLine2}</p>` : ''}
                    <p class="card-text">${address.city}, ${address.state} ${address.postalCode}</p>
                    <p class="card-text">${address.country}</p>
                    ${address.isDefault ? '<span class="badge bg-primary">Default</span>' : ''}
                    <div class="mt-2">
                        <button class="btn btn-sm btn-primary edit-address" data-id="${address.id}">Edit</button>
                        <button class="btn btn-sm btn-danger delete-address" data-id="${address.id}">Delete</button>
                        ${!address.isDefault ? `<button class="btn btn-sm btn-secondary set-default" data-id="${address.id}">Set as Default</button>` : ''}
                    </div>
                </div>
            `;
            addressesList.appendChild(addressCard);
        });

        // Add event listeners for address actions
        document.querySelectorAll('.edit-address').forEach(button => {
            button.addEventListener('click', (e) => editAddress(e.target.dataset.id));
        });
        document.querySelectorAll('.delete-address').forEach(button => {
            button.addEventListener('click', (e) => deleteAddress(e.target.dataset.id));
        });
        document.querySelectorAll('.set-default').forEach(button => {
            button.addEventListener('click', (e) => setDefaultAddress(e.target.dataset.id));
        });
    } catch (error) {
        console.error('Error loading addresses:', error);
        alert('Failed to load addresses');
    }
}

async function loadReservations() {
    try {
        const response = await fetch('/api/reservations', {
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to load reservations');
        }

        const reservations = await response.json();
        const reservationsList = document.getElementById('reservationsList');
        reservationsList.innerHTML = '';

        if (reservations.length === 0) {
            reservationsList.innerHTML = '<p>No reservations found.</p>';
            return;
        }

        reservations.forEach(reservation => {
            const reservationCard = document.createElement('div');
            reservationCard.className = 'card mb-3';
            reservationCard.innerHTML = `
                <div class="card-body">
                    <h6 class="card-title">Reservation #${reservation.id}</h6>
                    <p class="card-text">Date: ${new Date(reservation.reservationDate).toLocaleDateString()}</p>
                    <p class="card-text">Time: ${reservation.reservationTime}</p>
                    <p class="card-text">Party Size: ${reservation.partySize}</p>
                    <p class="card-text">Status: ${reservation.status}</p>
                    <button class="btn btn-sm btn-danger cancel-reservation" data-id="${reservation.id}">Cancel</button>
                </div>
            `;
            reservationsList.appendChild(reservationCard);
        });

        // Add event listeners for reservation actions
        document.querySelectorAll('.cancel-reservation').forEach(button => {
            button.addEventListener('click', (e) => cancelReservation(e.target.dataset.id));
        });
    } catch (error) {
        console.error('Error loading reservations:', error);
        alert('Failed to load reservations');
    }
}

function showEditProfileForm() {
    document.getElementById('userInfo').style.display = 'none';
    document.getElementById('editUserInfo').style.display = 'block';
}

function hideEditProfileForm() {
    document.getElementById('userInfo').style.display = 'block';
    document.getElementById('editUserInfo').style.display = 'none';
}

async function updateProfile(e) {
    e.preventDefault();
    try {
        const response = await fetch('/api/user/profile', {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                username: document.getElementById('editUsername').value,
                email: document.getElementById('editEmail').value
            })
        });

        if (!response.ok) {
            throw new Error('Failed to update profile');
        }

        hideEditProfileForm();
        loadUserInfo();
        alert('Profile updated successfully');
    } catch (error) {
        console.error('Error updating profile:', error);
        alert('Failed to update profile');
    }
}

function showChangePasswordModal() {
    const modal = new bootstrap.Modal(document.getElementById('changePasswordModal'));
    modal.show();
}

async function changePassword(e) {
    e.preventDefault();
    const currentPassword = document.getElementById('currentPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (newPassword !== confirmPassword) {
        alert('New passwords do not match');
        return;
    }

    try {
        const response = await fetch('/api/user/change-password', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                currentPassword,
                newPassword
            })
        });

        if (!response.ok) {
            throw new Error('Failed to change password');
        }

        const modal = bootstrap.Modal.getInstance(document.getElementById('changePasswordModal'));
        modal.hide();
        document.getElementById('changePasswordForm').reset();
        alert('Password changed successfully');
    } catch (error) {
        console.error('Error changing password:', error);
        alert('Failed to change password');
    }
}

function showAddAddressForm() {
    document.getElementById('addressesList').style.display = 'none';
    document.getElementById('addAddressForm').style.display = 'block';
}

function hideAddAddressForm() {
    document.getElementById('addressesList').style.display = 'block';
    document.getElementById('addAddressForm').style.display = 'none';
}

async function addNewAddress(e) {
    e.preventDefault();
    try {
        const response = await fetch('/api/user/addresses', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            },
            body: JSON.stringify({
                addressLine1: document.getElementById('addressLine1').value,
                addressLine2: document.getElementById('addressLine2').value,
                city: document.getElementById('city').value,
                state: document.getElementById('state').value,
                postalCode: document.getElementById('postalCode').value,
                country: document.getElementById('country').value,
                isDefault: document.getElementById('isDefault').checked
            })
        });

        if (!response.ok) {
            throw new Error('Failed to add address');
        }

        hideAddAddressForm();
        document.getElementById('newAddressForm').reset();
        loadAddresses();
        alert('Address added successfully');
    } catch (error) {
        console.error('Error adding address:', error);
        alert('Failed to add address');
    }
}

async function editAddress(addressId) {
    // Implementation for editing an existing address
    // Similar to addNewAddress but with PUT method
}

async function deleteAddress(addressId) {
    if (!confirm('Are you sure you want to delete this address?')) {
        return;
    }

    try {
        const response = await fetch(`/api/user/addresses/${addressId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to delete address');
        }

        loadAddresses();
        alert('Address deleted successfully');
    } catch (error) {
        console.error('Error deleting address:', error);
        alert('Failed to delete address');
    }
}

async function setDefaultAddress(addressId) {
    try {
        const response = await fetch(`/api/user/addresses/${addressId}/default`, {
            method: 'PUT',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to set default address');
        }

        loadAddresses();
        alert('Default address updated successfully');
    } catch (error) {
        console.error('Error setting default address:', error);
        alert('Failed to set default address');
    }
}

async function cancelReservation(reservationId) {
    if (!confirm('Are you sure you want to cancel this reservation?')) {
        return;
    }

    try {
        const response = await fetch(`/api/reservations/${reservationId}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${localStorage.getItem('token')}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to cancel reservation');
        }

        loadReservations();
        alert('Reservation cancelled successfully');
    } catch (error) {
        console.error('Error cancelling reservation:', error);
        alert('Failed to cancel reservation');
    }
} 