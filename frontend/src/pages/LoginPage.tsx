import LoginForm from "../components/forms/LoginForm";

const Login = () => {
    return (
        <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8 flex items-center justify-center">
            <div className="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden transform hover:scale-105 transition duration-300">
                <div className="px-8 py-12">
                    <h2 className="text-4xl font-extrabold text-center text-gray-800 mb-8">
                        <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">
                            로그인
                        </span>
                    </h2>
                    <LoginForm />
                </div>
            </div>
        </div>
    );
};

export default Login;
