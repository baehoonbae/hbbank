import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../api/http";

const AccountList = () => {
    const navigate = useNavigate();
    const user = sessionStorage.getItem('user');
    useEffect(() => {
        getAccounts(user ? JSON.parse(user).id : 0);
    }, []);

    interface Account {
        id: number,
        accountName: string,
        accountNumber: string,
        balance: number,
        interestRate: number,
    }

    const [accounts, setAccounts] = useState<Account[]>([]);

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
        <div>
            <h1>환영합니다.</h1>
            <ul>
                {accounts.map((account) => (
                    <li key={account.id}>{account.accountName}</li>
                ))}
            </ul>
        </div>
    );
};

export default AccountList;