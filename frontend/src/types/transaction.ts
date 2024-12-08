import { Account } from "./account";

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