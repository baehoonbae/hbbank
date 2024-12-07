import { useNavigate } from "react-router-dom";

const Home = () => {
    const navigate = useNavigate();
    
    return (
        <div>
            <h1>HB은행에 오신 것을 환영합니다</h1>
            <div className="card">
                <button
                    className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded mx-2"
                    onClick={() => { navigate('/login'); }}
                >
                    로그인
                </button>
                <button
                    className="bg-green-500 hover:bg-green-700 text-white font-bold py-2 px-4 rounded mx-2"
                    onClick={() => { navigate('/signup'); }}
                >
                    회원가입
                </button>
            </div>
            <p className="read-the-docs">
                HB은행과 함께 더 나은 금융생활을 시작하세요
            </p>
        </div>
    );
};

export default Home;
