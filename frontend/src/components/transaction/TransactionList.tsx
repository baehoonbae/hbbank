import { useRecoilValue } from "recoil";
import { transactionState } from "../../atoms/transaction";
import TransactionItem from "./TransactionItem";
import { useTransactions } from "../../hooks/useTransactions";
import { useEffect } from "react";
import TransactionSummary from "./TransactionSummary";

const TransactionList = (accountId: { accountId: number }) => {
    const { fetchTransactions } = useTransactions();
    const transactions = useRecoilValue(transactionState);

    useEffect(() => {
        fetchTransactions(accountId.accountId);
    }, [accountId]);

    return (
        <>
            <TransactionSummary transactions={transactions} />
            <div className="space-y-6">
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
        </>
    )
}

export default TransactionList;