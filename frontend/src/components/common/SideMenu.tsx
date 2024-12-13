import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { handleLogout } from "../../api/http";
import { HomeIcon, PlusIcon, BanknotesIcon, KeyIcon, ArrowRightOnRectangleIcon, ArrowPathIcon, ClockIcon } from '@heroicons/react/24/outline';
import { useAccounts } from '../../hooks/useAccounts';

const SideMenu = () => {
    const isAuthenticated = sessionStorage.getItem('accessToken');
    const navigate = useNavigate();
    const { fetchAccounts } = useAccounts();
    useEffect(() => {
        if (isAuthenticated) {
            fetchAccounts();
        }
    }, [isAuthenticated]);

    return (
        <>
            <div className="fixed left-0 top-0 h-screen w-64 bg-[#191F28] text-white">
                <div className="p-6">
                    <h1 className="text-3xl font-extrabold text-white mb-12 tracking-tight">HB</h1>

                    <nav className="space-y-6">
                        <button
                            onClick={() => isAuthenticated ? navigate('/user-dashboard') : navigate('/')}
                            className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                        >
                            <HomeIcon className="w-6 h-6" />
                            <span className="font-medium">홈</span>
                        </button>

                        <button
                            onClick={() => navigate('/create-account')}
                            className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                        >
                            <PlusIcon className="w-6 h-6" />
                            <span className="font-medium">계좌 개설</span>
                        </button>
                        <button
                            onClick={() => navigate('/transfer')}
                            className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                        >
                            <BanknotesIcon className="w-6 h-6" />
                            <span className="font-medium">즉시이체</span>
                        </button>
                        <button
                            onClick={() => navigate('/auto-transfer')}
                            className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                        >
                            <ArrowPathIcon className="w-6 h-6" />
                            <span className="font-medium">자동이체</span>
                        </button>
                        <button
                            onClick={() => navigate('/reserve-transfer')}
                            className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                        >
                            <ClockIcon className="w-6 h-6" />
                            <span className="font-medium">예약이체</span>
                        </button>
                        <button
                            onClick={() => navigate('/transaction')}
                            className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                        >
                            <ClockIcon className="w-6 h-6" />
                            <span className="font-medium">거래내역</span>
                        </button>
                    </nav>
                </div>

                <div className="absolute bottom-0 w-full p-6">
                    {isAuthenticated ? (
                        <button
                            onClick={handleLogout}
                            className="w-full flex items-center justify-center space-x-3 py-3 px-4 rounded-xl bg-[#2D3540] hover:bg-[#3D4550] transition-all duration-200"
                        >
                            <ArrowRightOnRectangleIcon className="w-6 h-6" />
                            <span className="font-medium">로그아웃</span>
                        </button>
                    ) : (
                        <button
                            onClick={() => navigate('/login')}
                            className="w-full flex items-center justify-center space-x-3 py-3 px-4 rounded-xl bg-blue-500 hover:bg-blue-600 transition-all duration-200"
                        >
                            <KeyIcon className="w-6 h-6" />
                            <span className="font-medium">로그인</span>
                        </button>
                    )}
                </div>
            </div>
        </>
    );
};

export default SideMenu;