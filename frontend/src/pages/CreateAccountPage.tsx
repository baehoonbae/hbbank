import { useState } from "react";
import { useRecoilValue } from "recoil";
import { AccountCreateDTO, accountTypeState } from '../atoms/account';
import CreateAccountForm from "../components/forms/CreateAccountForm";
import { useAccounts } from "../hooks/useAccounts";

const CreateAccount = () => {
    const user = sessionStorage.getItem('user');
    const { createAccount } = useAccounts();
    const accountTypes = useRecoilValue(accountTypeState);
    const [formData, setFormData] = useState<AccountCreateDTO>({
        userId: user ? JSON.parse(user).id : 0,
        accountTypeCode: '',
        balance: 0,
        password: '',
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    return (
        <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8 flex items-center justify-center">
            <div className="max-w-md w-full bg-white rounded-xl shadow-2xl p-8 transform hover:scale-105 transition duration-300">
                <h2 className="text-4xl font-extrabold text-center mb-8">
                    <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">
                        새로운 계좌 개설
                    </span>
                </h2>
                <CreateAccountForm 
                    formData={formData}
                    accountTypes={accountTypes}
                    onChange={handleChange}
                    onSubmit={() => createAccount(formData)}
                />
            </div>
        </div>
    );
};

export default CreateAccount;