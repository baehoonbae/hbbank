import { useState } from "react";
import { http } from "../api/http";
import { useNavigate } from "react-router-dom";

const SignIn = () => {
    const [userId, setUserId] = useState('');
    const [password, setPassword] = useState('');
    const navigate = useNavigate();

    const handleLogin = async () => {
        try {
            console.log(userId, password);
            const response = await http.post('/user/login', {
                userId,
                password,
            });
            sessionStorage.setItem('accessToken', response.data.token);
            sessionStorage.setItem('user', JSON.stringify(response.data));
            navigate('/');
        } catch (error) {
            console.error(error);
        }
    }

    return (
        <div className="flex flex-col items-center justify-center min-h-screen">
            <div className="w-96 p-8 bg-white rounded-lg shadow-md">
                <h2 className="text-2xl font-bold mb-6 text-center">로그인</h2>
                <div>
                    <label className="block text-sm font-medium text-gray-700">아이디</label>
                    <input
                        type="text"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        placeholder="아이디를 입력하세요"
                        value={userId}
                        onChange={(e) => setUserId(e.target.value)}
                    />
                </div>
                <div>
                    <label className="block text-sm font-medium text-gray-700">비밀번호</label>
                    <input
                        type="password"
                        className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                        placeholder="비밀번호를 입력하세요"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                    />
                </div>
                <button
                    type="submit"
                    className="w-full bg-blue-500 text-white py-2 px-4 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    onClick={handleLogin}
                >
                    로그인
                </button>
            </div>
        </div>
    );
};

export default SignIn;
