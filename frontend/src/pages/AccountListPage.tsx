import { useNavigate } from "react-router-dom";
import { useRecoilValue } from "recoil";
import { accountState } from "../atoms/account";
import AccountItem from '../components/account/AccountItem';

const AccountListPage = () => {
    const navigate = useNavigate();
    const accounts = useRecoilValue(accountState);

    return (
        <div className="space-y-4">
            <h1 className="text-2xl font-bold mb-4">계좌 목록</h1>
            <ul className="space-y-4">
                {accounts.map(account => (
                    <li key={`account-${account.id}`}>
                        <button
                            className="w-full text-left"
                            onClick={() => navigate(`/account/${account.id}`)}
                        >
                            <AccountItem account={account} />
                        </button>
                    </li>
                ))}
            </ul>
        </div>
    );
};

export default AccountListPage;