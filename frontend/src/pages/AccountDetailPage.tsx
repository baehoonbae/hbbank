import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import http from "../api/http";
import AccountItem from "../components/account/AccountItem";
import TransferButton from "../components/buttons/TransferButton";
import { Account } from "../types/account";
import { Transaction } from "../types/transaction";
import TransactionItem from "../components/transaction/TransactionItem";
import TransactionSummary from "../components/transaction/TransactionSummary";

const AccountDetail = () => {
    const { id } = useParams();
    const [account, setAccount] = useState<Account>();
    const [transactions, setTransactions] = useState<Transaction[]>([]);

    useEffect(() => {
        if (id) getAccount(id);
    }, [id]);

    useEffect(() => {
        if (account?.id) getTransactions(account.id);
    }, [account?.id]);

    const getAccount = async (id: string) => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await http.get(`/account/${id}`, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            setAccount(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    const getTransactions = async (accountId: number) => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await http.get(`/transaction/transactions/${accountId}`, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            setTransactions(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-8">
            <div className="max-w-5xl mx-auto space-y-8">
                <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
                    <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600">
                        계좌 상세 정보
                    </span>
                </h1>

                <div className="transform hover:scale-[1.02] transition-all duration-300">
                    {account && <AccountItem account={account} />}
                </div>

                <div className="flex justify-end">
                    {account && (
                        <div className="transform hover:scale-105 transition-all duration-300">
                            <TransferButton />
                        </div>
                    )}
                </div>

                <div className="transform hover:scale-[1.01] transition-all duration-300">
                    <TransactionSummary transactions={transactions} />
                </div>

                <div className="space-y-6">
                    <h2 className="text-2xl font-bold text-gray-800 flex items-center gap-3">
                        <span className="text-2xl">💳</span>
                        거래내역
                    </h2>
                    <div className="bg-white rounded-2xl shadow-xl p-6 border border-gray-100 backdrop-blur-lg bg-opacity-90">
                        {transactions.length > 0 ? (
                            <div className="space-y-4">
                                {transactions.map((transaction, index) => (
                                    <div key={index} className="transform hover:scale-[1.01] transition-all duration-300">
                                        <TransactionItem transaction={transaction} />
                                    </div>
                                ))}
                            </div>
                        ) : (
                            <div className="text-center py-12 text-gray-500">
                                거래내역이 없습니다.
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default AccountDetail;