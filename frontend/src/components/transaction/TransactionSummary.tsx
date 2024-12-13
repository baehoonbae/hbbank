import { Transaction } from "../../atoms/transaction";
import { ArrowDownCircleIcon, ArrowUpCircleIcon } from "@heroicons/react/24/solid";

interface TransactionSummaryProps {
    transactions: Transaction[];
}

const TransactionSummary = ({ transactions }: TransactionSummaryProps) => {
    return (
        <div className="mt-8 bg-white p-6 rounded-2xl shadow-2xl backdrop-blur-sm bg-opacity-90 border border-gray-100">
            <div className="grid grid-cols-2 gap-6">
                <div className="group p-6 rounded-xl bg-gradient-to-br from-blue-50 to-white border border-blue-100 hover:shadow-lg hover:shadow-blue-100/50 transition-all duration-300 ease-in-out transform hover:-translate-y-1">
                    <div className="text-gray-800 font-semibold text-lg mb-4 flex items-center">
                        <ArrowDownCircleIcon className="w-8 h-8 mr-3 text-blue-500 group-hover:rotate-12 transition-transform duration-300" />
                        <span className="relative">
                            입금
                            <span className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                                {transactions.filter(t => t.transactionType === "입금").length}건
                            </span>
                        </span>
                    </div>
                    <div className="flex items-baseline space-x-2">
                        <span className="text-blue-600 text-3xl font-bold tracking-tight">
                            {transactions.reduce((sum, t) => sum + (t.transactionType === "입금" ? t.depositAmount : 0), 0).toLocaleString()}
                        </span>
                        <span className="text-gray-600 font-medium">원</span>
                    </div>
                </div>
                
                <div className="group p-6 rounded-xl bg-gradient-to-br from-red-50 to-white border border-red-100 hover:shadow-lg hover:shadow-red-100/50 transition-all duration-300 ease-in-out transform hover:-translate-y-1">
                    <div className="text-gray-800 font-semibold text-lg mb-4 flex items-center">
                        <ArrowUpCircleIcon className="w-8 h-8 mr-3 text-red-500 group-hover:rotate-12 transition-transform duration-300" />
                        <span className="relative">
                            출금
                            <span className="ml-2 inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-red-100 text-red-800">
                                {transactions.filter(t => t.transactionType === "출금").length}건
                            </span>
                        </span>
                    </div>
                    <div className="flex items-baseline space-x-2">
                        <span className="text-red-600 text-3xl font-bold tracking-tight">
                            {transactions.reduce((sum, t) => sum + (t.transactionType === "출금" ? t.withdrawalAmount : 0), 0).toLocaleString()}
                        </span>
                        <span className="text-gray-600 font-medium">원</span>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default TransactionSummary;