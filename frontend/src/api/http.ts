const BASE_URL = import.meta.env.VITE_API_BASE_URL;

interface RequestConfig extends RequestInit {
    data?: any;
}

export async function request<T>(endpoint: string, { data, ...customConfig }: RequestConfig = {}) {
    const config: RequestInit = {
        method: data ? 'POST' : 'GET',
        headers: {
            'Content-Type': 'application/json',
        },
        ...customConfig,
    };

    if (data) {
        config.body = JSON.stringify(data);
    }

    // JWT 토큰이 있다면 헤더에 추가
    const token = localStorage.getItem('token');
    if (token) {
        config.headers = {
            ...config.headers,
            Authorization: `Bearer ${token}`,
        };
    }

    try {
        const response = await fetch(`${BASE_URL}${endpoint}`, config);
        const data = await response.json();

        if (response.ok) {
            return data;
        }
        throw new Error(data.message || '요청 처리 중 오류가 발생했습니다.');
    } catch (error) {
        return Promise.reject(error);
    }
}

// HTTP 메서드별 헬퍼 함수들
export const http = {
    get: <T>(endpoint: string, config?: RequestConfig) => 
        request<T>(endpoint, { ...config, method: 'GET' }),
    
    post: <T>(endpoint: string, data?: any, config?: RequestConfig) =>
        request<T>(endpoint, { ...config, method: 'POST', data }),
    
    put: <T>(endpoint: string, data?: any, config?: RequestConfig) =>
        request<T>(endpoint, { ...config, method: 'PUT', data }),
    
    patch: <T>(endpoint: string, data?: any, config?: RequestConfig) =>
        request<T>(endpoint, { ...config, method: 'PATCH', data }),
    
    delete: <T>(endpoint: string, config?: RequestConfig) =>
        request<T>(endpoint, { ...config, method: 'DELETE' }),
};
