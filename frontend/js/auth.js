/**
 * AgroDoc — auth.js
 * Frontend authentication utilities.
 *
 * Handles:
 * - JWT token storage and retrieval (localStorage)
 * - User session management
 * - Route guards (redirect unauthenticated users away from protected pages)
 * - Redirect authenticated users away from login/register pages
 * - Populating the navbar with the logged-in user's name
 *
 * Usage:
 *   Include this script BEFORE page-specific scripts on every page.
 *   Call requireAuth() on protected pages.
 *   Call requireGuest() on login/register pages.
 */

// ============================================================================
// CONSTANTS
// ============================================================================
const TOKEN_KEY = 'agrodoc_token';
const USER_KEY  = 'agrodoc_user';

// ============================================================================
// TOKEN MANAGEMENT
// ============================================================================

/**
 * Store the JWT token in localStorage after a successful login.
 * @param {string} token - The JWT string returned by the backend.
 */
function setToken(token) {
    if (!token || typeof token !== 'string') {
        console.error('[auth.js] setToken: invalid token value');
        return;
    }
    localStorage.setItem(TOKEN_KEY, token);
}

/**
 * Retrieve the stored JWT token.
 * @returns {string|null} The JWT string, or null if not logged in.
 */
function getToken() {
    return localStorage.getItem(TOKEN_KEY);
}

/**
 * Remove the JWT token (logout).
 */
function clearToken() {
    localStorage.removeItem(TOKEN_KEY);
}

/**
 * Returns true if a JWT token exists in storage.
 * NOTE: This only checks for presence, not expiry or validity.
 * Token validation is enforced by the Spring Boot backend on every request.
 * @returns {boolean}
 */
function isLoggedIn() {
    return !!getToken();
}

// ============================================================================
// USER PROFILE CACHE
// ============================================================================

/**
 * Store the authenticated user's basic profile in localStorage.
 * Used to quickly display the user's name in the navbar without an API call.
 *
 * @param {Object} user - { id, fullName, email }
 */
function setUser(user) {
    if (!user) return;
    try {
        localStorage.setItem(USER_KEY, JSON.stringify(user));
    } catch (e) {
        console.error('[auth.js] setUser: failed to serialize user object', e);
    }
}

/**
 * Retrieve the cached user profile.
 * @returns {Object|null} { id, fullName, email } or null.
 */
function getUser() {
    const raw = localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try {
        return JSON.parse(raw);
    } catch (e) {
        console.error('[auth.js] getUser: failed to parse user object', e);
        return null;
    }
}

/**
 * Remove the cached user profile (called during logout).
 */
function clearUser() {
    localStorage.removeItem(USER_KEY);
}

// ============================================================================
// LOGOUT
// ============================================================================

/**
 * Log out the current user:
 * 1. Clears token and user from localStorage.
 * 2. Redirects to the landing page.
 */
function logout() {
    clearToken();
    clearUser();
    window.location.href = '/index.html';
}

// ============================================================================
// ROUTE GUARDS
// ============================================================================

/**
 * Protect a page that requires authentication.
 * Call this at the top of the script for every protected page
 * (dashboard, scan, result, history, settings).
 *
 * If the user is not logged in, they are redirected to login.html.
 * If the user IS logged in, the page renders normally.
 *
 * @param {Object} [options]
 * @param {string} [options.redirectTo='/login.html'] - Override redirect destination.
 */
function requireAuth(options = {}) {
    const redirectTo = options.redirectTo || '/login.html';
    if (!isLoggedIn()) {
        const currentUrl = encodeURIComponent(window.location.href);
        window.location.href = `${redirectTo}?next=${currentUrl}`;
    }
}

/**
 * Protect a page that should only be accessible to guests (not logged in).
 * Call this on login.html and register.html.
 *
 * If the user IS logged in, they are redirected to the dashboard.
 * If the user is NOT logged in, the page renders normally.
 *
 * @param {Object} [options]
 * @param {string} [options.redirectTo='/dashboard.html'] - Override redirect destination.
 */
function requireGuest(options = {}) {
    const redirectTo = options.redirectTo || '/dashboard.html';
    if (isLoggedIn()) {
        window.location.href = redirectTo;
    }
}

// ============================================================================
// NAVBAR HYDRATION
// ============================================================================

/**
 * Populates the navbar with authenticated user information and binds
 * the logout button. Call this on every page that uses the authenticated navbar.
 *
 * Expects these element IDs in the navbar HTML:
 *   - nav-user-name     — displays the user's full name
 *   - nav-user-avatar   — optional avatar with user initial
 *   - btn-logout        — logout button
 */
function hydrateNavbar() {
    const user = getUser();

    // Display user name
    const nameEl = document.getElementById('nav-user-name');
    if (nameEl && user && user.fullName) {
        nameEl.textContent = user.fullName;
    }

    // Display first initial in avatar
    const avatarEl = document.getElementById('nav-user-avatar');
    if (avatarEl && user && user.fullName) {
        avatarEl.textContent = user.fullName.charAt(0).toUpperCase();
    }

    // Bind logout button
    const logoutBtn = document.getElementById('btn-logout');
    if (logoutBtn) {
        logoutBtn.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }

    // Bind mobile hamburger menu toggle
    const hamburger = document.querySelector('.nav-hamburger');
    const navLinks  = document.querySelector('.nav-links');
    if (hamburger && navLinks) {
        hamburger.addEventListener('click', () => {
            navLinks.classList.toggle('open');
        });
    }

    // Add scrolled class to navbar on scroll
    const nav = document.querySelector('.agrodoc-nav');
    if (nav) {
        window.addEventListener('scroll', () => {
            nav.classList.toggle('scrolled', window.scrollY > 10);
        }, { passive: true });
    }

    // Highlight active nav link
    _setActiveNavLink();
}

/**
 * Marks the current page's nav link as active based on the URL.
 * @private
 */
function _setActiveNavLink() {
    const currentPath = window.location.pathname;
    const links = document.querySelectorAll('.nav-link');
    links.forEach(link => {
        const href = link.getAttribute('href');
        if (href && currentPath.endsWith(href)) {
            link.classList.add('active');
        }
    });
}

// ============================================================================
// LOGIN FORM HANDLER UTILITY
// ============================================================================

/**
 * Handles a completed login response from the backend.
 * Stores token + user profile and redirects to the dashboard or 'next' param.
 *
 * @param {Object} response - { token, user: { id, fullName, email } }
 */
function handleLoginSuccess(response) {
    if (!response || !response.token) {
        console.error('[auth.js] handleLoginSuccess: no token in response');
        return;
    }
    setToken(response.token);
    if (response.user) {
        setUser(response.user);
    }

    // Redirect to 'next' URL param if present, otherwise to dashboard
    const params = new URLSearchParams(window.location.search);
    const next = params.get('next');
    window.location.href = next ? decodeURIComponent(next) : '/dashboard.html';
}

// ============================================================================
// EXPORTS (global scope for plain HTML/JS usage)
// ============================================================================
window.setToken          = setToken;
window.getToken          = getToken;
window.clearToken        = clearToken;
window.isLoggedIn        = isLoggedIn;
window.setUser           = setUser;
window.getUser           = getUser;
window.clearUser         = clearUser;
window.logout            = logout;
window.requireAuth       = requireAuth;
window.requireGuest      = requireGuest;
window.hydrateNavbar     = hydrateNavbar;
window.handleLoginSuccess = handleLoginSuccess;
