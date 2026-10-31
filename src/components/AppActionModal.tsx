import React from 'react';
import { AppItem, LauncherSettings } from '../launcher/types';
import { AppIcon, Pin, Trash2, EyeOff, Smartphone } from './Icons';

interface AppActionModalProps {
  app: AppItem;
  settings: LauncherSettings;
  onClose: () => void;
  onToggleFavorite: (appId: string) => void;
  onToggleHide: (appId: string) => void;
  onUninstall: (appId: string) => void;
}

export const AppActionModal: React.FC<AppActionModalProps> = ({
  app,
  settings,
  onClose,
  onToggleFavorite,
  onToggleHide,
  onUninstall
}) => {
  const isLight = settings.theme === 'warm-light';
  const textColor = isLight ? 'text-[#242725]' : 'text-[#F1F3F4]';
  const modalBg = isLight
    ? 'bg-[#FFFFFF] border-t border-black/10'
    : 'bg-[#1C1F24] border-t border-white/10';

  return (
    <div
      onClick={onClose}
      className="absolute inset-0 z-50 bg-black/40 backdrop-blur-xs flex items-end justify-center select-none animate-in fade-in"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className={`w-full rounded-t-3xl p-5 shadow-2xl ${modalBg} ${textColor} animate-in slide-in-from-bottom duration-200`}
      >
        <div className="w-10 h-1 rounded-full bg-black/20 dark:bg-white/20 mx-auto mb-4" />

        {/* App Info Header */}
        <div className="flex items-center gap-3.5 pb-4 border-b border-black/5 dark:border-white/5">
          <AppIcon
            name={app.iconName}
            color={app.color}
            shape={settings.iconShape}
            style={settings.iconStyle}
            theme={settings.theme}
            accentColor={settings.accentColor}
            size="md"
          />
          <div className="text-left flex-1 min-w-0">
            <h3 className="text-sm font-semibold truncate">{app.name}</h3>
            <p className="text-[11px] opacity-60 truncate font-mono">{app.packageName}</p>
          </div>
        </div>

        {/* Actions List */}
        <div className="py-2 space-y-1">
          <button
            onClick={() => {
              onToggleFavorite(app.id);
              onClose();
            }}
            className="w-full flex items-center gap-3 p-3 rounded-2xl hover:bg-black/5 dark:hover:bg-white/5 transition text-left text-xs font-medium"
          >
            <Pin size={16} className={app.isFavorite ? 'text-amber-500 fill-amber-500' : 'opacity-60'} />
            <span>{app.isFavorite ? 'Unpin from Favorites' : 'Pin to Smart Favorites'}</span>
          </button>

          <button
            onClick={() => {
              onToggleHide(app.id);
              onClose();
            }}
            className="w-full flex items-center gap-3 p-3 rounded-2xl hover:bg-black/5 dark:hover:bg-white/5 transition text-left text-xs font-medium"
          >
            <EyeOff size={16} className="opacity-60" />
            <span>{app.isHidden ? 'Unhide App' : 'Hide from App Drawer'}</span>
          </button>

          <button
            onClick={() => {
              alert(`Package: ${app.packageName}\nCategory: ${app.category}\nLaunches: ${app.launchCount}`);
              onClose();
            }}
            className="w-full flex items-center gap-3 p-3 rounded-2xl hover:bg-black/5 dark:hover:bg-white/5 transition text-left text-xs font-medium"
          >
            <Smartphone size={16} className="opacity-60" />
            <span>App Information</span>
          </button>

          <button
            onClick={() => {
              if (confirm(`Uninstall ${app.name}?`)) {
                onUninstall(app.id);
                onClose();
              }
            }}
            className="w-full flex items-center gap-3 p-3 rounded-2xl hover:bg-rose-500/10 text-rose-500 transition text-left text-xs font-medium"
          >
            <Trash2 size={16} />
            <span>Uninstall</span>
          </button>
        </div>
      </div>
    </div>
  );
};
