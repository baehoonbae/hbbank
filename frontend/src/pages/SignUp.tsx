import { useState } from "react";
import http from "../api/http";
import { useNavigate } from "react-router-dom";
import { UserSignUpDTO } from "../types/user";
import SignUpForm from "../components/forms/SignUpForm";

const SignUp = () => {
    const navigate = useNavigate();
    const [user, setUser] = useState<UserSignUpDTO>({
        name: '',
        birth: new Date().toISOString().split('T')[0],
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
            console.log(user);
            const response = await http.post('/user/regist', user);
            alert(response.data.message);
            navigate('/');
        } catch (error) {
            console.error(error);
            alert('회원가입에 실패하였습니다.');
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-100 to-purple-100 py-12 px-4 sm:px-6 lg:px-8">
            <div className="max-w-md mx-auto bg-white rounded-xl shadow-lg overflow-hidden">
                <div className="px-8 py-6">
                    <h2 className="text-3xl font-bold text-center text-gray-800 mb-8">회원가입</h2>
                    <SignUpForm
                        formData={user}
                        onChange={handleChange}
                        onSubmit={handleRegist}
                    />
                </div>
            </div>
        </div>
    );
};

export default SignUp;
