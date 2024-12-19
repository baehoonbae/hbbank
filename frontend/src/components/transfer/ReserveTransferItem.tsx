import { ReserveTransferResponseDTO } from "../../atoms/transfer";
import { PencilSquareIcon, TrashIcon } from "@heroicons/react/24/outline";
import { useNavigate } from "react-router-dom";
import { useReserveTransfer } from "../../hooks/useReserveTransfer";

const ReserveTransferItem = ({ reserveTransfer }: { reserveTransfer: ReserveTransferResponseDTO }) => {
    const navigate = useNavigate();
    const { deleteReserveTransfer } = useReserveTransfer();
    if (!reserveTransfer) return null;

    return (
        <div className="bg-gradient-to-r from-blue-50 to-indigo-50 p-8 rounded-2xl shadow-2xl hover:shadow-3xl transition-all duration-300 hover:scale-[1.02] border border-blue-100">
            <div className="flex justify-between items-start">
                <div className="space-y-3 text-left">
                    <h2 className="text-2xl font-extrabold text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600 truncate max-w-[300px] -ml-1">
                        {reserveTransfer.description}
                    </h2>
                    <p className="text-gray-600 font-medium tracking-wider whitespace-nowrap -ml-1">
                        예약일시: {reserveTransfer.reservedAt.replace('T', ' ').slice(0, -3)}
                    </p>
                </div>
                <div className="text-right space-y-3">
                    <div className="flex justify-end items-center gap-2">
                        <p className="text-3xl font-black text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-indigo-600 whitespace-nowrap">
                            {reserveTransfer.amount.toLocaleString()}
                            <span className="text-xl ml-1">원</span>
                        </p>
                        <button
                            onClick={() => navigate(`/reserve-transfer/edit/${reserveTransfer.id}`)}
                            className="p-2 hover:bg-indigo-100 rounded-full transition-colors"
                        >
                            <PencilSquareIcon className="w-5 h-5 text-indigo-600" />
                        </button>
                        <button
                            onClick={() => deleteReserveTransfer(reserveTransfer.id)}
                            className="p-2 hover:bg-red-100 rounded-full transition-colors"
                        >
                            <TrashIcon className="w-5 h-5 text-red-600" />
                        </button>
                    </div>
                    <p className="text-sm font-semibold text-indigo-600 bg-indigo-100 px-4 py-1 rounded-full inline-block truncate max-w-[200px]">
                        이체계좌: {reserveTransfer.toAccountNumber}
                    </p>
                </div>
            </div>
        </div>
    );
};

export default ReserveTransferItem;
