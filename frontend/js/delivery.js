document.addEventListener('DOMContentLoaded', function() {
    const deliveryForm = document.getElementById('deliveryForm');
    const submitButton = document.getElementById('submitDelivery');
    const loadingSpinner = document.getElementById('loadingSpinner');
    const errorMessage = document.getElementById('errorMessage');
    const menuItems = document.getElementById('menuItems');
    const cartItems = document.getElementById('cartItems');
    const emptyCartMessage = document.getElementById('emptyCartMessage');
    const cartTotal = document.getElementById('cartTotal');
    const totalAmount = document.getElementById('totalAmount');
    const nextToStep2 = document.getElementById('nextToStep2');
    const backToStep1 = document.getElementById('backToStep1');
    const step1 = document.getElementById('step1');
    const step2 = document.getElementById('step2');
    const step1Indicator = document.getElementById('step1Indicator');
    const step2Indicator = document.getElementById('step2Indicator');
    const restaurantSelect = document.getElementById('restaurant');

    let cart = [];
    let restaurants = [];

    // Fetch available restaurants
    async function fetchRestaurants() {
        try {
            const token = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];
            if (!token) {
                throw new Error('Please login to view restaurants');
            }

            const response = await fetch(`${API_CONFIG.BASE_URL}/api/restaurants`, {
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            });
            
            if (!response.ok) {
                if (response.status === 401) {
                    throw new Error('Please login to view restaurants');
                }
                throw new Error('Failed to fetch restaurants');
            }
            
            restaurants = await response.json();
            
            // Populate restaurant dropdown
            restaurantSelect.innerHTML = `
                <option value="">Select a restaurant</option>
                ${restaurants.map(restaurant => `
                    <option value="${restaurant.id}">${restaurant.name}</option>
                `).join('')}
            `;
        } catch (error) {
            console.error('Error fetching restaurants:', error);
            errorMessage.textContent = error.message || 'Failed to load restaurants. Please try again later.';
            errorMessage.style.display = 'block';
        }
    }

    // Initialize restaurants when DOM is loaded
    fetchRestaurants();

    // Step navigation functions
    function goToStep2() {
        step1.style.display = 'none';
        step2.style.display = 'block';
        step1Indicator.classList.remove('active');
        step2Indicator.classList.add('active');
    }

    function goToStep1() {
        step2.style.display = 'none';
        step1.style.display = 'block';
        step2Indicator.classList.remove('active');
        step1Indicator.classList.add('active');
    }

    // Event listeners for step navigation
    nextToStep2.addEventListener('click', goToStep2);
    backToStep1.addEventListener('click', goToStep1);

    // Menu operations
    const menuOperations = {
        // Fetch all menu categories
        async fetchCategories() {
            try {
                const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MENU}/categories`);
                if (!response.ok) {
                    throw new Error('Failed to fetch categories');
                }
                return await response.json();
            } catch (error) {
                console.error('Error fetching categories:', error);
                throw error;
            }
        },

        // Fetch menu items by category
        async fetchItemsByCategory(categoryId) {
            try {
                const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MENU}/items/category/${categoryId}`);
                if (!response.ok) {
                    throw new Error('Failed to fetch items for category');
                }
                return await response.json();
            } catch (error) {
                console.error('Error fetching items for category:', error);
                throw error;
            }
        },

        // Fetch vegetarian items
        async fetchVegetarianItems() {
            try {
                const response = await fetch(`${API_CONFIG.BASE_URL}${API_CONFIG.ENDPOINTS.MENU}/items/vegetarian`);
                if (!response.ok) {
                    throw new Error('Failed to fetch vegetarian items');
                }
                return await response.json();
            } catch (error) {
                console.error('Error fetching vegetarian items:', error);
                throw error;
            }
        },

        // Fetch non-vegetarian items
        async fetchNonVegetarianItems() {
            try {
                const allItems = await Promise.all(
                    (await this.fetchCategories()).map(cat => this.fetchItemsByCategory(cat.id))
                );
                return allItems.flat().filter(item => !item.vegetarian);
            } catch (error) {
                console.error('Error fetching non-vegetarian items:', error);
                throw error;
            }
        },

        // Render menu items
        renderMenuItems(items, containerId = 'menuItems') {
            const menuContainer = document.getElementById(containerId);
            if (!menuContainer) return;

            menuContainer.innerHTML = items.map(item => `
                <div class="col-md-6 mb-4">
                    <div class="card h-100 menu-item-card">
                        ${item.imageUrl ? `<img src="${item.imageUrl}" class="card-img-top" alt="${item.name}">` : ''}
                        <div class="card-body">
                            <h5 class="card-title">${item.name}</h5>
                            <p class="card-text">${item.description}</p>
                            <div class="d-flex justify-content-between align-items-center">
                                <span class="price">$${item.price.toFixed(2)}</span>
                                ${item.vegetarian ? '<span class="badge bg-success">Vegetarian</span>' : ''}
                            </div>
                            <div class="mt-2">
                                <small class="text-muted">
                                    Serves ${item.servesPeople} | Prep time: ${item.preparationTimeMinutes} mins
                                </small>
                            </div>
                            <div class="mt-3">
                                <button class="btn btn-primary add-to-cart" data-item-id="${item.id}">
                                    Add to Cart
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `).join('');

            // Add event listeners for add to cart buttons
            document.querySelectorAll('.add-to-cart').forEach(button => {
                button.addEventListener('click', function() {
                    const itemId = parseInt(this.dataset.itemId);
                    addToCart(itemId);
                });
            });
        },

        // Render category tabs
        renderCategoryTabs(categories) {
            const categoryTabs = document.getElementById('categoryTabs');
            if (!categoryTabs) return;

            categoryTabs.innerHTML = `
                <li class="nav-item">
                    <a class="nav-link active" data-bs-toggle="tab" href="#all" role="tab">All</a>
                </li>
                ${categories.map(category => `
                    <li class="nav-item">
                        <a class="nav-link" data-bs-toggle="tab" href="#category-${category.id}" role="tab">
                            ${category.name}
                        </a>
                    </li>
                `).join('')}
                <li class="nav-item">
                    <a class="nav-link" data-bs-toggle="tab" href="#vegetarian" role="tab">Vegetarian</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" data-bs-toggle="tab" href="#non-vegetarian" role="tab">Non-Vegetarian</a>
                </li>
            `;
        },

        // Initialize menu
        async initializeMenu() {
            try {
                // Fetch and render categories
                const categories = await this.fetchCategories();
                this.renderCategoryTabs(categories);

                // Fetch and render items for each category
                for (const category of categories) {
                    const items = await this.fetchItemsByCategory(category.id);
                    const tabContent = document.getElementById('categoryTabsContent');
                    if (tabContent) {
                        tabContent.innerHTML += `
                            <div class="tab-pane fade" id="category-${category.id}" role="tabpanel">
                                <div class="row" id="category-${category.id}-items"></div>
                            </div>
                        `;
                        this.renderMenuItems(items, `category-${category.id}-items`);
                    }
                }

                // Fetch and render vegetarian items
                const vegetarianItems = await this.fetchVegetarianItems();
                const tabContent = document.getElementById('categoryTabsContent');
                if (tabContent) {
                    tabContent.innerHTML += `
                        <div class="tab-pane fade" id="vegetarian" role="tabpanel">
                            <div class="row" id="vegetarian-items"></div>
                        </div>
                    `;
                    this.renderMenuItems(vegetarianItems, 'vegetarian-items');
                }

                // Fetch and render non-vegetarian items
                const nonVegetarianItems = await this.fetchNonVegetarianItems();
                if (tabContent) {
                    tabContent.innerHTML += `
                        <div class="tab-pane fade" id="non-vegetarian" role="tabpanel">
                            <div class="row" id="non-vegetarian-items"></div>
                        </div>
                    `;
                    this.renderMenuItems(nonVegetarianItems, 'non-vegetarian-items');
                }

                // Add event listeners for tab changes
                const tabLinks = document.querySelectorAll('.nav-link');
                tabLinks.forEach(link => {
                    link.addEventListener('click', async (e) => {
                        e.preventDefault();
                        const target = e.target.getAttribute('href').substring(1);
                        
                        if (target === 'all') {
                            // Show all items
                            const allItems = await Promise.all(
                                categories.map(cat => this.fetchItemsByCategory(cat.id))
                            );
                            this.renderMenuItems(allItems.flat());
                        } else if (target === 'vegetarian') {
                            const items = await this.fetchVegetarianItems();
                            this.renderMenuItems(items, 'vegetarian-items');
                        } else if (target === 'non-vegetarian') {
                            const items = await this.fetchNonVegetarianItems();
                            this.renderMenuItems(items, 'non-vegetarian-items');
                        } else if (target.startsWith('category-')) {
                            const categoryId = target.split('-')[1];
                            const items = await this.fetchItemsByCategory(categoryId);
                            this.renderMenuItems(items, `${target}-items`);
                        }
                    });
                });

            } catch (error) {
                console.error('Error initializing menu:', error);
                const menuContainer = document.getElementById('menuItems');
                if (menuContainer) {
                    menuContainer.innerHTML = `
                        <div class="col-12 text-center">
                            <div class="alert alert-danger">
                                Error loading menu items. Please try again later.
                            </div>
                        </div>
                    `;
                }
            }
        }
    };

    // Initialize menu when DOM is loaded
    menuOperations.initializeMenu();

    // Cart functions
    function addToCart(itemId) {
        // Find the item card in any of the tab containers
        const itemCard = document.querySelector(`[data-item-id="${itemId}"]`).closest('.card');
        if (!itemCard) return;

        const itemName = itemCard.querySelector('.card-title').textContent;
        const itemPrice = parseFloat(itemCard.querySelector('.price').textContent.replace('$', ''));
        
        const existingItem = cart.find(item => item.id === itemId);
        if (existingItem) {
            existingItem.quantity += 1;
        } else {
            cart.push({
                id: itemId,
                name: itemName,
                price: itemPrice,
                quantity: 1
            });
        }
        
        updateCartDisplay();
        // Enable next button if cart is not empty
        nextToStep2.disabled = cart.length === 0;
    }

    function updateCartDisplay() {
        cartItems.innerHTML = '';
        
        if (cart.length === 0) {
            emptyCartMessage.style.display = 'block';
            cartTotal.style.display = 'none';
            nextToStep2.disabled = true;
            return;
        }

        emptyCartMessage.style.display = 'none';
        cartTotal.style.display = 'block';
        nextToStep2.disabled = false;

        let total = 0;
        cart.forEach(item => {
            const itemTotal = item.price * item.quantity;
            total += itemTotal;

            const itemDiv = document.createElement('div');
            itemDiv.className = 'cart-item';
            itemDiv.innerHTML = `
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <h6 class="mb-0">${item.name}</h6>
                        <small class="text-muted">$${item.price.toFixed(2)} each</small>
                    </div>
                    <div class="d-flex align-items-center">
                        <button class="btn btn-sm btn-outline-secondary decrease-quantity" data-item-id="${item.id}">-</button>
                        <span class="mx-2">${item.quantity}</span>
                        <button class="btn btn-sm btn-outline-secondary increase-quantity" data-item-id="${item.id}">+</button>
                        <button class="btn btn-sm btn-danger ms-2 remove-item" data-item-id="${item.id}">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
            `;
            cartItems.appendChild(itemDiv);
        });

        totalAmount.textContent = total.toFixed(2);

        // Add event listeners for cart controls
        document.querySelectorAll('.decrease-quantity').forEach(button => {
            button.addEventListener('click', function() {
                const itemId = parseInt(this.dataset.itemId);
                updateQuantity(itemId, -1);
            });
        });

        document.querySelectorAll('.increase-quantity').forEach(button => {
            button.addEventListener('click', function() {
                const itemId = parseInt(this.dataset.itemId);
                updateQuantity(itemId, 1);
            });
        });

        document.querySelectorAll('.remove-item').forEach(button => {
            button.addEventListener('click', function() {
                const itemId = parseInt(this.dataset.itemId);
                removeFromCart(itemId);
            });
        });
    }

    function updateQuantity(itemId, change) {
        const item = cart.find(item => item.id === itemId);
        if (item) {
            item.quantity += change;
            if (item.quantity <= 0) {
                removeFromCart(itemId);
            } else {
                updateCartDisplay();
            }
        }
    }

    function removeFromCart(itemId) {
        cart = cart.filter(item => item.id !== itemId);
        updateCartDisplay();
    }

    // Handle form submission
    deliveryForm.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        if (cart.length === 0) {
            errorMessage.textContent = 'Please add at least one item to your cart';
            errorMessage.style.display = 'block';
            return;
        }

        const selectedRestaurantId = restaurantSelect.value;
        if (!selectedRestaurantId) {
            errorMessage.textContent = 'Please select a restaurant';
            errorMessage.style.display = 'block';
            return;
        }

        // Show loading state
        submitButton.disabled = true;
        loadingSpinner.style.display = 'inline-block';
        errorMessage.style.display = 'none';

        try {
            // Get user ID from auth token
            const token = document.cookie.split('; ').find(row => row.startsWith('token='))?.split('=')[1];
            if (!token) {
                throw new Error('Please login to place an order');
            }

            const userdata = document.cookie.split('; ').find(row => row.startsWith('user='))?.split('=')[1];
            const user = JSON.parse(decodeURIComponent(userdata));

            // Get user ID from token
            const tokenPayload = JSON.parse(atob(token.split('.')[1]));

            if (!user || !token || !tokenPayload || !user.id) {
                throw new Error('Invalid user session. Please login again.');
            }

            // Create order request object
            const orderRequest = {
                restaurantId: parseInt(selectedRestaurantId),
                items: cart.map(item => ({
                    menuItemId: item.id,
                    quantity: item.quantity,
                    specialInstructions: item.specialInstructions || ''
                })),
                deliveryAddress: document.getElementById('address').value,
                deliveryCity: document.getElementById('city').value,
                deliveryState: document.getElementById('state').value,
                deliveryZipCode: document.getElementById('zipCode').value,
                contactPhone: document.getElementById('phone').value,
                specialInstructions: document.getElementById('specialInstructions').value
            };

            // Make API call
            const response = await fetch(`http://localhost:8080/api/orders/user/${user.id}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${token}`
                },
                body: JSON.stringify(orderRequest)
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || 'Failed to place order');
            }

            const result = await response.json();
            
            // Show success message with order number
            alert(`Order placed successfully! Your order number is: ${result.tokenNumber}`);
            
            // Reset form and cart
            deliveryForm.reset();
            cart = [];
            updateCartDisplay();
            window.location.href = 'order-success.html?orderId=' + result.id + '&ticketCode=' + result.tokenNumber;
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
}); 