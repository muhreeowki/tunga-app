document.addEventListener('DOMContentLoaded', function() {
    const deliveryForm = document.getElementById('deliveryForm');
    const cartItems = document.getElementById('cart-items');
    const totalAmount = document.getElementById('total-amount');
    const addToCartButtons = document.querySelectorAll('.add-to-cart');
    
    let cart = [];
    let total = 0;

    // Add to cart functionality
    addToCartButtons.forEach(button => {
        button.addEventListener('click', function() {
            const item = this.dataset.item;
            const price = parseFloat(this.dataset.price);
            
            // Add item to cart
            cart.push({ item, price });
            total += price;
            
            // Update cart display
            updateCart();
        });
    });

    // Update cart display
    function updateCart() {
        cartItems.innerHTML = '';
        cart.forEach((item, index) => {
            const cartItem = document.createElement('div');
            cartItem.className = 'd-flex justify-content-between align-items-center mb-2';
            cartItem.innerHTML = `
                <span>${item.item}</span>
                <div>
                    <span class="me-2">$${item.price.toFixed(2)}</span>
                    <button class="btn btn-sm btn-danger remove-item" data-index="${index}">×</button>
                </div>
            `;
            cartItems.appendChild(cartItem);
        });

        // Update total
        totalAmount.textContent = total.toFixed(2);

        // Add event listeners to remove buttons
        document.querySelectorAll('.remove-item').forEach(button => {
            button.addEventListener('click', function() {
                const index = parseInt(this.dataset.index);
                total -= cart[index].price;
                cart.splice(index, 1);
                updateCart();
            });
        });
    }

    // Handle form submission
    deliveryForm.addEventListener('submit', function(e) {
        e.preventDefault();

        if (cart.length === 0) {
            alert('Please add items to your cart before placing an order.');
            return;
        }

        // Generate a random token number
        const tokenNumber = 'DEL-' + Math.floor(100000 + Math.random() * 900000);

        // Create order object
        const order = {
            items: cart,
            total: total,
            name: document.getElementById('deliveryName').value,
            address: document.getElementById('deliveryAddress').value,
            phone: document.getElementById('deliveryPhone').value,
            email: document.getElementById('deliveryEmail').value,
            token: tokenNumber,
            estimatedDeliveryTime: new Date(Date.now() + 90 * 60000) // 90 minutes from now
        };

        // Here you would typically send this data to your backend API
        console.log('Order submitted:', order);

        // Show success message with token number and estimated delivery time
        const deliveryTime = order.estimatedDeliveryTime.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
        alert(`Order placed successfully!\nToken Number: ${tokenNumber}\nEstimated Delivery Time: ${deliveryTime}`);

        // Reset form and cart
        deliveryForm.reset();
        cart = [];
        total = 0;
        updateCart();
    });
}); 