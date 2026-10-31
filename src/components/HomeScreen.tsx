import React, { useState } from 'react';
import { AppItem, AgendaItem, LauncherSettings } from '../launcher/types';
import { AppIcon, Plus, Play, Pause, SkipForward, SkipBack, Search, Sun, Check } from './Icons';

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
  onOpenContextRing
}) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [showAddAgenda, setShowAddAgenda] = useState(false);
  const [newTitle, setNewTitle] = useState('');
  const [newTime, setNewTime] = useState('11:30 AM');

  const isLight = settings.theme === 'warm-light';
  const textColor = isLight ? 'text-[#242725]' : 'text-[#F1F3F4]';
  const subtextColor = isLight ? 'text-[#5A605B]' : 'text-[#A0A8B0]';
  const cardBg = isLight
    ? 'bg-white/70 backdrop-blur-md border border-black/5 shadow-xs'
    : 'bg-[#1C1F24]/80 backdrop-blur-md border border-white/10 shadow-md';

  const handleCreateAgenda = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle.trim()) return;
    onAddAgenda(newTitle.trim(), newTime);
    setNewTitle('');
    setShowAddAgenda(false);
  };

  const alphabet = '#ACDFGHIKLMNOPQRSUVWXYZ'.split('');

  return (
    <div className={`relative h-full flex flex-col justify-between px-5 pt-8 pb-4 select-none ${textColor}`}>
      {/* Scrollable upper content */}
      <div className="flex-1 overflow-y-auto no-scrollbar pr-5">
        {/* Large Minimalist Clock */}
        <div className="mt-2">
          <div className="text-6xl font-light tracking-tight font-sans text-left leading-none">
            10:24
          </div>
          <div className="text-base font-normal mt-2 tracking-wide text-left opacity-90">
            Wed, 29 Oct
          </div>
          <div className="flex items-center gap-2 mt-2 text-sm text-left">
            <Sun size={17} className="text-amber-500 shrink-0 fill-amber-500/20" />
            <span className="font-medium">26°</span>
            <span className={subtextColor}>Clear • New Delhi</span>
          </div>
        </div>

        {/* Agenda / Today Card */}
        <div className={`mt-5 p-4 rounded-3xl ${cardBg} transition-all`}>
          <div className="flex items-center justify-between mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider opacity-75">Today</span>
            <button
              onClick={() => setShowAddAgenda(!showAddAgenda)}
              className="p-1 rounded-full hover:bg-black/5 dark:hover:bg-white/10 text-xs flex items-center gap-1 transition"
              title="Add task"
            >
              <Plus size={15} />
            </button>
          </div>

          {showAddAgenda && (
            <form onSubmit={handleCreateAgenda} className="mb-3 p-2 bg-black/5 dark:bg-white/5 rounded-xl space-y-2">
              <input
                type="text"
                placeholder="Task title..."
                value={newTitle}
                onChange={(e) => setNewTitle(e.target.value)}
                className="w-full text-xs px-2 py-1.5 rounded-lg bg-white dark:bg-neutral-800 border border-black/10 dark:border-white/10 outline-none"
                autoFocus
              />
              <div className="flex gap-2">
                <input
                  type="text"
                  placeholder="Time (e.g. 2:00 PM)"
                  value={newTime}
                  onChange={(e) => setNewTime(e.target.value)}
                  className="w-1/2 text-xs px-2 py-1 rounded-lg bg-white dark:bg-neutral-800 border border-black/10 dark:border-white/10 outline-none"
                />
                <button
                  type="submit"
                  style={{ backgroundColor: settings.accentColor }}
                  className="w-1/2 text-xs text-white rounded-lg font-medium py-1"
                >
                  Save
                </button>
              </div>
            </form>
          )}

          <div className="space-y-2.5">
            {agenda.map((item) => (
              <div
                key={item.id}
                onClick={() => onToggleAgenda(item.id)}
                className="flex items-center justify-between text-xs cursor-pointer group"
              >
                <div className="flex items-center gap-2.5">
                  <div
                    style={{ backgroundColor: item.completed ? '#9CA3AF' : settings.accentColor }}
                    className="w-[3px] h-3.5 rounded-full shrink-0"
                  />
                  <span className={`${item.completed ? 'line-through opacity-50' : 'font-medium'}`}>
                    {item.title}
                  </span>
                </div>
                <div className="flex items-center gap-2">
                  <span className={`text-[11px] ${subtextColor}`}>{item.time}</span>
                  {item.completed && <Check size={12} className="text-emerald-500" />}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Music Player Mini Card */}
        <div className={`mt-3 p-3 rounded-2xl ${cardBg} flex items-center justify-between`}>
          <div className="flex items-center gap-3 min-w-0">
            {/* Album art */}
            <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-purple-900 via-indigo-950 to-neutral-900 flex items-center justify-center shrink-0 shadow-inner overflow-hidden relative">
              <div className="absolute inset-0 bg-cover bg-center opacity-70" style={{ backgroundImage: `url('https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=120&q=80')` }} />
              <div className="relative z-10 w-2 h-2 rounded-full bg-white/50" />
            </div>
            <div className="min-w-0 text-left">
              <div className="text-xs font-semibold truncate leading-tight">After Dark</div>
              <div className={`text-[11px] ${subtextColor} truncate`}>Mr.Kitty</div>
            </div>
          </div>

          <div className="flex items-center gap-2 shrink-0">
            <button className="p-1 hover:opacity-75 transition" title="Previous">
              <SkipBack size={15} />
            </button>
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              style={{ backgroundColor: settings.accentColor }}
              className="w-7 h-7 rounded-full text-white flex items-center justify-center shadow-xs hover:scale-105 active:scale-95 transition"
              title={isPlaying ? 'Pause' : 'Play'}
            >
              {isPlaying ? <Pause size={13} fill="currentColor" /> : <Play size={13} fill="currentColor" className="ml-0.5" />}
            </button>
            <button className="p-1 hover:opacity-75 transition" title="Next">
              <SkipForward size={15} />
            </button>
          </div>
        </div>

        {/* Favorite Applications List */}
        <div className="mt-4 space-y-1.5 pb-16">
          {favorites.map((app) => (
            <div
              key={app.id}
              onClick={() => onLaunchApp(app)}
              onContextMenu={(e) => {
                e.preventDefault();
                if (onOpenContextRing) {
                  onOpenContextRing(180, 260, app);
                } else {
                  onAppLongPress(app);
                }
              }}
              className="flex items-center gap-3.5 py-1 px-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition group"
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
              <span className="text-sm font-medium tracking-tight text-left flex-1">
                {app.name}
              </span>
            </div>
          ))}

          {/* Social folder stack teaser */}
          <div
            onClick={onOpenSocialFolder}
            className="flex items-center gap-3.5 py-1 px-2 rounded-xl hover:bg-black/5 dark:hover:bg-white/5 cursor-pointer transition border border-dashed border-black/10 dark:border-white/10 mt-2"
          >
            <div className="w-10 h-10 rounded-2xl bg-black/5 dark:bg-white/5 flex items-center justify-center shrink-0">
              <div className="grid grid-cols-2 gap-1 p-2">
                <div className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
                <div className="w-1.5 h-1.5 rounded-full bg-pink-500" />
                <div className="w-1.5 h-1.5 rounded-full bg-sky-500" />
                <div className="w-1.5 h-1.5 rounded-full bg-indigo-500" />
              </div>
            </div>
            <div className="text-left flex-1">
              <span className="text-sm font-medium tracking-tight block">Social Stack</span>
              <span className={`text-[11px] ${subtextColor}`}>6 apps grouped</span>
            </div>
          </div>
        </div>
      </div>

      {/* Vertical Alphabet Fast Rail on Right */}
      <div className="absolute right-1 top-20 bottom-24 flex flex-col items-center justify-between text-[9px] font-mono opacity-50 hover:opacity-100 transition py-1 select-none pointer-events-auto">
        {alphabet.map((letter) => (
          <button
            key={letter}
            onClick={() => onLetterJump(letter)}
            className="hover:font-bold hover:scale-125 transition px-1"
          >
            {letter}
          </button>
        ))}
      </div>

      {/* Floating Action Buttons: Context Ring & Search */}
      <div className="absolute bottom-6 right-6 flex items-center gap-2">
        <button
          onClick={() => onOpenContextRing && onOpenContextRing(180, 300)}
          style={{ backgroundColor: isLight ? 'rgba(255,255,255,0.85)' : 'rgba(30,34,40,0.85)' }}
          className="w-12 h-12 rounded-full flex items-center justify-center shadow-lg border border-black/10 dark:border-white/15 backdrop-blur-md hover:scale-105 active:scale-95 transition-all text-[#2A9D8F]"
          title="Open Context Ring"
        >
          <div className="w-5 h-5 rounded-full border-2 border-[#2A9D8F] flex items-center justify-center">
            <div className="w-1.5 h-1.5 rounded-full bg-[#2A9D8F]" />
          </div>
        </button>

        <button
          onClick={onOpenSearch}
          style={{ backgroundColor: isLight ? 'rgba(255,255,255,0.85)' : 'rgba(30,34,40,0.85)' }}
          className="w-13 h-13 rounded-full flex items-center justify-center shadow-lg border border-black/10 dark:border-white/15 backdrop-blur-md hover:scale-105 active:scale-95 transition-all text-[#2D312E] dark:text-[#F1F3F4]"
          title="Open App Search"
        >
          <Search size={22} style={{ color: settings.accentColor }} strokeWidth={2.2} />
        </button>
      </div>
    </div>
  );
};
