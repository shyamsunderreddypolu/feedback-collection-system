import axios from 'axios';

// Dynamically use REACT_APP_API_BASE_URL in production (e.g. Railway URL), fallback to local backend
const baseURL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

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
