import React, { useRef, useState } from 'react';
import { LauncherSettings, LauncherTheme } from '../launcher/types';
import { WALLPAPERS } from '../launcher/sampleData';
import { Wifi, Signal, Battery, Lock } from 'lucide-react';

interface PhoneFrameProps {
  children: React.ReactNode;
  settings: LauncherSettings;
  activeScreenIndex: number;
  onNavigateScreen: (index: number) => void;
  onTriggerGesture: (type: 'swipeUp' | 'swipeDown' | 'swipeLeft' | 'swipeRight' | 'doubleTap' | 'longPress') => void;
}

export const PhoneFrame: React.FC<PhoneFrameProps> = ({
  children,
  settings,
  activeScreenIndex,
  onNavigateScreen,
  onTriggerGesture
}) => {
  const touchStartX = useRef<number | null>(null);
  const touchStartY = useRef<number | null>(null);
  const lastTapTime = useRef<number>(0);
  const longPressTimer = useRef<NodeJS.Timeout | null>(null);

  const isLight = settings.theme === 'warm-light';
  const currentWallpaper = WALLPAPERS[settings.wallpaperIndex % WALLPAPERS.length] || WALLPAPERS[0];

  // Handle Touch/Mouse gesture events
  const handlePointerDown = (e: React.PointerEvent) => {
    touchStartX.current = e.clientX;
    touchStartY.current = e.clientY;

    // Check double tap
    const now = Date.now();
    if (now - lastTapTime.current < 300) {
      onTriggerGesture('doubleTap');
    }
    lastTapTime.current = now;

    // Long press
    longPressTimer.current = setTimeout(() => {
      onTriggerGesture('longPress');
    }, 600);
  };

  const handlePointerUp = (e: React.PointerEvent) => {
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
    }

    if (touchStartX.current === null || touchStartY.current === null) return;

    const diffX = e.clientX - touchStartX.current;
    const diffY = e.clientY - touchStartY.current;

    // Minimum swipe threshold
    if (Math.abs(diffX) > 40 || Math.abs(diffY) > 40) {
      if (Math.abs(diffX) > Math.abs(diffY)) {
        if (diffX > 0) {
          onTriggerGesture('swipeRight');
        } else {
          onTriggerGesture('swipeLeft');
        }
      } else {
        if (diffY > 0) {
          onTriggerGesture('swipeDown');
        } else {
          onTriggerGesture('swipeUp');
        }
      }
    }

    touchStartX.current = null;
    touchStartY.current = null;
  };

  const handlePointerMove = () => {
    // If user moves noticeably, cancel long press
    if (longPressTimer.current) {
      clearTimeout(longPressTimer.current);
    }
  };

  // Theme styling for the phone body
  const phoneBg =
    settings.theme === 'warm-light'
      ? 'bg-[#F7F6F2]'
      : settings.theme === 'charcoal-dark'
      ? 'bg-[#121417]'
      : 'bg-[#000000]';

  const statusBarText = isLight ? 'text-[#2D312E]' : 'text-[#F1F3F4]';

  return (
    <div className="relative mx-auto my-auto select-none transition-all duration-300">
      {/* Outer Phone Bezel */}
      <div className="w-[360px] h-[730px] rounded-[48px] p-[9px] bg-gradient-to-b from-[#2B2D31] via-[#1E2024] to-[#121316] shadow-[0_25px_60px_-15px_rgba(0,0,0,0.6),0_0_0_1px_rgba(255,255,255,0.1)] relative overflow-hidden">
        {/* Subtle bezel specular highlight */}
        <div className="absolute inset-x-8 top-0 h-[1.5px] bg-white/20 rounded-full" />

        {/* Screen container */}
        <div
          onPointerDown={handlePointerDown}
          onPointerUp={handlePointerUp}
          onPointerMove={handlePointerMove}
          className={`w-full h-full rounded-[40px] ${phoneBg} relative overflow-hidden flex flex-col`}
          style={{
            backgroundImage: `url('${currentWallpaper.url}')`,
            backgroundSize: 'cover',
            backgroundPosition: 'center'
          }}
        >
          {/* Subtle overlay so text remains readable against the mountain wallpaper */}
          <div
            className={`absolute inset-0 pointer-events-none transition-opacity ${
              isLight ? 'bg-[#F7F6F2]/85 backdrop-blur-[2px]' : 'bg-[#121417]/88 backdrop-blur-[2px]'
            }`}
          />

          {/* Android Status Bar */}
          <div
            className={`relative z-20 h-10 px-6 flex items-center justify-between text-xs font-semibold ${statusBarText} shrink-0 pointer-events-none`}
          >
            {/* Time */}
            <span className="tracking-tight text-[13px]">10:24</span>

            {/* Centered Punch-hole Selfie Camera */}
            <div className="absolute left-1/2 -translate-x-1/2 top-2.5 w-4 h-4 rounded-full bg-black border border-white/10 shadow-inner flex items-center justify-center">
              <div className="w-1.5 h-1.5 rounded-full bg-[#1A237E]/40" />
            </div>

            {/* Status Icons */}
            <div className="flex items-center gap-1.5 opacity-90">
              <Signal size={12} strokeWidth={2.5} />
              <Wifi size={13} strokeWidth={2.5} />
              <div className="flex items-center gap-0.5">
                <span className="text-[10px] font-mono">68%</span>
                <Battery size={14} strokeWidth={2.5} />
              </div>
            </div>
          </div>

          {/* Interactive Screen Content */}
          <div className="relative z-10 flex-1 overflow-hidden">{children}</div>

          {/* Android System Navigation Pill / Gesture Bar at bottom */}
          <div
            onClick={() => onNavigateScreen(1)}
            className="relative z-20 h-5 flex items-center justify-center cursor-pointer group shrink-0"
            title="Swipe up or tap for Home"
          >
            <div className="w-28 h-1 rounded-full bg-black/30 dark:bg-white/30 group-hover:bg-[#2A9D8F] transition-colors" />
          </div>
        </div>
      </div>
    </div>
  );
};
