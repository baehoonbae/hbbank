import { useState } from "react";
import http from "../api/http";
import { useNavigate } from "react-router-dom";

const SignUp = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState({
        name: '',
        birthDate: '',
        username: '',
        password: '',
        address: '',
        phone: '',
        email: ''
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setUser(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleRegist = async () => {
        try {
            const response = await http.post('/user/regist', user);
            alert(response.data.message);
            navigate('/');
        } catch (error) {
            console.error(error);
            alert('회원가입에 실패하였습니다.');
        }
    };

    const inputFields = [
        { name: 'name', type: 'text', placeholder: '이름을 입력하세요' },
        { name: 'birthDate', type: 'date' },
        { name: 'username', type: 'text', placeholder: '아이디를 입력하세요' },
        { name: 'password', type: 'password', placeholder: '비밀번호를 입력하세요' },
        { name: 'address', type: 'text', placeholder: '주소를 입력하세요' },
        { name: 'phone', type: 'tel', placeholder: '전화번호를 입력하세요' },
        { name: 'email', type: 'email', placeholder: '이메일을 입력하세요' }
    ];

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-100 to-purple-100 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md mx-auto bg-white rounded-xl shadow-lg overflow-hidden">
                <div className="px-8 py-6">
                    <h2 className="text-3xl font-bold text-center text-gray-800 mb-8">회원가입</h2>
                    <div className="space-y-4">
                        {inputFields.map((field) => (
                            <div key={field.name} className="space-y-1">
                                <input
                                    type={field.type}
                                    name={field.name}
                                    value={user[field.name as keyof typeof user]}
                                    onChange={handleChange}
                                    className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-transparent transition duration-200"
                                    placeholder={field.placeholder}
                                />
                            </div>
                        ))}
                        <button
                            className="w-full py-3 px-4 mt-6 bg-gradient-to-r from-blue-500 to-purple-500 text-white font-semibold rounded-lg shadow-md hover:from-blue-600 hover:to-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition duration-200"
                            onClick={handleRegist}
                        >
                            가입하기
                        </button>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default SignUp;
