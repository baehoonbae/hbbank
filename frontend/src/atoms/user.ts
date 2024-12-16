import { atom } from 'recoil';

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

export interface OAuth2UserAdditionalInfoDTO {
    birth: string;
    username: string;
    address: string;
    phone: string;
}

export const userSignUpState = atom<UserSignUpDTO>({
    key: 'userSignUpState',
    default: {
        name: '',
        birth: '',
        username: '',
        password: '',
        address: '',
        phone: '',
        email: ''
    }
});

export const userLoginState = atom<UserLoginDTO>({
    key: 'userLoginState',
    default: {
        username: '',
        password: ''
    }
});

export const userResponseState = atom<UserResponseDTO | null>({
    key: 'userResponseState',
    default: null
});

export const oAuth2UserAdditionalInfoState = atom<OAuth2UserAdditionalInfoDTO | null>({
    key: 'oAuth2UserAdditionalInfoState',
    default: null
});
