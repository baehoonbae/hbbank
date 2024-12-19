import { useEffect } from "react";
import { useState } from "react";
import { ReserveTransferResponseDTO } from "../atoms/transfer";
import { useParams } from "react-router-dom";
import ReserveTransferForm from "../components/forms/ReserveTransferForm";
import { useReserveTransfer } from "../hooks/useReserveTransfer";

const ReserveTransferEditPage = () => {
    const { id } = useParams();
    const { fetchReserveTransfer } = useReserveTransfer();
    const [reserveTransfer, setReserveTransfer] = useState<ReserveTransferResponseDTO | null>(null);

    useEffect(() => {
        fetchReserveTransfer(Number(id)).then(setReserveTransfer);
    }, [id]);

    return (
        <div>
            <ReserveTransferForm reserveTransfer={reserveTransfer} />
        </div>
    );
};

export default ReserveTransferEditPage;