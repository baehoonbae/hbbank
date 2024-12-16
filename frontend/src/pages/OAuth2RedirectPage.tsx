import { useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSetRecoilState } from 'recoil';
import { userResponseState } from '../atoms/user';
import { useUser } from '../hooks/useUser';

const OAuth2RedirectPage = () => {
    const navigate = useNavigate();
    const setUserResponse = useSetRecoilState(userResponseState);
    const effectRan = useRef(false);
    const { me } = useUser();

    useEffect(() => {
        if (effectRan.current) return;

        const handleRedirect = async () => {
            try {
                const params = new URLSearchParams(window.location.search);
                const token = params.get('token');
                const needAdditionalInfo = params.get('needAdditionalInfo') === 'true';
                
                if (!token) {
                    navigate('/login');
                    return;
                }

                sessionStorage.setItem('accessToken', token);
                
                const user = await me();
                if (!user) {
                    throw new Error('사용자 정보를 가져올 수 없습니다.');
                }

                sessionStorage.setItem('user', JSON.stringify(user));
                setUserResponse(user);

                if (needAdditionalInfo) {
                    navigate('/oauth2/additional-info');
                } else {
                    navigate('/');
                }
            } catch (error) {
                console.error('OAuth 리다이렉트 처리 실패:', error);
                sessionStorage.clear();
                navigate('/login');
            }
        };

        handleRedirect();
        effectRan.current = true;
    }, [navigate, setUserResponse, me]);

    return <div>로그인 처리중...</div>;
};

export default OAuth2RedirectPage; 