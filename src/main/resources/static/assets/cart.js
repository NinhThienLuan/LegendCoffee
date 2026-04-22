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

    addToCart(item) {
        // item: { id, variantId, comboId, name, price, image, variant, quantity }
        const cart = this.getCart();
        const incoming = {
            ...item,
            id: String(item.id),
            variantId: item.variantId ? String(item.variantId) : null,
            comboId: item.comboId ? String(item.comboId) : null,
            variant: normalizeVariant(item.variant),
            price: Number(item.price) || 0,
            quantity: Number(item.quantity) || 1
        };

        // Find existing item: 
        // If it's a combo, match by comboId.
        // If it's a variant, match by variantId and variant name.
        const existingItem = cart.find(i => {
            if (incoming.comboId) {
                return String(i.comboId) === incoming.comboId;
            }
            const iVariantId = i.variantId || i.id;
            const incomingVariantId = incoming.variantId || incoming.id;
            return String(iVariantId) === String(incomingVariantId) && normalizeVariant(i.variant) === incoming.variant;
        });

        if (existingItem) {
            existingItem.quantity += incoming.quantity;
        } else {
            cart.push({
                ...incoming
            });
        }

        this.saveCart(cart);
    },

    removeFromCart(id, variant, comboId) {
        const cart = this.getCart().filter(item => {
            if (comboId) {
                return String(item.comboId) !== String(comboId);
            }
            const itemVariantId = item.variantId || item.id;
            return !(String(itemVariantId) === String(id) && normalizeVariant(item.variant) === normalizeVariant(variant));
        });
        this.saveCart(cart);
    },

    updateQuantity(id, variant, quantity, comboId) {
        if (quantity < 1) {
            this.removeFromCart(id, variant, comboId);
            return;
        }

        const cart = this.getCart();
        const item = cart.find(i => {
            if (comboId) {
                return String(i.comboId) === String(comboId);
            }
            const iVariantId = i.variantId || i.id;
            return String(iVariantId) === String(id) && normalizeVariant(i.variant) === normalizeVariant(variant);
        });
        
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

function showToast(msg) {
    let toast = document.getElementById('cart-toast');
    if (!toast) {
        toast = document.createElement('div');
        toast.id = 'cart-toast';
        toast.className = 'fixed bottom-8 left-1/2 -translate-x-1/2 z-[200] bg-neutral-900 text-white px-8 py-4 rounded-2xl shadow-2xl font-black text-xs uppercase tracking-[0.2em] transform transition-all duration-500 translate-y-20 opacity-0 pointer-events-none border border-white/10';
        document.body.appendChild(toast);
    }
    toast.innerText = msg;
    toast.classList.remove('translate-y-20', 'opacity-0');
    setTimeout(() => {
        toast.classList.add('translate-y-20', 'opacity-0');
    }, 2500);
}
window.showToast = showToast;
