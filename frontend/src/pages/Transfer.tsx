import { useState } from "react";
import http from "../api/http";
import DepositSection from "../components/transfer/DepositSection";
import WithdrawSection from "../components/transfer/WithdrawSection";
import { TransferRequestDTO } from "../types/transfer";

const Transfer = () => {
    const [transferRequest, setTransferRequest] = useState<TransferRequestDTO | null>(null);

    const handleWithdrawChange = (accountId: number) => {
        setTransferRequest({...transferRequest, fromAccountId: accountId} as TransferRequestDTO);
    }

    const handleDepositChange = (toAccountNumber: string, amount: number) => {
        setTransferRequest({...transferRequest, toAccountNumber: toAccountNumber, amount: amount} as TransferRequestDTO);
    }

    const handleTransfer = async () => {
        if (!transferRequest || !transferRequest.fromAccountId || !transferRequest.toAccountNumber || !transferRequest.amount) {
            alert('모든 필드를 입력해주세요.');
            return;
        }

        if (transferRequest.amount <= 0) {
            alert('송금액은 0보다 커야 합니다.');
            return;
        }

        try {
            const accessToken = sessionStorage.getItem("accessToken");
            const response = await http.post("/transfer", transferRequest, {
                headers: {
                    "Authorization": `Bearer ${accessToken}`
                }
            });
            
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
                onClick={handleTransfer}
                className="bg-blue-500 hover:bg-blue-600 text-white font-bold py-4 px-8 rounded-xl shadow-lg transform transition-all hover:scale-[1.02] active:scale-[0.98] text-xl"
            >
                이체하기
            </button>
        </div>
    );
};

export default Transfer;