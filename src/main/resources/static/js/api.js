/**
 * FBCS API client helper.
 * Provides abstract wrapper methods for sending authenticated GET/POST requests to REST endpoints.
 */

const API_BASE = '/api';

/**
 * Fetch helper that automatically appends the cached JWT Bearer token.
 * Validates status code and handles global token expiration (401 triggers logout).
 */
async function apiCall(endpoint, method = 'GET', body = null) {
    const token = localStorage.getItem('fbcs_token');
    
    const headers = {
        'Content-Type': 'application/json'
    };
    
    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const config = {
        method: method,
        headers: headers
    };

    if (body && (method === 'POST' || method === 'PUT')) {
        config.body = JSON.stringify(body);
    }

    try {
        const response = await fetch(`${API_BASE}${endpoint}`, config);
        
        // Handle token expiration globally
        if (response.status === 401) {
            console.warn('Session expired or unauthorized. Logging out.');
            localStorage.removeItem('fbcs_token');
            localStorage.removeItem('fbcs_role');
            localStorage.removeItem('fbcs_name');
            window.location.href = 'login.html?session=expired';
            return null;
        }

        // Handle empty contents
        if (response.status === 204) {
            return { status: 'success', data: null };
        }

        return await response.json();
    } catch (err) {
        console.error(`API Call failed on ${endpoint}:`, err);
        throw err;
    }
}

/**
 * Endpoint queries mapping
 */
const api = {
    // Users Profile
    getUserProfile: () => apiCall('/users/profile'),
    
    // Surveys Actions
    getActiveSurveys: () => apiCall('/surveys/active'),
    getSurveyDetails: (id) => apiCall(`/surveys/${id}`),
    createSurvey: (surveyData) => apiCall('/surveys', 'POST', surveyData),
    
    // Responses Submissions
    submitResponse: (responseData) => apiCall('/responses', 'POST', responseData),
    
    // Analytics & Dashboard data
    getSurveyAnalytics: (surveyId) => apiCall(`/analytics/survey/${surveyId}`),
    
    // Notifications Alerts
    getNotifications: () => apiCall('/notifications'),
    markNotificationAsRead: (id) => apiCall(`/notifications/${id}/read`, 'PUT')
};
