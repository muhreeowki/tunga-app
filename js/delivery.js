document.addEventListener('DOMContentLoaded', function() {
    const deliveryForm = document.getElementById('deliveryForm');
    const submitButton = document.getElementById('submitDelivery');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const menuItems = document.getElementById('menuItems');

    // Fetch menu items on page load
    async function fetchMenuItems() {
        try {
            const items = await makeApiCall(API_CONFIG.ENDPOINTS.MENU);
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
            div.className = 'menu-item';
            div.innerHTML = `
                <input type="checkbox" id="item-${item.id}" value="${item.id}">
                <label for="item-${item.id}">${item.name} - $${item.price}</label>
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
                .map(checkbox => checkbox.value);

            // Create delivery object
            const delivery = {
                name: document.getElementById('name').value,
                email: document.getElementById('email').value,
                phone: document.getElementById('phone').value,
                address: document.getElementById('address').value,
                items: selectedItems,
                specialInstructions: document.getElementById('specialInstructions').value
            };

            // Make API call
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.DELIVERIES, 'POST', delivery);
            
            // Show success message with order number
            alert(`Order placed successfully! Your order number is: ${response.orderNumber}`);
            
            // Reset form
            deliveryForm.reset();
        } catch (error) {
            console.error('Delivery order failed:', error);
            errorMessage.textContent = 'Failed to place order. Please try again.';
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