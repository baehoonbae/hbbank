import { useEffect } from "react";
import { useParams } from "react-router-dom";
import AccountItem from "../components/account/AccountItem";
import TransferButton from "../components/buttons/TransferButton";
import TransactionList from "../components/transaction/TransactionList";
import { useAccounts } from "../hooks/useAccounts";

const AccountDetail = () => {
    const { id } = useParams();
    const { fetchAccount, selectedAccount } = useAccounts();

    useEffect(() => {
        if (id) fetchAccount(Number(id));
    }, [id]);

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 p-8">
            <div className="max-w-5xl mx-auto space-y-8">
                <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
                    <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-600 to-indigo-600">
                        계좌 상세 정보
                    </span>
                </h1>
                {selectedAccount && <AccountItem account={selectedAccount} />}
                {selectedAccount && <TransferButton />}
                <TransactionList accountId={selectedAccount?.id ?? 0} />
            </div>
        </div>
    );
};

export default AccountDetail;