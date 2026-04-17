/**
 * Session-based Cart Management System
 */

const CART_STORAGE_KEY = 'industrial_legend_cart';

const CartSystem = {
    getCart() {
        try {
            return JSON.parse(sessionStorage.getItem(CART_STORAGE_KEY)) || [];
        } catch (e) {
            console.error('Error parsing cart from session storage', e);
            return [];
        }
    },

    saveCart(cart) {
        sessionStorage.setItem(CART_STORAGE_KEY, JSON.stringify(cart));
        // Dispatch a custom event to notify other components (like Navbar)
        window.dispatchEvent(new CustomEvent('cartUpdated', { detail: cart }));
    },

    addToCart(product) {
        // product: { id, name, price, image, variant, quantity }
        const cart = this.getCart();
        const existingItem = cart.find(item => item.id === product.id && item.variant === product.variant);

        if (existingItem) {
            existingItem.quantity += (product.quantity || 1);
        } else {
            cart.push({
                ...product,
                quantity: product.quantity || 1
            });
        }

        this.saveCart(cart);
    },

    removeFromCart(id, variant) {
        const cart = this.getCart().filter(item => !(item.id === id && item.variant === variant));
        this.saveCart(cart);
    },

    updateQuantity(id, variant, quantity) {
        if (quantity < 1) {
            this.removeFromCart(id, variant);
            return;
        }

        const cart = this.getCart();
        const item = cart.find(i => i.id === id && i.variant === variant);
        if (item) {
            item.quantity = quantity;
            this.saveCart(cart);
        }
    },

    getCartCount() {
        return this.getCart().reduce((sum, item) => sum + item.quantity, 0);
    },

    calculateTotals() {
        const subtotal = this.getCart().reduce((sum, item) => sum + (item.price * item.quantity), 0);
        const shipping = subtotal > 0 ? 120000 : 0; // Flat industrial shipping
        const total = subtotal + shipping;
        return { subtotal, shipping, total };
    },

    clearCart() {
        this.saveCart([]);
    }
};

// Initialize globally
window.CartSystem = CartSystem;
