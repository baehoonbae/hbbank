import { useEffect, useState } from 'react';
import http from '../../api/http';
import type { Account } from '../../types/account';
import type { TransactionSearchDTO } from '../../types/transaction';

interface TransactionSearchBarProps {
    onSearch: (dto: TransactionSearchDTO) => void;
}

const TransactionSearchBar = ({ onSearch }: TransactionSearchBarProps) => {
    const [transactionSearchDTO, setTransactionSearchDTO] = useState<TransactionSearchDTO>({
        accountId: null,
        startDate: null,
        endDate: null,
        transactionType: 0, //0: 전체, 1: 입금, 2: 출금
        page: 0
    });
    const [accounts, setAccounts] = useState<Account[]>([]);

    useEffect(() => {
        getAccounts();
    }, []);

    const getAccounts = async () => {
        try {
            const userId = sessionStorage.getItem('user') ? JSON.parse(sessionStorage.getItem('user')!).id : 0;
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await http.get(`/account/accounts/${userId}`, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            setAccounts(response.data);
        } catch (error) {
            console.error(error);
        }
    }

    return (
        <div className="p-6 bg-white rounded-xl shadow-lg border border-gray-100">
            <h2 className="text-xl font-bold text-gray-800 mb-4">거래내역 조회</h2>
            <div className="flex flex-col gap-5">
                <div className="relative">
                    <select
                        className="w-full p-3 border border-gray-300 rounded-lg appearance-none bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={transactionSearchDTO.accountId ? transactionSearchDTO.accountId.toString() : ''}
                        onChange={(e) => setTransactionSearchDTO({ ...transactionSearchDTO, accountId: Number(e.target.value) })}
                    >
                        <option value="">계좌를 선택하세요</option>
                        {accounts.map((account) => (
                            <option key={account.id} value={account.id}>{account.accountName} ({account.accountNumber})</option>
                        ))}
                    </select>
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                        </svg>
                    </div>
                </div>

                <div className="flex gap-4 items-center">
                    <input
                        type="date"
                        className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={transactionSearchDTO.startDate ? transactionSearchDTO.startDate : ''}
                        onChange={(e) => setTransactionSearchDTO({ ...transactionSearchDTO, startDate: e.target.value })}
                    />
                    <span className="text-gray-500 font-medium">~</span>
                    <input
                        type="date"
                        className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={transactionSearchDTO.endDate ? transactionSearchDTO.endDate : ''}
                        onChange={(e) => setTransactionSearchDTO({ ...transactionSearchDTO, endDate: e.target.value })}
                    />
                </div>

                <div className="relative">
                    <select
                        className="w-full p-3 border border-gray-300 rounded-lg appearance-none bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={transactionSearchDTO.transactionType}
                        onChange={(e) => setTransactionSearchDTO({ ...transactionSearchDTO, transactionType: Number(e.target.value) })}
                    >
                        <option value="0">전체</option>
                        <option value="1">입금</option>
                        <option value="2">출금</option>
                    </select>
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                        <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7" />
                        </svg>
                    </div>
                </div>

                <button
                    className="w-full p-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transform transition-all active:scale-95 shadow-md"
                    onClick={() => onSearch(transactionSearchDTO)}>
                    거래내역 검색
                </button>
            </div>
        </div>
    );
};

export default TransactionSearchBar;