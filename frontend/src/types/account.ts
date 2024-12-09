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
