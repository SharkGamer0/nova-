import React, { useRef, useState } from 'react';
import { AppItem, LauncherSettings } from '../launcher/types';
import { AppIcon, Search, Pin, ArrowLeft, Play, Pause, SkipForward, SkipBack } from './Icons';
import { groupByAlphabet } from '../launcher/logic';

interface AppListScreenProps {
  settings: LauncherSettings;
  apps: AppItem[];
  searchQuery: string;
  onSearchChange: (q: string) => void;
  selectedCategory: string;
  onSelectCategory: (cat: string) => void;
  onLaunchApp: (app: AppItem) => void;
  onAppLongPress: (app: AppItem) => void;
  onBackToHome: () => void;
  onOpenContextRing?: (originX: number, originY: number, app?: AppItem) => void;
}

export const AppListScreen: React.FC<AppListScreenProps> = ({
  settings,
  apps,
  searchQuery,
  onSearchChange,
  selectedCategory,
  onSelectCategory,
  onLaunchApp,
  onAppLongPress,
  onBackToHome,
  onOpenContextRing
}) => {
  const [activeLetterHover, setActiveLetterHover] = useState<string | null>(null);
  const [isScrubbing, setIsScrubbing] = useState(false);
  const [isPlaying, setIsPlaying] = useState(false);
  const scrollContainerRef = useRef<HTMLDivElement>(null);
  const railRef = useRef<HTMLDivElement>(null);

  const isLight = settings.theme === 'warm-light';
  const textColor = isLight ? 'text-[#242725]' : 'text-[#F1F3F4]';
  const subtextColor = isLight ? 'text-[#5A605B]' : 'text-[#94A3B8]';
  const inputBg = isLight
    ? 'bg-black/5 border-black/10 focus:border-[#38BDF8]'
    : 'bg-white/5 border-white/10 focus:border-[#38BDF8]';

  const groups = groupByAlphabet(apps);
  const alphabetRail = ['↑', '★', ...'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split(''), '#'];

  const scrollToLetter = (letter: string) => {
    setActiveLetterHover(letter);
    if (letter === '↑') {
      scrollContainerRef.current?.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    if (letter === '★') {
      scrollContainerRef.current?.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }
    const element = document.getElementById(`section-letter-${letter}`);
    if (element && scrollContainerRef.current) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
  };

  const handleRailScrub = (clientY: number) => {
    if (!railRef.current) return;
    const rect = railRef.current.getBoundingClientRect();
    const relativeY = clientY - rect.top;
    const fraction = Math.max(0, Math.min(0.999, relativeY / rect.height));
    const index = Math.floor(fraction * alphabetRail.length);
    const letter = alphabetRail[index];
    if (letter) {
      scrollToLetter(letter);
    }
  };

  const categories = ['All', 'Favorites', 'Social', 'Productivity', 'Tools', 'Media', 'Games'];

  return (
    <div
      onMouseUp={() => {
        setIsScrubbing(false);
        setActiveLetterHover(null);
      }}
      className={`relative h-full flex flex-col pt-5 pb-3 px-5 select-none ${textColor}`}
    >
      {/* Top Search Bar */}
      <div className="relative flex items-center gap-2">
        <button
          onClick={onBackToHome}
          className="p-2 rounded-full hover:bg-white/10 transition shrink-0 text-neutral-300"
          title="Back to Home"
        >
          <ArrowLeft size={18} />
        </button>

        <div className="relative flex-1">
          <Search size={16} className={`absolute left-3.5 top-1/2 -translate-y-1/2 ${subtextColor}`} />
          <input
            type="text"
            placeholder="Search apps, commands or tools..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className={`w-full py-2.5 pl-10 pr-4 text-xs rounded-full border outline-none backdrop-blur-md transition-all text-white ${inputBg}`}
          />
        </div>
      </div>

      {/* Optional Top Media Widget in App Drawer */}
      {settings.showMusicDrawer && searchQuery.length === 0 && (
        <div className="mt-2.5 p-2 rounded-2xl bg-white/5 border border-white/10 flex items-center justify-between text-xs">
          <div className="flex items-center gap-2 min-w-0">
            <div className="w-7 h-7 rounded-lg bg-gradient-to-tr from-sky-950 to-indigo-900 flex items-center justify-center shrink-0 border border-white/10">
              <div className="w-1.5 h-1.5 rounded-full bg-white/70" />
            </div>
            <div className="truncate text-left min-w-0">
              <span className="font-medium truncate block text-[11px]">After Dark</span>
              <span className="text-[10px] text-neutral-400 truncate block">Mr.Kitty</span>
            </div>
          </div>
          <div className="flex items-center gap-1 shrink-0">
            <button className="p-1 hover:opacity-75 transition text-neutral-400">
              <SkipBack size={12} />
            </button>
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              style={{ backgroundColor: settings.accentColor || '#38BDF8' }}
              className="w-5 h-5 rounded-full text-white flex items-center justify-center shadow-xs"
            >
              {isPlaying ? <Pause size={10} fill="currentColor" /> : <Play size={10} fill="currentColor" className="ml-0.5" />}
            </button>
            <button className="p-1 hover:opacity-75 transition text-neutral-400">
              <SkipForward size={12} />
            </button>
          </div>
        </div>
      )}

      {/* Category Filter Chips */}
      <div className="flex items-center gap-1.5 overflow-x-auto no-scrollbar py-2 mt-1 shrink-0">
        {categories.map((cat) => {
          const isSelected = selectedCategory === cat;
          return (
            <button
              key={cat}
              onClick={() => onSelectCategory(cat)}
              style={
                isSelected
                  ? { backgroundColor: settings.accentColor || '#38BDF8', color: '#FFFFFF' }
                  : undefined
              }
              className={`text-xs px-3 py-1 rounded-full whitespace-nowrap font-medium transition-all ${
                isSelected
                  ? 'shadow-xs scale-102'
                  : 'bg-white/5 hover:bg-white/10 text-neutral-400 hover:text-white'
              }`}
            >
              {cat}
            </button>
          );
        })}
      </div>

      {/* Floating Letter Indicator Bubble (appears while user scrubs rail) */}
      {activeLetterHover && (
        <div className="absolute right-12 top-1/2 -translate-y-1/2 w-14 h-14 rounded-2xl bg-[#38BDF8] text-white flex items-center justify-center text-2xl font-bold shadow-2xl z-30 pointer-events-none animate-in fade-in zoom-in-90 border border-white/20">
          {activeLetterHover}
        </div>
      )}

      {/* Vertical Alphabetical App List */}
      <div className="relative flex-1 overflow-hidden flex mt-1">
        <div
          ref={scrollContainerRef}
          className="flex-1 overflow-y-auto no-scrollbar pr-4 pb-12 space-y-3.5"
        >
          {Object.keys(groups).length === 0 ? (
            <div className="text-center py-16 opacity-60 text-xs">
              No matching applications found.
            </div>
          ) : (
            Object.keys(groups)
              .sort()
              .map((letter) => (
                <div key={letter} id={`section-letter-${letter}`} className="pt-1">
                  {/* Letter Header */}
                  <div
                    style={{ color: settings.accentColor || '#38BDF8' }}
                    className="text-xs font-bold uppercase tracking-wider mb-2 ml-1"
                  >
                    {letter}
                  </div>

                  {/* App Items in this Letter */}
                  <div className="space-y-1">
                    {groups[letter].map((app) => (
                      <div
                        key={app.id}
                        onClick={() => onLaunchApp(app)}
                        onContextMenu={(e) => {
                          e.preventDefault();
                          if (onOpenContextRing) {
                            const rect = e.currentTarget.getBoundingClientRect();
                            onOpenContextRing(rect.left + rect.width / 2, rect.top + rect.height / 2, app);
                          } else {
                            onAppLongPress(app);
                          }
                        }}
                        className="flex items-center gap-3.5 py-1.5 px-2.5 rounded-2xl hover:bg-white/5 active:scale-99 cursor-pointer transition group"
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
                        <div className="flex-1 min-w-0 text-left">
                          <div className="text-sm font-medium tracking-tight truncate">
                            {app.name}
                          </div>
                          <div className={`text-[10px] ${subtextColor} truncate`}>
                            {app.category} • {app.launchCount} launches
                          </div>
                        </div>

                        {app.isFavorite && (
                          <Pin size={12} className="opacity-40 group-hover:opacity-100 text-amber-400 shrink-0" />
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))
          )}
        </div>

        {/* Right Edge Fast Alphabet Continuous Drag Rail */}
        {(settings.showAlphabetRail ?? true) && (
          <div
            ref={railRef}
            onMouseDown={(e) => {
              setIsScrubbing(true);
              handleRailScrub(e.clientY);
            }}
            onMouseMove={(e) => {
              if (isScrubbing) handleRailScrub(e.clientY);
            }}
            className="w-5 flex flex-col items-center justify-between py-1 text-[8px] font-mono opacity-50 hover:opacity-100 transition select-none shrink-0 cursor-ns-resize"
          >
            {alphabetRail.map((letter) => (
              <button
                key={letter}
                onClick={() => scrollToLetter(letter)}
                className={`transition px-0.5 leading-none ${
                  activeLetterHover === letter ? 'text-[#38BDF8] font-bold scale-130' : 'text-neutral-400 hover:text-white'
                }`}
              >
                {letter}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
