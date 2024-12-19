import { useRecoilState } from "recoil";
import { reserveTransferRequestState } from "../atoms/transfer";
import http from "../api/http";
import { useNavigate } from "react-router-dom";

export const useReserveTransfer = () => {
    const [reserveTransferRequest, setReserveTransferRequest] = useRecoilState(reserveTransferRequestState);
    const navigate = useNavigate();

    const registerReserveTransfer = async () => {
        try {
            const userId = JSON.parse(sessionStorage.getItem('user') || '{}').id;
            await http.post('/reserve-transfer/register', { ...reserveTransferRequest, userId: userId }, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }   
            });
            alert('예약이체가 등록되었습니다.');
            navigate('/reserve-transfer/manage');
            return true;
        } catch (error) {
            console.error('예약이체 실패:', error);
            return false;
        }
    }

    const updateReserveTransferRequest = (data: Partial<typeof reserveTransferRequest>) => {
        setReserveTransferRequest(prev => ({ ...prev, ...data }));
    };

    return {
        reserveTransferRequest,
        registerReserveTransfer,
        updateReserveTransferRequest,
    }
}