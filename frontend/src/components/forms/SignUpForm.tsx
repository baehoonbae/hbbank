import { useRecoilState } from 'recoil';
import { userSignUpState } from '../../atoms/user';
import { useUser } from '../../hooks/useUser';

const SignUpForm = () => {
    const { signUp } = useUser();
    const [, setUserSignUp] = useRecoilState(userSignUpState);
    const inputFields = [
        { name: 'name', type: 'text', placeholder: '이름' },
        { name: 'birth', type: 'date', placeholder: '생년월일' },
        { name: 'username', type: 'text', placeholder: '아이디' },
        { name: 'password', type: 'password', placeholder: '비밀번호' },
        { name: 'address', type: 'text', placeholder: '주소' },
        { name: 'phone', type: 'tel', placeholder: '전화번호' },
        { name: 'email', type: 'email', placeholder: '이메일' }
    ];

    return (
        <div className="space-y-5">
            {inputFields.map((field) => (
                <div key={field.name}>
                    <input
                        type={field.type}
                        name={field.name}
                        className="w-full px-5 py-4 text-lg bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all duration-300 ease-in-out placeholder:text-gray-400"
                        placeholder={field.placeholder}
                        onChange={(e) => setUserSignUp(prev => ({
                            ...prev,
                            [e.target.name]: e.target.value
                        }))}
                    />
                </div>
            ))}
            <button
                className="w-full py-4 px-5 mt-6 bg-blue-500 text-white text-lg font-semibold rounded-2xl hover:bg-blue-600 active:scale-[0.98] transition-all duration-200 ease-in-out shadow-lg shadow-blue-500/30"
                onClick={signUp}
            >
                시작하기
            </button>
        </div>
    );
};

export default SignUpForm;