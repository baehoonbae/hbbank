import { atom } from "recoil";

export interface TransferRequestDTO {
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    password: string;
}

export interface AutoTransferRequestDTO {
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    description: string;
    transferDay: number;
    startDate: string;
    endDate: string;
    password: string;
}

export const transferRequestState = atom<TransferRequestDTO>({
    key: 'transferRequestState',
    default: {
        fromAccountId: 0,
        toAccountNumber: '',
        amount: 0,
        password: ''
    }
});

export const autoTransferRequestState = atom<AutoTransferRequestDTO>({
    key: 'autoTransferRequestState',
    default: {
        fromAccountId: 0,
        toAccountNumber: '',
        amount: 0,
        description: '',
        transferDay: 0,
        startDate: '',
        endDate: '',
        password: ''
    }
});
