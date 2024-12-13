import { useUser } from '../../hooks/useUser';
import { useNavigate } from 'react-router-dom';

const LoginForm = () => {
    const { loginData, setLoginData, login } = useUser();
    const navigate = useNavigate();
    const inputFields = [
        { name: 'username', type: 'text', placeholder: '아이디' },
        { name: 'password', type: 'password', placeholder: '비밀번호' }
    ];

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setLoginData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <div className="space-y-4">
            {inputFields.map((field) => (
                <div key={field.name}>
                    <input
                        type={field.type}
                        name={field.name}
                        className="w-full px-5 py-4 text-lg bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all duration-300 ease-in-out placeholder:text-gray-400"
                        placeholder={field.placeholder}
                        value={loginData[field.name as keyof typeof loginData]}
                        onChange={handleChange}
                    />
                </div>
            ))}
            <button
                type="submit"
                className="w-full py-4 px-5 mt-6 bg-blue-500 text-white text-lg font-semibold rounded-2xl hover:bg-blue-600 active:scale-[0.98] transition-all duration-200 ease-in-out shadow-lg shadow-blue-500/30"
                onClick={login}
            >
                로그인
            </button>
            <div className="text-center mt-4">
                <button className="text-blue-500 hover:text-blue-600 transition-colors" onClick={() => navigate('/signup')}>
                    회원가입
                </button>
            </div>
        </div>
    );
};

export default LoginForm;