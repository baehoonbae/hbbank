import { Account } from "../../types/account";

const AccountItem = ({ account }: { account: Account }) => {
    return (
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-8 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 hover:scale-[1.02] border border-blue-100">
            <div className="flex justify-between items-center">
                <div className="space-y-3">
                    <h2 className="text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">
                        {account.accountName}
                    </h2>
                    <p className="text-gray-600 font-medium tracking-wider">
                        {account.accountNumber}
                    </p>
                </div>
                <div className="text-right space-y-3">
                    <p className="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600">
                        {account.balance.toLocaleString()}
                        <span className="text-xl ml-1">원</span>
                    </p>
                    <p className="text-sm font-semibold text-indigo-600 bg-indigo-100 px-4 py-1 rounded-full inline-block">
                        금리 {account.interestRate}%
                    </p>
                </div>
            </div>
        </div>
    );
};

export default AccountItem;
