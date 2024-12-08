import { Transaction } from "../../types/transaction";

const TransactionItem = ({ transaction }: { transaction: Transaction }) => {
    return (
        <div className="border-b border-gray-200 py-4 flex justify-between items-center hover:bg-gray-50">
            <div className="flex flex-col">
                <div className="flex items-center gap-2">
                    <span className={`px-2 py-1 rounded-full text-sm ${
                        transaction.transactionType === "출금" 
                        ? "bg-red-100 text-red-600"
                        : "bg-blue-100 text-blue-600"
                    }`}>
                        {transaction.transactionType}
                    </span>
                    <span className="text-gray-600">
                        {new Date(transaction.transactionDateTime).toLocaleString()}
                    </span>
                </div>
                <div className="mt-1 text-sm text-gray-500">
                    {transaction.transactionType === "출금" ? transaction.receiver : transaction.sender}
                </div>
            </div>
            <div className="flex flex-col items-end">
                <span className={`font-semibold ${
                    transaction.transactionType === "출금"
                    ? "text-red-600"
                    : "text-blue-600"
                }`}>
                    {transaction.transactionType === "출금" 
                        ? `-${transaction.withdrawalAmount.toLocaleString()}원`
                        : `+${transaction.depositAmount.toLocaleString()}원`
                    }
                </span>
                <span className="text-sm text-gray-500">
                    잔액: {transaction.balance.toLocaleString()}원
                </span>
            </div>
        </div>
    );
};

export default TransactionItem;