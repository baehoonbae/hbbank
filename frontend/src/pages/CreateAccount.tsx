import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import http from "../api/http";
import CreateAccountForm from "../components/forms/CreateAccountForm";
import { AccountType, AccountCreateDTO } from '../types/account';

const CreateAccount = () => {
    const user = sessionStorage.getItem('user');
    const navigate = useNavigate();
    const [accountTypes, setAccountTypes] = useState<AccountType[]>();
    const [formData, setFormData] = useState<AccountCreateDTO>({
        userId: user ? JSON.parse(user).id : 0,
        accountTypeCode: '',
        balance: 0,
        password: '',
    });

    useEffect(() => {
        getAccountTypes();
    }, []);

    const getAccountTypes = async () => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            const response = await http.get('/account/account-types', {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            setAccountTypes(response.data);
        } catch (error) {
            console.error(error);
        }
    }

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
    };

    const handleCreateAccount = async () => {
        try {
            const accessToken = sessionStorage.getItem('accessToken');
            await http.post('/account/create', formData, {
                headers: {
                    'Authorization': `Bearer ${accessToken}`
                }
            });
            navigate('/');
        } catch (error) {
            console.error(error);
        }
    }

    return (
        <div className="min-h-screen bg-gradient-to-br from-blue-100 to-purple-100 py-12 px-4 sm:px-6 lg:px-8 flex items-center justify-center">
            <div className="max-w-md w-full bg-white rounded-xl shadow-2xl p-8 transform hover:scale-105 transition duration-300">
                <h2 className="text-4xl font-extrabold text-center mb-8">
                    <span className="bg-clip-text text-transparent bg-gradient-to-r from-blue-500 to-purple-500">
                        새로운 계좌 개설
                    </span>
                </h2>
                <CreateAccountForm 
                    formData={formData}
                    accountTypes={accountTypes}
                    onChange={handleChange}
                    onSubmit={handleCreateAccount}
                />
            </div>
        </div>
    );
};

export default CreateAccount;