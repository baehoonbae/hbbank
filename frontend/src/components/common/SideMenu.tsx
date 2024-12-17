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

    const menuItems = [
        { path: isAuthenticated ? '/user-dashboard' : '/', icon: HomeIcon, text: '홈' },
        { path: '/create-account', icon: PlusIcon, text: '계좌 개설' },
        { path: '/transfer', icon: BanknotesIcon, text: '즉시이체' },
        { path: '/auto-transfer', icon: ArrowPathIcon, text: '자동이체' },
        { path: '/auto-transfer/manage', icon: ClockIcon, text: '자동이체관리' },
        { path: '/reserve-transfer', icon: ClockIcon, text: '예약이체' },
        { path: '/reserve-transfer/manage', icon: ClockIcon, text: '예약이체관리' },
        { path: '/transaction', icon: ClockIcon, text: '거래내역' }
    ];

    return (
        <>
            <div className="fixed left-0 top-0 h-screen w-64 bg-[#191F28] text-white">
                <div className="p-6">
                    <h1 className="text-3xl font-extrabold text-white mb-12 tracking-tight">HB</h1>

                    <nav className="space-y-6">
                        {menuItems.map((item, index) => {
                            const Icon = item.icon;
                            return (
                                <button
                                    key={index}
                                    onClick={() => navigate(item.path)}
                                    className="w-full flex items-center space-x-4 py-3 px-4 rounded-xl hover:bg-[#2D3540] transition-all duration-200"
                                >
                                    <Icon className="w-6 h-6" />
                                    <span className="font-medium">{item.text}</span>
                                </button>
                            );
                        })}
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