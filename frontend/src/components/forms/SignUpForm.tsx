import { useState } from 'react';
import { useRecoilState } from 'recoil';
import { userSignUpState } from '../../atoms/user';
import { useUser } from '../../hooks/useUser';
import http from '../../api/http';

const SignUpForm = () => {
    const { signUp } = useUser();
    const [userSignUp, setUserSignUp] = useRecoilState(userSignUpState);
    const [verificationCode, setVerificationCode] = useState('');
    const [isEmailSent, setIsEmailSent] = useState(false);
    const [isEmailVerified, setIsEmailVerified] = useState(false);

    const sendVerificationEmail = async (email: string) => {
        try {
            await http.post('/user/email/send', null, {
                params: { email }
            });
            setIsEmailSent(true);
            alert('인증 코드가 발송되었습니다.');
        } catch (error) {
            alert('인증 코드 발송에 실패했습니다.');
        }
    };

    const verifyEmail = async (email: string, code: string) => {
        try {
            await http.post('/user/email/verify', null, {
                params: { email, code }
            });
            setIsEmailVerified(true);
            alert('이메일 인증이 완료되었습니다.');
        } catch (error) {
            alert('인증에 실패했습니다.');
        }
    };

    const inputFields = [
        { name: 'name', type: 'text', placeholder: '이름' },
        { name: 'birth', type: 'date', placeholder: '생년월일' },
        { name: 'username', type: 'text', placeholder: '아이디' },
        { name: 'password', type: 'password', placeholder: '비밀번호' },
        { name: 'address', type: 'text', placeholder: '주소' },
        { name: 'phone', type: 'tel', placeholder: '전화번호' }
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

            {/* 이메일 입력 및 인증 부분 */}
            <div className="flex gap-2">
                <input
                    type="email"
                    name="email"
                    className="flex-1 px-5 py-4 text-lg bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all duration-300 ease-in-out placeholder:text-gray-400"
                    placeholder="이메일"
                    onChange={(e) => setUserSignUp(prev => ({
                        ...prev,
                        email: e.target.value
                    }))}
                    disabled={isEmailVerified}
                />
                <button
                    className="px-4 py-2 bg-blue-500 text-white rounded-xl hover:bg-blue-600 disabled:bg-gray-400"
                    onClick={() => sendVerificationEmail(userSignUp.email)}
                    disabled={isEmailVerified}
                >
                    인증하기
                </button>
            </div>

            {/* 인증 코드 입력 필드 */}
            {isEmailSent && !isEmailVerified && (
                <div className="flex gap-2">
                    <input
                        type="text"
                        className="flex-1 px-5 py-4 text-lg bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all duration-300 ease-in-out placeholder:text-gray-400"
                        placeholder="인증 코드 6자리 입력"
                        value={verificationCode}
                        onChange={(e) => setVerificationCode(e.target.value)}
                    />
                    <button
                        className="px-4 py-2 bg-green-500 text-white rounded-xl hover:bg-green-600"
                        onClick={() => verifyEmail(userSignUp.email, verificationCode)}
                    >
                        확인
                    </button>
                </div>
            )}

            <button
                className="w-full py-4 px-5 mt-6 bg-blue-500 text-white text-lg font-semibold rounded-2xl hover:bg-blue-600 active:scale-[0.98] transition-all duration-200 ease-in-out shadow-lg shadow-blue-500/30"
                onClick={signUp}
                disabled={!isEmailVerified}
            >
                시작하기
            </button>
        </div>
    );
};

export default SignUpForm;