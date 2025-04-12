document.addEventListener('DOMContentLoaded', function() {
    // Set active state for current page
    const currentPage = window.location.pathname.split('/').pop();
    const navLinks = document.querySelectorAll('.nav-link');
    
    navLinks.forEach(link => {
        const linkHref = link.getAttribute('href');
        if (linkHref === currentPage) {
            link.classList.add('active');
        }
    });

    // Handle logout
    const logoutLink = document.getElementById('logoutLink');
    if (logoutLink) {
        logoutLink.addEventListener('click', function(e) {
            e.preventDefault();
            localStorage.removeItem('token');
            localStorage.removeItem('user');
            window.location.href = 'index.html';
        });
    }

    // Check authentication status
    const token = localStorage.getItem('token');
    const loginNavItem = document.getElementById('loginNavItem');
    const registerNavItem = document.getElementById('registerNavItem');
    const profileNavItem = document.getElementById('profileNavItem');
    const logoutNavItem = document.getElementById('logoutNavItem');

    if (token) {
        // User is logged in
        if (loginNavItem) loginNavItem.style.display = 'none';
        if (registerNavItem) registerNavItem.style.display = 'none';
        if (profileNavItem) profileNavItem.style.display = 'block';
        if (logoutNavItem) logoutNavItem.style.display = 'block';
    } else {
        // User is not logged in
        if (loginNavItem) loginNavItem.style.display = 'block';
        if (registerNavItem) registerNavItem.style.display = 'block';
        if (profileNavItem) profileNavItem.style.display = 'none';
        if (logoutNavItem) logoutNavItem.style.display = 'none';
    }
}); 