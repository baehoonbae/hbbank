import { AccountType, AccountCreateDTO } from '../../types/account';

interface CreateAccountFormProps {
    formData: AccountCreateDTO;
    accountTypes?: AccountType[];
    onChange: (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => void;
    onSubmit: () => void;
}

const CreateAccountForm = ({ formData, accountTypes, onChange, onSubmit }: CreateAccountFormProps) => {
    return (
        <div className="space-y-6">
            <div className="relative">
                <select
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-transparent transition duration-200 appearance-none"
                    name="accountTypeCode"
                    value={formData.accountTypeCode}
                    onChange={onChange}
                >
                    <option value="">계좌 종류를 선택하세요</option>
                    {accountTypes?.map((type) => (
                        <option key={type.code} value={type.code}>
                            {type.name} (금리: {type.interestRate}% | 최소잔액: {type.minimumBalance.toLocaleString()}원)
                        </option>
                    ))}
                </select>
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 pointer-events-none">
                    <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                    </svg>
                </div>
            </div>

            <div className="relative">
                <input
                    type="number"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-transparent transition duration-200"
                    placeholder="초기 입금액을 입력하세요"
                    name="balance"
                    value={formData.balance}
                    onChange={onChange}
                />
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400">
                    원
                </div>
            </div>

            <div className="relative">
                <input
                    type="password"
                    maxLength={4}
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-400 focus:border-transparent transition duration-200"
                    placeholder="4자리 숫자를 입력하세요"
                    name="password"
                    value={formData.password}
                    onChange={onChange}
                />
                <div className="absolute right-3 top-1/2 transform -translate-y-1/2">
                    <svg className="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clipRule="evenodd" />
                    </svg>
                </div>
            </div>

            <button
                type="submit"
                className="w-full py-4 px-4 bg-gradient-to-r from-blue-500 to-purple-500 text-white text-lg font-bold rounded-lg shadow-md hover:from-blue-600 hover:to-purple-600 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transform hover:-translate-y-1 transition duration-200"
                onClick={onSubmit}
            >
                계좌 개설하기
            </button>
        </div>
    );
};

export default CreateAccountForm; 