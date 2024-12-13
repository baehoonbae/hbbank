import { useRecoilState } from 'recoil';
import { transactionState, transactionSearchState } from '../atoms/transaction';
import http from '../api/http';

export const useTransactions = () => {
    const [transactions, setTransactions] = useRecoilState(transactionState);
    const [searchParams, setSearchParams] = useRecoilState(transactionSearchState);

    const searchTransactions = async () => {
        try {
            const response = await http.post('/transaction/transactions/search', searchParams, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            setTransactions(response.data);
        } catch (error) {
            console.error('거래내역 조회 실패:', error);
        }
    };

    const fetchTransactions = async (accountId: number) => {
        try {
            const response = await http.get(`/transaction/transactions/${accountId}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            setTransactions(response.data);
        } catch (error) {
            console.error('거래내역 조회 실패:', error);
        }
    };

    const fetchMonthlyTransactions = async () => {
        try {
            const response = await http.post(`/transaction/transactions/search`,
                {
                    accountId: 0,
                    startDate: new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0],
                    endDate: new Date(new Date().getFullYear(), new Date().getMonth() + 1, 0).toISOString().split('T')[0],
                    transactionType: 2,
                    page: 0,
                },
                {
                    headers: {
                        Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                    }
                }
            );
            return response.data;
        } catch (error) {
            console.error('월별 거래내역 조회 실패:', error);
        }
    };

    const updateSearchParams = (newParams: Partial<typeof searchParams>) => {
        setSearchParams(prev => ({ ...prev, ...newParams }));
    };

    return { transactions, searchParams, searchTransactions, updateSearchParams, fetchTransactions, fetchMonthlyTransactions };
};
