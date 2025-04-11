document.addEventListener('DOMContentLoaded', function () {
    // Check if user is logged in
    const token = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];
    if (!token) {
        window.location.href = 'index.html';
    }
    const userdata = document.cookie.split('; ').find(row => row.startsWith('user='))?.split('=')[1];
    const user = JSON.parse(decodeURIComponent(userdata));
    if (!userdata || !user) {
        window.location.href = 'index.html';
    }

    // Get order details from URL parameters
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('orderId');
    const ticketCode = urlParams.get('ticketCode');

    if (orderId) {
        // Fetch order details from the backend
        fetchOrderDetails(orderId);
    } else if (ticketCode) {
        // Display ticket code if available
        document.getElementById('ticketCode').textContent = ticketCode;
    } else {
        // Redirect to home if no order details are available
        window.location.href = 'index.html';
    }
});

async function fetchOrderDetails(orderId) {
    try {
        const token = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];
        if (!token) {
            window.location.href = 'index.html';
        }
        const response = await fetch(`${API_CONFIG.BASE_URL}/api/orders/${orderId}`, {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });

        if (!response.ok) {
            throw new Error('Failed to fetch order details');
        }

        const order = await response.json();
        displayOrderDetails(order);
    } catch (error) {
        console.error('Error fetching order details:', error);
        throw new Error('Failed to load order details. Please try again later.');
    }
}

function displayOrderDetails(order) {
    // Display ticket code
    document.getElementById('ticketCode').textContent = order.tokenNumber || 'N/A';

    // Display order status
    const statusBadge = document.getElementById('orderStatus');
    statusBadge.textContent = order.status;
    statusBadge.className = `text-muted status-badge bg-${getStatusColor(order.status)}`;

    // Display restaurant details
    document.getElementById('restaurantName').textContent = order.restaurantName;
    // document.getElementById('restaurantAddress').textContent = `${order.address}, ${order.restaurant.city}, ${order.restaurant.state} ${order.restaurant.zipCode}`;

    // Display delivery details
    document.getElementById('deliveryAddress').textContent =
        `${order.deliveryAddress}, ${order.deliveryCity}, ${order.deliveryState} ${order.deliveryZipCode}`;
    document.getElementById('contactPhone').textContent = order.contactPhone;

    // Display estimated delivery time
    const estimatedTime = document.getElementById('estimatedTime');
    if (order.estimatedDeliveryTime) {
        const deliveryTime = new Date(order.estimatedDeliveryTime);
        estimatedTime.textContent = `Expected by ${deliveryTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
    } else {
        estimatedTime.textContent = 'Calculating...';
    }

    // Display order items
    const orderItemsContainer = document.getElementById('orderItems');
    orderItemsContainer.innerHTML = order.items.map(item => `
        <div class="order-item">
            <div class="d-flex justify-content-between">
                <div>
                    <h6 class="mb-1">${item.menuItemName}</h6>
                    <small class="text-muted">Quantity: ${item.quantity}</small>
                </div>
                <div class="text-end">
                    <h6 class="mb-1">$${(item.menuItemPrice * item.quantity).toFixed(2)}</h6>
                    <small class="text-muted">$${item.menuItemPrice.toFixed(2)} each</small>
                </div>
            </div>
        </div>
    `).join('');

    // Display total amount
    document.getElementById('totalAmount').textContent = `$${order.totalAmount.toFixed(2)}`;
}

function getStatusColor(status) {
    switch (status.toLowerCase()) {
        case 'preparing':
            return 'warning';
        case 'out for delivery':
            return 'info';
        case 'delivered':
            return 'success';
        case 'cancelled':
            return 'danger';
        default:
            return 'secondary';
    }
}

function showError(message) {
    // Create error alert
    const alertDiv = document.createElement('div');
    alertDiv.className = 'alert alert-danger alert-dismissible fade show';
    alertDiv.role = 'alert';
    alertDiv.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    `;

    // Insert alert at the top of the container
    const container = document.querySelector('.container');
    container.insertBefore(alertDiv, container.firstChild);
} 