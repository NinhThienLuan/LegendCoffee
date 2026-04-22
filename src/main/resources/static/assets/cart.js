/**
 * Session-based Cart Management System
 */

const CART_STORAGE_KEY = 'industrial_legend_cart';

function normalizeVariant(variant) {
    return (variant && String(variant).trim()) ? String(variant).trim() : '__default__';
}

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
        const incoming = {
            ...product,
            id: String(product.id),
            variant: normalizeVariant(product.variant),
            price: Number(product.price) || 0,
            quantity: Number(product.quantity) || 1
        };
        const existingItem = cart.find(item => String(item.id) === incoming.id && normalizeVariant(item.variant) === incoming.variant);

        if (existingItem) {
            existingItem.quantity += incoming.quantity;
        } else {
            cart.push({
                ...incoming
            });
        }

        this.saveCart(cart);
    },

    removeFromCart(id, variant) {
        const cart = this.getCart().filter(item => !(String(item.id) === String(id) && normalizeVariant(item.variant) === normalizeVariant(variant)));
        this.saveCart(cart);
    },

    updateQuantity(id, variant, quantity) {
        if (quantity < 1) {
            this.removeFromCart(id, variant);
            return;
        }

        const cart = this.getCart();
        const item = cart.find(i => String(i.id) === String(id) && normalizeVariant(i.variant) === normalizeVariant(variant));
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
        const vat = Math.round(subtotal * 0.08);
        const total = subtotal + vat;
        return { subtotal, vat, total, shipping: 0 };
    },

    clearCart() {
        this.saveCart([]);
    }
};

// Initialize globally
window.CartSystem = CartSystem;
