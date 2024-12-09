export interface TransferRequestDTO {
    fromAccountId: number;
    toAccountNumber: string;
    amount: number;
    password: string;
}