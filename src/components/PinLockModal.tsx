import React, { useState } from 'react';
import { Lock, ArrowLeft, Eye, EyeOff } from './Icons';

interface PinLockModalProps {
  correctPin: string;
  onSuccess: () => void;
  onCancel: () => void;
  theme: string;
}

export const PinLockModal: React.FC<PinLockModalProps> = ({
  correctPin,
  onSuccess,
  onCancel,
  theme
}) => {
  const [pin, setPin] = useState('');
  const [error, setError] = useState(false);

  const isLight = theme === 'warm-light';
  const textColor = isLight ? 'text-[#242725]' : 'text-[#F1F3F4]';
  const subtextColor = isLight ? 'text-[#5A605B]' : 'text-[#A0A8B0]';
  const keypadBg = isLight
    ? 'bg-black/5 hover:bg-black/10 active:bg-black/15'
    : 'bg-white/5 hover:bg-white/10 active:bg-white/15';

  const handleDigit = (digit: string) => {
    if (pin.length >= 4) return;
    const nextPin = pin + digit;
    setPin(nextPin);
    setError(false);

    if (nextPin.length === 4) {
      if (nextPin === correctPin) {
        setTimeout(() => onSuccess(), 200);
      } else {
        setError(true);
        setTimeout(() => setPin(''), 600);
      }
    }
  };

  const handleBackspace = () => {
    setPin((prev) => prev.slice(0, -1));
    setError(false);
  };

  return (
    <div className={`absolute inset-0 z-50 bg-[#F7F6F2] dark:bg-[#121417] flex flex-col justify-between pt-10 pb-8 px-6 select-none ${textColor} animate-in fade-in zoom-in-95`}>
      {/* Top Bar */}
      <div className="flex items-center justify-between">
        <button
          onClick={onCancel}
          className="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          title="Back"
        >
          <ArrowLeft size={20} />
        </button>
        <div className="w-8" />
      </div>

      {/* Lock Header */}
      <div className="flex flex-col items-center text-center space-y-3">
        <div className="w-12 h-12 rounded-2xl bg-black/5 dark:bg-white/10 flex items-center justify-center">
          <Lock size={22} className="opacity-80" />
        </div>
        <div>
          <h2 className="text-base font-semibold">Enter your password</h2>
          <p className={`text-xs ${subtextColor} mt-0.5`}>
            Default demo PIN is <span className="font-mono font-bold">1234</span>
          </p>
        </div>

        {/* PIN Dots */}
        <div className="flex gap-4 pt-4">
          {[0, 1, 2, 3].map((idx) => {
            const isFilled = pin.length > idx;
            return (
              <div
                key={idx}
                className={`w-3.5 h-3.5 rounded-full transition-all duration-200 ${
                  error
                    ? 'bg-rose-500 scale-110'
                    : isFilled
                    ? 'bg-[#2A9D8F] scale-110 shadow-xs'
                    : 'bg-black/20 dark:bg-white/20'
                }`}
              />
            );
          })}
        </div>
        {error && <span className="text-xs text-rose-500 font-medium">Incorrect PIN. Try 1234</span>}
      </div>

      {/* Numeric Keypad */}
      <div className="max-w-[260px] mx-auto w-full grid grid-cols-3 gap-3">
        {['1', '2', '3', '4', '5', '6', '7', '8', '9'].map((digit) => (
          <button
            key={digit}
            onClick={() => handleDigit(digit)}
            className={`w-16 h-16 rounded-full text-xl font-medium flex items-center justify-center transition-all ${keypadBg}`}
          >
            {digit}
          </button>
        ))}
        <div />
        <button
          onClick={() => handleDigit('0')}
          className={`w-16 h-16 rounded-full text-xl font-medium flex items-center justify-center transition-all ${keypadBg}`}
        >
          0
        </button>
        <button
          onClick={handleBackspace}
          className={`w-16 h-16 rounded-full text-xs font-semibold flex items-center justify-center transition-all ${keypadBg}`}
        >
          DEL
        </button>
      </div>
    </div>
  );
};
