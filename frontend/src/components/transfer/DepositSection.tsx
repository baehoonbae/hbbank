import { useState } from "react";

interface DepositSectionProps {
    onDepositChange: (accountNumber: string, amount: number) => void;
}

const DepositSection = ({ onDepositChange }: DepositSectionProps) => {
    const [toAccountNumber, setToAccountNumber] = useState<string>("");
    const [amount, setAmount] = useState<number>(0);

    const handleDepositChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setToAccountNumber(e.target.value);
        onDepositChange(e.target.value, amount);
    }

    const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setAmount(Number(e.target.value));
        onDepositChange(toAccountNumber, Number(e.target.value));
    }

    return (
        <>
            <div className="bg-white rounded-xl shadow-lg p-8 mb-8 transform transition-all hover:scale-[1.01]">
                <h3 className="text-xl font-bold mb-6 text-gray-800 flex items-center">
                    <span className="w-2 h-6 bg-green-500 rounded-full mr-3"></span>
                    입금계좌정보
                </h3>
                <div className="space-y-6">
                    <div className="flex items-center">
                        <label className="w-36 text-gray-700 font-medium">입금계좌번호</label>
                        <input
                            type="text"
                            className="flex-1 p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                            placeholder="계좌번호를 입력하세요"
                            onChange={handleDepositChange}
                        />
                    </div>
                    <div className="flex items-center">
                        <label className="w-36 text-gray-700 font-medium">이체금액</label>
                        <div className="flex-1 relative">
                            <input
                                type="number"
                                className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all"
                                placeholder="금액을 입력하세요"
                                onChange={handleAmountChange}
                            />
                            <span className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-500">원</span>
                        </div>
                    </div>
                </div>
            </div>
        </>
    )
}

export default DepositSection;
