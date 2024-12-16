import { useRecoilState } from 'recoil';
import { oAuth2UserAdditionalInfoState } from '../../atoms/user';
import { useUser } from '../../hooks/useUser';

const AdditionalInfoForm = () => {
    const { updateOAuth2UserAdditionalInfo } = useUser();
    const [, setOAuth2UserAdditionalInfo] = useRecoilState(oAuth2UserAdditionalInfoState);

    const handleChange = (field: string) => (e: React.ChangeEvent<HTMLInputElement>) => {
        setOAuth2UserAdditionalInfo(prev => ({
            ...prev!,
            [field]: e.target.value
        }));
    };

    const formFields = [
        { label: '생년월일', type: 'date', field: 'birth' },
        { label: '아이디', type: 'text', field: 'username' },
        { label: '주소', type: 'text', field: 'address' },
        { label: '전화번호', type: 'tel', field: 'phone' }
    ];

    return (
        <div className="space-y-5">
            {formFields.map(({ label, type, field }) => (
                <div key={field}>
                    <input
                        type={type}
                        className="w-full px-5 py-4 text-lg bg-gray-50 border-none rounded-2xl focus:ring-2 focus:ring-blue-500 focus:bg-white transition-all duration-300 ease-in-out placeholder:text-gray-400"
                        placeholder={label}
                        onChange={handleChange(field)}
                    />
                </div>
            ))}

            <button
                className="w-full py-4 px-5 mt-6 bg-blue-500 text-white text-lg font-semibold rounded-2xl hover:bg-blue-600 active:scale-[0.98] transition-all duration-200 ease-in-out shadow-lg shadow-blue-500/30"
                onClick={() => updateOAuth2UserAdditionalInfo()}
            >
                시작하기
            </button>
        </div>
    );
};

export default AdditionalInfoForm;
