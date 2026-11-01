import React, { useState } from 'react';
import { LauncherSettings, NoteItem } from '../launcher/types';
import { resolveIsDark, getSemanticColors } from '../launcher/theme';
import { Play, Pause, SkipBack, SkipForward, Plus, Check, Sun } from './Icons';

interface WidgetsScreenProps {
  settings: LauncherSettings;
  notes: NoteItem[];
  onToggleNote: (id: string) => void;
  onAddNote: (text: string) => void;
  onOpenSettings: () => void;
}

export const WidgetsScreen: React.FC<WidgetsScreenProps> = ({
  settings,
  notes,
  onToggleNote,
  onAddNote
}) => {
  const [isPlaying, setIsPlaying] = useState(false);
  const [showAddNote, setShowAddNote] = useState(false);
  const [newNoteText, setNewNoteText] = useState('');

  const isDark = resolveIsDark(settings.theme);
  const isLight = !isDark;
  const colors = getSemanticColors(settings.theme, settings.accentColor);
  const textColor = isLight ? 'text-slate-800' : 'text-slate-100';
  const subtextColor = isLight ? 'text-slate-600' : 'text-slate-400';
  const cardBg = isLight
    ? 'bg-white/85 backdrop-blur-md border border-black/8 shadow-xs'
    : 'bg-[#151B26]/85 backdrop-blur-md border border-white/10 shadow-sm';

  const handleAddNote = (e: React.FormEvent) => {
    e.preventDefault();
    if (!newNoteText.trim()) return;
    onAddNote(newNoteText.trim());
    setNewNoteText('');
    setShowAddNote(false);
  };

  return (
    <div className={`h-full flex flex-col justify-between pt-10 pb-6 px-5 select-none ${textColor}`}>
      {/* Scrollable widget container */}
      <div className="flex-1 overflow-y-auto no-scrollbar space-y-3 pr-1">
        {/* Row 1: Weather Widget & Agenda Widget */}
        <div className="grid grid-cols-2 gap-3">
          {/* Weather Widget */}
          <div
            style={{ backgroundColor: settings.accentColor }}
            className="p-4 rounded-3xl text-white shadow-md flex flex-col justify-between relative overflow-hidden"
          >
            <div className="text-xs font-medium opacity-90 text-left">New Delhi</div>
            <div className="my-2 flex items-center justify-between">
              <span className="text-3xl font-light tracking-tight">26°</span>
              <Sun size={28} className="text-amber-200 fill-amber-200/40" />
            </div>
            <div>
              <div className="text-xs font-semibold text-left">Clear Sky</div>
              <div className="text-[10px] opacity-80 text-left">28° / 16°</div>
            </div>
          </div>

          {/* Agenda Mini Widget */}
          <div className={`p-4 rounded-3xl ${cardBg} flex flex-col justify-between text-left`}>
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold">Wed, 29 Oct</span>
              <span className="text-xs opacity-60">+</span>
            </div>
            <div className="space-y-1.5 text-[11px] my-1">
              <div className="flex justify-between items-center">
                <span className="truncate border-l-2 pl-1 border-emerald-500">Chemistry</span>
                <span className={`text-[10px] ${subtextColor}`}>10:00 AM</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="truncate border-l-2 pl-1 border-sky-500">Gym</span>
                <span className={`text-[10px] ${subtextColor}`}>5:00 PM</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="truncate border-l-2 pl-1 border-amber-500">Read Book</span>
                <span className={`text-[10px] ${subtextColor}`}>9:00 PM</span>
              </div>
            </div>
          </div>
        </div>

        {/* Row 2: Wide Music Widget */}
        <div className={`p-3.5 rounded-3xl ${cardBg} flex items-center justify-between`}>
          <div className="flex items-center gap-3">
            <div className="w-12 h-12 rounded-2xl bg-neutral-900 flex items-center justify-center shrink-0 shadow-inner overflow-hidden relative">
              <div
                className="absolute inset-0 bg-cover bg-center"
                style={{
                  backgroundImage: `url('https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=150&q=80')`
                }}
              />
            </div>
            <div className="text-left">
              <div className="text-sm font-semibold truncate">After Dark</div>
              <div className={`text-xs ${subtextColor} truncate`}>Mr.Kitty</div>
            </div>
          </div>

          <div className="flex items-center gap-2">
            <button className="p-1 hover:opacity-75 transition" title="Previous">
              <SkipBack size={16} />
            </button>
            <button
              onClick={() => setIsPlaying(!isPlaying)}
              style={{ backgroundColor: settings.accentColor }}
              className="w-8 h-8 rounded-full text-white flex items-center justify-center shadow-xs hover:scale-105 active:scale-95 transition"
              title={isPlaying ? 'Pause' : 'Play'}
            >
              {isPlaying ? <Pause size={14} fill="currentColor" /> : <Play size={14} fill="currentColor" className="ml-0.5" />}
            </button>
            <button className="p-1 hover:opacity-75 transition" title="Next">
              <SkipForward size={16} />
            </button>
          </div>
        </div>

        {/* Row 3: Battery Progress Ring & Screen Time Dashboard */}
        <div className="grid grid-cols-2 gap-3">
          {/* Battery Ring Widget */}
          <div className={`p-4 rounded-3xl ${cardBg} flex flex-col items-center justify-center text-center`}>
            <div className="relative w-18 h-18 flex items-center justify-center">
              <svg className="w-full h-full -rotate-90" viewBox="0 0 36 36">
                <path
                  className="stroke-current opacity-15"
                  strokeWidth="3.5"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
                <path
                  style={{ stroke: settings.accentColor }}
                  strokeDasharray="68, 100"
                  strokeWidth="3.5"
                  strokeLinecap="round"
                  fill="none"
                  d="M18 2.0845 a 15.9155 15.9155 0 0 1 0 31.831 a 15.9155 15.9155 0 0 1 0 -31.831"
                />
              </svg>
              <div className="absolute inset-0 flex items-center justify-center text-sm font-bold">
                68%
              </div>
            </div>
            <span className={`text-[11px] mt-2 font-medium ${subtextColor}`}>Discharging</span>
          </div>

          {/* Screen Time Widget */}
          <div className={`p-3.5 rounded-3xl ${cardBg} flex flex-col justify-between text-left`}>
            <div>
              <div className={`text-[10px] uppercase font-semibold tracking-wider ${subtextColor}`}>
                Screen Time
              </div>
              <div className="text-xl font-bold tracking-tight">4h 32m</div>
            </div>

            {/* Mini Activity Bar Chart */}
            <div className="flex items-end gap-1 h-7 my-1">
              {[40, 65, 30, 85, 95, 60, 80, 50, 90, 75].map((height, i) => (
                <div
                  key={i}
                  style={{
                    height: `${height}%`,
                    backgroundColor: i === 4 ? settings.accentColor : isLight ? '#D1D5DB' : '#4B5563'
                  }}
                  className="flex-1 rounded-t-sm"
                />
              ))}
            </div>

            {/* App Breakdown */}
            <div className="space-y-0.5 text-[10px]">
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-red-500 inline-block" /> YouTube
                </span>
                <span className={subtextColor}>1h 12m</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-amber-500 inline-block" /> Chrome
                </span>
                <span className={subtextColor}>52m</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="flex items-center gap-1">
                  <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 inline-block" /> WhatsApp
                </span>
                <span className={subtextColor}>28m</span>
              </div>
            </div>
          </div>
        </div>

        {/* Row 4: Notes Checklist & Scenic Photo Widget */}
        <div className="grid grid-cols-2 gap-3">
          {/* Notes Widget */}
          <div className={`p-4 rounded-3xl ${cardBg} flex flex-col justify-between text-left`}>
            <div className="flex items-center justify-between mb-2">
              <span className="text-xs font-semibold">Notes</span>
              <button
                onClick={() => setShowAddNote(!showAddNote)}
                className="hover:opacity-75 transition p-0.5"
                title="Add note"
              >
                <Plus size={14} />
              </button>
            </div>

            {showAddNote && (
              <form onSubmit={handleAddNote} className="mb-2">
                <input
                  type="text"
                  placeholder="New note..."
                  value={newNoteText}
                  onChange={(e) => setNewNoteText(e.target.value)}
                  className={`w-full text-xs p-1.5 rounded-lg outline-none border ${
                    isDark
                      ? 'bg-neutral-900 border-white/15 text-white placeholder:text-neutral-500'
                      : 'bg-white border-black/15 text-slate-900 placeholder:text-slate-400'
                  }`}
                  autoFocus
                />
              </form>
            )}

            <div className="space-y-1.5 text-xs">
              {notes.map((note) => (
                <div
                  key={note.id}
                  onClick={() => onToggleNote(note.id)}
                  className="flex items-center gap-2 cursor-pointer group"
                >
                  <div
                    className={`w-3.5 h-3.5 rounded border flex items-center justify-center transition-all ${
                      note.completed
                        ? 'bg-emerald-500 border-emerald-500 text-white'
                        : isDark ? 'border-white/30' : 'border-black/30'
                    }`}
                  >
                    {note.completed && <Check size={10} strokeWidth={3} />}
                  </div>
                  <span className={`truncate text-[11px] ${note.completed ? 'line-through opacity-50' : ''}`}>
                    {note.text}
                  </span>
                </div>
              ))}
            </div>
          </div>

          {/* Photo / Nature Widget */}
          <div className="rounded-3xl overflow-hidden shadow-sm relative h-36 group">
            <div
              className="absolute inset-0 bg-cover bg-center transition-transform duration-700 group-hover:scale-105"
              style={{
                backgroundImage: `url('https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=400&q=80')`
              }}
            />
            <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent flex items-end p-3">
              <span className="text-[10px] text-white/90 font-medium tracking-wide">
                Misty Pines • Alps
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Page Indicator Dots */}
      <div className="flex items-center justify-center gap-2 pt-3 shrink-0">
        <div className={`w-1.5 h-1.5 rounded-full ${isDark ? 'bg-white/20' : 'bg-black/20'}`} />
        <div className={`w-1.5 h-1.5 rounded-full ${isDark ? 'bg-white/20' : 'bg-black/20'}`} />
        <div
          style={{ backgroundColor: settings.accentColor }}
          className="w-4 h-1.5 rounded-full shadow-xs"
        />
        <div className={`w-1.5 h-1.5 rounded-full ${isDark ? 'bg-white/20' : 'bg-black/20'}`} />
      </div>
    </div>
  );
};
