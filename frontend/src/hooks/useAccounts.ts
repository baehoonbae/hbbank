import { useNavigate } from 'react-router-dom';
import { useRecoilState } from 'recoil';
import http from '../api/http';
import { AccountCreateDTO, accountState, accountTypeState, selectedAccountState } from '../atoms/account';

export const useAccounts = () => {
    const [accounts, setAccounts] = useRecoilState(accountState);
    const [selectedAccount, setSelectedAccount] = useRecoilState(selectedAccountState);
    const [, setAccountTypes] = useRecoilState(accountTypeState);
    const navigate = useNavigate();

    const fetchAccount = async (accountId: number) => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await http.get(`/account/${accountId}`, {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            });
            setSelectedAccount(response.data);
        } catch (error) {
            console.error(error);
        }
    };

    const fetchAccounts = async () => {
        try {
            const user = JSON.parse(sessionStorage.getItem('user') || '{}');
            const response = await http.get(`/account/accounts/${user.id}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            setAccounts(response.data);
        } catch (error) {
            console.error('계좌 조회 실패:', error);
        }
    };

    const fetchAccountTypes = async () => {
        try {
            const response = await http.get('/account/account-types');
            setAccountTypes(response.data);
        } catch (error) {
            console.error('계좌 종류 조회 실패:', error);
        }
    };

    const createAccount = async (account: AccountCreateDTO) => {
        try {
            const response = await http.post('/account/create', account);
            setAccounts([...accounts, response.data]);
            alert('계좌 생성 완료');
            navigate('/');
        } catch (error) {
            console.error('계좌 생성 실패:', error);
        }
    };

    const selectAccount = (accountId: number) => {
        const account = accounts.find(acc => acc.id === accountId);
        setSelectedAccount(account || null);
    };

    return { accounts, selectedAccount, fetchAccount, fetchAccounts, createAccount, selectAccount, fetchAccountTypes };
};
