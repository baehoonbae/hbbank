import { BanknotesIcon } from "@heroicons/react/24/solid";

const AccountBalanceSection = ({ total }: { total: number }) => {
    return (
        <>
            <div className="bg-white p-7 rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 border border-gray-100">
                <div className="flex items-center gap-3 mb-5">
                    <BanknotesIcon className="w-8 h-8 text-blue-500" />
                    <h2 className="text-xl font-bold text-gray-900">총 잔액</h2>
                </div>
                <div className="text-4xl font-bold text-blue-500">{total.toLocaleString()}<span className="text-lg ml-1">원</span></div>
                <p className="text-gray-500 mt-2 text-sm">전체 계좌 잔액</p>
            </div>
        </>
    )
}

export default AccountBalanceSection;