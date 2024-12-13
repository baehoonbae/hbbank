import { Transaction } from "../../atoms/transaction";

const TransactionItem = ({ transaction }: { transaction: Transaction }) => {
    const isWithdrawal = transaction.transactionType === "출금";
    
    return (
        <div className="px-5 py-4 flex justify-between items-center hover:bg-gray-50 transition-all duration-150 cursor-pointer group">
            <div className="flex flex-col">
                <div className="flex items-center gap-2.5">
                    <span className={`px-2.5 py-1 rounded-lg text-sm font-bold ${
                        isWithdrawal
                        ? "bg-red-50 text-red-500"
                        : "bg-blue-50 text-blue-500"
                    }`}>
                        {transaction.transactionType}
                    </span>
                    <span className="text-sm text-gray-500">
                        {new Date(transaction.transactionDateTime).toLocaleString('ko-KR', {
                            month: 'long',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                        })}
                    </span>
                </div>
                <div className="mt-1.5 text-base font-bold text-gray-900">
                    {isWithdrawal ? transaction.receiver : transaction.sender}
                </div>
            </div>
            <div className="flex flex-col items-end">
                <span className={`text-lg font-bold tracking-tight ${
                    isWithdrawal
                    ? "text-red-500"
                    : "text-blue-500"
                }`}>
                    {isWithdrawal 
                        ? `-${transaction.withdrawalAmount.toLocaleString('ko-KR')}원`
                        : `+${transaction.depositAmount.toLocaleString('ko-KR')}원`
                    }
                </span>
                <span className="mt-0.5 text-sm text-gray-500 group-hover:text-gray-900 transition-colors">
                    {transaction.balance.toLocaleString('ko-KR')}원
                </span>
            </div>
        </div>
    );
};

export default TransactionItem;