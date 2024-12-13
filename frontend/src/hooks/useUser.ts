import { useNavigate } from 'react-router-dom';
import { useRecoilState } from 'recoil';
import http from '../api/http';
import { userLoginState, UserResponseDTO, userResponseState, userSignUpState } from '../atoms/user';
export const useUser = () => {
    const [signUpData, setSignUpData] = useRecoilState(userSignUpState);
    const [loginData, setLoginData] = useRecoilState(userLoginState);
    const [userData, setUserData] = useRecoilState(userResponseState);
    const navigate = useNavigate();

    const login = async () => {
        try {
            const response = await http.post<UserResponseDTO>('/user/login', loginData);
            setUserData(response.data);
            sessionStorage.setItem('accessToken', response.data.accessToken);
            sessionStorage.setItem('user', JSON.stringify(response.data));
            alert(response.data.message);
            navigate('/');
            return true;
        } catch (error) {
            console.error('로그인 실패:', error);
            return false;
        }
    };

    const signUp = async () => {
        try {
            await http.post('/user/signup', signUpData);
            alert('회원가입이 완료되었습니다.');
            navigate('/');
            return true;
        } catch (error) {
            console.error('회원가입 실패:', error);
            return false;
        }
    };

    const logout = () => {
        setUserData(null);
        sessionStorage.removeItem('accessToken');
    };

    return {
        signUpData,
        loginData,
        userData,
        setSignUpData,
        setLoginData,
        login,
        signUp,
        logout
    };
};
