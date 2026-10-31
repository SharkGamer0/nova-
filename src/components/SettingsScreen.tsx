import React, { useState } from 'react';
import { GestureAction, GestureType, IconShape, IconStyle, LauncherSettings, LauncherTheme } from '../launcher/types';
import {
  ArrowLeft,
  ChevronRight,
  Sun,
  Moon,
  Palette,
  Sliders,
  Lock,
  EyeOff,
  Check,
  Download,
  Code2,
  Smartphone
} from './Icons';
import { AppIcon } from './Icons';

interface SettingsScreenProps {
  settings: LauncherSettings;
  onUpdateSettings: (updater: (prev: LauncherSettings) => LauncherSettings) => void;
  onBack: () => void;
  onOpenHiddenAppsPin: () => void;
  onOpenAndroidCodeViewer: () => void;
  onOpenContextRingDesigner?: () => void;
}

type SettingsSubPage =
  | 'root'
  | 'homeCustomization'
  | 'appearance'
  | 'animatedIcons'
  | 'iconDesigner'
  | 'gestures'
  | 'focusMode'
  | 'backup'
  | 'about';

export const SettingsScreen: React.FC<SettingsScreenProps> = ({
  settings,
  onUpdateSettings,
  onBack,
  onOpenHiddenAppsPin,
  onOpenAndroidCodeViewer,
  onOpenContextRingDesigner
}) => {
  const [subPage, setSubPage] = useState<SettingsSubPage>('root');

  const isLight = settings.theme === 'warm-light';
  const textColor = isLight ? 'text-[#242725]' : 'text-[#F1F3F4]';
  const subtextColor = isLight ? 'text-[#5A605B]' : 'text-[#A0A8B0]';
  const itemHover = isLight ? 'hover:bg-black/5' : 'hover:bg-white/5';
  const cardBg = isLight
    ? 'bg-white/70 backdrop-blur-md border border-black/5'
    : 'bg-[#1C1F24]/80 backdrop-blur-md border border-white/10';

  const accentColors = ['#38BDF8', '#6366F1', '#2A9D8F', '#E76F51', '#264653', '#3B82F6', '#10B981'];

  // Root Menu
  if (subPage === 'root') {
    const menuItems = [
      {
        id: 'homeCustomization',
        title: 'Home Screen & Glance',
        desc: 'Clock style, music player, wallpaper dimming & info toggles',
        badge: 'Redesigned',
        action: () => setSubPage('homeCustomization')
      },
      {
        id: 'contextRings',
        title: 'Context Rings Designer',
        desc: 'Radial command menu, custom actions & gesture bindings',
        badge: 'New',
        action: () => onOpenContextRingDesigner && onOpenContextRingDesigner()
      },
      {
        id: 'home',
        title: 'Home Screen',
        desc: 'Layout, widgets, favorites',
        action: () => setSubPage('appearance')
      },
      {
        id: 'animatedIcons',
        title: 'Animated Icon Engine',
        desc: 'Custom GIF, WebP & Lottie animations with battery saver',
        badge: 'New',
        action: () => setSubPage('animatedIcons')
      },
      {
        id: 'applist',
        title: 'App List',
        desc: 'Search, sorting, categories',
        action: () => setSubPage('appearance')
      },
      {
        id: 'appearance',
        title: 'Appearance',
        desc: 'Theme, icons, font, colors',
        action: () => setSubPage('appearance')
      },
      {
        id: 'iconDesigner',
        title: 'Icon Designer',
        desc: 'Change icon shape and style',
        action: () => setSubPage('iconDesigner')
      },
      {
        id: 'gestures',
        title: 'Gestures',
        desc: 'Configure swipe and tap actions',
        action: () => setSubPage('gestures')
      },
      {
        id: 'hiddenApps',
        title: 'Hidden Apps',
        desc: 'Privacy and app lock',
        action: () => onOpenHiddenAppsPin()
      },
      {
        id: 'focusMode',
        title: 'Focus Mode',
        desc: 'Reduce distractions',
        action: () => setSubPage('focusMode')
      },
      {
        id: 'backup',
        title: 'Backup & Restore',
        desc: 'Save and load your setup',
        action: () => setSubPage('backup')
      },
      {
        id: 'androidCode',
        title: 'Android Studio Source & APK',
        desc: 'Inspect Kotlin Compose code & download project',
        badge: 'Native',
        action: () => onOpenAndroidCodeViewer()
      },
      {
        id: 'about',
        title: 'About NovaForge',
        desc: 'Version 1.0.0, architecture',
        action: () => setSubPage('about')
      }
    ];

    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center justify-between pb-3 border-b border-black/5 dark:border-white/5">
          <div className="flex items-center gap-3">
            <button
              onClick={onBack}
              className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
              title="Back"
            >
              <ArrowLeft size={18} />
            </button>
            <h1 className="text-base font-semibold">Launcher Settings</h1>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-2 space-y-1">
          {/* NovaForge Official Brand Card */}
          <div
            onClick={() => setSubPage('about')}
            className={`p-3.5 mb-2 rounded-2xl ${cardBg} flex items-center gap-3.5 cursor-pointer hover:border-cyan-500/30 transition shadow-sm`}
          >
            <img
              src="/novaforge_logo.png"
              alt="NovaForge Logo"
              className="w-12 h-12 rounded-xl object-contain shadow-md border border-cyan-500/20 bg-[#07090E] shrink-0"
            />
            <div className="flex-1 min-w-0 text-left">
              <div className="text-sm font-semibold truncate flex items-center gap-1.5">
                <span>NovaForge Launcher</span>
              </div>
              <div className="text-[11px] text-cyan-400 font-mono">v1.2.0 Cosmic Orbit</div>
              <div className={`text-[10px] ${subtextColor} truncate`}>Local-First • Jetpack Compose • 100% Free</div>
            </div>
            <ChevronRight size={16} className={subtextColor} />
          </div>

          {menuItems.map((item) => (
            <div
              key={item.id}
              onClick={item.action}
              className={`flex items-center justify-between p-3 rounded-2xl ${itemHover} cursor-pointer transition`}
            >
              <div className="text-left">
                <div className="flex items-center gap-2">
                  <span className="text-sm font-medium">{item.title}</span>
                  {item.badge && (
                    <span
                      style={{ backgroundColor: settings.accentColor }}
                      className="text-[10px] text-white px-2 py-0.5 rounded-full font-semibold"
                    >
                      {item.badge}
                    </span>
                  )}
                </div>
                <p className={`text-xs ${subtextColor}`}>{item.desc}</p>
              </div>
              <ChevronRight size={16} className={subtextColor} />
            </div>
          ))}
        </div>
      </div>
    );
  }

  // Home Screen & Glance Customization Subpage
  if (subPage === 'homeCustomization') {
    const clockStyles: Array<{ id: 'digital' | 'minimal' | 'bold' | 'serif' | 'monospace' | 'cosmic'; name: string; preview: string }> = [
      { id: 'digital', name: 'Digital', preview: '10:24 (Clean sans-serif)' },
      { id: 'minimal', name: 'Minimal', preview: '10:24 (Thin airy display)' },
      { id: 'bold', name: 'Bold', preview: '10:24 (Heavy display numerals)' },
      { id: 'serif', name: 'Serif', preview: '10:24 (Editorial serif digits)' },
      { id: 'monospace', name: 'Monospace', preview: '10:24 (Tech glyph numerals)' },
      { id: 'cosmic', name: 'Cosmic Orbit', preview: '10:24 (Cyan celestial accent)' }
    ];

    const currentStyle = settings.clockStyle || 'digital';
    const currentSize = settings.clockSize || 'large';
    const dimVal = Math.round((settings.wallpaperDimOverlay ?? 0.20) * 100);

    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-base font-semibold">Home Screen & Glance</h1>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-3 space-y-4 text-left">
          {/* Clock Typography Style */}
          <div>
            <label className="text-xs font-semibold block mb-2 text-neutral-300">Clock Typography Style</label>
            <div className="space-y-1.5">
              {clockStyles.map((style) => (
                <div
                  key={style.id}
                  onClick={() => onUpdateSettings((prev) => ({ ...prev, clockStyle: style.id }))}
                  className={`p-2.5 rounded-xl border cursor-pointer transition flex items-center justify-between ${
                    currentStyle === style.id
                      ? 'border-[#38BDF8] bg-[#38BDF8]/10'
                      : 'border-white/10 bg-white/5 hover:bg-white/10'
                  }`}
                >
                  <div>
                    <span className="text-xs font-medium block">{style.name}</span>
                    <span className="text-[10px] text-neutral-400 block">{style.preview}</span>
                  </div>
                  {currentStyle === style.id && <Check size={16} className="text-[#38BDF8]" />}
                </div>
              ))}
            </div>
          </div>

          {/* Clock Size */}
          <div>
            <label className="text-xs font-semibold block mb-2 text-neutral-300">Clock Size</label>
            <div className="grid grid-cols-3 gap-2">
              {(['compact', 'standard', 'large'] as const).map((size) => (
                <button
                  key={size}
                  onClick={() => onUpdateSettings((prev) => ({ ...prev, clockSize: size }))}
                  className={`py-2 text-xs rounded-xl border transition capitalize ${
                    currentSize === size
                      ? 'border-[#38BDF8] bg-[#38BDF8]/20 font-semibold text-[#38BDF8]'
                      : 'border-white/10 bg-white/5 hover:bg-white/10 text-neutral-300'
                  }`}
                >
                  {size}
                </button>
              ))}
            </div>
          </div>

          {/* Wallpaper Readability Dimming Overlay */}
          <div className="p-3 rounded-2xl bg-white/5 border border-white/10">
            <div className="flex items-center justify-between mb-1">
              <span className="text-xs font-medium">Wallpaper Readability Scrim</span>
              <span className="text-xs font-mono text-[#38BDF8]">{dimVal}%</span>
            </div>
            <p className="text-[10px] text-neutral-400 mb-2">
              Translucent overlay for text legibility over any wallpaper without obscuring it.
            </p>
            <input
              type="range"
              min="0"
              max="70"
              value={dimVal}
              onChange={(e) => {
                const val = parseFloat(e.target.value) / 100;
                onUpdateSettings((prev) => ({ ...prev, wallpaperDimOverlay: val }));
              }}
              className="w-full accent-[#38BDF8] cursor-pointer"
            />
          </div>

          {/* Information & Glance Toggles */}
          <div className="space-y-2">
            <label className="text-xs font-semibold block text-neutral-300">Glance & Interface Elements</label>

            {[
              { key: 'showBattery', label: 'Battery Percentage & Status', desc: 'Display live battery in top header' },
              { key: 'showWeather', label: 'Live Weather Glance', desc: 'Temperature and condition under clock' },
              { key: 'showCalendarEvent', label: 'Upcoming Calendar Agenda', desc: 'Upcoming tasks or countdown under clock' },
              { key: 'showMusicHome', label: 'Home Screen Music Widget', desc: 'Compact MediaSession player on Home' },
              { key: 'showMusicDrawer', label: 'App Drawer Music Widget', desc: 'Compact player at top of App Drawer' },
              { key: 'showAlphabetRail', label: 'Vertical A–Z Fast Rail', desc: 'Continuous scrub letter index on drawer' },
              { key: 'hapticsEnabled', label: 'Haptic Feedback', desc: 'Vibrate on Context Ring selections' }
            ].map((toggle) => {
              const val = (settings as any)[toggle.key] ?? true;
              return (
                <div
                  key={toggle.key}
                  onClick={() => onUpdateSettings((prev) => ({ ...prev, [toggle.key]: !val }))}
                  className="p-2.5 rounded-xl bg-white/5 border border-white/10 flex items-center justify-between cursor-pointer hover:bg-white/10 transition"
                >
                  <div className="pr-2">
                    <span className="text-xs font-medium block">{toggle.label}</span>
                    <span className="text-[10px] text-neutral-400 block">{toggle.desc}</span>
                  </div>
                  <div className={`w-8 h-4 rounded-full p-0.5 transition-colors ${val ? 'bg-[#38BDF8]' : 'bg-neutral-700'}`}>
                    <div className={`w-3 h-3 rounded-full bg-white transition-transform ${val ? 'translate-x-4' : 'translate-x-0'}`} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    );
  }

  // Appearance & Themes Subpage
  if (subPage === 'appearance') {
    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-base font-semibold">Appearance & Themes</h1>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-3 space-y-5">
          {/* Theme Selector */}
          <div>
            <label className="text-xs font-semibold block mb-2 text-left">Theme</label>
            <div className="grid grid-cols-3 gap-2">
              {[
                { id: 'warm-light', label: 'Warm Light', icon: Sun },
                { id: 'charcoal-dark', label: 'Charcoal', icon: Moon },
                { id: 'monochrome', label: 'Minimal', icon: Palette }
              ].map(({ id, label, icon: Icon }) => (
                <button
                  key={id}
                  onClick={() =>
                    onUpdateSettings((prev) => ({
                      ...prev,
                      theme: id as LauncherTheme
                    }))
                  }
                  className={`p-3 rounded-2xl flex flex-col items-center gap-2 text-xs font-medium border transition ${
                    settings.theme === id
                      ? 'border-emerald-500 bg-emerald-500/10'
                      : 'border-black/10 dark:border-white/10 hover:bg-black/5'
                  }`}
                >
                  <Icon size={18} />
                  <span>{label}</span>
                </button>
              ))}
            </div>
          </div>

          {/* Accent Color */}
          <div>
            <label className="text-xs font-semibold block mb-2 text-left">Accent Color</label>
            <div className="flex items-center gap-3">
              {accentColors.map((color) => (
                <button
                  key={color}
                  onClick={() =>
                    onUpdateSettings((prev) => ({
                      ...prev,
                      accentColor: color
                    }))
                  }
                  style={{ backgroundColor: color }}
                  className="w-8 h-8 rounded-full flex items-center justify-center text-white shadow-xs hover:scale-110 transition"
                >
                  {settings.accentColor === color && <Check size={14} strokeWidth={3} />}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Animated Icon Engine Subpage
  if (subPage === 'animatedIcons') {
    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="text-base font-semibold">Animated Icon Engine</h1>
            <p className={`text-[10px] ${subtextColor}`}>Native Android GIF, WebP & Lottie engine</p>
          </div>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-3 space-y-4 text-left">
          {/* Battery & Performance Engine Status */}
          <div className={`p-4 rounded-3xl ${cardBg} space-y-3`}>
            <div className="flex items-center justify-between">
              <div>
                <div className="text-xs font-semibold">Battery & Performance Engine</div>
                <div className={`text-[10px] ${subtextColor}`}>Throttles animations & pauses when screen is off</div>
              </div>
              <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/20 text-emerald-400 font-mono">
                Active
              </span>
            </div>

            <div className="grid grid-cols-3 gap-2 pt-1">
              {[
                { name: 'Battery Saver', max: '2 max', fps: '15 fps' },
                { name: 'Balanced', max: '6 max', fps: '30 fps', active: true },
                { name: 'Performance', max: '16 max', fps: '60 fps' }
              ].map((m) => (
                <div
                  key={m.name}
                  className={`p-2.5 rounded-2xl border text-center transition cursor-pointer ${
                    m.active
                      ? 'border-emerald-500/50 bg-emerald-500/10'
                      : 'border-white/5 bg-black/5 dark:bg-white/5'
                  }`}
                >
                  <div className="text-[11px] font-semibold">{m.name}</div>
                  <div className={`text-[9px] ${subtextColor}`}>{m.max} • {m.fps}</div>
                </div>
              ))}
            </div>
          </div>

          {/* Curated Animations Showcase */}
          <div className="space-y-2">
            <div className="text-[11px] font-bold tracking-wider opacity-60">CURATED PRESET LIBRARY</div>
            <div className="grid grid-cols-2 gap-2.5">
              {[
                { name: 'Quantum Orbit', fmt: 'WebP', size: '8.4 KB', color: '#2A9D8F' },
                { name: 'Minimal Pulse', fmt: 'Lottie', size: '4.2 KB', color: '#E76F51' },
                { name: 'Fluid Wave', fmt: 'GIF', size: '12.3 KB', color: '#3B82F6' },
                { name: 'Nova Sparks', fmt: 'Lottie', size: '6.1 KB', color: '#F43F5E' }
              ].map((item) => (
                <div
                  key={item.name}
                  className={`p-3 rounded-2xl ${cardBg} flex items-center gap-3`}
                >
                  <div
                    style={{ backgroundColor: item.color }}
                    className="w-10 h-10 rounded-xl flex items-center justify-center text-white font-bold animate-pulse text-xs"
                  >
                    ✦
                  </div>
                  <div>
                    <div className="text-xs font-semibold">{item.name}</div>
                    <div className={`text-[10px] ${subtextColor}`}>{item.fmt} • {item.size}</div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Native Source Inspector */}
          <div className={`p-4 rounded-3xl ${cardBg} space-y-2`}>
            <div className="text-xs font-semibold">Native Kotlin Engine Architecture</div>
            <p className={`text-[11px] ${subtextColor}`}>
              Built with <code>AnimationRepository.kt</code>, <code>AnimationPlaybackController.kt</code>, and <code>AnimatedIconView.kt</code>.
            </p>
            <button
              onClick={onOpenAndroidCodeViewer}
              style={{ borderColor: settings.accentColor, color: settings.accentColor }}
              className="text-xs font-semibold px-3 py-1.5 rounded-xl border flex items-center gap-2 mt-1 hover:bg-emerald-500/10 transition"
            >
              <Code2 size={14} /> View Android Kotlin Source
            </button>
          </div>
        </div>
      </div>
    );
  }

  // Icon Designer Subpage
  if (subPage === 'iconDesigner') {
    const shapes: { id: IconShape; label: string }[] = [
      { id: 'circle', label: 'Circle' },
      { id: 'squircle', label: 'Squircle' },
      { id: 'rounded-square', label: 'Rounded' },
      { id: 'square', label: 'Square' },
      { id: 'pebble', label: 'Pebble' }
    ];

    const styles: { id: IconStyle; label: string }[] = [
      { id: 'default', label: 'Default' },
      { id: 'monochrome', label: 'Monochrome' },
      { id: 'outline', label: 'Outline' },
      { id: 'custom', label: 'Custom' }
    ];

    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-base font-semibold">Icon Designer</h1>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-3 space-y-5">
          {/* Live Preview Box */}
          <div className={`p-4 rounded-3xl ${cardBg} text-center space-y-3`}>
            <div className="text-xs opacity-70">Interactive Icon Preview</div>
            <div className="flex items-center justify-center gap-4 py-2">
              <AppIcon
                name="Headphones"
                color="#1DB954"
                shape={settings.iconShape}
                style={settings.iconStyle}
                theme={settings.theme}
                accentColor={settings.accentColor}
                size="lg"
              />
              <AppIcon
                name="Compass"
                color="#EA4335"
                shape={settings.iconShape}
                style={settings.iconStyle}
                theme={settings.theme}
                accentColor={settings.accentColor}
                size="lg"
              />
              <AppIcon
                name="MessageSquare"
                color="#25D366"
                shape={settings.iconShape}
                style={settings.iconStyle}
                theme={settings.theme}
                accentColor={settings.accentColor}
                size="lg"
              />
            </div>
          </div>

          {/* Shape Selector */}
          <div>
            <label className="text-xs font-semibold block mb-2 text-left">Icon Shape</label>
            <div className="grid grid-cols-5 gap-1.5">
              {shapes.map((s) => (
                <button
                  key={s.id}
                  onClick={() =>
                    onUpdateSettings((prev) => ({
                      ...prev,
                      iconShape: s.id
                    }))
                  }
                  className={`py-2 px-1 rounded-xl text-[11px] font-medium border text-center transition ${
                    settings.iconShape === s.id
                      ? 'border-emerald-500 bg-emerald-500/10 font-bold'
                      : 'border-black/10 dark:border-white/10 hover:bg-black/5'
                  }`}
                >
                  {s.label}
                </button>
              ))}
            </div>
          </div>

          {/* Style Selector */}
          <div>
            <label className="text-xs font-semibold block mb-2 text-left">Icon Style</label>
            <div className="grid grid-cols-4 gap-2">
              {styles.map((st) => (
                <button
                  key={st.id}
                  onClick={() =>
                    onUpdateSettings((prev) => ({
                      ...prev,
                      iconStyle: st.id
                    }))
                  }
                  className={`py-2 px-1 rounded-xl text-xs font-medium border text-center transition ${
                    settings.iconStyle === st.id
                      ? 'border-emerald-500 bg-emerald-500/10 font-bold'
                      : 'border-black/10 dark:border-white/10 hover:bg-black/5'
                  }`}
                >
                  {st.label}
                </button>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  // Gestures Subpage
  if (subPage === 'gestures') {
    const gestureOptions: { label: string; action: GestureAction }[] = [
      { label: 'Open App List', action: 'openAppList' },
      { label: 'Open Widgets Page', action: 'openWidgets' },
      { label: 'Open Search', action: 'openSearch' },
      { label: 'Open Settings', action: 'openSettings' },
      { label: 'Lock Screen', action: 'lockScreen' },
      { label: 'Edit Home Screen', action: 'editHomeScreen' },
      { label: 'Previous Page', action: 'previousPage' },
      { label: 'Next Page', action: 'nextPage' }
    ];

    const gestureList: { key: GestureType; title: string; arrow: string }[] = [
      { key: 'swipeUp', title: 'Swipe Up', arrow: '↑' },
      { key: 'swipeDown', title: 'Swipe Down', arrow: '↓' },
      { key: 'swipeLeft', title: 'Swipe Left', arrow: '←' },
      { key: 'swipeRight', title: 'Swipe Right', arrow: '→' },
      { key: 'doubleTap', title: 'Double Tap', arrow: '⦿' },
      { key: 'longPress', title: 'Long Press', arrow: '👆' }
    ];

    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-base font-semibold">Gesture Controls</h1>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-2 space-y-2">
          {gestureList.map(({ key, title, arrow }) => {
            const currentAction = settings.gestures[key];
            const currentOption = gestureOptions.find((o) => o.action === currentAction);

            return (
              <div key={key} className={`p-3 rounded-2xl ${cardBg} flex items-center justify-between`}>
                <div className="flex items-center gap-3 text-left">
                  <div
                    style={{ backgroundColor: settings.accentColor }}
                    className="w-7 h-7 rounded-full text-white flex items-center justify-center font-bold text-xs"
                  >
                    {arrow}
                  </div>
                  <div>
                    <div className="text-xs font-semibold">{title}</div>
                    <div className={`text-[11px] ${subtextColor}`}>{currentOption?.label}</div>
                  </div>
                </div>

                <select
                  value={currentAction}
                  onChange={(e) => {
                    const newAction = e.target.value as GestureAction;
                    onUpdateSettings((prev) => ({
                      ...prev,
                      gestures: { ...prev.gestures, [key]: newAction }
                    }));
                  }}
                  className="text-xs bg-black/5 dark:bg-white/10 rounded-lg p-1.5 outline-none border border-black/10 dark:border-white/10"
                >
                  {gestureOptions.map((opt) => (
                    <option key={opt.action} value={opt.action} className="bg-neutral-800 text-white">
                      {opt.label}
                    </option>
                  ))}
                </select>
              </div>
            );
          })}
        </div>
      </div>
    );
  }

  // Focus Mode Subpage
  if (subPage === 'focusMode') {
    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-base font-semibold">Focus Mode</h1>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-3 space-y-4">
          <div className={`p-4 rounded-3xl ${cardBg} flex items-center justify-between`}>
            <div className="text-left">
              <span className="text-sm font-semibold block">Enable Focus Mode</span>
              <span className={`text-xs ${subtextColor}`}>Hides distracting social and game apps</span>
            </div>
            <button
              onClick={() =>
                onUpdateSettings((prev) => ({
                  ...prev,
                  focusModeEnabled: !prev.focusModeEnabled
                }))
              }
              style={{
                backgroundColor: settings.focusModeEnabled ? settings.accentColor : '#9CA3AF'
              }}
              className="w-12 h-6 rounded-full relative transition-colors duration-200 cursor-pointer p-0.5"
            >
              <div
                className={`w-5 h-5 rounded-full bg-white transition-transform duration-200 ${
                  settings.focusModeEnabled ? 'translate-x-6' : 'translate-x-0'
                }`}
              />
            </button>
          </div>

          <div className={`p-4 rounded-3xl ${cardBg} space-y-2 text-left`}>
            <div className="text-xs font-semibold">Allowed Apps (Whitelisted)</div>
            <p className={`text-xs ${subtextColor}`}>
              {settings.focusAllowedAppIds.length} apps currently permitted during focus sessions.
            </p>
            <div className="text-xs font-medium pt-2">Schedule</div>
            <div className={`text-xs ${subtextColor}`}>{settings.focusSchedule}</div>
          </div>
        </div>
      </div>
    );
  }

  // Backup & Restore
  if (subPage === 'backup') {
    return (
      <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
        <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
          <button
            onClick={() => setSubPage('root')}
            className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
          >
            <ArrowLeft size={18} />
          </button>
          <h1 className="text-base font-semibold">Backup & Restore</h1>
        </div>

        <div className="flex-1 overflow-y-auto no-scrollbar py-4 space-y-3 text-left">
          <div className={`p-4 rounded-3xl ${cardBg} space-y-2`}>
            <div className="text-sm font-semibold">Save Layout Configuration</div>
            <p className={`text-xs ${subtextColor}`}>
              Export your launcher arrangement, icon shapes, and preferences to JSON.
            </p>
            <button
              onClick={() => {
                const data = JSON.stringify(settings, null, 2);
                const blob = new Blob([data], { type: 'application/json' });
                const url = URL.createObjectURL(blob);
                const a = document.createElement('a');
                a.href = url;
                a.download = 'novaforge-launcher-backup.json';
                a.click();
              }}
              style={{ backgroundColor: settings.accentColor }}
              className="text-xs text-white font-medium px-4 py-2 rounded-xl flex items-center gap-2 mt-2"
            >
              <Download size={14} /> Export Backup (.json)
            </button>
          </div>
        </div>
      </div>
    );
  }

  // About Subpage
  return (
    <div className={`h-full flex flex-col pt-8 pb-4 px-5 select-none ${textColor}`}>
      <div className="flex items-center gap-3 pb-3 border-b border-black/5 dark:border-white/5">
        <button
          onClick={() => setSubPage('root')}
          className="p-1.5 rounded-full hover:bg-black/5 dark:hover:bg-white/10 transition"
        >
          <ArrowLeft size={18} />
        </button>
        <h1 className="text-base font-semibold">About NovaForge</h1>
      </div>

      <div className="flex-1 overflow-y-auto no-scrollbar py-6 space-y-4 text-center">
        <img
          src="/novaforge_logo.png"
          alt="NovaForge Official Brand Logo"
          className="w-20 h-20 rounded-2xl mx-auto object-contain shadow-2xl border border-cyan-500/20 bg-[#07090E]"
        />
        <div>
          <div className="text-lg font-bold">NovaForge Launcher</div>
          <div className="text-xs text-cyan-400 font-mono">v1.2.0 Cosmic Orbit Edition</div>
        </div>
        <p className={`text-xs ${subtextColor} max-w-xs mx-auto leading-relaxed`}>
          A native Android HOME launcher crafted with Jetpack Compose, Material 3, and deep customization. Minimalist space aesthetic, zero bloat, fast gesture navigation, real icon caching, and local-first privacy.
        </p>

        <button
          onClick={onOpenAndroidCodeViewer}
          style={{ borderColor: settings.accentColor, color: settings.accentColor }}
          className="text-xs font-semibold px-4 py-2 rounded-xl border flex items-center gap-2 mx-auto hover:bg-emerald-500/10 transition"
        >
          <Code2 size={16} /> View Android Source Code & APK Guide
        </button>
      </div>
    </div>
  );
};
