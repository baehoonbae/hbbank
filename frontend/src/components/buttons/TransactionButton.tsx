import { useNavigate } from "react-router-dom";

const TransactionButton = () => {
    const navigate = useNavigate();

    return (
        <div>
            <button
                className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mx-2"
                onClick={() => { navigate('/transaction'); }}
            >
                거래내역
            </button>
        </div>
    );
};

export default TransactionButton;
