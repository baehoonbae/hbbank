import { useNavigate } from "react-router-dom";

const AccountListButton = () => {
    const navigate = useNavigate();

    return (
        <div>
            <button
                className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mx-2"
                onClick={() => { navigate('/account-list'); }}
            >
                계좌목록
            </button>
        </div>
    );
};  

export default AccountListButton;
