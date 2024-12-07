import { useState } from "react";
import { http } from "../api/http";
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
            if (response.status === 200) {
                alert('회원가입이 완료되었습니다.');
                navigate('/login');
            } else {
                alert('회원가입에 실패하였습니다.');
            }
        } catch (error) {
            console.error(error);
            alert('회원가입에 실패하였습니다.');
        }
    };

    const inputFields = [
        { label: '이름', name: 'name', type: 'text', placeholder: '이름을 입력하세요' },
        { label: '생년월일', name: 'birthDate', type: 'date' },
        { label: '아이디', name: 'username', type: 'text', placeholder: '아이디를 입력하세요' },
        { label: '비밀번호', name: 'password', type: 'password', placeholder: '비밀번호를 입력하세요' },
        { label: '주소', name: 'address', type: 'text', placeholder: '주소를 입력하세요' },
        { label: '전화번호', name: 'phone', type: 'tel', placeholder: '전화번호를 입력하세요' },
        { label: '이메일', name: 'email', type: 'email', placeholder: '이메일을 입력하세요' }
    ];

    return (
        <div className="flex flex-col items-center justify-center min-h-screen">
            <div className="w-96 p-8 bg-white rounded-lg shadow-md">
                <h2 className="text-2xl font-bold mb-6 text-center">회원가입</h2>
                {inputFields.map((field) => (
                    <div key={field.name}>
                        <label className="block text-sm font-medium text-gray-700">{field.label}</label>
                        <input
                            type={field.type}
                            name={field.name}
                            value={user[field.name as keyof typeof user]}
                            onChange={handleChange}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                            placeholder={field.placeholder}
                        />
                    </div>
                ))}
                <button
                    className="w-full bg-blue-500 text-white py-2 px-4 rounded-md hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    onClick={handleRegist}
                >
                    회원가입
                </button>
            </div>
        </div>
    );
};

export default SignUp;
