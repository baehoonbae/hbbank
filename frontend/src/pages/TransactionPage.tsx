import type { Transaction } from '../atoms/transaction';
import TransactionItem from '../components/transaction/TransactionItem';
import TransactionSearchBar from '../components/transaction/TransactionSearchBar';
import { useTransactions } from '../hooks/useTransactions';

const Transaction = () => {
    const { transactions, searchTransactions } = useTransactions();

    return (
        <>
            <TransactionSearchBar search={searchTransactions} />
            <div className="flex flex-col gap-5">
                {transactions.map((transaction) => (
                    <TransactionItem key={transaction.id} transaction={transaction} />
                ))}
            </div>
        </>
    );
};

export default Transaction;