import { useState } from "react";
import http from "../api/http";
import { useNavigate } from "react-router-dom";

const SignIn = () => {
    interface LoginForm {
        username: string;
        password: string;
    }
    const [formData, setFormData] = useState<LoginForm>({
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
            const response = await http.post('/user/login', formData);
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
    }

    const inputFields = [
        { name: 'username', type: 'text', placeholder: '아이디를 입력하세요' },
        { name: 'password', type: 'password', placeholder: '비밀번호를 입력하세요' }
    ];

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-100 to-purple-100 py-12 px-4 sm:px-6 lg:px-8 flex items-center justify-center">
            <div className="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden transform hover:scale-105 transition duration-300">
                <div className="px-8 py-12">
                    <h2 className="text-4xl font-extrabold text-center text-gray-800 mb-8">
                        <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">
                            로그인
                        </span>
                    </h2>
                    <div className="space-y-6">
                        {inputFields.map((field) => (
                            <div key={field.name} className="space-y-2">
                                <input
                                    type={field.type}
                                    name={field.name}
                                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-transparent transition duration-200"
                                    placeholder={field.placeholder}
                                    value={formData[field.name as keyof typeof formData]}
                                    onChange={handleChange}
                                />
                            </div>
                        ))}
                        <button
                            type="submit"
                            className="w-full py-4 px-4 mt-8 bg-gradient-to-r from-blue-500 to-purple-500 text-white text-lg font-bold rounded-lg shadow-md hover:from-blue-600 hover:to-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transform hover:-translate-y-1 transition duration-200"
                            onClick={handleLogin}
                        >
                            로그인하기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SignIn;
