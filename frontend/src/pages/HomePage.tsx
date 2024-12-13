import LoginButton from "../components/buttons/LoginButton";
import SignUpButton from "../components/buttons/SignUpButton";

const Home = () => {
    return (
        <div className="min-h-screen flex flex-col items-center justify-center bg-gradient-to-b from-white to-gray-50 p-8">
            <div className="max-w-4xl text-center">
                <h1 className="text-7xl font-black mb-6 tracking-tight leading-tight">
                    금융이 
                    <span className="ml-7 bg-gradient-to-r from-blue-500 to-indigo-600 bg-clip-text text-transparent">
                        쉬워진다
                    </span>
                </h1>
                <p className="text-gray-600 text-2xl mb-16 font-medium leading-relaxed">
                    복잡한 금융을 간단하게,<br />
                    <span className="text-blue-500 font-semibold">HB</span>와 함께라면 누구나 금융전문가
                </p>
                <div className="flex flex-col md:flex-row gap-6 justify-center items-center w-full max-w-lg mx-auto">
                    <div className="w-full md:w-1/2 transform hover:scale-105 transition-all duration-200">
                        <LoginButton />
                    </div>
                    <div className="w-full md:w-1/2 transform hover:scale-105 transition-all duration-200">
                        <SignUpButton />
                    </div>
                </div>
            </div>
            <div className="absolute bottom-0 left-0 right-0 h-1/3 bg-gradient-to-t from-blue-50 to-transparent -z-10"></div>
        </div>
    );
};

export default Home;
