import { atom } from "recoil";

export interface TransferRequestDTO {
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
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