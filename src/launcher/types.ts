export interface AppItem {
  id: string;
  name: string;
  packageName: string;
  iconName: string;
  color: string;
  category: 'Social' | 'Productivity' | 'Tools' | 'Media' | 'Games';
  isFavorite: boolean;
  isHidden: boolean;
  launchCount: number;
  lastUsedTime: number; // timestamp
}

export type IconShape = 'circle' | 'squircle' | 'rounded-square' | 'square' | 'pebble';
export type IconStyle = 'default' | 'monochrome' | 'outline' | 'custom';
export type LauncherTheme = 'warm-light' | 'charcoal-dark' | 'monochrome';

export interface AgendaItem {
  id: string;
  title: string;
  time: string;
  completed: boolean;
}

export interface NoteItem {
  id: string;
  text: string;
  completed: boolean;
}

export type GestureType = 'swipeUp' | 'swipeDown' | 'swipeLeft' | 'swipeRight' | 'doubleTap' | 'longPress';

export type GestureAction =
  | 'openAppList'
  | 'openWidgets'
  | 'openSearch'
  | 'openSettings'
  | 'lockScreen'
  | 'editHomeScreen'
  | 'previousPage'
  | 'nextPage';

export interface GestureConfig {
  swipeUp: GestureAction;
  swipeDown: GestureAction;
  swipeLeft: GestureAction;
  swipeRight: GestureAction;
  doubleTap: GestureAction;
  longPress: GestureAction;
}

export interface LauncherSettings {
  theme: LauncherTheme;
  accentColor: string;
  iconShape: IconShape;
  iconStyle: IconStyle;
  gestures: GestureConfig;
  focusModeEnabled: boolean;
  focusAllowedAppIds: string[];
  focusSchedule: string;
  hiddenAppsPin: string;
  wallpaperIndex: number;
  clockStyle?: 'digital' | 'minimal' | 'bold' | 'serif' | 'monospace' | 'cosmic';
  clockSize?: 'compact' | 'standard' | 'large';
  showBattery?: boolean;
  showWeather?: boolean;
  showCalendarEvent?: boolean;
  showMusicHome?: boolean;
  showMusicDrawer?: boolean;
  musicExpanded?: boolean;
  showAlphabetRail?: boolean;
  wallpaperDimOverlay?: number;
  homePageCount?: number;
  hapticsEnabled?: boolean;
}

export type RingActionType =
  | 'LAUNCH_APP'
  | 'APP_SHORTCUT'
  | 'SWITCH_WORKSPACE'
  | 'OPEN_APP_DRAWER'
  | 'OPEN_SETTINGS'
  | 'OPEN_WIDGETS'
  | 'OPEN_COMMAND_BAR'
  | 'TOGGLE_FOCUS_MODE'
  | 'OPEN_CONSTELLATIONS'
  | 'SYSTEM_SETTING'
  | 'EXECUTE_MACRO'
  | 'MEDIA_CONTROL'
  | 'NESTED_RING';

export interface ContextRingAction {
  id: string;
  title: string;
  iconName: string;
  actionType: RingActionType;
  payload?: string;
  secondaryLabel?: string;
  accentHex?: string;
  requiresConfirmation?: boolean;
}

export interface ContextRingDefinition {
  id: string;
  title: string;
  description?: string;
  slotCount: number;
  radiusDp: number;
  iconSizeDp: number;
  actions: ContextRingAction[];
  isSystemPreset?: boolean;
  parentRingId?: string | null;
}

export interface ContextRingConfig {
  defaultGlobalRingId: string;
  rings: ContextRingDefinition[];
  appAssignments: Record<string, string>;
  workspaceAssignments: Record<string, string>;
  folderAssignments: Record<string, string>;
  hapticIntensity: 'off' | 'light' | 'medium' | 'strong';
  gestureTrigger: 'long_press' | 'double_tap' | 'swipe_up' | 'swipe_down';
  showLocalSuggestions: boolean;
}

export interface ActiveRingSession {
  currentRing: ContextRingDefinition;
  originX: number;
  originY: number;
  clampedCenterX: number;
  clampedCenterY: number;
  targetPackage?: string;
  targetWorkspaceId?: string;
  targetFolderId?: string;
  breadcrumbRingIds: string[];
  selectedIndex: number;
  highlightedAction?: ContextRingAction | null;
}
