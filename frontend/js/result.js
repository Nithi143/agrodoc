/**
 * AgroDoc — result.js
 * Frontend logic for rendering disease diagnostic results, confidence meters,
 * detailed disease information, and categorized organic/chemical remedies.
 */

document.addEventListener('DOMContentLoaded', async () => {
    // Guard route: Requires authenticated user
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

    // Parse Scan ID from URL query parameters
    const urlParams = new URLSearchParams(window.location.search);
    const scanIdParam = urlParams.get('id');

    let scanResult = null;

    // Check if we have cached results in sessionStorage from a recent scan
    const cachedScanRaw = sessionStorage.getItem('agrodoc_current_scan');
    const cachedPreview = sessionStorage.getItem('agrodoc_current_preview');

    if (cachedScanRaw) {
        try {
            const parsed = JSON.parse(cachedScanRaw);
            if (!scanIdParam || String(parsed.scanId) === String(scanIdParam)) {
                scanResult = parsed;
            }
        } catch (e) {
            console.warn('[result.js] Failed to parse cached scan:', e);
        }
    }

    // If not found in cache and scanId is provided in URL, fetch from API
    if (!scanResult && scanIdParam) {
        try {
            scanResult = await api.getScanById(scanIdParam);
        } catch (err) {
            console.error('[result.js] Failed to fetch scan by ID:', err);
            showErrorState('Unable to load scan diagnosis. The requested scan may not exist or belongs to another user.');
            return;
        }
    }

    if (!scanResult) {
        showErrorState('No scan diagnostic data found. Please upload a tomato leaf to run an analysis.');
        return;
    }

    renderScanResult(scanResult, cachedPreview);
});

/**
 * Render the full scan result DTO into the DOM.
 */
function renderScanResult(data, cachedPreview) {
    const isHealthy = Boolean(data.isHealthy);
    const confidence = typeof data.confidenceScore === 'number' ? data.confidenceScore : 0.0;
    const disease = data.diseaseDetail;

    // 1. Analyzed Photograph
    const imgPlaceholder = document.getElementById('image-placeholder');
    const imgCaption = document.getElementById('result-image-caption');
    const imgContainer = document.getElementById('image-container');

    if (imgContainer) {
        const imageSrc = (cachedPreview && (!data.scanId || String(data.scanId) === String(new URLSearchParams(window.location.search).get('id'))))
            ? cachedPreview
            : (data.imageFilename ? `http://localhost:8080/api/scan/images/${encodeURIComponent(data.imageFilename)}` : null);

        if (imageSrc) {
            imgContainer.innerHTML = `
                <img src="${imageSrc}" alt="Scanned tomato leaf" class="img-fluid rounded" style="max-height: 280px; object-fit: contain;">
            `;
        }
    }

    if (imgCaption && data.imageFilename) {
        imgCaption.textContent = data.imageFilename;
    }

    // 2. Health Status Badge
    const healthBadge = document.getElementById('badge-health-status');
    if (healthBadge) {
        if (isHealthy) {
            healthBadge.className = 'badge badge-success';
            healthBadge.textContent = 'Healthy Foliage';
        } else {
            healthBadge.className = 'badge badge-danger';
            healthBadge.textContent = 'Diseased';
        }
    }

    // 3. Confidence Meter
    const confidencePercentEl = document.getElementById('confidence-percentage');
    const confidenceBarEl = document.getElementById('confidence-bar');
    if (confidencePercentEl) {
        confidencePercentEl.textContent = `${confidence.toFixed(1)}%`;
    }
    if (confidenceBarEl) {
        const clampedConfidence = Math.min(100, Math.max(0, confidence));
        confidenceBarEl.style.width = `${clampedConfidence}%`;
        if (isHealthy) {
            confidenceBarEl.style.backgroundColor = 'var(--color-success)';
        } else if (clampedConfidence < 70) {
            confidenceBarEl.style.backgroundColor = 'var(--color-warning)';
        } else {
            confidenceBarEl.style.backgroundColor = 'var(--color-danger)';
        }
    }

    // 4. Scan Timestamp
    const scanDateEl = document.getElementById('result-scan-date');
    if (scanDateEl && data.scannedAt) {
        try {
            const dateObj = new Date(data.scannedAt);
            scanDateEl.textContent = dateObj.toLocaleDateString(undefined, {
                year: 'numeric',
                month: 'short',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
        } catch (e) {
            scanDateEl.textContent = data.scannedAt;
        }
    }

    // 5. Disease Identification Titles
    const diseaseNameEl = document.getElementById('result-disease-name');
    const scientificNameEl = document.getElementById('result-scientific-name');
    const severityBadgeEl = document.getElementById('result-severity-badge');

    if (diseaseNameEl) {
        if (isHealthy) {
            diseaseNameEl.textContent = 'Healthy Tomato Plant';
        } else if (disease && disease.diseaseName) {
            diseaseNameEl.textContent = disease.diseaseName;
        } else {
            diseaseNameEl.textContent = data.diseaseLabel || 'Unidentified Condition';
        }
    }

    if (scientificNameEl) {
        if (isHealthy) {
            scientificNameEl.textContent = 'Solanum lycopersicum';
        } else if (disease && disease.scientificName) {
            scientificNameEl.textContent = disease.scientificName;
        } else {
            scientificNameEl.textContent = 'Foliage Pathology';
        }
    }

    if (severityBadgeEl) {
        if (isHealthy) {
            severityBadgeEl.className = 'badge badge-success';
            severityBadgeEl.textContent = 'No Severity';
        } else if (disease && disease.severityLevel) {
            const level = disease.severityLevel.toUpperCase();
            if (level === 'HIGH') {
                severityBadgeEl.className = 'badge badge-severity-high';
                severityBadgeEl.textContent = 'High Severity';
            } else if (level === 'LOW') {
                severityBadgeEl.className = 'badge badge-severity-low';
                severityBadgeEl.textContent = 'Low Severity';
            } else {
                severityBadgeEl.className = 'badge badge-severity-med';
                severityBadgeEl.textContent = 'Medium Severity';
            }
        } else {
            severityBadgeEl.className = 'badge badge-neutral';
            severityBadgeEl.textContent = 'Advisory';
        }
    }

    // 6. Description
    const descriptionEl = document.getElementById('result-description');
    if (descriptionEl) {
        if (isHealthy) {
            descriptionEl.textContent = 'The uploaded leaf shows no diagnostic signs of known tomato foliage pathogens. Leaves appear vigorous with healthy cellular development and uniform pigmentation.';
        } else if (disease && disease.description) {
            descriptionEl.textContent = disease.description;
        } else {
            descriptionEl.textContent = `The AI classifier detected symptoms consistent with ${data.diseaseLabel}. Consult an agronomist or the treatment library for detailed management.`;
        }
    }

    // 7. Symptoms List
    const symptomsListEl = document.getElementById('result-symptoms-list');
    if (symptomsListEl) {
        symptomsListEl.innerHTML = '';
        if (isHealthy) {
            symptomsListEl.innerHTML = `
                <li>• Uniform green chlorophyll distribution across leaf margins.</li>
                <li>• No necrotic lesions, target spots, or fungal powder evident.</li>
                <li>• Healthy venation and leaf texture.</li>
            `;
        } else if (disease && disease.symptoms) {
            const lines = disease.symptoms.split(/\r?\n/).filter(line => line.trim().length > 0);
            lines.forEach(line => {
                const li = document.createElement('li');
                const clean = line.replace(/^[•\-\*]\s*/, '').trim();
                li.textContent = `• ${clean}`;
                symptomsListEl.appendChild(li);
            });
        } else {
            symptomsListEl.innerHTML = `<li>• Manifestations characteristic of ${data.diseaseLabel || 'detected pathogen'}.</li>`;
        }
    }

    // 8. Remedies (Organic & Chemical Tabs)
    const organicContainer = document.getElementById('organic-remedies');
    const chemicalContainer = document.getElementById('chemical-remedies');

    const remedies = (disease && Array.isArray(disease.remedies)) ? disease.remedies : [];
    const organicRemedies = remedies.filter(r => (r.remedyType || '').toUpperCase() === 'ORGANIC');
    const chemicalRemedies = remedies.filter(r => (r.remedyType || '').toUpperCase() === 'CHEMICAL');

    if (organicContainer) {
        if (isHealthy) {
            organicContainer.innerHTML = `
                <div class="p-3 bg-subtle rounded border">
                    <strong class="text-sm text-primary d-block mb-1">Preventive Plant Care</strong>
                    <p class="text-xs text-muted mb-0">Continue organic compost additions and maintain microbial soil health with regular organic mulch.</p>
                </div>
            `;
        } else if (organicRemedies.length > 0) {
            organicContainer.innerHTML = `
                <div class="d-flex flex-column gap-3">
                    ${organicRemedies.map(r => `
                        <div class="p-3 bg-subtle rounded border">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <strong class="text-sm text-primary">${escapeHtml(r.remedyName)}</strong>
                                <span class="badge badge-success">Eco-Approved</span>
                            </div>
                            <p class="text-xs text-muted mb-2"><strong>Application:</strong> ${escapeHtml(r.application)}</p>
                            ${r.notes ? `<p class="text-xs text-secondary mb-0"><strong>Notes:</strong> ${escapeHtml(r.notes)}</p>` : ''}
                        </div>
                    `).join('')}
                </div>
            `;
        } else {
            organicContainer.innerHTML = `
                <div class="p-3 bg-subtle rounded border text-muted text-xs">
                    No specific organic remedies listed for this diagnosis in the database.
                </div>
            `;
        }
    }

    if (chemicalContainer) {
        if (isHealthy) {
            chemicalContainer.innerHTML = `
                <div class="p-3 bg-subtle rounded border text-muted text-xs">
                    No synthetic chemical fungicide or pesticide intervention is warranted for healthy tomato crops.
                </div>
            `;
        } else if (chemicalRemedies.length > 0) {
            chemicalContainer.innerHTML = `
                <div class="d-flex flex-column gap-3">
                    <div class="alert alert-warning py-2 mb-2" role="alert">
                        <i data-lucide="alert-triangle" aria-hidden="true" style="flex-shrink: 0;"></i>
                        <span class="text-xs">Follow safety intervals (PHI), wear personal protective equipment, and adhere to local agricultural chemical guidelines.</span>
                    </div>
                    ${chemicalRemedies.map(r => `
                        <div class="p-3 bg-subtle rounded border">
                            <div class="d-flex justify-content-between align-items-center mb-1">
                                <strong class="text-sm text-primary">${escapeHtml(r.remedyName)}</strong>
                                <span class="badge badge-warning">Synthetic</span>
                            </div>
                            <p class="text-xs text-muted mb-2"><strong>Application:</strong> ${escapeHtml(r.application)}</p>
                            ${r.notes ? `<p class="text-xs text-secondary mb-0"><strong>Notes:</strong> ${escapeHtml(r.notes)}</p>` : ''}
                        </div>
                    `).join('')}
                </div>
            `;
        } else {
            chemicalContainer.innerHTML = `
                <div class="p-3 bg-subtle rounded border text-muted text-xs">
                    No synthetic chemical remedies currently registered for this diagnosis.
                </div>
            `;
        }
    }

    // 9. Cultural Prevention Tips
    const preventionSection = document.getElementById('cultural-prevention-tips-list');
    if (preventionSection) {
        preventionSection.innerHTML = '';
        if (disease && disease.preventionTips) {
            const tips = disease.preventionTips.split(/\r?\n/).filter(t => t.trim().length > 0);
            tips.forEach(tip => {
                const li = document.createElement('li');
                li.textContent = `• ${tip.replace(/^[•\-\*]\s*/, '').trim()}`;
                preventionSection.appendChild(li);
            });
        } else {
            preventionSection.innerHTML = `
                <li>• Drip irrigate at the base; avoid wetting tomato foliage during irrigation.</li>
                <li>• Apply organic mulch around plant base to prevent soil splash.</li>
                <li>• Prune lower foliage up to 12 inches off the ground to optimize ventilation.</li>
                <li>• Practice a minimum 3-year crop rotation schedule away from Solanaceae crops.</li>
            `;
        }
    }

    if (window.lucide && typeof lucide.createIcons === 'function') {
        lucide.createIcons();
    }
}

function showErrorState(message) {
    const main = document.getElementById('main');
    if (!main) return;
    main.innerHTML = `
        <div class="container py-5 text-center">
            <div class="card p-5 mx-auto" style="max-width: 600px;">
                <div class="mb-3" style="font-size: 3rem;">⚠️</div>
                <h2 class="text-primary mb-2">Scan Data Unavailable</h2>
                <p class="text-muted mb-4">${escapeHtml(message)}</p>
                <div class="d-flex justify-content-center gap-3">
                    <a href="scan.html" class="btn btn-primary">
                        <i data-lucide="camera" aria-hidden="true"></i>
                        New Scan
                    </a>
                    <a href="history.html" class="btn btn-outline">
                        <i data-lucide="history" aria-hidden="true"></i>
                        View Scan History
                    </a>
                </div>
            </div>
        </div>
    `;
    if (window.lucide && typeof lucide.createIcons === 'function') {
        lucide.createIcons();
    }
}

function escapeHtml(str) {
    if (!str) return '';
    return String(str)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}
