import { useForm } from "react-hook-form";
import { useAccounts } from "../../hooks/useAccounts";
import { ReserveTransferRequestDTO, ReserveTransferResponseDTO } from "../../atoms/transfer";
import { useReserveTransfer } from "../../hooks/useReserveTransfer";
import { useEffect } from "react";

const ReserveTransferForm = ({ reserveTransfer }: { reserveTransfer: ReserveTransferResponseDTO | null }) => {
    const { accounts } = useAccounts();
    const { registerReserveTransfer, updateReserveTransferRequest, updateReserveTransfer } = useReserveTransfer();
    const { register, handleSubmit, formState: { errors }, watch, reset } = useForm<ReserveTransferRequestDTO>();
    const isEdit = reserveTransfer !== null;
    const fromAccountId = watch('fromAccountId');
    const today = new Date().toISOString().split('T')[0];

    useEffect(() => {
        if (reserveTransfer) {
            reset({
                fromAccountId: reserveTransfer.fromAccountId,
                toAccountNumber: reserveTransfer.toAccountNumber,
                amount: reserveTransfer.amount,
                description: reserveTransfer.description,
                reservedAt: reserveTransfer.reservedAt,
            });
        }
    }, [reserveTransfer, reset]);

    useEffect(() => {
        if (reserveTransfer) {
            updateReserveTransferRequest({
                fromAccountId: reserveTransfer.fromAccountId,
                toAccountNumber: reserveTransfer.toAccountNumber,
                amount: reserveTransfer.amount,
                description: reserveTransfer.description,
                reservedAt: reserveTransfer.reservedAt,
            });
        }
    }, [reserveTransfer]);

    const formatAccountNumber = (accountNumber: string | undefined) => {
        if (!accountNumber) return '';
        return `${accountNumber.substring(0, 3)}-${accountNumber.substring(3, 6)}-${accountNumber.substring(6, 8)}-${accountNumber.substring(8, 14)}-${accountNumber.substring(14)}`;
    }

    return (
        <div className="max-w-[480px] mx-auto p-6 space-y-6">
            <h1 className="text-2xl font-bold text-center">예약이체</h1>

            <div className="space-y-4">
                <div>
                    <select
                        {...register('fromAccountId', { required: '출금 계좌를 선택해주세요' })}
                        onChange={(e) => {
                            const selectedAccount = accounts.find(acc => acc.id === Number(e.target.value));
                            if (selectedAccount) {
                                updateReserveTransferRequest({ fromAccountId: selectedAccount.id });
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
                        onChange={(e) => updateReserveTransferRequest({ toAccountNumber: e.target.value })}
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
                        onChange={(e) => updateReserveTransferRequest({ amount: Number(e.target.value) })}
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
                        onChange={(e) => updateReserveTransferRequest({ description: e.target.value })}
                        placeholder="이체 설명을 입력해주세요"
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.description && <p className="mt-2 text-sm text-red-500">{errors.description.message}</p>}
                </div>

                <div>
                    <input
                        type="datetime-local"
                        {...register('reservedAt', { required: '예약일시를 선택해주세요' })}
                        onChange={(e) => updateReserveTransferRequest({ reservedAt: e.target.value })}
                        min={today}
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.reservedAt && <p className="mt-2 text-sm text-red-500">{errors.reservedAt.message}</p>}
                </div>

                <div>
                    <input
                        type="password"
                        {...register('password', { required: '계좌 비밀번호를 입력해주세요' })}
                        placeholder="계좌 비밀번호 4자리"
                        onChange={(e) => updateReserveTransferRequest({ password: e.target.value })}
                        maxLength={4}
                        className="w-full px-5 py-4 text-lg border-2 border-gray-200 rounded-2xl focus:ring-4 focus:ring-blue-100 focus:border-blue-400 transition-all duration-200 hover:border-blue-400"
                    />
                    {errors.password && <p className="mt-2 text-sm text-red-500">{errors.password.message}</p>}
                </div>
            </div>

            <button
                onClick={handleSubmit(isEdit && reserveTransfer ?
                    () => updateReserveTransfer(reserveTransfer.id) :
                    registerReserveTransfer
                )}
                className="w-full py-4 px-5 mt-8 bg-blue-500 text-white text-lg font-semibold rounded-2xl hover:bg-blue-600 active:scale-[0.98] transition-all duration-200 ease-in-out shadow-lg shadow-blue-500/30"
            >
                예약이체 시작하기
            </button>
        </div>
    );
};

export default ReserveTransferForm;