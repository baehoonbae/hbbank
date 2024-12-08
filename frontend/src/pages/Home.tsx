import { useEffect, useState } from "react";
import AccountListButton from "../components/buttons/AccountListlButton";
import LoginButton from "../components/buttons/LoginButton";
import LogoutButton from "../components/buttons/LogoutButton";
import SignUpButton from "../components/buttons/SignUpButton";
import TransferButton from "../components/buttons/TransferButton";
import TransactionButton from "../components/buttons/TransactionButton";
import CreateAccountButton from "../components/buttons/CreateAccountButton";

const Home = () => {
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const checkLoginStatus = () => {
            const user = sessionStorage.getItem('user');
            setIsLoggedIn(user !== null);
        };

        checkLoginStatus();
        window.addEventListener('storage', checkLoginStatus);
        
        return () => {
            window.removeEventListener('storage', checkLoginStatus);
        };
    }, []);

    return (
        <div className="min-h-screen bg-gradient-to-b from-blue-50 to-white flex flex-col items-center justify-center p-8">
            <h1 className="text-4xl font-bold text-blue-800 mb-12 animate-fade-in">
                HB은행에 오신 것을 환영합니다
            </h1>
            
            <div className="w-full max-w-2xl bg-white rounded-2xl shadow-lg p-8 mb-8">
                {isLoggedIn ? (
                    <div className="flex flex-col md:flex-row gap-4 justify-center items-center">
                        <TransferButton />
                        <AccountListButton />
                        <CreateAccountButton />
                        <TransactionButton />
                        <LogoutButton />
                    </div>
                ) : (
                    <div className="flex flex-col md:flex-row gap-6 justify-center items-center">
                        <LoginButton />
                        <SignUpButton />
                    </div>
                )}
            </div>

            <p className="text-lg text-gray-600 italic animate-pulse">
                HB은행과 함께 더 나은 금융생활을 시작하세요
            </p>
        </div>
    );
};

export default Home;
