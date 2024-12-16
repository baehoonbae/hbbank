import AdditionalInfoForm from '../components/forms/AdditionalInfoForm';

const AdditionalInfoPage = () => {
    return (
        <div className="min-h-screen py-12 px-4 sm:px-6 lg:px-8 flex items-center justify-center">
            <div className="max-w-md w-full bg-white rounded-xl shadow-lg overflow-hidden transform hover:scale-105 transition duration-300">
                <div className="px-8 py-12">
                    <h2 className="text-3xl font-bold text-center text-gray-800 mb-8">추가 정보 입력</h2>
                    <AdditionalInfoForm />
                </div>
            </div>
        </div>
    );
};

export default AdditionalInfoPage;