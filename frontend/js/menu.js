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
document.addEventListener('DOMContentLoaded', () => {
    menuOperations.initializeMenu();
});

// Menu functionality
class Menu {
    constructor() {
        this.items = [];
        this.loadMenuItems();
    }

    async loadMenuItems() {
        try {
            const response = await fetch(`${API_BASE_URL}/menu/items`);
            if (!response.ok) {
                throw new Error('Failed to load menu items');
            }
            this.items = await response.json();
            this.renderMenuItems();
        } catch (error) {
            console.error('Error loading menu items:', error);
            alert('Failed to load menu items. Please try again later.');
        }
    }

    renderMenuItems() {
        const menuContainer = document.getElementById('menuItems');
        if (!menuContainer) return;

        menuContainer.innerHTML = this.items.map(item => `
            <div class="col-md-4 mb-4">
                <div class="card h-100">
                    <img src="${item.image_url || 'https://via.placeholder.com/300x200'}" class="card-img-top" alt="${item.name}">
                    <div class="card-body">
                        <h5 class="card-title">${item.name}</h5>
                        <p class="card-text">${item.description}</p>
                        <p class="price">$${item.price.toFixed(2)}</p>
                        <button class="btn btn-primary add-to-cart" data-item-id="${item.id}">
                            Add to Cart
                        </button>
                    </div>
                </div>
            </div>
        `).join('');
    }
}

// Initialize menu
const menu = new Menu(); 