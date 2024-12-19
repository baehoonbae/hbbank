import { useRecoilState } from 'recoil';
import { TransferRequestDTO, transferRequestState } from '../atoms/transfer';
import http from '../api/http';
import { useNavigate } from 'react-router-dom';

export const useTransfer = () => {
    const [transferRequest, setTransferRequest] = useRecoilState(transferRequestState);
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

    const updateTransferRequest = (data: Partial<typeof transferRequest>) => {
        setTransferRequest(prev => ({ ...prev, ...data }));
    };

    return {
        transferRequest,
        transfer,
        updateTransferRequest,
    };
};
