import { useState } from 'react';
import TransactionSearchBar from '../components/transaction/TransactionSearchBar';
import type { Transaction, TransactionSearchDTO } from '../types/transaction';
import http from '../api/http';
import TransactionItem from '../components/transaction/TransactionItem';

const Transaction = () => {
    const [transactions, setTransactions] = useState<Transaction[]>([]);

    const getTransactions = async (dto: TransactionSearchDTO) => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await http.post('/transaction/transactions/search', dto, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            setTransactions(response.data);
        } catch (error) {
            console.error('거래내역을 불러오는데 실패했습니다:', error);
        }
    }

    return (
        <>
            <TransactionSearchBar onSearch={(searchDTO) => { getTransactions(searchDTO); }} />
            <div className="flex flex-col gap-5">
                {transactions.map((transaction) => (
                    <TransactionItem transaction={transaction} />
                ))}
            </div>
        </>
    );
};

export default Transaction;