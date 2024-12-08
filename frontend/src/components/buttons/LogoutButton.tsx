import { useNavigate } from "react-router-dom";
import http from "../../api/http";

const LogoutButton = () => {
    const navigate = useNavigate();

    const handleLogout = async () => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            await http.post('/user/logout', null, {
                headers: {Authorization: `Bearer ${accessToken}`}
            });
            sessionStorage.removeItem('accessToken');
            sessionStorage.removeItem('user');
            window.dispatchEvent(new Event('storage'));
            navigate('/');
        } catch (error) {
            console.error('로그아웃 실패:', error);
        }
    }

    return (
        <div>
            <button
                className="bg-red-500 hover:bg-red-700 text-white font-bold py-2 px-4 rounded mx-2"
                onClick={handleLogout}
            >
                로그아웃
            </button>
        </div>
    );
};

export default LogoutButton;
