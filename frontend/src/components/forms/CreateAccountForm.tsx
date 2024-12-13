import { useEffect } from 'react';
import { AccountType, AccountCreateDTO } from '../../atoms/account';
import { ChevronDownIcon, LockClosedIcon, BanknotesIcon, BuildingLibraryIcon, SparklesIcon } from "@heroicons/react/24/solid";
import { useAccounts } from '../../hooks/useAccounts';

interface CreateAccountFormProps {
    formData: AccountCreateDTO;
    accountTypes?: AccountType[];
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void;
    onSubmit: () => void;
}

const CreateAccountForm = ({ formData, accountTypes, onChange, onSubmit }: CreateAccountFormProps) => {
    const { fetchAccountTypes } = useAccounts();

    useEffect(() => {
        fetchAccountTypes();
    }, []); 

    return (
        <div className="space-y-8">
            <div className="relative">
                <label className="block text-sm font-bold text-gray-700 mb-2">
                    어떤 계좌를 만들까요? <BuildingLibraryIcon className="w-4 h-4 inline" />
                </label>
                <select
                    className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 appearance-none bg-white hover:border-blue-400"
                    name="accountTypeCode"
                    value={formData.accountTypeCode}
                    onChange={onChange}
                >
                    <option value="">계좌 종류 선택하기</option>
                    {accountTypes?.map((type) => (
                        <option key={type.code} value={type.code}>
                            {type.name} | 금리 {type.interestRate}%
                        </option>
                    ))}
                </select>
                <div className="absolute right-4 top-[60%] transform -translate-y-1/2 pointer-events-none">
                    <ChevronDownIcon className="h-6 w-6 text-blue-500" />
                </div>
            </div>

            <div className="relative">
                <label className="block text-sm font-bold text-gray-700 mb-2">
                    얼마를 넣을까요? <BanknotesIcon className="w-4 h-4 inline" />
                </label>
                <input
                    type="number"
                    className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    placeholder="금액을 입력해 주세요" 
                    name="balance"
                    value={formData.balance}
                    onChange={onChange}
                />
                <div className="absolute right-5 top-[65%] transform -translate-y-1/2 text-gray-400 font-medium pointer-events-none">
                    원
                </div>
            </div>

            <div className="relative">
                <label className="block text-sm font-bold text-gray-700 mb-2">
                    계좌 비밀번호를 설정해 주세요 <LockClosedIcon className="w-4 h-4 inline" />
                </label>
                <input
                    type="password"
                    maxLength={4}
                    className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    placeholder="4자리 숫자"
                    name="password"
                    value={formData.password}
                    onChange={onChange}
                />
                <div className="absolute right-4 top-[60%] transform -translate-y-1/2">
                    <LockClosedIcon className="h-6 w-6 text-blue-500" />
                </div>
            </div>

            <button
                type="submit"
                className="w-full py-5 px-6 mt-4 bg-blue-500 text-white text-xl font-bold rounded-2xl shadow-lg hover:bg-blue-600 focus:outline-none focus:ring-4 focus:ring-blue-200 transform hover:-translate-y-1 transition-all duration-200 relative overflow-hidden group"
                onClick={onSubmit}
            >
                <span className="relative z-10 flex items-center justify-center gap-2">
                    계좌 만들기 
                    <SparklesIcon className="w-6 h-6" />
                </span>
                <div className="absolute inset-0 bg-gradient-to-r from-blue-600 to-blue-400 transform scale-x-0 group-hover:scale-x-100 transition-transform duration-200 origin-left"></div>
            </button>
        </div>
    );
};

export default CreateAccountForm;