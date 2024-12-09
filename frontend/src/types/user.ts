export interface UserSignUpDTO {
    name: string;
    birth: string;
    username: string;
    password: string;
    address: string;
    phone: string;
    email: string;
}

export interface UserLoginDTO {
    username: string;
    password: string;
}

export interface UserResponseDTO {
    id: number;
    name: string;
    username: string;
    email: string;
    accessToken: string;
    message: string;
}

