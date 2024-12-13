import { useState, useEffect } from "react";
import { accountState } from "../../atoms/account";
import { CreditCardIcon } from "@heroicons/react/24/outline";
import { useTransfer } from "../../hooks/useTransfer";
import { useRecoilValue } from "recoil";

const WithdrawSection = () => {
    const { updateTransferRequest } = useTransfer();
    const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);
    const accounts = useRecoilValue(accountState);

    useEffect(() => {
        if (selectedAccountId) {
            updateTransferRequest({ fromAccountId: selectedAccountId });
        }
    }, [selectedAccountId])

    const handleAccountChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSelectedAccountId(Number(e.target.value));
        updateTransferRequest({ fromAccountId: Number(e.target.value) });
    }

    return (
        <>
            <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-2xl shadow-2xl p-10 mb-8 transform transition-all duration-300 hover:scale-[1.02] border border-blue-100">
                <h3 className="text-2xl font-extrabold mb-8 text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600 flex items-center">
                    <span className="w-2 h-8 bg-gradient-to-b from-blue-500 to-indigo-500 rounded-full mr-4 animate-pulse"></span>
                    출금계좌정보
                </h3>
                <div className="space-y-8">
                    <div className="flex items-center group">
                        <label className="w-40 text-gray-700 font-bold text-lg group-hover:text-blue-600 transition-colors">출금계좌</label>
                        <div className="flex-1 relative">
                            <select
                                className="w-full p-4 pl-12 border-2 border-gray-200 rounded-xl focus:ring-4 focus:ring-blue-200 focus:border-blue-400 transition-all duration-300 bg-white bg-opacity-70 backdrop-blur-sm text-gray-700 font-medium"
                                value={selectedAccountId || ""}
                                onChange={handleAccountChange}
                            >
                                <option value="" disabled>계좌를 선택하세요</option>
                                {accounts.map((account) => (
                                    <option key={account.id} value={account.id}>
                                        {account.accountName} ({account.accountNumber})
                                    </option>
                                ))}
                            </select>
                            <CreditCardIcon className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400" />
                        </div>
                    </div>
                    <div className="flex items-center group">
                        <label className="w-40 text-gray-700 font-bold text-lg group-hover:text-blue-600 transition-colors">출금가능금액</label>
                        <div className="flex-1 p-4 pl-12 bg-white bg-opacity-70 backdrop-blur-sm rounded-xl border-2 border-gray-200 font-bold text-lg relative">
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">
                                {selectedAccountId
                                    ? accounts.find((account) => account.id === selectedAccountId)?.balance.toLocaleString()
                                    : 0}
                            </span>
                            <span className="ml-2 text-gray-600">원</span>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default WithdrawSection;