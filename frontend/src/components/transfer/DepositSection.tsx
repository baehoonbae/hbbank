import { useState } from "react";

interface DepositSectionProps {
    onDepositChange: (accountNumber: string, amount: number) => void;
}

const DepositSection = ({ onDepositChange }: DepositSectionProps) => {
    const [toAccountNumber, setToAccountNumber] = useState<string>("");
    const [amount, setAmount] = useState<number>(0);

    const handleDepositChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const cleanedAccountNumber = e.target.value.replace(/-/g, '');
        setToAccountNumber(cleanedAccountNumber);
        onDepositChange(cleanedAccountNumber, amount);
    }

    const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setAmount(Number(e.target.value));
        onDepositChange(toAccountNumber, Number(e.target.value));
    }

    return (
        <>
            <div className="bg-gradient-to-r from-indigo-50 to-purple-50 rounded-2xl shadow-2xl p-10 mb-8 transform transition-all duration-300 hover:scale-[1.02] border border-purple-100 backdrop-blur-lg">
                <h3 className="text-2xl font-extrabold mb-8 text-transparent bg-clip-text bg-gradient-to-r from-indigo-600 to-purple-600 flex items-center">
                    <span className="w-2 h-8 bg-gradient-to-b from-indigo-500 to-purple-500 rounded-full mr-4 animate-pulse"></span>
                    입금계좌정보
                </h3>
                <div className="space-y-8">
                    <div className="flex items-center group">
                        <label className="w-40 text-gray-700 font-bold text-lg group-hover:text-indigo-600 transition-colors">입금계좌번호</label>
                        <input
                            type="text"
                            className="flex-1 p-4 border-2 border-gray-200 rounded-xl focus:ring-4 focus:ring-indigo-200 focus:border-indigo-400 transition-all duration-300 bg-white bg-opacity-70 backdrop-blur-sm"
                            placeholder="💳 계좌번호를 입력하세요"
                            onChange={handleDepositChange}
                        />
                    </div>
                    <div className="flex items-center group">
                        <label className="w-40 text-gray-700 font-bold text-lg group-hover:text-indigo-600 transition-colors">이체금액</label>
                        <div className="flex-1 relative">
                            <input
                                type="text"
                                inputMode="numeric"
                                pattern="[0-9]*"
                                className="w-full p-4 border-2 border-gray-200 rounded-xl focus:ring-4 focus:ring-indigo-200 focus:border-indigo-400 transition-all duration-300 bg-white bg-opacity-70 backdrop-blur-sm"
                                placeholder="💰 금액을 입력하세요"
                                onChange={handleAmountChange}
                            />
                            <span className="absolute right-6 top-1/2 -translate-y-1/2 text-gray-600 font-bold">원</span>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default DepositSection;
