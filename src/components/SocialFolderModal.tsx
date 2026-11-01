import { AppItem, LauncherSettings } from '../launcher/types';
import { AppIcon } from './Icons';
import { resolveIsDark, getSemanticColors } from '../launcher/theme';

interface SocialFolderModalProps {
  settings: LauncherSettings;
  apps: AppItem[];
  onClose: () => void;
  onLaunchApp: (app: AppItem) => void;
}

export const SocialFolderModal: React.FC<SocialFolderModalProps> = ({
  settings,
  apps,
  onClose,
  onLaunchApp
}) => {
  const isDark = resolveIsDark(settings.theme);
  const colors = getSemanticColors(settings.theme, settings.accentColor);
  const textColor = isDark ? 'text-[#F8FAFC]' : 'text-[#1E293B]';
  const modalBg = isDark
    ? 'bg-[#151B26] border border-white/10'
    : 'bg-[#FFFFFF] border border-black/10 shadow-2xl';

  return (
    <div className="absolute inset-0 z-40 bg-black/40 backdrop-blur-xs flex items-center justify-center p-6 animate-in fade-in">
      <div
        className={`w-full max-w-[280px] p-5 rounded-3xl shadow-2xl ${modalBg} ${textColor} select-none`}
      >
        <div className="flex items-center justify-between pb-3 border-b border-black/5 dark:border-white/5 mb-4">
          <span className="text-sm font-semibold">Social</span>
          <button
            onClick={onClose}
            className="w-6 h-6 rounded-full hover:bg-black/5 dark:hover:bg-white/10 flex items-center justify-center text-xs opacity-60 hover:opacity-100 transition"
          >
            ✕
          </button>
        </div>

        <div className="grid grid-cols-3 gap-y-4 gap-x-2 text-center">
          {apps.map((app) => (
            <div
              key={app.id}
              onClick={() => {
                onLaunchApp(app);
                onClose();
              }}
              className="flex flex-col items-center gap-1.5 p-1 rounded-xl hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition active:scale-95"
            >
              <AppIcon
                name={app.iconName}
                color={app.color}
                shape={settings.iconShape}
                style={settings.iconStyle}
                theme={settings.theme}
                accentColor={settings.accentColor}
                size="md"
              />
              <span className="text-[11px] font-medium tracking-tight truncate w-full">
                {app.name}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};
