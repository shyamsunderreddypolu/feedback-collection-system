import axios from 'axios';

// Default to the live Railway Cloud backend URL, or use environment variable if provided
const baseURL = process.env.REACT_APP_API_BASE_URL || 'https://feedback-collection-system-production.up.railway.app/api';

const API = axios.create({
  baseURL: baseURL,
  headers: {
    'Content-Type': 'application/json',
  },
});

API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

export default API;
