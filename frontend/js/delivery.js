document.addEventListener('DOMContentLoaded', function() {
    const deliveryForm = document.getElementById('deliveryForm');
    const submitButton = document.getElementById('submitDelivery');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const menuItems = document.getElementById('menuItems');

    // Fetch menu items on page load
    async function fetchMenuItems() {
        try {
            const response = await fetch(`http://localhost:8080/menu/items`);
            if (!response.ok) {
                throw new Error('Failed to fetch menu items');
            }
            const items = await response.json();
            displayMenuItems(items);
        } catch (error) {
            console.error('Failed to fetch menu items:', error);
            errorMessage.textContent = 'Failed to load menu items. Please try again.';
            errorMessage.style.display = 'block';
        }
    }

    function displayMenuItems(items) {
        menuItems.innerHTML = '';
        items.forEach(item => {
            const div = document.createElement('div');
            div.className = 'menu-item mb-3';
            div.innerHTML = `
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" id="item-${item.id}" value="${item.id}">
                    <label class="form-check-label" for="item-${item.id}">
                        ${item.name} - $${item.price.toFixed(2)}
                    </label>
                </div>
            `;
            menuItems.appendChild(div);
        });
    }

    // Handle form submission
    deliveryForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        // Show loading state
        submitButton.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        errorMessage.style.display = 'none';

        try {
            // Get selected items
            const selectedItems = Array.from(document.querySelectorAll('#menuItems input:checked'))
                .map(checkbox => ({
                    id: parseInt(checkbox.value),
                    quantity: 1
                }));

            if (selectedItems.length === 0) {
                throw new Error('Please select at least one item');
            }

            // Create delivery object
            const delivery = {
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value,
                address: document.getElementById('address').value,
                items: selectedItems,
                specialInstructions: document.getElementById('specialInstructions').value
            };

            // Get auth token
            const token = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];
            if (!token) {
                throw new Error('Please login to place an order');
            }

            // Make API call
            const response = await fetch(`http://localhost:8080/orders`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(delivery)
            });

            if (!response.ok) {
                throw new Error('Failed to place order');
            }

            const result = await response.json();
            
            // Show success message with order number
            alert(`Order placed successfully! Your order number is: ${result.orderNumber}`);
            
            // Reset form
            deliveryForm.reset();
            window.location.href = '/orders.html';
        } catch (error) {
            console.error('Delivery order failed:', error);
            errorMessage.textContent = error.message || 'Failed to place order. Please try again.';
            errorMessage.style.display = 'block';
        } finally {
            // Hide loading state
            submitButton.disabled = false;
            loadingSpinner.style.display = 'none';
        }
    });

    // Fetch menu items when page loads
    fetchMenuItems();
}); 