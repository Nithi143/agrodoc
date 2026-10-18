/**
 * AgroDoc — api.js
 * Centralized API communication module for the AgroDoc frontend.
 *
 * All HTTP calls to the Spring Boot backend go through this module.
 * JWT tokens are automatically attached to authenticated requests.
 * Error responses are normalized into a consistent format.
 *
 * Usage:
 *   import { api } from './api.js';
 *   const result = await api.get('/diseases');
 *   const scan = await api.postMultipart('/scan/upload', formData);
 */

// ============================================================================
// CONFIGURATION
// ============================================================================

/**
 * Base URL of the Spring Boot backend.
 * Update this value if the backend runs on a different host or port.
 * This value should NOT be committed with a production URL — use your dev URL.
 */
const API_BASE_URL = 'http://localhost:8080/api';

// ============================================================================
// TOKEN HELPERS (imported from auth.js)
// ============================================================================

/**
 * Retrieves the stored JWT token.
 * Defined here as a fallback so api.js can be used standalone.
 * The canonical token management functions live in auth.js.
 */
function _getToken() {
    return localStorage.getItem('agrodoc_token');
}

// ============================================================================
// INTERNAL FETCH WRAPPER
// ============================================================================

/**
 * Core fetch wrapper used by all public API methods.
 *
 * @param {string} endpoint - Path relative to API_BASE_URL (e.g. '/auth/login')
 * @param {Object} options - Fetch options (method, body, headers, etc.)
 * @param {boolean} requiresAuth - If true, attaches the JWT Authorization header.
 * @returns {Promise<any>} Parsed JSON response body.
 * @throws {ApiError} Normalized error object with status, message, and field errors.
 */
async function _fetch(endpoint, options = {}, requiresAuth = true) {
    const headers = {
        ...options.headers,
    };

    // Attach JWT token if this is an authenticated request
    if (requiresAuth) {
        const token = _getToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }
        // If no token and auth is required, the backend will return 401.
        // The response handler below will redirect to login.
    }

    // Default Content-Type to JSON unless caller overrides (e.g. multipart uploads)
    if (!headers['Content-Type'] && !(options.body instanceof FormData)) {
        headers['Content-Type'] = 'application/json';
    }

    let response;
    try {
        response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers,
        });
    } catch (networkError) {
        // Network failure (server down, no internet, CORS preflight failure)
        throw new ApiError(
            0,
            'Unable to reach the server. Please check your connection.',
            null,
            networkError
        );
    }

    // Handle 401 Unauthorized — token expired or invalid
    if (response.status === 401) {
        // Clear invalid token and redirect to login
        localStorage.removeItem('agrodoc_token');
        localStorage.removeItem('agrodoc_user');
        // Redirect to login only if not already on an auth page
        const authPages = ['/login.html', '/register.html', '/index.html', '/'];
        const currentPath = window.location.pathname;
        const isAuthPage = authPages.some(p => currentPath.endsWith(p));
        if (!isAuthPage) {
            window.location.href = '/login.html?reason=session_expired';
        }
        throw new ApiError(401, 'Your session has expired. Please log in again.');
    }

    // Parse the response body
    let data = null;
    const contentType = response.headers.get('Content-Type') || '';

    if (contentType.includes('application/json')) {
        data = await response.json();
    } else if (response.status !== 204) {
        // 204 No Content has no body
        data = await response.text();
    }

    // Handle HTTP error responses (4xx, 5xx)
    if (!response.ok) {
        const message =
            (data && (data.message || data.error)) ||
            `Request failed with status ${response.status}`;
        const fieldErrors = (data && data.details) || null;
        throw new ApiError(response.status, message, fieldErrors);
    }

    return data;
}

// ============================================================================
// ApiError CLASS
// ============================================================================

/**
 * Normalized error class thrown by all api.js methods.
 * Consumers can inspect .status, .message, and .fieldErrors for UI rendering.
 *
 * @property {number} status - HTTP status code (0 for network failures)
 * @property {string} message - Human-readable error message
 * @property {Object|null} fieldErrors - Field-specific validation errors from backend
 * @property {Error|null} cause - Original error (for network failures)
 */
class ApiError extends Error {
    constructor(status, message, fieldErrors = null, cause = null) {
        super(message);
        this.name = 'ApiError';
        this.status = status;
        this.message = message;
        this.fieldErrors = fieldErrors;
        this.cause = cause;
    }

    /**
     * Get the error message for a specific form field.
     * @param {string} fieldName - The field name (matches backend DTO field name)
     * @returns {string|null} Error message for that field, or null if none.
     */
    getFieldError(fieldName) {
        return (this.fieldErrors && this.fieldErrors[fieldName]) || null;
    }

    /**
     * Returns true if this is a network/connectivity error.
     */
    isNetworkError() {
        return this.status === 0;
    }

    /**
     * Returns true if the request was rejected due to authentication failure.
     */
    isAuthError() {
        return this.status === 401 || this.status === 403;
    }
}

// ============================================================================
// PUBLIC API METHODS
// ============================================================================

const api = {

    // --------------------------------------------------------------------------
    // Authentication Endpoints (no JWT required)
    // --------------------------------------------------------------------------

    /**
     * Register a new user account.
     * POST /api/auth/register
     *
     * @param {Object} data - { fullName, email, password }
     * @returns {Promise<Object>} { message: string }
     */
    async register(data) {
        return _fetch('/auth/register', {
            method: 'POST',
            body: JSON.stringify(data),
        }, false);
    },

    /**
     * Authenticate a user and receive a JWT token.
     * POST /api/auth/login
     *
     * @param {Object} data - { email, password }
     * @returns {Promise<Object>} { token, user: { id, fullName, email } }
     */
    async login(data) {
        return _fetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data),
        }, false);
    },

    // --------------------------------------------------------------------------
    // User Endpoints (JWT required)
    // --------------------------------------------------------------------------

    /**
     * Get the currently authenticated user's profile.
     * GET /api/users/me
     *
     * @returns {Promise<Object>} { id, fullName, email, createdAt }
     */
    async getCurrentUser() {
        return _fetch('/users/me', { method: 'GET' });
    },

    /**
     * Update the current user's display name.
     * PATCH /api/users/me
     *
     * @param {Object} data - { fullName }
     * @returns {Promise<Object>} Updated user profile
     */
    async updateProfile(data) {
        return _fetch('/users/me', {
            method: 'PATCH',
            body: JSON.stringify(data),
        });
    },

    /**
     * Change the current user's password.
     * POST /api/users/me/change-password
     *
     * @param {Object} data - { currentPassword, newPassword }
     * @returns {Promise<Object>} { message: string }
     */
    async changePassword(data) {
        return _fetch('/users/me/change-password', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    },

    // --------------------------------------------------------------------------
    // Disease Scan Endpoints (JWT required)
    // --------------------------------------------------------------------------

    /**
     * Upload a tomato leaf image for AI disease detection.
     * POST /api/scan/upload
     *
     * NOTE: This sends a multipart/form-data request with the image file.
     * The backend will forward the image to the external AI API.
     * Do NOT call this with fake/demo data — it must reach the real backend.
     *
     * @param {FormData} formData - FormData containing the 'image' file field
     * @returns {Promise<Object>} ScanResultDTO from backend
     */
    async uploadScan(formData) {
        // Do NOT set Content-Type manually — fetch sets it automatically
        // for FormData with the correct multipart boundary.
        return _fetch('/scan/upload', {
            method: 'POST',
            body: formData,
            headers: {}, // Override default JSON content-type
        });
    },

    // --------------------------------------------------------------------------
    // Scan History Endpoints (JWT required)
    // --------------------------------------------------------------------------

    /**
     * Get the current user's scan history (paginated, newest first).
     * GET /api/scan/history?page=0&size=10
     *
     * @param {number} page - Page number (0-indexed)
     * @param {number} size - Items per page
     * @returns {Promise<Object>} Paginated history response
     */
    async getScanHistory(page = 0, size = 10) {
        return _fetch(`/scan/history?page=${page}&size=${size}`, { method: 'GET' });
    },

    /**
     * Get a single scan history record by ID.
     * GET /api/scan/history/{id}
     *
     * @param {number} id - Scan history record ID
     * @returns {Promise<Object>} Full scan result with disease info and remedies
     */
    async getScanById(id) {
        return _fetch(`/scan/history/${id}`, { method: 'GET' });
    },

    /**
     * Delete a scan history record.
     * DELETE /api/scan/history/{id}
     *
     * @param {number} id - Scan history record ID
     * @returns {Promise<null>} 204 No Content on success
     */
    async deleteScan(id) {
        return _fetch(`/scan/history/${id}`, { method: 'DELETE' });
    },

    // --------------------------------------------------------------------------
    // Disease Library Endpoints (no JWT required for public access)
    // --------------------------------------------------------------------------

    /**
     * Get all diseases in the library.
     * GET /api/diseases
     *
     * @returns {Promise<Array>} Array of disease summary objects
     */
    async getDiseases() {
        return _fetch('/diseases', { method: 'GET' }, false);
    },

    /**
     * Get a single disease by ID with full detail and remedies.
     * GET /api/diseases/{id}
     *
     * @param {number} id - Disease ID
     * @returns {Promise<Object>} Full disease detail with remedies
     */
    async getDiseaseById(id) {
        return _fetch(`/diseases/${id}`, { method: 'GET' }, false);
    },

    // --------------------------------------------------------------------------
    // Dashboard Summary Endpoint (JWT required)
    // --------------------------------------------------------------------------

    /**
     * Get dashboard statistics for the current user.
     * GET /api/users/me/stats
     *
     * @returns {Promise<Object>} { totalScans, mostCommonDisease, lastScanDate, recentScans[] }
     */
    async getDashboardStats() {
        return _fetch('/users/me/stats', { method: 'GET' });
    },
};

// ============================================================================
// UI HELPER UTILITIES
// ============================================================================

/**
 * Displays a backend validation error on a form field element.
 *
 * @param {string} fieldId - The id of the input element
 * @param {string} message - Error message to display
 */
function showFieldError(fieldId, message) {
    const input = document.getElementById(fieldId);
    const errorEl = document.getElementById(`${fieldId}-error`);
    if (input) {
        input.classList.add('is-invalid');
        input.classList.remove('is-valid');
    }
    if (errorEl) {
        errorEl.textContent = message;
        errorEl.classList.remove('d-none');
    }
}

/**
 * Clears validation error from a form field.
 *
 * @param {string} fieldId - The id of the input element
 */
function clearFieldError(fieldId) {
    const input = document.getElementById(fieldId);
    const errorEl = document.getElementById(`${fieldId}-error`);
    if (input) {
        input.classList.remove('is-invalid');
    }
    if (errorEl) {
        errorEl.textContent = '';
        errorEl.classList.add('d-none');
    }
}

/**
 * Shows an API error in a named alert element.
 *
 * @param {string} alertId - ID of the alert element
 * @param {ApiError|Error} error - The error to display
 */
function showApiError(alertId, error) {
    const alertEl = document.getElementById(alertId);
    if (!alertEl) return;
    alertEl.textContent = error.message || 'An unexpected error occurred.';
    alertEl.classList.remove('d-none');
}

/**
 * Hides an alert element.
 *
 * @param {string} alertId - ID of the alert element to hide
 */
function hideAlert(alertId) {
    const alertEl = document.getElementById(alertId);
    if (alertEl) alertEl.classList.add('d-none');
}

// ============================================================================
// EXPORTS (available globally via window, or via ES module import)
// ============================================================================
// Since this project uses plain HTML without a bundler, expose everything on
// the window object so other scripts can access them via the global scope.

window.api = api;
window.ApiError = ApiError;
window.showFieldError = showFieldError;
window.clearFieldError = clearFieldError;
window.showApiError = showApiError;
window.hideAlert = hideAlert;
