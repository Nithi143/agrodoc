/**
 * AgroDoc — landing.js
 * Client-side interactions for the landing/home page.
 */

document.addEventListener('DOMContentLoaded', () => {
    // If user is already authenticated, redirect them to their dashboard
    if (typeof isLoggedIn === 'function' && isLoggedIn()) {
        window.location.replace('dashboard.html');
        return;
    }

    // Initialize Lucide icons
    if (window.lucide && typeof lucide.createIcons === 'function') {
        lucide.createIcons();
    }

    // Mobile Navbar Hamburger Toggle
    const hamburgerBtn = document.getElementById('nav-hamburger');
    const navLinks = document.getElementById('nav-links-list');

    if (hamburgerBtn && navLinks) {
        hamburgerBtn.addEventListener('click', () => {
            const isOpen = navLinks.classList.toggle('open');
            hamburgerBtn.setAttribute('aria-expanded', String(isOpen));
        });

        // Close mobile menu when a nav link is clicked
        navLinks.querySelectorAll('.nav-link').forEach(link => {
            link.addEventListener('click', () => {
                navLinks.classList.remove('open');
                hamburgerBtn.setAttribute('aria-expanded', 'false');
            });
        });
    }

    // Dynamic navbar shadow on scroll
    const mainNav = document.getElementById('main-nav');
    if (mainNav) {
        window.addEventListener('scroll', () => {
            if (window.scrollY > 20) {
                mainNav.classList.add('scrolled');
            } else {
                mainNav.classList.remove('scrolled');
            }
        }, { passive: true });
    }
});
