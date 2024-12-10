import axios from 'axios';
import { jwtDecode } from 'jwt-decode';

interface JwtPayload {
    exp: number;
    userId: number;
}

export const http = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL
});

const isTokenExpired = (token: string) => {
    try {
        const decoded = jwtDecode<JwtPayload>(token);
        // 만료 10분 전부터는 만료된 것으로 간주
        return decoded.exp * 1000 < Date.now() + (10 * 60 * 1000);
    } catch {
        return true;
    }
};

export const handleLogout = async () => {
    try {
        const accessToken = sessionStorage.getItem('accessToken');
        if (accessToken) {
            await http.post('/user/logout', null, {
                headers: { Authorization: `Bearer ${accessToken}` }
            });
        }
    } catch (error) {
        console.error('로그아웃 실패:', error);
    } finally {
        sessionStorage.removeItem('accessToken');
        sessionStorage.removeItem('user');
        window.dispatchEvent(new Event('storage'));
        window.location.href = '/';
    }
};

http.interceptors.request.use(
    async (config) => {
        const accessToken = sessionStorage.getItem('accessToken');
        
        if (accessToken && isTokenExpired(accessToken)) {
            try {
                const response = await axios.post(
                    `${import.meta.env.VITE_API_BASE_URL}/user/refresh`,
                    null,
                    { withCredentials: true }
                );
                const newAccessToken = response.data.accessToken;
                sessionStorage.setItem('accessToken', newAccessToken);
                config.headers.Authorization = `Bearer ${newAccessToken}`;
            } catch {
                handleLogout();
                return Promise.reject('토큰 갱신 실패');
            }
        } else if (accessToken) {
            config.headers.Authorization = `Bearer ${accessToken}`;
        }
        
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

http.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response?.status === 401 || error.response?.status === 403) {
            handleLogout();
        }
        return Promise.reject(error);
    }
);

export default http;
