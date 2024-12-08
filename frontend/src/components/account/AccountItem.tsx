import { Account } from "../../types/account";

const AccountItem = ({ account }: { account: Account }) => {
    return (
        <div
            className="bg-white p-6 rounded-lg shadow-md hover:shadow-lg transition-shadow"
        >
            <div className="flex justify-between items-center">
                <div>
                    <h2 className="text-xl font-semibold text-gray-700">{account.accountName}</h2>
                    <p className="text-gray-500 mt-1">{account.accountNumber}</p>
                </div>
                <div className="text-right">
                    <p className="text-2xl font-bold text-gray-900">
                        {account.balance.toLocaleString()}원
                    </p>
                    <p className="text-sm text-gray-500 mt-1">
                        금리 {account.interestRate}%
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AccountItem;
