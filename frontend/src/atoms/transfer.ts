import { atom } from "recoil";

export enum TransferType {
    INSTANT,
    AUTO,
    RESERVE
}

export interface TransferRequestDTO {
    type: TransferType;
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

export interface ReserveTransferRequestDTO {
    userId: number;
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    description: string;
    reservedAt: string;
    password: string;
}

export interface ReserveTransferResponseDTO {
    id: number;
    userId: number;
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    description: string;
    reservedAt: string;
}

export const transferRequestState = atom<TransferRequestDTO>({
    key: 'transferRequestState',
    default: {
        type: TransferType.INSTANT,
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

export const reserveTransferListState = atom<ReserveTransferResponseDTO[]>({
    key: 'reserveTransferListState',
    default: []
});

export const reserveTransferRequestState = atom<ReserveTransferRequestDTO>({
    key: 'reserveTransferRequestState',
    default: {
        userId: 0,
        fromAccountId: 0,
        toAccountNumber: '',
        amount: 0,
        description: '',
        reservedAt: '',
        password: ''
    }
});

