import { atom } from "recoil";

export interface TransferRequestDTO {
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    password: string;
}

export interface AutoTransferRequestDTO {
    userId: number;
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    description: string;
    transferDay: number;
    startDate: string;
    endDate: string;
    password: string;
}

export interface AutoTransferResponseDTO {
    id: number;
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    description: string;
    transferDay: number;
    nextTransferDate: string;
    startDate: string;
    endDate: string;
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
        userId: 0,
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

export const autoTransferListState = atom<AutoTransferResponseDTO[]>({
    key: 'autoTransferListState',
    default: []
});
