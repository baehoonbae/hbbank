import { useForm } from 'react-hook-form';
import { AutoTransferRequestDTO, AutoTransferResponseDTO } from '../../atoms/transfer';
import { useTransfer } from '../../hooks/useTransfer';
import { useAccounts } from '../../hooks/useAccounts';
import { useEffect } from 'react';

const AutoTransferForm = ({ autoTransfer }: { autoTransfer: AutoTransferResponseDTO | null }) => {
    const { accounts } = useAccounts();
    const { registerAutoTransfer, updateAutoTransferRequest, updateAutoTransfer } = useTransfer();
    const { register, handleSubmit, formState: { errors }, watch, reset } = useForm<AutoTransferRequestDTO>();
    const isEdit = autoTransfer !== null;

    useEffect(() => {
        if (autoTransfer) {
            reset({
                fromAccountId: autoTransfer.fromAccountId,
                toAccountNumber: autoTransfer.toAccountNumber,
                amount: autoTransfer.amount,
                description: autoTransfer.description,
                transferDay: autoTransfer.transferDay,
                startDate: autoTransfer.startDate,
                endDate: autoTransfer.endDate
            });
        }
    }, [autoTransfer, reset]);

    useEffect(() => {
        if(autoTransfer) {
            updateAutoTransferRequest({
                fromAccountId: autoTransfer.fromAccountId,
                toAccountNumber: autoTransfer.toAccountNumber,
                amount: autoTransfer.amount,
                description: autoTransfer.description,
                transferDay: autoTransfer.transferDay,
                startDate: autoTransfer.startDate,
                endDate: autoTransfer.endDate
            });
        }
    }, [autoTransfer]);

    const startDate = watch('startDate');
    const fromAccountId = watch('fromAccountId');
    const today = new Date().toISOString().split('T')[0];

    const formatAccountNumber = (accountNumber: string | undefined) => {
        if (!accountNumber) return '';
        return `${accountNumber.substring(0, 3)}-${accountNumber.substring(3, 6)}-${accountNumber.substring(6, 8)}-${accountNumber.substring(8, 14)}-${accountNumber.substring(14)}`;
    }

    return (
        <div className="max-w-[480px] mx-auto p-6 space-y-6">
            <h1 className="text-2xl font-bold text-center">매월 자동이체 설정</h1>

            <div className="space-y-4">
                <div>
                    <select
                        {...register('fromAccountId', { required: '출금 계좌를 선택해주세요' })}
                        onChange={(e) => {
                            const selectedAccount = accounts.find(acc => acc.id === Number(e.target.value));
                            if (selectedAccount) {
                                updateAutoTransferRequest({ fromAccountId: selectedAccount.id });
                            }
                        }}
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 appearance-none hover:border-blue-400"
                    >
                        <option value="">어떤 계좌에서 보낼까요?</option>
                        {accounts.map((account) => (
                            <option key={account.accountNumber} value={account.id}>
                                {account.accountName} • {account.accountNumber}
                            </option>
                        ))}
                    </select>
                    {errors.fromAccountId && <p className="mt-2 text-sm text-red-500">{errors.fromAccountId.message}</p>}
                </div>
                <div>
                    <input
                        type="text"
                        {...register('toAccountNumber', {
                            required: '받는 계좌번호를 입력해주세요',
                            pattern: { value: /^[0-9]{15}$/, message: '하이픈(-)을 제외한 계좌번호 15자리를 정확히 입력해주세요' },
                            validate: value => value !== formatAccountNumber(accounts.find(acc => acc.id === fromAccountId)?.accountNumber) || '출금 계좌와 입금 계좌가 동일할 수 없습니다'
                        })}
                        onChange={(e) => updateAutoTransferRequest({ toAccountNumber: e.target.value })}
                        placeholder="어디로 보낼까요? (계좌번호 15자리)"
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.toAccountNumber && <p className="mt-2 text-sm text-red-500">{errors.toAccountNumber.message}</p>}
                </div>
                <div>
                    <input
                        type="number"
                        {...register('amount', {
                            required: '보낼 금액을 입력해주세요',
                            min: { value: 1, message: '1원 이상 입력해주세요' }
                        })}
                        onChange={(e) => updateAutoTransferRequest({ amount: Number(e.target.value) })}
                        placeholder="얼마를 보낼까요?"
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.amount && <p className="mt-2 text-sm text-red-500">{errors.amount.message}</p>}
                </div>

                <div>
                    <input
                        type="text"
                        {...register('description', {
                            required: '이체 설명을 입력해주세요'
                        })}
                        onChange={(e) => updateAutoTransferRequest({ description: e.target.value })}
                        placeholder="이체 설명을 입력해주세요"
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.description && <p className="mt-2 text-sm text-red-500">{errors.description.message}</p>}
                </div>

                <div>
                    <input
                        type="number"
                        {...register('transferDay', {
                            required: '이체일을 선택해주세요',
                            min: { value: 1, message: '1일부터 31일 중 선택해주세요' },
                            max: { value: 31, message: '1일부터 31일 중 선택해주세요' }
                        })}
                        onChange={(e) => updateAutoTransferRequest({ transferDay: Number(e.target.value) })}
                        placeholder="매월 몇 일에 보낼까요? (1-31)"
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.transferDay && <p className="mt-2 text-sm text-red-500">{errors.transferDay.message}</p>}
                </div>

                <div className="flex gap-4">
                    <div className="flex-1">
                        <input
                            type="date"
                            {...register('startDate', { required: '시작일을 선택해주세요' })}
                            onChange={(e) => updateAutoTransferRequest({ startDate: e.target.value })}
                            min={today}
                            className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                        />
                        {errors.startDate && <p className="mt-2 text-sm text-red-500">{errors.startDate.message}</p>}
                    </div>
                    <div className="flex-1">
                        <input
                            type="date"
                            {...register('endDate', {
                                required: '종료일을 선택해주세요',
                                validate: value => !startDate || value >= startDate || '종료일은 시작일 이후여야 합니다'
                            })}
                            onChange={(e) => updateAutoTransferRequest({ endDate: e.target.value })}
                            min={startDate || today}
                            className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                        />
                        {errors.endDate && <p className="mt-2 text-sm text-red-500">{errors.endDate.message}</p>}
                    </div>
                </div>

                <div>
                    <input
                        type="password"
                        {...register('password', { required: '계좌 비밀번호를 입력해주세요' })}
                        onChange={(e) => updateAutoTransferRequest({ password: e.target.value })}
                        placeholder="계좌 비밀번호 4자리"
                        maxLength={4}
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.password && <p className="mt-2 text-sm text-red-500">{errors.password.message}</p>}
                </div>
            </div>

            <button
                onClick={handleSubmit(isEdit ? () => updateAutoTransfer(autoTransfer.id) : registerAutoTransfer)}
                className="w-full py-4 px-5 mt-8 bg-blue-500 text-white text-lg font-semibold rounded-2xl hover:bg-blue-600 active:scale-[0.98] transition-all duration-200 ease-in-out shadow-lg shadow-blue-500/30"
            >
                자동이체 시작하기
            </button>
        </div>
    );
};

export default AutoTransferForm;