/**
 * AgroDoc — scan.js
 * Frontend logic for leaf photograph upload, validation, and AI diagnosis submission.
 */

document.addEventListener('DOMContentLoaded', () => {
    // 1. Guard route: Requires authenticated user
    if (typeof requireAuth === 'function') {
        requireAuth();
    }
    if (typeof hydrateNavbar === 'function') {
        hydrateNavbar();
    }
    if (window.lucide && typeof lucide.createIcons === 'function') {
        lucide.createIcons();
    }

    // Bind logout in footer
    const footerLogout = document.getElementById('footer-btn-logout');
    if (footerLogout && typeof logout === 'function') {
        footerLogout.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    }

    // DOM Elements
    const dropzone = document.getElementById('dropzone');
    const fileInput = document.getElementById('leaf-image-input');
    const browseBtn = document.getElementById('btn-browse-file');
    const previewArea = document.getElementById('preview-area');
    const previewImg = document.getElementById('image-preview');
    const previewName = document.getElementById('preview-filename');
    const previewSize = document.getElementById('preview-filesize');
    const removeBtn = document.getElementById('btn-remove-image');
    const startBtn = document.getElementById('btn-start-analysis');
    const errorAlert = document.getElementById('scan-error-alert');

    let currentFile = null;
    let currentPreviewUrl = null;

    // Allowed constraints
    const MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB
    const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/webp'];

    function showError(message) {
        if (!errorAlert) return;
        errorAlert.textContent = message;
        errorAlert.classList.remove('d-none');
        errorAlert.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
    }

    function hideError() {
        if (!errorAlert) return;
        errorAlert.textContent = '';
        errorAlert.classList.add('d-none');
    }

    function validateFile(file) {
        if (!file) {
            showError('Please select a photograph of a tomato leaf.');
            return false;
        }

        if (file.size > MAX_FILE_SIZE) {
            showError(`The selected file (${(file.size / (1024 * 1024)).toFixed(2)} MB) exceeds the 5 MB limit. Please select a smaller photo.`);
            return false;
        }

        const fileType = file.type.toLowerCase();
        const fileName = file.name.toLowerCase();
        const hasValidExt = fileName.endsWith('.jpg') || fileName.endsWith('.jpeg') || fileName.endsWith('.png') || fileName.endsWith('.webp');

        if (!ALLOWED_TYPES.includes(fileType) && !hasValidExt) {
            showError('Invalid file type. Please upload a clear photo in JPEG, PNG, or WebP format.');
            return false;
        }

        hideError();
        return true;
    }

    function handleFile(file) {
        if (!validateFile(file)) {
            clearSelection();
            return;
        }

        currentFile = file;
        previewName.textContent = file.name;
        previewSize.textContent = `${(file.size / 1024).toFixed(1)} KB`;

        const reader = new FileReader();
        reader.onload = (e) => {
            currentPreviewUrl = e.target.result;
            previewImg.src = currentPreviewUrl;
            previewArea.classList.remove('d-none');
            dropzone.classList.add('d-none');
            if (window.lucide) lucide.createIcons();
        };
        reader.onerror = () => {
            showError('Failed to read the selected file. Please try selecting it again.');
            clearSelection();
        };
        reader.readAsDataURL(file);
    }

    function clearSelection() {
        currentFile = null;
        currentPreviewUrl = null;
        if (fileInput) fileInput.value = '';
        if (previewImg) previewImg.src = '#';
        if (previewArea) previewArea.classList.add('d-none');
        if (dropzone) dropzone.classList.remove('d-none');
    }

    // Dropzone Click
    if (dropzone && fileInput) {
        dropzone.addEventListener('click', () => fileInput.click());
    }

    if (browseBtn && fileInput) {
        browseBtn.addEventListener('click', (e) => {
            e.stopPropagation();
            fileInput.click();
        });
    }

    // File Input Change
    if (fileInput) {
        fileInput.addEventListener('change', () => {
            if (fileInput.files && fileInput.files.length > 0) {
                handleFile(fileInput.files[0]);
            }
        });
    }

    // Drag and Drop Events
    if (dropzone) {
        ['dragenter', 'dragover'].forEach(eventName => {
            dropzone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                dropzone.style.borderColor = 'var(--color-primary)';
                dropzone.style.backgroundColor = 'var(--color-primary-subtle)';
            }, false);
        });

        ['dragleave', 'drop'].forEach(eventName => {
            dropzone.addEventListener(eventName, (e) => {
                e.preventDefault();
                e.stopPropagation();
                dropzone.style.borderColor = 'var(--color-border-strong)';
                dropzone.style.backgroundColor = 'var(--color-surface-subtle)';
            }, false);
        });

        dropzone.addEventListener('drop', (e) => {
            const dt = e.dataTransfer;
            if (dt && dt.files && dt.files.length > 0) {
                handleFile(dt.files[0]);
            }
        });
    }

    // Remove Button
    if (removeBtn) {
        removeBtn.addEventListener('click', () => {
            clearSelection();
            hideError();
        });
    }

    // Submit Analysis to Backend
    if (startBtn) {
        startBtn.addEventListener('click', async () => {
            if (!currentFile) {
                showError('Please select or drop an image file first.');
                return;
            }

            hideError();

            // Set loading state
            const originalBtnHtml = startBtn.innerHTML;
            startBtn.disabled = true;
            if (removeBtn) removeBtn.disabled = true;
            startBtn.innerHTML = `
                <span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>
                <span>Analyzing Leaf Symptoms with AI…</span>
            `;

            try {
                const formData = new FormData();
                formData.append('image', currentFile);

                // Call backend REST endpoint POST /api/scan/upload
                const result = await api.uploadScan(formData);

                // Cache in sessionStorage for immediate fast rendering on result.html
                sessionStorage.setItem('agrodoc_current_scan', JSON.stringify(result));
                if (currentPreviewUrl) {
                    sessionStorage.setItem('agrodoc_current_preview', currentPreviewUrl);
                }

                // Redirect to result page with the scan ID
                window.location.href = `result.html?id=${result.scanId}`;

            } catch (err) {
                console.error('[scan.js] Diagnostic upload failed:', err);

                // Check for 503 Service Unavailable (API key unconfigured)
                if (err.status === 503) {
                    showError(
                        'AI Diagnostic Service Unavailable (HTTP 503): ' +
                        (err.message || 'The Plant.id AI API key is not configured on the server. Please configure PLANT_API_KEY.')
                    );
                } else if (err.status === 413) {
                    showError('File upload error: The image exceeds the 5 MB server limit.');
                } else {
                    showError(err.message || 'An unexpected error occurred while analyzing the leaf image. Please try again.');
                }

                // Reset button state
                startBtn.disabled = false;
                if (removeBtn) removeBtn.disabled = false;
                startBtn.innerHTML = originalBtnHtml;
                if (window.lucide) lucide.createIcons();
            }
        });
    }
});
