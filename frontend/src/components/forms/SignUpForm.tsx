import { UserSignUpDTO } from '../../types/user';

interface SignUpFormProps {
    formData: UserSignUpDTO;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
    onSubmit: () => void;
}

const SignUpForm = ({ formData, onChange, onSubmit }: SignUpFormProps) => {
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
        <div className="space-y-6">
            {inputFields.map((field) => (
                <div key={field.name} className="space-y-2">
                    <input
                        type={field.type}
                        name={field.name}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-transparent transition duration-200"
                        placeholder={field.placeholder}
                        value={formData[field.name as keyof UserSignUpDTO]}
                        onChange={onChange}
                    />
                </div>
            ))}
            <button
                type="submit"
                className="w-full py-4 px-4 bg-gradient-to-r from-green-500 to-blue-500 text-white text-lg font-bold rounded-lg shadow-md hover:from-green-600 hover:to-blue-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transform hover:-translate-y-1 transition duration-200"
                onClick={onSubmit}
            >
                회원가입
            </button>
        </div>
    );
};

export default SignUpForm; 