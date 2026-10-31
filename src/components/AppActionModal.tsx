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
  const [showInfo, setShowInfo] = React.useState(false);
  const [showUninstallConfirm, setShowUninstallConfirm] = React.useState(false);

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

        {showInfo ? (
          <div className="py-3 space-y-2 text-xs">
            <div className="bg-black/5 dark:bg-white/5 p-3 rounded-xl space-y-1.5 font-mono text-[11px]">
              <div><span className="opacity-50">Package:</span> {app.packageName}</div>
              <div><span className="opacity-50">Category:</span> {app.category}</div>
              <div><span className="opacity-50">Launches:</span> {app.launchCount}</div>
              {app.lastUsedTime && (
                <div><span className="opacity-50">Last Used:</span> {new Date(app.lastUsedTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}</div>
              )}
            </div>
            <div className="flex items-center justify-center gap-1.5 pt-1 text-[10px] opacity-40 font-mono">
              <img src="/novaforge_logo.png" alt="" className="w-3.5 h-3.5 rounded-sm" />
              <span>NovaForge Application Inspector</span>
            </div>
            <button
              onClick={() => setShowInfo(false)}
              className="w-full py-2 rounded-xl bg-black/10 dark:bg-white/10 hover:bg-black/15 dark:hover:bg-white/15 font-medium transition text-xs"
            >
              Back
            </button>
          </div>
        ) : showUninstallConfirm ? (
          <div className="py-3 space-y-3 text-xs text-center">
            <p className="font-medium text-rose-400">Uninstall {app.name}?</p>
            <p className="opacity-60 text-[11px]">This will remove the package shortcut and stored cache.</p>
            <div className="flex gap-2">
              <button
                onClick={() => setShowUninstallConfirm(false)}
                className="flex-1 py-2 rounded-xl bg-black/10 dark:bg-white/10 font-medium transition text-xs"
              >
                Cancel
              </button>
              <button
                onClick={() => {
                  onUninstall(app.id);
                  onClose();
                }}
                className="flex-1 py-2 rounded-xl bg-rose-600 hover:bg-rose-500 text-white font-medium transition text-xs"
              >
                Confirm Uninstall
              </button>
            </div>
          </div>
        ) : (
          /* Actions List */
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
              onClick={() => setShowInfo(true)}
              className="w-full flex items-center gap-3 p-3 rounded-2xl hover:bg-black/5 dark:hover:bg-white/5 transition text-left text-xs font-medium"
            >
              <Smartphone size={16} className="opacity-60" />
              <span>App Information</span>
            </button>

            <button
              onClick={() => setShowUninstallConfirm(true)}
              className="w-full flex items-center gap-3 p-3 rounded-2xl hover:bg-rose-500/10 text-rose-500 transition text-left text-xs font-medium"
            >
              <Trash2 size={16} />
              <span>Uninstall</span>
            </button>
          </div>
        )}
      </div>
    </div>
  );
};
