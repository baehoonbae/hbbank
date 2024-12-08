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
        <>
            <h1 className="text-2xl font-bold mb-4">계좌 상세</h1>
            {account && <AccountItem account={account} />}
            <div className="mt-4">
                {account && <TransferButton account={account} />}
            </div>
            <TransactionSummary transactions={transactions} />
            <div className="mt-8">
                <h2 className="text-xl font-bold mb-4">거래내역</h2>
                <div className="bg-white rounded-lg shadow-md p-4">
                    {transactions.map((transaction, index) => (
                        <TransactionItem transaction={transaction} key={index} />
                    ))}
                </div>
            </div>
        </>
    );
};

export default AccountDetail;