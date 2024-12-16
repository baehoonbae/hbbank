import { useNavigate } from 'react-router-dom';
import { useRecoilState } from 'recoil';
import http from '../api/http';
import { oAuth2UserAdditionalInfoState, userLoginState, UserResponseDTO, userResponseState, userSignUpState } from '../atoms/user';
export const useUser = () => {
    const [signUpData, setSignUpData] = useRecoilState(userSignUpState);
    const [loginData, setLoginData] = useRecoilState(userLoginState);
    const [userData, setUserData] = useRecoilState(userResponseState);
    const [oAuth2UserAdditionalInfo,] = useRecoilState(oAuth2UserAdditionalInfoState);
    const navigate = useNavigate();

    const me = async () => {
        const accessToken = sessionStorage.getItem('accessToken');
        const response = await http.get('/user/me', {
            headers: {
                Authorization: `Bearer ${accessToken}`
            }
        });
        setUserData(response.data);
        return response.data;
    }

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

    const updateOAuth2UserAdditionalInfo = async () => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            await http.post('/user/oauth2/additional-info', oAuth2UserAdditionalInfo, {
                headers: {
                    Authorization: `Bearer ${accessToken}`
                }
            });
            alert('추가 정보가 성공적으로 저장되었습니다.');
            navigate('/');

        } catch (error) {
            console.error('추가 정보 업데이트 실패:', error);

        }
    };

    return {
        signUpData,
        loginData,
        userData,
        setSignUpData,
        setLoginData,
        login,
        signUp,
        logout,
        updateOAuth2UserAdditionalInfo,
        me
    };
};
