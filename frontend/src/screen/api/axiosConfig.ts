import axios from 'axios';
import Cookies from 'js-cookie';

// Create axios instance with base configuration
const axiosInstance = axios.create({
  baseURL: 'http://localhost:8080/',
  headers: {
    'Content-Type': 'application/json'
  }
});

// Add auth token to requests
axiosInstance.interceptors.request.use(
  config => {
    const token = Cookies.get('auth_token');
    if (token) {
      config.headers = config.headers || {};
      config.headers['Authorization'] = `Bearer ${token}`;
    }
    return config;
  },
  error => {
    return Promise.reject(error);
  }
);

// Handle token refresh on 403 error
axiosInstance.interceptors.response.use(
  response => response,
  async error => {
    const originalRequest = error.config;

    if (error.response?.status === 403 && !originalRequest._retry) {
      originalRequest._retry = true;

      try {
        const token = Cookies.get('refresh_token');
        if (!token) {
          window.location.href = '/';
          return Promise.reject(error);
        }

        console.log('Tentativo di refresh del token...');

        const response: any = await axios.post('http://localhost:8080/auth/refresh', {
          token: token
        });

        console.log('Risposta refresh token:', response.data);

        if (response.data.accessToken) {
          Cookies.set('auth_token', response.data.accessToken, {expires: 1, secure: true, sameSite: "Strict"});

          if (response.data.refreshToken) {
            Cookies.set('refresh_token', response.data.refreshToken, {expires: 7, secure: true, sameSite: "Strict"});
          }

          // Retry original request with new token
          originalRequest.headers['Authorization'] = `Bearer ${response.data.accessToken}`;
          return axiosInstance(originalRequest);
        } else {
          throw new Error('Formato risposta refresh token non valido');
        }

      } catch (refreshError: any) {
        console.error('Errore durante il refresh del token:', refreshError);

        if (refreshError.response) {
          console.error('Risposta errore:', refreshError.response.data);
          console.error('Status errore:', refreshError.response.status);
        }

        Cookies.remove('auth_token');
        Cookies.remove('refresh_token');
        window.location.href = '/';
        return Promise.reject(refreshError);
      }
    }

    return Promise.reject(error);
  }
);

export default axiosInstance;