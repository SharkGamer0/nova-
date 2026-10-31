import React from 'react';
import { Lock } from './Icons';

interface ScreenLockOverlayProps {
  onUnlock: () => void;
}

export const ScreenLockOverlay: React.FC<ScreenLockOverlayProps> = ({ onUnlock }) => {
  return (
    <div
      onClick={onUnlock}
      className="absolute inset-0 z-50 bg-black/95 text-neutral-200 flex flex-col items-center justify-between py-16 px-6 cursor-pointer select-none animate-in fade-in duration-300"
    >
      <div className="flex items-center gap-1.5 opacity-60 text-xs">
        <Lock size={14} />
        <span>Locked</span>
      </div>

      <div className="text-center">
        <div className="text-7xl font-extralight tracking-tight font-sans">10:24</div>
        <div className="text-sm font-light opacity-75 mt-2">Wednesday, October 29</div>
      </div>

      <div className="text-center opacity-60 text-xs animate-pulse">
        Double tap or click anywhere to wake
      </div>
    </div>
  );
};
