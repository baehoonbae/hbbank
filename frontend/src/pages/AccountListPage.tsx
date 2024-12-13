import { useNavigate } from "react-router-dom";
import { useRecoilValue } from "recoil";
import { accountState } from "../atoms/account";
import AccountItem from '../components/account/AccountItem';

const AccountList = () => {
    const navigate = useNavigate();
    const accounts = useRecoilValue(accountState);

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