import { useState } from "react";
import PasswordModal from "../components/forms/PasswordModal";
import DepositSection from "../components/transfer/DepositSection";
import WithdrawSection from "../components/transfer/WithdrawSection";

const Transfer = () => {
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);

    const openPasswordModal = () => {
        setIsPasswordModalOpen(true);
    }

    const closePasswordModal = () => {
        setIsPasswordModalOpen(false);
    }

    return (
        <div className="max-w-4xl mx-auto p-8 bg-gray-50 min-h-screen">
            <h2 className="text-3xl font-bold mb-8 text-gray-800 border-b pb-4">계좌이체</h2>
            <WithdrawSection />
            <DepositSection />
            <button
                onClick={openPasswordModal}
                className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-4 px-8 rounded-xl shadow-lg transform transition-all hover:scale-[1.02] active:scale-[0.98] text-xl"
            >
                이체하기
            </button>
            <PasswordModal
                isOpen={isPasswordModalOpen}
                onClose={closePasswordModal}
            />
        </div>
    );
};

export default Transfer;