import { useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../api/http";
import LoginForm from "../components/forms/LoginForm";
import { UserLoginDTO } from "../types/user";
import { UserResponseDTO } from "../types/user";

const Login = () => {
    const [formData, setFormData] = useState<UserLoginDTO>({
        username: '',
        password: ''
    });
    const navigate = useNavigate();

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleLogin = async () => {
        try {
            const response = await http.post<UserResponseDTO>('/user/login', formData);
            sessionStorage.setItem('accessToken', response.data.accessToken);
            sessionStorage.setItem('user', JSON.stringify({
                id: response.data.id,
                name: response.data.name,
                username: response.data.username,
                email: response.data.email
            }));
            alert(response.data.message);
            navigate('/');
        } catch (error) {
            console.error(error);
            alert('로그인 실패!');
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-100 to-purple-100 py-12 px-4 sm:px-6 lg:px-8 flex items-center justify-center">
            <div className="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden transform hover:scale-105 transition duration-300">
                <div className="px-8 py-12">
                    <h2 className="text-4xl font-extrabold text-center text-gray-800 mb-8">
                        <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">
                            로그인
                        </span>
                    </h2>
                    <LoginForm 
                        formData={formData}
                        onSubmit={handleLogin}
                        onChange={handleChange}
                    />
                </div>
            </div>
        </div>
    );
};

export default Login;
