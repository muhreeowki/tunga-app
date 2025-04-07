// Menu operations
const menuOperations = {
    // Fetch all menu categories
    async fetchCategories() {
        try {
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.MENU + '/categories', 'GET');
            return response;
        } catch (error) {
            console.error('Error fetching categories:', error);
            throw error;
        }
    },

    // Fetch menu items by category
    async fetchItemsByCategory(categoryId) {
        try {
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.MENU + `/items/category/${categoryId}`, 'GET');
            return response;
        } catch (error) {
            console.error('Error fetching items for category:', error);
            throw error;
        }
    },

    // Fetch vegetarian items
    async fetchVegetarianItems() {
        try {
            const response = await makeApiCall(API_CONFIG.ENDPOINTS.MENU + '/items/vegetarian', 'GET');
            return response;
        } catch (error) {
            console.error('Error fetching vegetarian items:', error);
            throw error;
        }
    },

    // Render menu items
    renderMenuItems(items) {
        const menuContainer = document.getElementById('menuItems');
        if (!menuContainer) return;

        menuContainer.innerHTML = items.map(item => `
            <div class="col-md-4 mb-4">
                <div class="card h-100">
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
                        <button class="btn btn-primary mt-3 add-to-cart" data-item-id="${item.id}">
                            Add to Cart
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
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
document.addEventListener('DOMContentLoaded', () => {
    menuOperations.initializeMenu();
}); 