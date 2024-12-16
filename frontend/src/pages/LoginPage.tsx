import LoginForm from "../components/forms/LoginForm";
import GoogleLoginButton from '../components/buttons/GoogleLoginButton';

const LoginPage = () => {
    return (
        <div className="min-h-screen flex items-center justify-center bg-gray-50">
            <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow">
                <h2 className="text-center text-3xl font-bold text-gray-900">
                    로그인
                </h2>
                
                <LoginForm />

                <div className="mt-6">
                    <div className="relative">
                        <div className="absolute inset-0 flex items-center">
                            <div className="w-full border-t border-gray-300" />
                        </div>
                        <div className="relative flex justify-center text-sm">
                            <span className="px-2 bg-white text-gray-500">
                                또는
                            </span>
                        </div>
                    </div>

                    <div className="mt-6">
                        <GoogleLoginButton />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
