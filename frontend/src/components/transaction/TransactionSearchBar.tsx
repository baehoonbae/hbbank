import { useRecoilValue } from 'recoil';
import { accountState } from '../../atoms/account';
import { useTransactions } from '../../hooks/useTransactions';

const TransactionSearchBar = ({ search }: { search: () => void }) => {
    const { searchParams, updateSearchParams } = useTransactions();
    const accounts = useRecoilValue(accountState);

    return (
        <div className="p-6 bg-white rounded-xl shadow-lg border border-gray-100">
            <h2 className="text-xl font-bold text-gray-800 mb-4">거래내역 조회</h2>
            <div className="flex flex-col gap-5">
                <div className="relative">
                    <select
                        className="w-full p-3 border border-gray-300 rounded-lg appearance-none bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={searchParams.accountId ? searchParams.accountId.toString() : ''}
                        onChange={(e) => updateSearchParams({ accountId: Number(e.target.value) })}
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
                        value={searchParams.startDate ? searchParams.startDate : ''}
                        onChange={(e) => updateSearchParams({ startDate: e.target.value })}
                    />
                    <span className="text-gray-500 font-medium">~</span>
                    <input
                        type="date"
                        className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={searchParams.endDate ? searchParams.endDate : ''}
                        onChange={(e) => updateSearchParams({ endDate: e.target.value })}
                    />
                </div>

                <div className="relative">
                    <select
                        className="w-full p-3 border border-gray-300 rounded-lg appearance-none bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-transparent transition-all"
                        value={searchParams.transactionType}
                        onChange={(e) => updateSearchParams({ transactionType: Number(e.target.value) })}
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
                    onClick={search}>
                    거래내역 검색
                </button>
            </div>
        </div>
    );
};

export default TransactionSearchBar;