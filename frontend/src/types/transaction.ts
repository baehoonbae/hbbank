
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
    accountId: number | null;
    startDate: string | null;
    endDate: string | null;
    transactionType: number;
    page: number;
}