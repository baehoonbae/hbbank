import { Transaction } from "../../types/transaction";

const TransactionItem = ({ transaction }: { transaction: Transaction }) => {
    const isWithdrawal = transaction.transactionType === "출금";
    
    return (
        <div className="border-b border-gray-200 py-6 px-4 flex justify-between items-center hover:bg-gray-100 transition-all duration-200 rounded-lg shadow-sm hover:shadow-md">
            <div className="flex flex-col">
                <div className="flex items-center gap-3">
                    <span className={`px-3 py-1.5 rounded-full text-sm font-medium tracking-wide ${
                        isWithdrawal
                        ? "bg-red-100 text-red-600 ring-2 ring-red-200"
                        : "bg-blue-100 text-blue-600 ring-2 ring-blue-200"
                    }`}>
                        {transaction.transactionType}
                    </span>
                    <span className="text-gray-700 font-medium">
                        {new Date(transaction.transactionDateTime).toLocaleString('ko-KR', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                        })}
                    </span>
                </div>
                <div className="mt-2 text-base text-gray-600 font-medium">
                    {isWithdrawal ? transaction.receiver : transaction.sender}
                </div>
            </div>
            <div className="flex flex-col items-end">
                <span className={`text-xl font-bold ${
                    isWithdrawal
                    ? "text-red-600"
                    : "text-blue-600"
                }`}>
                    {isWithdrawal 
                        ? `-${transaction.withdrawalAmount.toLocaleString('ko-KR')}원`
                        : `+${transaction.depositAmount.toLocaleString('ko-KR')}원`
                    }
                </span>
                <span className="mt-1 text-base font-medium text-gray-600">
                    잔액: {transaction.balance.toLocaleString('ko-KR')}원
                </span>
            </div>
        </div>
    );
};

export default TransactionItem;