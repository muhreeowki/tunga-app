// Cart functionality
class Cart {
    constructor() {
        this.items = [];
        this.loadCart();
        this.updateCartDisplay();
        this.setupEventListeners();
    }

    loadCart() {
        const savedCart = localStorage.getItem('cart');
        if (savedCart) {
            this.items = JSON.parse(savedCart);
        }
    }

    saveCart() {
        localStorage.setItem('cart', JSON.stringify(this.items));
    }

    addItem(item) {
        const existingItem = this.items.find(i => i.id === item.id);
        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            this.items.push({ ...item, quantity: 1 });
        }
        this.saveCart();
        this.updateCartDisplay();
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

    clearCart() {
        this.items = [];
        this.saveCart();
        this.updateCartDisplay();
    }

    calculateSubtotal() {
        return this.items.reduce((total, item) => total + (item.price * item.quantity), 0);
    }

    calculateTax() {
        return this.calculateSubtotal() * 0.1; // 10% tax
    }

    calculateDeliveryFee() {
        return this.items.length > 0 ? 5.00 : 0; // $5 delivery fee
    }

    calculateTotal() {
        return this.calculateSubtotal() + this.calculateTax() + this.calculateDeliveryFee();
    }

    updateCartDisplay() {
        const cartItemsContainer = document.getElementById('cartItems');
        const emptyCartMessage = document.getElementById('emptyCartMessage');
        const subtotalElement = document.getElementById('subtotal');
        const taxElement = document.getElementById('tax');
        const deliveryFeeElement = document.getElementById('deliveryFee');
        const totalElement = document.getElementById('total');
        const checkoutButton = document.getElementById('checkoutButton');

        if (this.items.length === 0) {
            cartItemsContainer.style.display = 'none';
            emptyCartMessage.style.display = 'block';
            checkoutButton.disabled = true;
        } else {
            cartItemsContainer.style.display = 'block';
            emptyCartMessage.style.display = 'none';
            checkoutButton.disabled = false;

            cartItemsContainer.innerHTML = this.items.map(item => `
                <div class="cart-item mb-3">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <h5 class="mb-1">${item.name}</h5>
                            <p class="mb-1">$${item.price.toFixed(2)}</p>
                        </div>
                        <div class="d-flex align-items-center">
                            <div class="input-group" style="width: 120px;">
                                <button class="btn btn-outline-secondary" type="button" onclick="cart.updateQuantity(${item.id}, ${item.quantity - 1})">-</button>
                                <input type="number" class="form-control text-center" value="${item.quantity}" min="1" onchange="cart.updateQuantity(${item.id}, this.value)">
                                <button class="btn btn-outline-secondary" type="button" onclick="cart.updateQuantity(${item.id}, ${item.quantity + 1})">+</button>
                            </div>
                            <button class="btn btn-danger ms-2" onclick="cart.removeItem(${item.id})">
                                <i class="fas fa-trash"></i>
                            </button>
                        </div>
                    </div>
                </div>
            `).join('');
        }

        subtotalElement.textContent = `$${this.calculateSubtotal().toFixed(2)}`;
        taxElement.textContent = `$${this.calculateTax().toFixed(2)}`;
        deliveryFeeElement.textContent = `$${this.calculateDeliveryFee().toFixed(2)}`;
        totalElement.textContent = `$${this.calculateTotal().toFixed(2)}`;
    }

    setupEventListeners() {
        document.getElementById('checkoutButton').addEventListener('click', () => {
            if (this.items.length === 0) return;

            // Check if user is logged in
            const token = localStorage.getItem('token');
            if (!token) {
                window.location.href = 'login.html';
                return;
            }

            // Redirect to delivery page for checkout
            window.location.href = 'delivery.html';
        });
    }
}

// Initialize cart
const cart = new Cart(); 