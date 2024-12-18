import { useRecoilState } from 'recoil';
import { autoTransferRequestState, TransferRequestDTO, transferRequestState } from '../atoms/transfer';
import http from '../api/http';
import { useNavigate } from 'react-router-dom';

export const useTransfer = () => {
    const [transferRequest, setTransferRequest] = useRecoilState(transferRequestState);
    const [autoTransferRequest, setAutoTransferRequest] = useRecoilState(autoTransferRequestState);
    const navigate = useNavigate();

    const transfer = async () => {
        const updatedTransferRequest = { ...transferRequest, password: transferRequest.password } as TransferRequestDTO;
        if (!updatedTransferRequest || !updatedTransferRequest.fromAccountId || !updatedTransferRequest.toAccountNumber || !updatedTransferRequest.amount) {
            alert('모든 필드를 입력해주세요.');
            return;
        }
        if (updatedTransferRequest.amount <= 0) {
            alert('송금액은 0보다 커야 합니다.');
            return;
        }
        try {
            await http.post('/transfer', updatedTransferRequest, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            alert('이체가 완료되었습니다.');
            navigate('/');
            return true;
        } catch (error) {
            console.error('이체 실패:', error);
            return false;
        }
    };

    const registerAutoTransfer = async () => {
        try {
            const userId = JSON.parse(sessionStorage.getItem('user') || '{}').id;
            await http.post('/auto-transfer/register', { ...autoTransferRequest, userId: userId }, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            alert('자동이체가 등록되었습니다.');
            navigate('/auto-transfer/manage');
            return true;
        } catch (error) {
            console.error('자동이체 실패:', error);
            return false;
        }
    }

    const fetchAutoTransfer = async (id: number) => {
        try {
            const response = await http.get(`/auto-transfer/${id}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            return response.data;
        } catch (error) {
            console.error('자동이체 조회 실패:', error);
            return null;
        }
    }

    const fetchAutoTransferList = async () => {
        try {
            const userId = JSON.parse(sessionStorage.getItem('user') || '{}').id;
            const response = await http.get(`/auto-transfer/list/${userId}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            return response.data;
        } catch (error) {
            console.error('자동이체 목록 조회 실패:', error);
            return [];
        }
    }

    const updateAutoTransfer = async (id: number) => {
        try {
            await http.put(`/auto-transfer/${id}`, autoTransferRequest, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            alert('자동이체가 수정되었습니다.');
            navigate('/auto-transfer/manage');
            return true;
        } catch (error) {
            console.error('자동이체 수정 실패:', error);
            return false;
        }
    }

    const deleteAutoTransfer = async (id: number) => {
        try {
            await http.delete(`/auto-transfer/${id}`, {
                headers: {
                    Authorization: `Bearer ${sessionStorage.getItem('accessToken')}`
                }
            });
            alert('자동이체가 삭제되었습니다.');
            navigate('/auto-transfer/manage');
            return true;
        } catch (error) {
            console.error('자동이체 삭제 실패:', error);
            return false;
        }
    }

    const updateTransferRequest = (data: Partial<typeof transferRequest>) => {
        setTransferRequest(prev => ({ ...prev, ...data }));
    };

    const updateAutoTransferRequest = (data: Partial<typeof autoTransferRequest>) => {
        setAutoTransferRequest(prev => ({ ...prev, ...data }));
    };

    return {
        transferRequest,
        transfer,
        updateTransferRequest,

        autoTransferRequest,
        updateAutoTransferRequest,
        registerAutoTransfer,
        fetchAutoTransfer,
        fetchAutoTransferList,
        updateAutoTransfer,
        deleteAutoTransfer
    };
};
