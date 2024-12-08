interface LoginFormProps {
    formData: {
        username: string;
        password: string;
    };
    onSubmit: () => void;
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
}

const LoginForm = ({ formData, onSubmit, onChange }: LoginFormProps) => {
    const inputFields = [
        { name: 'username', type: 'text', placeholder: '아이디를 입력하세요' },
        { name: 'password', type: 'password', placeholder: '비밀번호를 입력하세요' }
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
                        value={formData[field.name as keyof typeof formData]}
                        onChange={onChange}
                    />
                </div>
            ))}
            <button
                type="submit"
                className="w-full py-4 px-4 mt-8 bg-gradient-to-r from-blue-500 to-purple-500 text-white text-lg font-bold rounded-lg shadow-md hover:from-blue-600 hover:to-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transform hover:-translate-y-1 transition duration-200"
                onClick={onSubmit}
            >
                로그인하기
            </button>
        </div>
    );
};

export default LoginForm; 