import { atom } from 'recoil';

export interface Account {
    id: number,
    accountName: string,
    accountNumber: string,
    balance: number,
    interestRate: number,
}

export interface AccountType {
    code: string;
    description: string;
    interestRate: number;
    minimumBalance: number;
    name: string;
}

export interface AccountCreateDTO {
    userId: number;
    accountTypeCode: string;
    balance: number;
    password: string;
}

export const accountState = atom<Account[]>({
    key: 'accountState',
    default: []
});

export const accountTypeState = atom<AccountType[]>({
    key: 'accountTypeState', 
    default: []
});

export const selectedAccountState = atom<Account | null>({
    key: 'selectedAccountState',
    default: null
});
