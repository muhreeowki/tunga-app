# TUNGA Restaurant Website

A modern, responsive website for TUNGA restaurant, featuring online table reservations and home delivery services.

## Features

- **Online Table Reservation**
  - Book tables up to one month in advance
  - Minimum 6 hours advance booking required
  - Select from multiple restaurant locations
  - Specify number of guests
  - Receive confirmation token

- **Home Delivery Service**
  - Browse menu items (Vegetarian and Non-vegetarian)
  - Add items to cart
  - View order summary
  - Specify delivery address
  - Receive estimated delivery time
  - Get confirmation token

- **Menu Display**
  - Categorized menu items
  - Price and serving size information
  - Vegetarian and Non-vegetarian sections

## Technologies Used

- HTML5
- CSS3
- Bootstrap 5.3.0
- JavaScript (ES6+)
- Font Awesome Icons

## Project Structure

```
tunga-bootstrap/
├── index.html          # Home page
├── reservation.html    # Table reservation page
├── delivery.html      # Home delivery page
├── menu.html          # Menu display page
├── css/
│   └── style.css      # Custom styles
├── js/
│   ├── reservation.js # Reservation functionality
│   └── delivery.js    # Delivery functionality
└── images/            # Image assets
```

## Setup and Usage

1. Clone the repository
2. Open `index.html` in your web browser
3. Navigate through the website using the navigation menu

## API Integration

The website is designed to work with a REST API backend. Currently, the frontend simulates API calls by logging data to the console. To integrate with a real backend:

1. Update the form submission handlers in `reservation.js` and `delivery.js`
2. Replace the console.log statements with actual API calls
3. Handle the API responses appropriately

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)

## Future Enhancements

- User authentication and account management
- Order tracking system
- Payment gateway integration
- Customer reviews and ratings
- Special offers and promotions
- Mobile app integration

## License

This project is licensed under the MIT License - see the LICENSE file for details. 