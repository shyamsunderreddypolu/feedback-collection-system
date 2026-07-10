/**
 * FBCS Authentication client handler.
 * Interfaces with /api/auth endpoints, manages JWT in localStorage, and routes users based on roles.
 */

const AUTH_URL = '/api/auth';

document.addEventListener('DOMContentLoaded', () => {
    checkTokenState();

    // Login Form Submit Listener
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    // Register Form Submit Listener
    const registerForm = document.getElementById('register-form');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }

    // Logout Link Listener
    const logoutLink = document.getElementById('logout-link');
    if (logoutLink) {
        logoutLink.addEventListener('click', handleLogout);
    }
});

/**
 * Handle user Login
 */
async function handleLogin(e) {
    e.preventDefault();
    hideAlert();

    const email = document.getElementById('email').value;
    const password = document.getElementById('password').value;

    try {
        const response = await fetch(`${AUTH_URL}/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ email, password })
        });

        const data = await response.json();

        if (response.ok) {
            // Save authentication details in localStorage
            localStorage.setItem('fbcs_token', data.token);
            localStorage.setItem('fbcs_role', data.role);
            localStorage.setItem('fbcs_name', data.name);

            showAlert('Authentication successful! Redirecting...', 'success');
            
            // Redirect based on user role
            setTimeout(() => {
                if (data.role === 'ROLE_ADMIN') {
                    window.location.href = 'index.html'; // Modify later if admin dashboard is separate
                } else {
                    window.location.href = 'index.html';
                }
            }, 1000);
        } else {
            showAlert(data.message || 'Login failed. Please check your credentials.', 'danger');
        }
    } catch (err) {
        console.error('Error logging in:', err);
        showAlert('Could not connect to authentication server. Please ensure the backend is running.', 'danger');
    }
}

/**
 * Handle user Registration
 */
async function handleRegister(e) {
    e.preventDefault();
    hideAlert();

    const name = document.getElementById('name').value;
    const email = document.getElementById('email').value;
    const subgroup = document.getElementById('subgroup').value;
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirm-password').value;

    // Client-side passwords match validation
    if (password !== confirmPassword) {
        showAlert('Passwords do not match.', 'danger');
        return;
    }

    try {
        const response = await fetch(`${AUTH_URL}/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ name, email, subgroup, password })
        });

        const data = await response.json();

        if (response.status === 201 || response.ok) {
            showAlert('Registration successful! Redirecting to login page...', 'success');
            setTimeout(() => {
                window.location.href = 'login.html';
            }, 1500);
        } else {
            showAlert(data.message || 'Registration failed. Email might be already in use.', 'danger');
        }
    } catch (err) {
        console.error('Error registering:', err);
        showAlert('Could not connect to registration server.', 'danger');
    }
}

/**
 * Check if user is logged in, and update header links dynamically
 */
function checkTokenState() {
    const token = localStorage.getItem('fbcs_token');
    const name = localStorage.getItem('fbcs_name');

    const loginLink = document.getElementById('login-link');
    const registerLink = document.getElementById('register-link');
    const logoutLink = document.getElementById('logout-link');
    const heroButtons = document.getElementById('hero-buttons');

    if (token) {
        // Toggle header links
        if (loginLink) loginLink.style.display = 'none';
        if (registerLink) registerLink.style.display = 'none';
        if (logoutLink) {
            logoutLink.style.display = 'inline';
            logoutLink.innerText = `Sign Out (${name || 'User'})`;
        }

        // Toggle CTA button inside index.html hero section
        if (heroButtons) {
            heroButtons.innerHTML = `
                <a href="#" class="btn btn-primary" onclick="alert('Successfully authenticated! Active surveys list will load here once the REST service is built.'); return false;">Go to Dashboard</a>
            `;
        }
    }
}

/**
 * Clear cache and redirect
 */
function handleLogout(e) {
    if (e) e.preventDefault();
    localStorage.removeItem('fbcs_token');
    localStorage.removeItem('fbcs_role');
    localStorage.removeItem('fbcs_name');
    window.location.href = 'index.html';
}
