import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../api/http";
import AccountItem from '../components/account/AccountItem';
import { Account } from "../types/account";

const AccountList = () => {
    const navigate = useNavigate();
    const user = sessionStorage.getItem('user');
    const [accounts, setAccounts] = useState<Account[]>([]);

    useEffect(() => {
        getAccounts(user ? JSON.parse(user).id : 0);
    }, []);

    const getAccounts = async (userId: number) => {
        try {
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
        <>
            <h1 className="text-2xl font-bold mb-4">계좌 목록</h1>
            <div className="space-y-4">
                {accounts.map((account) => (
                    <button
                        className="w-full"
                        key={account.id}
                        onClick={() => { navigate(`/account/${account.id}`) }}
                    >
                        <AccountItem account={account} />
                    </button>
                ))}
            </div>
        </>
    );
};

export default AccountList;