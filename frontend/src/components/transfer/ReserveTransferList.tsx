import { useEffect, useState } from "react";
import { ReserveTransferResponseDTO } from "../../atoms/transfer";
import { ArrowsRightLeftIcon } from "@heroicons/react/24/outline";
import { useNavigate } from "react-router-dom";
import { useReserveTransfer } from "../../hooks/useReserveTransfer";
import ReserveTransferItem from "./ReserveTransferItem";

const ReserveTransferList = () => {
    const navigate = useNavigate();
    const [reserveTransferList, setReserveTransferList] = useState<ReserveTransferResponseDTO[]>([]);
    const { fetchReserveTransfers } = useReserveTransfer();

    useEffect(() => {
        fetchReserveTransfers().then(setReserveTransferList);
    }, [fetchReserveTransfers]);

    if (reserveTransferList.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center p-8 bg-gray-50 rounded-lg">
                <ArrowsRightLeftIcon className="w-32 h-32 mb-4 text-gray-400" />
                <h3 className="text-xl font-bold text-gray-600 mb-2">
                    등록된 예약이체가 없습니다
                </h3>
                <p className="text-gray-500 text-center">
                    새로운 예약이체를 등록하여 편리하게 이체를 관리해보세요
                </p>
                <button
                    onClick={() => navigate("/reserve-transfer")}
                    className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition-colors"
                >
                    예약이체 등록
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {reserveTransferList.map((reserveTransfer) => (
                <ReserveTransferItem key={reserveTransfer.id} reserveTransfer={reserveTransfer} />
            ))}
        </div>
    );
};

export default ReserveTransferList;
