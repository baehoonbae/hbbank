import { CreditCardIcon } from "@heroicons/react/24/solid";

const AccountCountSection = (total: { total: number }) => {
    return (
        <>
            <div className="bg-white p-7 rounded-2xl shadow-sm hover:shadow-md transition-all duration-200 border border-gray-100">
                <div className="flex items-center gap-3 mb-5">
                    <CreditCardIcon className="w-8 h-8 text-gray-900" />
                    <h2 className="text-xl font-bold text-gray-900">내 계좌</h2>
                </div>
                <div className="text-4xl font-bold text-gray-900">{total.total}<span className="text-lg ml-1">개</span></div>
                <p className="text-gray-500 mt-2 text-sm">총 보유 계좌 수</p>
            </div>
        </>
    )
}

export default AccountCountSection;