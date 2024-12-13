import { useEffect, useState } from "react";
import AccountBalanceSection from "../components/account/AccountBalanceSection";
import AccountCountSection from "../components/account/AccountCountSection";
import AccountExpenseSection from "../components/account/AccountExpenseSection";
import { accountState } from "../atoms/account";
import { Transaction } from "../atoms/transaction";
import AccountListPage from "./AccountListPage";
import { SparklesIcon } from "@heroicons/react/24/outline";
import { useRecoilValue } from "recoil";
import { useAccounts } from "../hooks/useAccounts";
import { useTransactions } from "../hooks/useTransactions";

const UserDashBoard = () => {
    const user = JSON.parse(sessionStorage.getItem('user') || '{}');
    const [monthlyTransaction, setMonthlyTransaction] = useState<Transaction[]>([]);
    const accounts = useRecoilValue(accountState);
    const { fetchAccounts } = useAccounts();
    const { fetchMonthlyTransactions } = useTransactions();

    useEffect(() => {
        fetchAccounts();
        fetchMonthlyTransactions().then(setMonthlyTransaction);
    }, []);

    return (
        <div className="p-8 bg-[#F9FAFB] min-h-screen">
            {/* 헤더 섹션 */}
            <div className="mb-12">
                <h1 className="text-4xl font-bold text-gray-900 mb-3">
                    {user.name}님의 금융
                </h1>
                <p className="text-lg text-gray-600 flex items-center gap-2">
                    오늘도 현명한 금융생활 되세요
                    <SparklesIcon className="w-5 h-5" />
                </p>
            </div>

            {/* 계좌 요약 섹션 */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 mb-10">
                <AccountCountSection total={accounts.length} />
                <AccountBalanceSection total={accounts.reduce((acc, account) => acc + account.balance, 0)} />
                <AccountExpenseSection total={monthlyTransaction.reduce((acc, transaction) =>
                    transaction.sender !== transaction.receiver ? acc + transaction.withdrawalAmount : acc, 0)} />
            </div>
            <AccountListPage />
        </div>
    );
};

export default UserDashBoard;