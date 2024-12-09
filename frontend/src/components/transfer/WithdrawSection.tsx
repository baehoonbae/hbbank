import { useState, useEffect } from "react";
import { Account } from "../../types/account";
import http from "../../api/http";

interface WithdrawSectionProps {
    onAccountSelect: (accountId: number) => void;
}

const WithdrawSection = ({ onAccountSelect }: WithdrawSectionProps) => {
    const [myAccounts, setMyAccounts] = useState<Account[]>([]);
    const [selectedAccountId, setSelectedAccountId] = useState<number | null>(null);

    useEffect(() => {
        getMyAccounts();
    }, [])

    const handleAccountChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSelectedAccountId(Number(e.target.value));
        onAccountSelect(Number(e.target.value));
    }

    const getMyAccounts = async () => {
        const userId = JSON.parse(sessionStorage.getItem("user")!).id;
        const accessToken = sessionStorage.getItem("accessToken");
        const response = await http.get(`/account/accounts/${userId}`, {
            headers: {
                "Authorization": `Bearer ${accessToken}`
            }
        });
        setMyAccounts(response.data);
    }

    return (
        <>
            <div className="bg-white rounded-xl shadow-lg p-8 mb-8 transform transition-all hover:scale-[1.01]">
                <h3 className="text-xl font-bold mb-6 text-gray-800 flex items-center">
                    <span className="w-2 h-6 bg-blue-500 rounded-full mr-3"></span>
                    출금계좌정보
                </h3>
                <div className="space-y-6">
                    <div className="flex items-center">
                        <label className="w-36 text-gray-700 font-medium">출금계좌</label>
                        <select 
                            className="flex-1 p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                            value={selectedAccountId || ""}
                            onChange={handleAccountChange}
                        >
                            <option value="" disabled>계좌를 선택하세요</option>
                            {myAccounts.map((account) => (
                                <option key={account.id} value={account.id}>
                                    {account.accountName} ({account.accountNumber})
                                </option>
                            ))}
                        </select>
                    </div>
                    <div className="flex items-center">
                        <label className="w-36 text-gray-700 font-medium">출금가능금액</label>
                        <div className="flex-1 p-3 bg-gray-50 rounded-lg border border-gray-200 font-semibold text-blue-600">
                            {selectedAccountId 
                                ? myAccounts.find((account) => account.id === selectedAccountId)?.balance.toLocaleString()
                                : 0}원
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default WithdrawSection;