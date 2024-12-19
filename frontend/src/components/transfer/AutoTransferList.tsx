import { useEffect, useState } from "react";
import AutoTransferItem from "./AutoTransferItem";
import { AutoTransferResponseDTO } from "../../atoms/transfer";
import { ArrowsRightLeftIcon } from "@heroicons/react/24/outline";
import { useNavigate } from "react-router-dom";
import { useAutoTransfer } from "../../hooks/useAutoTransfer";

const AutoTransferList = () => {
    const navigate = useNavigate();
    const [autoTransferList, setAutoTransferList] = useState<AutoTransferResponseDTO[]>([]);
    const { fetchAutoTransferList } = useAutoTransfer();

    useEffect(() => {
        fetchAutoTransferList().then(setAutoTransferList);
    }, [fetchAutoTransferList]);

    if (autoTransferList.length === 0) {
        return (
            <div className="flex flex-col items-center justify-center p-8 bg-gray-50 rounded-lg">
                <ArrowsRightLeftIcon className="w-32 h-32 mb-4 text-gray-400" />
                <h3 className="text-xl font-bold text-gray-600 mb-2">
                    등록된 자동이체가 없습니다
                </h3>
                <p className="text-gray-500 text-center">
                    새로운 자동이체를 등록하여 편리하게 이체를 관리해보세요
                </p>
                <button
                    onClick={() => navigate("/auto-transfer")}
                    className="bg-blue-500 text-white px-4 py-2 rounded-md hover:bg-blue-600 transition-colors"
                >
                    자동이체 등록
                </button>
            </div>
        );
    }

    return (
        <div className="space-y-4">
            {autoTransferList.map((autoTransfer) => (
                <AutoTransferItem key={autoTransfer.id} autoTransfer={autoTransfer} />
            ))}
        </div>
    );
};

export default AutoTransferList;
