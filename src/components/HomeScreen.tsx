import React, { useState, useEffect } from 'react';
import { AppItem, AgendaItem, LauncherSettings } from '../launcher/types';
import { resolveIsDark, getSemanticColors } from '../launcher/theme';
import { AppIcon, Plus, Play, Pause, SkipForward, SkipBack, Search, Sun, Check, Sparkles } from './Icons';

interface HomeScreenProps {
  settings: LauncherSettings;
  favorites: AppItem[];
  agenda: AgendaItem[];
  onToggleAgenda: (id: string) => void;
  onAddAgenda: (title: string, time: string) => void;
  onLaunchApp: (app: AppItem) => void;
  onAppLongPress: (app: AppItem) => void;
  onOpenSearch: () => void;
  onLetterJump: (letter: string) => void;
  onOpenSocialFolder: () => void;
  onOpenContextRing?: (originX: number, originY: number, app?: AppItem) => void;
  activeWorkspaceId?: string;
  onSwitchWorkspace?: (id: string) => void;
  onOpenSpaces?: () => void;
}

export const HomeScreen: React.FC<HomeScreenProps> = ({
  settings,
  favorites,
  agenda,
  onToggleAgenda,
  onAddAgenda,
  onLaunchApp,
  onAppLongPress,
  onOpenSearch,
  onLetterJump,
  onOpenSocialFolder,
  onOpenContextRing,
  activeWorkspaceId = 'personal',
  onSwitchWorkspace,
  onOpenSpaces
}) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [isMusicExpanded, setIsMusicExpanded] = useState(settings.musicExpanded ?? false);
  const [showAddAgenda, setShowAddAgenda] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newTime, setNewTime] = useState('11:30 AM');
  const [currentTime, setCurrentTime] = useState('10:24');
  const [currentDate, setCurrentDate] = useState('Wed, 29 Oct');

  useEffect(() => {
    const updateTime = () => {
      const now = new Date();
      const hours = String(now.getHours()).padStart(2, '0');
      const minutes = String(now.getMinutes()).padStart(2, '0');
      setCurrentTime(`${hours}:${minutes}`);

      const days = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
      const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
      setCurrentDate(`${days[now.getDay()]}, ${now.getDate()} ${months[now.getMonth()]}`);
    };
    updateTime();
    const interval = setInterval(updateTime, 1000);
    return () => clearInterval(interval);
  }, []);

  const isDark = resolveIsDark(settings.theme);
  const isLight = !isDark;
  const colors = getSemanticColors(settings.theme, settings.accentColor);
  const textColor = isLight ? 'text-slate-800' : 'text-slate-100';
  const subtextColor = isLight ? 'text-slate-600' : 'text-slate-400';
  const cardBg = isLight
    ? 'bg-white/85 backdrop-blur-md border border-black/8 shadow-xs'
    : 'bg-[#0D1117]/80 backdrop-blur-md border border-white/10 shadow-sm';

  const clockStyle = settings.clockStyle || 'digital';
  const clockSize = settings.clockSize || 'large';
  const showBattery = settings.showBattery ?? true;
  const showWeather = settings.showWeather ?? true;
  const showCalendarEvent = settings.showCalendarEvent ?? true;
  const showMusicHome = settings.showMusicHome ?? true;
  const showAlphabetRail = settings.showAlphabetRail ?? true;
  const dimOverlayAlpha = settings.wallpaperDimOverlay ?? 0.20;

  const handleCreateAgenda = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim()) return;
    onAddAgenda(newTitle.trim(), newTime);
    setNewTitle('');
    setShowAddAgenda(false);
  };

  const alphabet = '#ACDFGHIKLMNOPQRSUVWXYZ'.split('');

  const workspaces = [
    { id: 'personal', name: 'Personal', color: '#38BDF8' },
    { id: 'work', name: 'Work', color: '#6366F1' },
    { id: 'study', name: 'Study', color: '#10B981' },
    { id: 'gaming', name: 'Gaming', color: '#F97316' },
    { id: 'travel', name: 'Travel', color: '#EAB308' }
  ];

  // Font styling according to chosen clock style
  const getClockClasses = () => {
    let sizeClass = 'text-6xl';
    if (clockSize === 'compact') sizeClass = 'text-5xl';
    if (clockSize === 'standard') sizeClass = 'text-6xl';
    if (clockSize === 'large') sizeClass = 'text-7xl';

    const shadow = isDark
      ? 'drop-shadow-[0_2px_12px_rgba(0,0,0,0.7)] text-white'
      : 'drop-shadow-[0_1px_4px_rgba(255,255,255,0.9)] text-slate-900';

    switch (clockStyle) {
      case 'minimal':
        return `${sizeClass} ${shadow} font-thin tracking-widest font-sans`;
      case 'bold':
        return `${sizeClass} ${shadow} font-black tracking-tighter font-sans`;
      case 'serif':
        return `${sizeClass} ${shadow} font-light italic font-serif tracking-normal`;
      case 'monospace':
        return `${sizeClass} ${shadow} font-normal font-mono tracking-tight`;
      case 'cosmic':
        return `${sizeClass} font-extralight tracking-widest text-[#38BDF8] drop-shadow-[0_0_14px_rgba(56,189,248,0.45)]`;
      case 'digital':
      default:
        return `${sizeClass} ${shadow} font-extralight tracking-tight font-sans`;
    }
  };

  return (
    <div
      onContextMenu={(e) => {
        // Long-press or secondary click on wallpaper opens global/workspace Context Ring
        if (e.target === e.currentTarget || (e.target as HTMLElement).tagName === 'DIV') {
          e.preventDefault();
          if (onOpenContextRing) {
            const rect = e.currentTarget.getBoundingClientRect();
            onOpenContextRing(e.clientX - rect.left, e.clientY - rect.top);
          }
        }
      }}
      className={`relative h-full flex flex-col justify-between px-5 pt-5 pb-3 select-none transition-colors duration-300 ${textColor}`}
    >
      {/* Top Header Row: Spaces Capsule Bar + Battery Indicator */}
      <div className="flex items-center justify-between gap-2 mb-2">
        <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-1">
          {workspaces.map((ws) => {
            const isCurrent = ws.id === activeWorkspaceId;
            return (
              <button
                key={ws.id}
                onClick={() => {
                  if (isCurrent && onOpenSpaces) {
                    onOpenSpaces();
                  } else if (onSwitchWorkspace) {
                    onSwitchWorkspace(ws.id);
                  }
                }}
                className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs transition-all backdrop-blur-md ${
                  isCurrent
                    ? isDark
                      ? 'bg-white/20 font-semibold text-white shadow-xs border border-white/20'
                      : 'bg-white/85 font-semibold text-slate-900 shadow-xs border border-black/10'
                    : isDark
                    ? 'text-slate-300 hover:text-white bg-black/30 hover:bg-white/10 border border-white/10'
                    : 'text-slate-700 hover:text-slate-900 bg-white/50 hover:bg-white/80 border border-black/8'
                }`}
              >
                <div
                  className="w-1.5 h-1.5 rounded-full"
                  style={{ backgroundColor: ws.color }}
                />
                <span>{ws.name}</span>
              </button>
            );
          })}
        </div>

        {showBattery && (
          <div className={`flex items-center gap-1.5 text-xs shrink-0 font-mono px-2 py-0.5 rounded-full backdrop-blur-md ${
            isDark ? 'text-slate-200 bg-black/30 border border-white/10' : 'text-slate-800 bg-white/70 border border-black/8 shadow-2xs'
          }`}>
            <span className="text-[11px] font-semibold">88%</span>
            <div className="w-4 h-2 border border-current rounded-xs p-[1px] flex items-center">
              <div className="h-full bg-emerald-500 w-3/4 rounded-2xs" />
            </div>
          </div>
        )}
      </div>

      {/* Scrollable upper content */}
      <div className="flex-1 overflow-y-auto no-scrollbar pr-3">
        {/* Large Typography-Driven Clock */}
        <div className="mt-2 text-left">
          <div className="flex items-center gap-2">
            <div className={`${getClockClasses()} leading-none`}>
              {currentTime}
            </div>
            {clockStyle === 'cosmic' && (
              <Sparkles size={18} className="text-[#38BDF8] animate-pulse" />
            )}
          </div>
          <div className={`text-sm font-medium mt-2 tracking-wide ${isDark ? 'text-slate-200 drop-shadow-[0_1px_4px_rgba(0,0,0,0.7)]' : 'text-slate-800 drop-shadow-[0_1px_3px_rgba(255,255,255,0.8)]'}`}>
            {currentDate}
          </div>

          <div className="flex items-center gap-2.5 mt-2 text-xs flex-wrap">
            {showWeather && (
              <div className={`flex items-center gap-1.5 px-2.5 py-1 rounded-full backdrop-blur-md ${isDark ? 'bg-black/30 text-slate-200 border border-white/10' : 'bg-white/70 text-slate-800 border border-black/8 shadow-2xs'}`}>
                <Sun size={13} className="text-amber-400 shrink-0 fill-amber-400/20" />
                <span className="font-semibold">24°C</span>
                <span className={isDark ? 'text-slate-300' : 'text-slate-600'}>Clear Sky</span>
              </div>
            )}
            {showCalendarEvent && agenda.length > 0 && (
              <div className={`flex items-center gap-1.5 truncate px-2.5 py-1 rounded-full backdrop-blur-md ${isDark ? 'bg-black/30 text-slate-200 border border-white/10' : 'bg-white/70 text-slate-800 border border-black/8 shadow-2xs'}`}>
                <span className="w-1.5 h-1.5 rounded-full bg-[#38BDF8]" />
                <span className="truncate">{agenda[0].title} ({agenda[0].time})</span>
              </div>
            )}
          </div>
        </div>

        {/* Compact Music Player Widget */}
        {showMusicHome && (
          <div className={`mt-3.5 p-2.5 rounded-2xl ${cardBg} transition-all`}>
            <div className="flex items-center justify-between">
              <div
                onClick={() => setIsMusicExpanded(!isMusicExpanded)}
                className="flex items-center gap-2.5 min-w-0 cursor-pointer flex-1"
              >
                {/* Vinyl / Cover Art */}
                <div className="w-9 h-9 rounded-xl bg-gradient-to-tr from-sky-950 via-indigo-950 to-neutral-900 flex items-center justify-center shrink-0 shadow-inner overflow-hidden relative border border-white/10">
                  <div
                    className="absolute inset-0 bg-cover bg-center opacity-70"
                    style={{ backgroundImage: `url('https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=120&q=80')` }}
                  />
                  <div className="relative z-10 w-2 h-2 rounded-full bg-white/70 shadow-xs" />
                </div>
                <div className="min-w-0 text-left">
                  <div className="text-xs font-semibold truncate leading-tight">After Dark</div>
                  <div className={`text-[11px] ${subtextColor} truncate flex items-center gap-1`}>
                    <span>Mr.Kitty</span>
                    <span className="text-[9px] text-[#38BDF8] font-mono">• Spotify</span>
                  </div>
                </div>
              </div>

              <div className="flex items-center gap-1.5 shrink-0">
                <button
                  className={`p-1 hover:opacity-75 transition ${isDark ? 'text-slate-300' : 'text-slate-600'}`}
                  title="Previous"
                >
                  <SkipBack size={14} />
                </button>
                <button
                  onClick={() => setIsPlaying(!isPlaying)}
                  style={{ backgroundColor: settings.accentColor || '#38BDF8' }}
                  className="w-7 h-7 rounded-full text-white flex items-center justify-center shadow-xs hover:scale-105 active:scale-95 transition"
                  title={isPlaying ? 'Pause' : 'Play'}
                >
                  {isPlaying ? <Pause size={12} fill="currentColor" /> : <Play size={12} fill="currentColor" className="ml-0.5" />}
                </button>
                <button
                  className={`p-1 hover:opacity-75 transition ${isDark ? 'text-slate-300' : 'text-slate-600'}`}
                  title="Next"
                >
                  <SkipForward size={14} />
                </button>
              </div>
            </div>

            {isMusicExpanded && (
              <div className={`mt-2.5 pt-2 border-t flex items-center justify-between text-[10px] ${isDark ? 'border-white/10 text-slate-400' : 'border-black/10 text-slate-500'}`}>
                <span>Active MediaSession (Spotify)</span>
                <span className="text-[#38BDF8] cursor-pointer font-medium" onClick={() => setIsMusicExpanded(false)}>
                  Collapse
                </span>
              </div>
            )}
          </div>
        )}

        {/* Agenda / Today Card (if enabled) */}
        {showCalendarEvent && (
          <div className={`mt-3 p-3 rounded-2xl ${cardBg} transition-all`}>
            <div className="flex items-center justify-between mb-2">
              <span className={`text-[11px] font-semibold uppercase tracking-wider ${isDark ? 'text-slate-300' : 'text-slate-700'}`}>Today's Schedule</span>
              <button
                onClick={() => setShowAddAgenda(!showAddAgenda)}
                className={`p-1 rounded-full transition text-xs flex items-center gap-1 ${isDark ? 'hover:bg-white/10 text-slate-300' : 'hover:bg-black/5 text-slate-600'}`}
                title="Add task"
              >
                <Plus size={14} />
              </button>
            </div>

            {showAddAgenda && (
              <form onSubmit={handleCreateAgenda} className={`mb-2.5 p-2 rounded-xl space-y-2 ${isDark ? 'bg-white/5 border border-white/10' : 'bg-black/5 border border-black/10'}`}>
                <input
                  type="text"
                  placeholder="Task title..."
                  value={newTitle}
                  onChange={(e) => setNewTitle(e.target.value)}
                  className={`w-full text-xs px-2 py-1.5 rounded-lg border outline-none ${
                    isDark
                      ? 'bg-neutral-900 border-white/15 text-white placeholder:text-neutral-500'
                      : 'bg-white border-black/15 text-slate-900 placeholder:text-slate-400'
                  }`}
                  autoFocus
                />
                <div className="flex gap-2">
                  <input
                    type="text"
                    placeholder="Time (e.g. 2:00 PM)"
                    value={newTime}
                    onChange={(e) => setNewTime(e.target.value)}
                    className={`w-1/2 text-xs px-2 py-1 rounded-lg border outline-none ${
                      isDark
                        ? 'bg-neutral-900 border-white/15 text-white placeholder:text-neutral-500'
                        : 'bg-white border-black/15 text-slate-900 placeholder:text-slate-400'
                    }`}
                  />
                  <button
                    type="submit"
                    style={{ backgroundColor: settings.accentColor || '#38BDF8' }}
                    className="w-1/2 text-xs text-white rounded-lg font-medium py-1 shadow-xs hover:opacity-90 transition"
                  >
                    Save
                  </button>
                </div>
              </form>
            )}

            <div className="space-y-2">
              {agenda.slice(0, 3).map((item) => (
                <div
                  key={item.id}
                  onClick={() => onToggleAgenda(item.id)}
                  className="flex items-center justify-between text-xs cursor-pointer group"
                >
                  <div className="flex items-center gap-2">
                    <div
                      style={{ backgroundColor: item.completed ? '#64748B' : (settings.accentColor || '#38BDF8') }}
                      className="w-[2.5px] h-3.5 rounded-full shrink-0"
                    />
                    <span className={`${item.completed ? 'line-through opacity-50' : 'font-normal'}`}>
                      {item.title}
                    </span>
                  </div>
                  <div className="flex items-center gap-2">
                    <span className={`text-[10px] ${subtextColor}`}>{item.time}</span>
                    {item.completed && <Check size={11} className="text-emerald-400" />}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Favorite Applications List (Gesture-enabled) */}
        <div className="mt-3.5 space-y-1 pb-16">
          <div className={`text-[10px] font-semibold uppercase tracking-wider px-1 mb-1 ${isDark ? 'text-slate-400' : 'text-slate-600'}`}>
            Pinned Essentials
          </div>
          {favorites.map((app) => (
            <div
              key={app.id}
              onClick={() => onLaunchApp(app)}
              onContextMenu={(e) => {
                e.preventDefault();
                e.stopPropagation();
                if (onOpenContextRing) {
                  const rect = e.currentTarget.getBoundingClientRect();
                  onOpenContextRing(rect.left + rect.width / 2, rect.top + rect.height / 2, app);
                } else {
                  onAppLongPress(app);
                }
              }}
              className={`flex items-center gap-3.5 py-1.5 px-2 rounded-xl cursor-pointer transition group ${
                isDark ? 'hover:bg-white/5' : 'hover:bg-black/5'
              }`}
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
              <span className={`text-sm font-medium tracking-tight text-left flex-1 ${isDark ? 'text-slate-100 drop-shadow-[0_1px_3px_rgba(0,0,0,0.85)]' : 'text-slate-900 drop-shadow-[0_1px_2px_rgba(255,255,255,0.9)]'}`}>
                {app.name}
              </span>
            </div>
          ))}

          {/* Social Stack capsule */}
          <div
            onClick={onOpenSocialFolder}
            className={`flex items-center gap-3.5 py-1.5 px-2 rounded-xl cursor-pointer transition border border-dashed mt-1 backdrop-blur-xs ${
              isDark ? 'hover:bg-white/10 border-white/20 bg-black/20' : 'hover:bg-white/90 border-black/20 bg-white/40'
            }`}
          >
            <div className={`w-9 h-9 rounded-xl flex items-center justify-center shrink-0 ${isDark ? 'bg-white/5' : 'bg-black/5'}`}>
              <div className="grid grid-cols-2 gap-1 p-1.5">
                <div className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                <div className="w-1.5 h-1.5 rounded-full bg-pink-500" />
                <div className="w-1.5 h-1.5 rounded-full bg-sky-500" />
                <div className="w-1.5 h-1.5 rounded-full bg-indigo-500" />
              </div>
            </div>
            <div className="text-left flex-1">
              <span className={`text-xs font-medium block ${isDark ? 'text-slate-200' : 'text-slate-800'}`}>Social Orbit</span>
              <span className={`text-[10px] ${subtextColor}`}>4 grouped apps</span>
            </div>
          </div>
        </div>
      </div>

      {/* Vertical Alphabet Fast Rail on Right (if enabled) */}
      {showAlphabetRail && (
        <div className="absolute right-1 top-16 bottom-20 flex flex-col items-center justify-between text-[8px] font-mono opacity-50 hover:opacity-100 transition py-1 select-none pointer-events-auto">
          {alphabet.map((letter) => (
            <button
              key={letter}
              onClick={() => onLetterJump(letter)}
              className={`hover:font-bold hover:scale-125 transition px-1 py-0.5 ${
                isDark ? 'text-slate-400 hover:text-white' : 'text-slate-500 hover:text-slate-900'
              }`}
            >
              {letter}
            </button>
          ))}
        </div>
      )}

      {/* Compact Bottom Command Bar */}
      <div className="relative pt-2">
        <button
          onClick={onOpenSearch}
          className={`w-full h-11 px-4 rounded-2xl flex items-center justify-between transition-all backdrop-blur-md border ${
            isDark
              ? 'bg-white/10 hover:bg-white/15 border-white/12 text-slate-200'
              : 'bg-white/90 hover:bg-white border-black/10 text-slate-800 shadow-xs'
          }`}
        >
          <div className="flex items-center gap-2.5">
            <Search size={16} style={{ color: settings.accentColor || '#38BDF8' }} />
            <span className={`text-xs ${isDark ? 'text-slate-400' : 'text-slate-500'}`}>Search apps or type /command...</span>
          </div>
          <div className={`text-[10px] font-mono px-2 py-0.5 rounded-md ${
            isDark ? 'text-slate-300 bg-white/10' : 'text-slate-600 bg-black/5'
          }`}>
            /
          </div>
        </button>
      </div>
    </div>
  );
};
