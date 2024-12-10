import { useState } from "react";
import http from "../api/http";
import DepositSection from "../components/transfer/DepositSection";
import WithdrawSection from "../components/transfer/WithdrawSection";
import { TransferRequestDTO } from "../types/transfer";
import PasswordModal from "../components/forms/PasswordModal";
import { useNavigate } from "react-router-dom";

const Transfer = () => {
    const [transferRequest, setTransferRequest] = useState<TransferRequestDTO | null>(null);
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
    const navigate = useNavigate();

    const handleWithdrawChange = (accountId: number) => {
        setTransferRequest({ ...transferRequest, fromAccountId: accountId } as TransferRequestDTO);
    }

    const handleDepositChange = (toAccountNumber: string, amount: number) => {
        setTransferRequest({ ...transferRequest, toAccountNumber: toAccountNumber, amount: amount } as TransferRequestDTO);
    }

    const openPasswordModal = () => {
        setIsPasswordModalOpen(true);
    }

    const closePasswordModal = () => {
        setIsPasswordModalOpen(false);
    }

    const handlePasswordConfirm = async (password: string) => {
        const updatedTransferRequest = { ...transferRequest, password: password } as TransferRequestDTO;
        if (!updatedTransferRequest || !updatedTransferRequest.fromAccountId || !updatedTransferRequest.toAccountNumber || !updatedTransferRequest.amount) {
            alert('모든 필드를 입력해주세요.');
            return;
        }
        if (updatedTransferRequest.amount <= 0) {
            alert('송금액은 0보다 커야 합니다.');
            return;
        }
        try {
            const accessToken = sessionStorage.getItem("accessToken");
            await http.post("/transfer", updatedTransferRequest, {
                headers: {
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            alert("이체가 완료되었습니다.");
            navigate("/");
        } catch (error) {
            console.error(error);
        }
    }

    return (
        <div className="max-w-4xl mx-auto p-8 bg-gray-50 min-h-screen">
            <h2 className="text-3xl font-bold mb-8 text-gray-800 border-b pb-4">계좌이체</h2>
            <WithdrawSection onAccountSelect={handleWithdrawChange} />
            <DepositSection onDepositChange={handleDepositChange} />
            <button
                onClick={openPasswordModal}
                className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-4 px-8 rounded-xl shadow-lg transform transition-all hover:scale-[1.02] active:scale-[0.98] text-xl"
            >
                이체하기
            </button>
            <PasswordModal
                isOpen={isPasswordModalOpen}
                onClose={closePasswordModal}
                onConfirm={handlePasswordConfirm}
            />
        </div>
    );
};

export default Transfer;