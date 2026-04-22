// Cart Badge Update Logic for Navbar
(function () {
  // Navbar scroll behavior
  let lastScrollY = window.scrollY;
  const navbar = document.getElementById('mainNavbar');
  window.addEventListener('scroll', () => {
    if (!navbar) return;
    if (window.scrollY > lastScrollY && window.scrollY > 40) {
      navbar.style.transform = 'translateY(-100%)';
    } else {
      navbar.style.transform = 'translateY(0)';
    }
    lastScrollY = window.scrollY;
  });

  function updateNavbarCartBadge() {
    const badge = document.getElementById('cart-badge');
    if (!badge) {
      console.log('[CartBadge] Badge element not found');
      return;
    }
    let count = 0;
    if (window.CartSystem && typeof window.CartSystem.getCartCount === 'function') {
      count = window.CartSystem.getCartCount();
      console.log('[CartBadge] Got cart count from CartSystem:', count);
    } else {
      // Fallback: read cart from localStorage
      try {
        const cartRaw = localStorage.getItem('cart');
        console.log('[CartBadge] Fallback to localStorage. Raw cart:', cartRaw);
        if (cartRaw) {
          const cart = JSON.parse(cartRaw);
          if (Array.isArray(cart)) {
            count = cart.reduce((sum, item) => sum + (item.quantity || 0), 0);
            console.log('[CartBadge] Parsed cart array. Total quantity:', count, cart);
          } else {
            console.log('[CartBadge] Cart in localStorage is not an array:', cart);
          }
        } else {
          console.log('[CartBadge] No cart found in localStorage');
        }
      } catch (e) {
        console.log('[CartBadge] Error parsing cart from localStorage:', e);
      }
    }
    const cartIconLink = badge.closest('a[aria-label="Cart"]');
    if (count > 0) {
      badge.innerText = count;
      badge.classList.remove('opacity-0');
      // Add hover classes to cart icon link
      if (cartIconLink) {
        cartIconLink.classList.add('text-[#351f1b]', 'bg-[#efe9e5]');
      }
    } else {
      badge.classList.add('opacity-0');
      // Remove hover classes from cart icon link
      if (cartIconLink) {
        cartIconLink.classList.remove('text-[#351f1b]', 'bg-[#efe9e5]');
      }
    }
  }
  document.addEventListener('DOMContentLoaded', function () {
    console.log('[CartBadge] Script loaded, DOMContentLoaded');
    updateNavbarCartBadge();
    console.log('[CartBadge] DOMContentLoaded, badge updated');
  });
  window.addEventListener('cartUpdated', function () {
    updateNavbarCartBadge();
    console.log('[CartBadge] cartUpdated event, badge updated');
  });
  window.addEventListener('storage', function (e) {
    if (e.key === 'cart') {
      updateNavbarCartBadge();
      console.log('[CartBadge] storage event, badge updated');
    }
  });
})();
