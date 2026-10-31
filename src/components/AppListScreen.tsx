import React, { useRef, useState } from 'react';
import { AppItem, LauncherSettings } from '../launcher/types';
import { AppIcon, Search, Sliders, Pin, ArrowLeft } from './Icons';
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
  onBackToHome
}) => {
  const [activeLetterHover, setActiveLetterHover] = useState<string | null>(null);
  const scrollContainerRef = useRef<HTMLDivElement>(null);

  const isLight = settings.theme === 'warm-light';
  const textColor = isLight ? 'text-[#242725]' : 'text-[#F1F3F4]';
  const subtextColor = isLight ? 'text-[#5A605B]' : 'text-[#A0A8B0]';
  const inputBg = isLight
    ? 'bg-black/5 border-black/10 focus:border-[#2A9D8F]'
    : 'bg-white/5 border-white/10 focus:border-[#2A9D8F]';

  const groups = groupByAlphabet(apps);
  const alphabet = '#ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('');

  const scrollToLetter = (letter: string) => {
    setActiveLetterHover(letter);
    const element = document.getElementById(`section-letter-${letter}`);
    if (element && scrollContainerRef.current) {
      element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
    setTimeout(() => setActiveLetterHover(null), 1200);
  };

  const categories = ['All', 'Favorites', 'Social', 'Productivity', 'Tools', 'Media', 'Games'];

  return (
    <div className={`relative h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
      {/* Top Search Bar */}
      <div className="relative flex items-center gap-2">
        <button
          onClick={onBackToHome}
          className="p-2 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition shrink-0"
          title="Back to Home"
        >
          <ArrowLeft size={18} />
        </button>

        <div className="relative flex-1">
          <Search size={16} className={`absolute left-3.5 top-1/2 -translate-y-1/2 ${subtextColor}`} />
          <input
            type="text"
            placeholder="Search apps, contacts, web..."
            value={searchQuery}
            onChange={(e) => onSearchChange(e.target.value)}
            className={`w-full py-2.5 pl-10 pr-4 text-xs rounded-full border outline-none backdrop-blur-md transition-all ${inputBg}`}
          />
        </div>
      </div>

      {/* Category Filter Chips */}
      <div className="flex items-center gap-2 overflow-x-auto no-scrollbar py-2.5 mt-1 shrink-0">
        {categories.map((cat) => {
          const isSelected = selectedCategory === cat;
          return (
            <button
              key={cat}
              onClick={() => onSelectCategory(cat)}
              style={
                isSelected
                  ? { backgroundColor: settings.accentColor, color: '#FFFFFF' }
                  : undefined
              }
              className={`text-xs px-3.5 py-1.5 rounded-full whitespace-nowrap font-medium transition-all ${
                isSelected
                  ? 'shadow-xs scale-102'
                  : 'bg-black/5 dark:bg-white/10 hover:bg-black/10 dark:hover:bg-white/15'
              }`}
            >
              {cat}
            </button>
          );
        })}
      </div>

      {/* Active Fast Jump Letter Indicator Overlay */}
      {activeLetterHover && (
        <div className="absolute right-12 top-1/2 -translate-y-1/2 w-14 h-14 rounded-2xl bg-black/80 dark:bg-white/90 text-white dark:text-black flex items-center justify-center text-2xl font-bold shadow-2xl z-30 pointer-events-none animate-in fade-in zoom-in-90">
          {activeLetterHover}
        </div>
      )}

      {/* Vertical Alphabetical App List */}
      <div className="relative flex-1 overflow-hidden flex">
        <div
          ref={scrollContainerRef}
          className="flex-1 overflow-y-auto no-scrollbar pr-6 pb-12 space-y-4"
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
                    style={{ color: settings.accentColor }}
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
                          onAppLongPress(app);
                        }}
                        className="flex items-center gap-3.5 py-1.5 px-2.5 rounded-2xl hover:bg-black/5 dark:hover:bg-white/5 active:scale-99 cursor-pointer transition group"
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
                          <Pin size={12} className="opacity-40 group-hover:opacity-100 text-amber-500 shrink-0" />
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))
          )}
        </div>

        {/* Right Edge Fast Alphabet Rail */}
        <div className="w-5 flex flex-col items-center justify-between py-2 text-[9px] font-mono opacity-50 hover:opacity-100 transition select-none shrink-0">
          {alphabet.map((letter) => (
            <button
              key={letter}
              onClick={() => scrollToLetter(letter)}
              className="hover:font-bold hover:scale-130 transition px-0.5 leading-none"
            >
              {letter}
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
