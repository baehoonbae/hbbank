import { atom } from 'recoil';

export interface Transaction {
    id: number;
    accountId: number;
    transactionDateTime: string;
    transactionType: string;
    sender: string;
    receiver: string;
    withdrawalAmount: number;
    depositAmount: number;
    balance: number;
}

export interface TransactionSearchDTO {
    userId: number;
    accountId: number | null;
    startDate: string | null;
    endDate: string | null;
    transactionType: number;
    page: number;
}

export const transactionState = atom<Transaction[]>({
    key: 'transactionState',
    default: []
});

export const transactionSearchState = atom<TransactionSearchDTO>({
    key: 'transactionSearchState',
    default: {
        userId: 0,
        accountId: 0,
        startDate: null,
        endDate: null,
        transactionType: 0,
        page: 0
    }
});