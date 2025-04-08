// Cart functionality
class Cart {
    constructor() {
        this.items = [];
        this.loadCart();
        this.setupEventListeners();
    }

    loadCart() {
        const savedCart = localStorage.getItem('cart');
        if (savedCart) {
            this.items = JSON.parse(savedCart);
        }
        this.updateCartDisplay();
    }

    saveCart() {
        localStorage.setItem('cart', JSON.stringify(this.items));
    }

    async addItem(itemId) {
        try {
            const response = await fetch(`${API_BASE_URL}/menu/items/${itemId}`);
            if (!response.ok) {
                throw new Error('Failed to fetch item details');
            }
            const item = await response.json();
            
            const existingItem = this.items.find(i => i.id === item.id);
            if (existingItem) {
                existingItem.quantity += 1;
            } else {
                this.items.push({
                    id: item.id,
                    name: item.name,
                    price: item.price,
                    quantity: 1
                });
            }
            
            this.saveCart();
            this.updateCartDisplay();
            this.showNotification('Item added to cart!');
        } catch (error) {
            console.error('Error adding item to cart:', error);
            alert('Failed to add item to cart. Please try again.');
        }
    }

    removeItem(itemId) {
        this.items = this.items.filter(item => item.id !== itemId);
        this.saveCart();
        this.updateCartDisplay();
    }

    updateQuantity(itemId, quantity) {
        const item = this.items.find(i => i.id === itemId);
        if (item) {
            item.quantity = Math.max(1, quantity);
            this.saveCart();
            this.updateCartDisplay();
        }
    }

    getTotal() {
        return this.items.reduce((total, item) => total + (item.price * item.quantity), 0);
    }

    setupEventListeners() {
        // Handle add to cart buttons from menu
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('add-to-cart')) {
                const itemId = e.target.dataset.itemId;
                this.addItem(itemId);
            }
        });

        // Handle cart item quantity changes
        document.addEventListener('change', (e) => {
            if (e.target.classList.contains('cart-item-quantity')) {
                const itemId = e.target.dataset.itemId;
                const quantity = parseInt(e.target.value);
                this.updateQuantity(itemId, quantity);
            }
        });

        // Handle remove item buttons
        document.addEventListener('click', (e) => {
            if (e.target.classList.contains('remove-item')) {
                const itemId = e.target.dataset.itemId;
                this.removeItem(itemId);
            }
        });

        // Handle checkout button
        const checkoutBtn = document.getElementById('checkoutBtn');
        if (checkoutBtn) {
            checkoutBtn.addEventListener('click', () => this.handleCheckout());
        }
    }

    updateCartDisplay() {
        const cartItems = document.getElementById('cartItems');
        const cartTotal = document.getElementById('cartTotal');
        const cartCount = document.getElementById('cartCount');

        if (cartItems) {
            cartItems.innerHTML = this.items.map(item => `
                <div class="cart-item">
                    <div class="item-details">
                        <h5>${item.name}</h5>
                        <p>$${item.price.toFixed(2)}</p>
                    </div>
                    <div class="item-quantity">
                        <input type="number" class="cart-item-quantity" 
                               data-item-id="${item.id}" 
                               value="${item.quantity}" 
                               min="1">
                    </div>
                    <button class="btn btn-danger remove-item" data-item-id="${item.id}">
                        Remove
                    </button>
                </div>
            `).join('');
        }

        if (cartTotal) {
            cartTotal.textContent = `$${this.getTotal().toFixed(2)}`;
        }

        if (cartCount) {
            const totalItems = this.items.reduce((total, item) => total + item.quantity, 0);
            cartCount.textContent = totalItems;
        }
    }

    showNotification(message) {
        const notification = document.createElement('div');
        notification.className = 'alert alert-success notification';
        notification.textContent = message;
        document.body.appendChild(notification);

        setTimeout(() => {
            notification.remove();
        }, 3000);
    }

    async handleCheckout() {
        const token = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];
        
        if (!token) {
            window.location.href = '/login.html';
            return;
        }

        try {
            const response = await fetch(`${API_BASE_URL}/orders`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify({
                    items: this.items
                })
            });

            if (!response.ok) {
                throw new Error('Failed to create order');
            }

            this.items = [];
            this.saveCart();
            this.updateCartDisplay();
            window.location.href = '/orders.html';
        } catch (error) {
            console.error('Error during checkout:', error);
            alert('Failed to complete checkout. Please try again.');
        }
    }
}

// Initialize cart
const cart = new Cart(); 