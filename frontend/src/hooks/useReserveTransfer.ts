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

    const fetchReserveTransfer = async (id: number) => {
        try {
            const response = await http.get(`/reserve-transfer/${id}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            return response.data;
        } catch (error) {
            console.error('예약이체 조회 실패:', error);
            return null;
        }
    }

    const fetchReserveTransfers = async () => {
        try {
            const userId = JSON.parse(sessionStorage.getItem('user') || '{}').id;
            const response = await http.get(`/reserve-transfer/list/${userId}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            return response.data;
        } catch (error) {
            console.error('예약이체 목록 조회 실패:', error);
            return [];
        }
    }

    const updateReserveTransfer = async (id: number) => {
        try {
            await http.put(`/reserve-transfer/${id}`, reserveTransferRequest, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            alert('예약이체가 수정되었습니다.');
            navigate('/reserve-transfer/manage');
            return true;
        } catch (error) {
            console.error('예약이체 업데이트 실패:', error);
            return false;
        }
    }

    const deleteReserveTransfer = async (id: number) => {
        try {
            await http.delete(`/reserve-transfer/${id}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            alert('예약이체가 삭제되었습니다.');
            navigate('/reserve-transfer/manage');
            return true;
        } catch (error) {
            console.error('예약이체 삭제 실패:', error);
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
        fetchReserveTransfer,
        fetchReserveTransfers,
        updateReserveTransfer,
        deleteReserveTransfer
    }
}