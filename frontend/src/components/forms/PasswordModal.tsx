import { useState } from "react";

interface PasswordModalProps {
    isOpen: boolean;
    onClose: () => void;
    onConfirm: (password: string) => void;
}

const PasswordModal = ({ isOpen, onClose, onConfirm }: PasswordModalProps) => {
    const [password, setPassword] = useState<string>("");

    const handleSubmit = () => {
        onConfirm(password);
        setPassword("");
        onClose();
    };

    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
            <div className="bg-white p-6 rounded-lg shadow-xl w-96">
                <h3 className="text-lg font-semibold mb-4">계좌 비밀번호 입력</h3>
                <input
                    type="password"
                    maxLength={4}
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    placeholder="4자리 비밀번호 입력"
                    className="w-full p-2 border rounded mb-4"
                />
                <div className="flex justify-end gap-2">
                    <button
                        type="button"
                        onClick={onClose}
                        className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded"
                    >
                        취소
                    </button>
                    <button
                        className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                        onClick={handleSubmit}
                    >
                        확인
                    </button>
                </div>
            </div>
        </div>
    );
};

export default PasswordModal;