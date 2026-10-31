import React, { useState } from 'react';
import confetti from 'canvas-confetti';
import {
  INITIAL_APPS,
  INITIAL_AGENDA,
  INITIAL_NOTES,
  INITIAL_SETTINGS,
  SOCIAL_STACK,
  INITIAL_CONTEXT_RINGS
} from './launcher/sampleData';
import { AppItem, AgendaItem, NoteItem, LauncherSettings, GestureAction, ContextRingConfig, ActiveRingSession, ContextRingAction } from './launcher/types';
import { filterApps, calculateSmartFavorites, resolveContextRing } from './launcher/logic';
import { PhoneFrame } from './components/PhoneFrame';
import { HomeScreen } from './components/HomeScreen';
import { AppListScreen } from './components/AppListScreen';
import { WidgetsScreen } from './components/WidgetsScreen';
import { SettingsScreen } from './components/SettingsScreen';
import { PinLockModal } from './components/PinLockModal';
import { SocialFolderModal } from './components/SocialFolderModal';
import { AppActionModal } from './components/AppActionModal';
import { AndroidCodeModal } from './components/AndroidCodeModal';
import { ScreenLockOverlay } from './components/ScreenLockOverlay';
import { ContextRingOverlay } from './components/ContextRingOverlay';
import { ContextRingDesignerModal } from './components/ContextRingDesignerModal';
import {
  Code2,
  Sun,
  Moon,
  Smartphone,
  Sliders,
  Grid,
  Check,
  Download
} from './components/Icons';

export default function App() {
  const [settings, setSettings] = useState<LauncherSettings>(INITIAL_SETTINGS);
  const [apps, setApps] = useState<AppItem[]>(INITIAL_APPS);
  const [agenda, setAgenda] = useState<AgendaItem[]>(INITIAL_AGENDA);
  const [notes, setNotes] = useState<NoteItem[]>(INITIAL_NOTES);

  // Context Rings State
  const [contextRingConfig, setContextRingConfig] = useState<ContextRingConfig>(INITIAL_CONTEXT_RINGS);
  const [activeRingSession, setActiveRingSession] = useState<ActiveRingSession | null>(null);
  const [showContextRingDesigner, setShowContextRingDesigner] = useState<boolean>(false);

  // Active Screen: 0 = App List, 1 = Home, 2 = Widgets, 3 = Settings
  const [activeScreenIndex, setActiveScreenIndex] = useState<number>(1);
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedCategory, setSelectedCategory] = useState<string>('All');

  // Modals & Overlays
  const [showPinModal, setShowPinModal] = useState<boolean>(false);
  const [showHiddenUnlocked, setShowHiddenUnlocked] = useState<boolean>(false);
  const [showSocialFolder, setShowSocialFolder] = useState<boolean>(false);
  const [selectedAppForAction, setSelectedAppForAction] = useState<AppItem | null>(null);
  const [showAndroidCodeModal, setShowAndroidCodeModal] = useState<boolean>(false);
  const [isScreenLocked, setIsScreenLocked] = useState<boolean>(false);
  const [gestureToast, setGestureToast] = useState<string | null>(null);

  // View Mode: 'phone' or 'gallery' (all 5 side-by-side)
  const [viewMode, setViewMode] = useState<'phone' | 'gallery'>('phone');

  // Computed favorites & filtered apps
  const visibleApps = filterApps(
    apps,
    searchQuery,
    selectedCategory,
    settings.focusModeEnabled,
    settings.focusAllowedAppIds,
    showHiddenUnlocked
  );

  const smartFavorites = calculateSmartFavorites(apps, 8);

  const socialApps = apps.filter((a) => SOCIAL_STACK.includes(a.id));

  // Trigger feedback toast
  const triggerToast = (msg: string) => {
    setGestureToast(msg);
    setTimeout(() => setGestureToast(null), 2500);
  };

  // Handle gestures
  const handleTriggerGesture = (
    type: 'swipeUp' | 'swipeDown' | 'swipeLeft' | 'swipeRight' | 'doubleTap' | 'longPress'
  ) => {
    const action: GestureAction = settings.gestures[type];
    triggerToast(`Gesture: ${type} → ${action}`);

    switch (action) {
      case 'openAppList':
        setActiveScreenIndex(0);
        break;
      case 'openWidgets':
        setActiveScreenIndex(2);
        break;
      case 'openSearch':
        setActiveScreenIndex(0);
        break;
      case 'openSettings':
        setActiveScreenIndex(3);
        break;
      case 'lockScreen':
        setIsScreenLocked(true);
        break;
      case 'previousPage':
        setActiveScreenIndex((prev) => Math.max(0, prev - 1));
        break;
      case 'nextPage':
        setActiveScreenIndex((prev) => Math.min(3, prev + 1));
        break;
      case 'editHomeScreen':
        setActiveScreenIndex(3);
        break;
    }
  };

  // Launch app simulated
  const handleLaunchApp = (app: AppItem) => {
    // Update usage count & recency
    setApps((prev) =>
      prev.map((a) =>
        a.id === app.id
          ? { ...a, launchCount: a.launchCount + 1, lastUsedTime: Date.now() }
          : a
      )
    );
    triggerToast(`Launched ${app.name} (${app.packageName})`);
  };

  // Toggle agenda complete
  const handleToggleAgenda = (id: string) => {
    setAgenda((prev) =>
      prev.map((item) => (item.id === id ? { ...item, completed: !item.completed } : item))
    );
  };

  // Add agenda item
  const handleAddAgenda = (title: string, time: string) => {
    setAgenda((prev) => [
      ...prev,
      { id: `agenda-${Date.now()}`, title, time, completed: false }
    ]);
    triggerToast('Added agenda item');
  };

  // Toggle notes
  const handleToggleNote = (id: string) => {
    setNotes((prev) =>
      prev.map((n) => (n.id === id ? { ...n, completed: !n.completed } : n))
    );
  };

  // Add note
  const handleAddNote = (text: string) => {
    setNotes((prev) => [...prev, { id: `n-${Date.now()}`, text, completed: false }]);
    triggerToast('Added note');
  };

  // Toggle favorite
  const handleToggleFavorite = (appId: string) => {
    setApps((prev) =>
      prev.map((a) => (a.id === appId ? { ...a, isFavorite: !a.isFavorite } : a))
    );
  };

  // Toggle hide app
  const handleToggleHide = (appId: string) => {
    setApps((prev) =>
      prev.map((a) => (a.id === appId ? { ...a, isHidden: !a.isHidden } : a))
    );
  };

  // Uninstall app
  const handleUninstall = (appId: string) => {
    setApps((prev) => prev.filter((a) => a.id !== appId));
    triggerToast('Uninstalled package');
  };

  // Trigger Context Ring
  const handleTriggerContextRing = (originX = 180, originY = 280, targetApp?: AppItem) => {
    const ring = resolveContextRing(contextRingConfig, targetApp?.packageName);
    setActiveRingSession({
      currentRing: ring,
      originX,
      originY,
      clampedCenterX: originX,
      clampedCenterY: originY,
      targetPackage: targetApp?.packageName,
      breadcrumbRingIds: [],
      selectedIndex: -1
    });
  };

  const handleSelectRingAction = (action: ContextRingAction) => {
    setActiveRingSession(null);
    triggerToast(`Context Ring: ${action.title} (${action.actionType})`);

    switch (action.actionType) {
      case 'LAUNCH_APP': {
        const payload = action.payload;
        if (payload) {
          const app = apps.find((a) => a.packageName === payload || a.name.toLowerCase() === payload.toLowerCase());
          if (app) handleLaunchApp(app);
          else triggerToast(`Launched: ${action.title}`);
        }
        break;
      }
      case 'OPEN_APP_DRAWER':
        setActiveScreenIndex(0);
        break;
      case 'OPEN_COMMAND_BAR':
        setActiveScreenIndex(0);
        break;
      case 'OPEN_SETTINGS':
        setActiveScreenIndex(3);
        break;
      case 'OPEN_WIDGETS':
        setActiveScreenIndex(2);
        break;
      case 'TOGGLE_FOCUS_MODE':
        setSettings((prev) => ({ ...prev, focusModeEnabled: !prev.focusModeEnabled }));
        break;
      case 'EXECUTE_MACRO':
        triggerToast(`Macro executed: ${action.payload}`);
        break;
      case 'SYSTEM_SETTING':
        triggerToast(`System Intent: ${action.payload}`);
        break;
      case 'MEDIA_CONTROL':
        triggerToast(`Media Control: ${action.payload}`);
        break;
      default:
        break;
    }
  };

  const handlePushNestedRing = (targetRingId: string) => {
    if (!activeRingSession) return;
    const targetRing = contextRingConfig.rings.find((r) => r.id === targetRingId);
    if (!targetRing) return;
    setActiveRingSession({
      ...activeRingSession,
      currentRing: targetRing,
      breadcrumbRingIds: [...activeRingSession.breadcrumbRingIds, activeRingSession.currentRing.id],
      selectedIndex: -1
    });
  };

  const handlePopNestedRing = () => {
    if (!activeRingSession || activeRingSession.breadcrumbRingIds.length === 0) {
      setActiveRingSession(null);
      return;
    }
    const prevRingId = activeRingSession.breadcrumbRingIds[activeRingSession.breadcrumbRingIds.length - 1];
    const prevBreadcrumbs = activeRingSession.breadcrumbRingIds.slice(0, -1);
    const prevRing = contextRingConfig.rings.find((r) => r.id === prevRingId) || contextRingConfig.rings[0];
    setActiveRingSession({
      ...activeRingSession,
      currentRing: prevRing,
      breadcrumbRingIds: prevBreadcrumbs,
      selectedIndex: -1
    });
  };

  return (
    <div className="min-h-screen bg-[#0F1115] text-[#ECEFF4] flex flex-col font-sans selection:bg-[#2A9D8F]/30 selection:text-[#2A9D8F]">
      {/* Top Navbar */}
      <header className="h-14 border-b border-white/10 bg-[#07090E]/95 backdrop-blur-md px-6 flex items-center justify-between z-30 shrink-0">
        <div className="flex items-center gap-3">
          <img
            src="/novaforge_logo.png"
            alt="NovaForge Logo"
            className="w-8 h-8 rounded-xl object-contain shadow-md border border-cyan-500/20 bg-[#07090E]"
          />
          <div>
            <div className="flex items-center gap-2">
              <span className="font-bold text-sm tracking-tight text-white">NovaForge Launcher</span>
              <span className="text-[10px] font-mono bg-cyan-500/20 text-cyan-400 border border-cyan-500/30 px-2 py-0.5 rounded-full">
                Cosmic Orbit Edition
              </span>
            </div>
            <p className="text-[11px] text-neutral-400 hidden sm:block">
              Kotlin • Jetpack Compose • Material 3 • AppWidgetHost
            </p>
          </div>
        </div>

        {/* Center Screen Navigator */}
        <div className="hidden md:flex items-center bg-black/40 p-1 rounded-2xl border border-white/10 text-xs">
          {[
            { id: 0, label: 'App List' },
            { id: 1, label: 'Home Screen' },
            { id: 2, label: 'Widgets Page' },
            { id: 3, label: 'Settings' }
          ].map((screen) => (
            <button
              key={screen.id}
              onClick={() => {
                setActiveScreenIndex(screen.id);
                setViewMode('phone');
              }}
              style={
                activeScreenIndex === screen.id && viewMode === 'phone'
                  ? { backgroundColor: settings.accentColor, color: '#FFFFFF' }
                  : undefined
              }
              className={`px-3.5 py-1.5 rounded-xl font-medium transition ${
                activeScreenIndex === screen.id && viewMode === 'phone'
                  ? 'shadow-xs font-semibold'
                  : 'text-neutral-400 hover:text-white'
              }`}
            >
              {screen.label}
            </button>
          ))}
        </div>

        {/* Action Controls */}
        <div className="flex items-center gap-2.5">
          {/* Focus Mode Quick Pill */}
          <button
            onClick={() => {
              setSettings((prev) => ({
                ...prev,
                focusModeEnabled: !prev.focusModeEnabled
              }));
              triggerToast(
                settings.focusModeEnabled ? 'Focus mode disabled' : 'Focus mode activated'
              );
            }}
            className={`text-xs px-3 py-1.5 rounded-xl border flex items-center gap-1.5 transition ${
              settings.focusModeEnabled
                ? 'bg-amber-500/20 border-amber-500/40 text-amber-300 font-semibold'
                : 'bg-white/5 border-white/10 text-neutral-300 hover:bg-white/10'
            }`}
          >
            <span
              className={`w-2 h-2 rounded-full ${
                settings.focusModeEnabled ? 'bg-amber-400 animate-ping' : 'bg-neutral-500'
              }`}
            />
            <span>Focus Mode</span>
          </button>

          {/* Gallery vs Phone view toggle */}
          <button
            onClick={() => setViewMode(viewMode === 'phone' ? 'gallery' : 'phone')}
            className={`text-xs px-3 py-1.5 rounded-xl border flex items-center gap-1.5 transition ${
              viewMode === 'gallery'
                ? 'bg-[#2A9D8F]/20 border-[#2A9D8F]/50 text-[#2A9D8F] font-semibold'
                : 'bg-white/5 border-white/10 text-neutral-300 hover:bg-white/10'
            }`}
            title="Toggle 5-Screen Showcase Gallery"
          >
            <Grid size={14} />
            <span className="hidden sm:inline">Showcase Gallery</span>
          </button>

          {/* Context Rings Quick Designer Button */}
          <button
            onClick={() => setShowContextRingDesigner(true)}
            className="text-xs px-3 py-1.5 rounded-xl border border-[#2A9D8F]/40 bg-[#2A9D8F]/15 text-[#2A9D8F] font-semibold flex items-center gap-1.5 hover:bg-[#2A9D8F]/25 transition"
            title="Open Context Rings Designer"
          >
            <div className="w-2.5 h-2.5 rounded-full border border-[#2A9D8F] flex items-center justify-center">
              <div className="w-1 h-1 rounded-full bg-[#2A9D8F]" />
            </div>
            <span>Context Rings</span>
          </button>

          {/* Android Code & APK Export Button */}
          <button
            onClick={() => setShowAndroidCodeModal(true)}
            style={{ backgroundColor: settings.accentColor }}
            className="text-xs font-semibold px-3.5 py-1.5 rounded-xl text-white flex items-center gap-2 shadow-md hover:scale-102 active:scale-98 transition"
          >
            <Code2 size={15} />
            <span className="hidden sm:inline">Android Studio Source (.zip)</span>
            <span className="sm:hidden">Android Code</span>
          </button>
        </div>
      </header>

      {/* Gesture / System Event Toast */}
      {gestureToast && (
        <div className="fixed top-18 left-1/2 -translate-x-1/2 z-50 bg-[#1E2126] text-white border border-white/15 px-4 py-2 rounded-full text-xs font-mono shadow-2xl flex items-center gap-2 animate-in fade-in slide-in-from-top-2">
          <span style={{ backgroundColor: settings.accentColor }} className="w-2 h-2 rounded-full" />
          {gestureToast}
        </div>
      )}

      {/* Main Workspace */}
      <main className="flex-1 flex flex-col items-center justify-center p-4 sm:p-6 overflow-x-auto relative">
        {viewMode === 'phone' ? (
          /* Single Interactive Phone Simulation */
          <div className="flex flex-col items-center gap-3">
            <PhoneFrame
              settings={settings}
              activeScreenIndex={activeScreenIndex}
              onNavigateScreen={(idx) => setActiveScreenIndex(idx)}
              onTriggerGesture={handleTriggerGesture}
            >
              {/* Active Screen View */}
              {activeScreenIndex === 0 && (
                <AppListScreen
                  settings={settings}
                  apps={visibleApps}
                  searchQuery={searchQuery}
                  onSearchChange={(q) => setSearchQuery(q)}
                  selectedCategory={selectedCategory}
                  onSelectCategory={(cat) => setSelectedCategory(cat)}
                  onLaunchApp={handleLaunchApp}
                  onAppLongPress={(app) => setSelectedAppForAction(app)}
                  onBackToHome={() => setActiveScreenIndex(1)}
                  onOpenContextRing={handleTriggerContextRing}
                />
              )}

              {activeScreenIndex === 1 && (
                <HomeScreen
                  settings={settings}
                  favorites={smartFavorites}
                  agenda={agenda}
                  onToggleAgenda={handleToggleAgenda}
                  onAddAgenda={handleAddAgenda}
                  onLaunchApp={handleLaunchApp}
                  onAppLongPress={(app) => setSelectedAppForAction(app)}
                  onOpenSearch={() => setActiveScreenIndex(0)}
                  onLetterJump={(letter) => {
                    setActiveScreenIndex(0);
                    setTimeout(() => {
                      const el = document.getElementById(`section-letter-${letter}`);
                      el?.scrollIntoView({ behavior: 'smooth' });
                    }, 100);
                  }}
                  onOpenSocialFolder={() => setShowSocialFolder(true)}
                  onOpenContextRing={handleTriggerContextRing}
                />
              )}

              {activeScreenIndex === 2 && (
                <WidgetsScreen
                  settings={settings}
                  notes={notes}
                  onToggleNote={handleToggleNote}
                  onAddNote={handleAddNote}
                  onOpenSettings={() => setActiveScreenIndex(3)}
                />
              )}

              {activeScreenIndex === 3 && (
                <SettingsScreen
                  settings={settings}
                  onUpdateSettings={(updater) => setSettings(updater)}
                  onBack={() => setActiveScreenIndex(1)}
                  onOpenHiddenAppsPin={() => setShowPinModal(true)}
                  onOpenAndroidCodeViewer={() => setShowAndroidCodeModal(true)}
                  onOpenContextRingDesigner={() => setShowContextRingDesigner(true)}
                />
              )}

              {/* Context Ring Radial Action Overlay */}
              {activeRingSession && (
                <ContextRingOverlay
                  session={activeRingSession}
                  config={contextRingConfig}
                  onSelectAction={handleSelectRingAction}
                  onNavigateBack={handlePopNestedRing}
                  onDismiss={() => setActiveRingSession(null)}
                  onPushNestedRing={handlePushNestedRing}
                />
              )}

              {/* Modals & Overlays inside Phone */}
              {showPinModal && (
                <PinLockModal
                  correctPin={settings.hiddenAppsPin}
                  onSuccess={() => {
                    setShowPinModal(false);
                    setShowHiddenUnlocked(true);
                    setActiveScreenIndex(0);
                    setSelectedCategory('All');
                    triggerToast('Hidden vault unlocked!');
                  }}
                  onCancel={() => setShowPinModal(false)}
                  theme={settings.theme}
                />
              )}

              {showSocialFolder && (
                <SocialFolderModal
                  settings={settings}
                  apps={socialApps}
                  onClose={() => setShowSocialFolder(false)}
                  onLaunchApp={handleLaunchApp}
                />
              )}

              {selectedAppForAction && (
                <AppActionModal
                  app={selectedAppForAction}
                  settings={settings}
                  onClose={() => setSelectedAppForAction(null)}
                  onToggleFavorite={handleToggleFavorite}
                  onToggleHide={handleToggleHide}
                  onUninstall={handleUninstall}
                />
              )}

              {isScreenLocked && (
                <ScreenLockOverlay onUnlock={() => setIsScreenLocked(false)} />
              )}
            </PhoneFrame>

            {/* Hint underneath phone */}
            <div className="flex items-center gap-4 text-xs text-neutral-400 text-center">
              <span>👆 Swipe Left / Right to change pages</span>
              <span>•</span>
              <span>⦿ Double-click to simulate Screen Lock</span>
              <span>•</span>
              <span>Right-click app for Context Menu</span>
            </div>
          </div>
        ) : (
          /* 5-Screen Side-by-Side Showcase Mode (Matching the reference PNG!) */
          <div className="w-full max-w-7xl overflow-x-auto pb-8">
            <div className="text-center mb-6">
              <h2 className="text-xl font-bold text-white">NovaForge Launcher Showcase Overview</h2>
              <p className="text-xs text-neutral-400">
                1:1 fidelity matching the reference architectural diagram: Home, App List, Widgets, Settings, and Gesture Controls
              </p>
            </div>

            <div className="flex gap-6 justify-center min-w-[1500px] px-4">
              {/* Screen 1: Home */}
              <div className="flex flex-col items-center">
                <span className="text-xs font-bold text-neutral-300 mb-2">1. Home Screen</span>
                <PhoneFrame
                  settings={settings}
                  activeScreenIndex={1}
                  onNavigateScreen={() => {}}
                  onTriggerGesture={handleTriggerGesture}
                >
                  <HomeScreen
                    settings={settings}
                    favorites={smartFavorites}
                    agenda={agenda}
                    onToggleAgenda={handleToggleAgenda}
                    onAddAgenda={handleAddAgenda}
                    onLaunchApp={handleLaunchApp}
                    onAppLongPress={(app) => setSelectedAppForAction(app)}
                    onOpenSearch={() => setActiveScreenIndex(0)}
                    onLetterJump={() => {}}
                    onOpenSocialFolder={() => setShowSocialFolder(true)}
                  />
                </PhoneFrame>
              </div>

              {/* Screen 2: App List */}
              <div className="flex flex-col items-center">
                <span className="text-xs font-bold text-neutral-300 mb-2">2. App List</span>
                <PhoneFrame
                  settings={settings}
                  activeScreenIndex={0}
                  onNavigateScreen={() => {}}
                  onTriggerGesture={handleTriggerGesture}
                >
                  <AppListScreen
                    settings={settings}
                    apps={visibleApps}
                    searchQuery={searchQuery}
                    onSearchChange={(q) => setSearchQuery(q)}
                    selectedCategory={selectedCategory}
                    onSelectCategory={(cat) => setSelectedCategory(cat)}
                    onLaunchApp={handleLaunchApp}
                    onAppLongPress={(app) => setSelectedAppForAction(app)}
                    onBackToHome={() => {}}
                    onOpenContextRing={handleTriggerContextRing}
                  />
                </PhoneFrame>
              </div>

              {/* Screen 3: Widgets Page */}
              <div className="flex flex-col items-center">
                <span className="text-xs font-bold text-neutral-300 mb-2">3. Widgets Page</span>
                <PhoneFrame
                  settings={settings}
                  activeScreenIndex={2}
                  onNavigateScreen={() => {}}
                  onTriggerGesture={handleTriggerGesture}
                >
                  <WidgetsScreen
                    settings={settings}
                    notes={notes}
                    onToggleNote={handleToggleNote}
                    onAddNote={handleAddNote}
                    onOpenSettings={() => {}}
                  />
                </PhoneFrame>
              </div>

              {/* Screen 4: Settings */}
              <div className="flex flex-col items-center">
                <span className="text-xs font-bold text-neutral-300 mb-2">4. Customization / Settings</span>
                <PhoneFrame
                  settings={settings}
                  activeScreenIndex={3}
                  onNavigateScreen={() => {}}
                  onTriggerGesture={handleTriggerGesture}
                >
                  <SettingsScreen
                    settings={settings}
                    onUpdateSettings={(updater) => setSettings(updater)}
                    onBack={() => {}}
                    onOpenHiddenAppsPin={() => setShowPinModal(true)}
                    onOpenAndroidCodeViewer={() => setShowAndroidCodeModal(true)}
                  />
                </PhoneFrame>
              </div>
            </div>
          </div>
        )}
      </main>

      {/* Modal for Android Studio Source Code & APK Guide */}
      {showAndroidCodeModal && (
        <AndroidCodeModal
          onClose={() => setShowAndroidCodeModal(false)}
          accentColor={settings.accentColor}
        />
      )}

      {/* Context Rings Designer Modal */}
      {showContextRingDesigner && (
        <ContextRingDesignerModal
          config={contextRingConfig}
          allApps={apps}
          onSaveConfig={(updated) => {
            setContextRingConfig(updated);
            triggerToast('Context Rings configuration saved');
          }}
          onClose={() => setShowContextRingDesigner(false)}
        />
      )}
    </div>
  );
}
