/**
 * FBCS Application Core Script
 * Coordinates basic UI interactions and alerts.
 */

document.addEventListener('DOMContentLoaded', () => {
    console.log('FBCS Client Loaded.');
    initAlertDismissals();
});

/**
 * Renders an alert box with customizable themes (success, danger)
 */
function showAlert(message, type = 'success') {
    const alertBox = document.getElementById('alert-box');
    if (!alertBox) return;

    alertBox.className = `alert alert-${type}`;
    alertBox.innerHTML = `
        <span>${type === 'success' ? '✓' : '⚠'}</span>
        <div>${message}</div>
    `;
    alertBox.style.display = 'flex';
    
    // Smooth scroll to alert box if needed
    alertBox.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function hideAlert() {
    const alertBox = document.getElementById('alert-box');
    if (alertBox) {
        alertBox.style.display = 'none';
    }
}

function initAlertDismissals() {
    // Basic automatic hide function for alert boxes if user clicks inside them
    const alertBox = document.getElementById('alert-box');
    if (alertBox) {
        alertBox.addEventListener('click', hideAlert);
    }
}
