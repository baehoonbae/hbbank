import { useEffect } from "react";
import { useState } from "react";
import AutoTransferForm from "../components/forms/AutoTransferForm";
import { AutoTransferResponseDTO } from "../atoms/transfer";
import { useParams } from "react-router-dom";
import { useAutoTransfer } from "../hooks/useAutoTransfer";

const AutoTransferEditPage = () => {
    const { id } = useParams();
    const { fetchAutoTransfer } = useAutoTransfer();    
    const [autoTransfer, setAutoTransfer] = useState<AutoTransferResponseDTO | null>(null);

    useEffect(() => {
        fetchAutoTransfer(Number(id)).then(setAutoTransfer);
    }, [id]);

    return (
        <div>
            <AutoTransferForm autoTransfer={autoTransfer} />
        </div>
    );
};

export default AutoTransferEditPage;