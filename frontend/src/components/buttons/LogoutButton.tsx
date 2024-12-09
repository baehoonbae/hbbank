import { handleLogout } from "../../api/http";

const LogoutButton = () => {
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
