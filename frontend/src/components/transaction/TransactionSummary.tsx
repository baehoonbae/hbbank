import { Transaction } from "../../types/transaction";

interface TransactionSummaryProps {
    transactions: Transaction[];
}

const TransactionSummary = ({ transactions }: TransactionSummaryProps) => {
    return (
        <div className="mt-8 bg-gradient-to-r from-gray-50 to-gray-100 p-6 rounded-xl shadow-lg">
            <div className="flex justify-between items-center space-x-8">
                <div className="flex-1 bg-white p-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-300">
                    <div className="text-gray-600 text-sm mb-1">
                        총 입금금액 ({transactions.filter(t => t.transactionType === "입금").length}건)
                    </div>
                    <div className="flex items-baseline">
                        <span className="text-blue-600 text-2xl font-bold">
                            {transactions.reduce((sum, t) => sum + (t.transactionType === "입금" ? t.depositAmount : 0), 0).toLocaleString()}
                        </span>
                        <span className="text-blue-400 ml-1">원</span>
                    </div>
                </div>
                <div className="flex-1 bg-white p-4 rounded-lg shadow-md hover:shadow-lg transition-all duration-300">
                    <div className="text-gray-600 text-sm mb-1">
                        총 출금금액 ({transactions.filter(t => t.transactionType === "출금").length}건)
                    </div>
                    <div className="flex items-baseline">
                        <span className="text-red-600 text-2xl font-bold">
                            {transactions.reduce((sum, t) => sum + (t.transactionType === "출금" ? t.withdrawalAmount : 0), 0).toLocaleString()}
                        </span>
                        <span className="text-red-400 ml-1">원</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TransactionSummary; 