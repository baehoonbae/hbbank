import axios from 'axios';

const baseURL = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/hbbank/api';

const http = axios.create({
    baseURL: baseURL,
    withCredentials: true,
});

export default http;
