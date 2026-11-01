import { AgendaItem, AppItem, NoteItem, LauncherSettings } from './types';

export const INITIAL_APPS: AppItem[] = [
  // A
  {
    id: 'app-amazon',
    name: 'Amazon',
    packageName: 'in.amazon.mShop.android.shopping',
    iconName: 'ShoppingBag',
    color: '#FF9900',
    category: 'Productivity',
    isFavorite: false,
    isHidden: false,
    launchCount: 14,
    lastUsedTime: Date.now() - 3600000 * 3
  },
  {
    id: 'app-store',
    name: 'App Store',
    packageName: 'com.android.vending',
    iconName: 'Store',
    color: '#0086F8',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 22,
    lastUsedTime: Date.now() - 3600000 * 5
  },
  {
    id: 'app-authenticator',
    name: 'Authenticator',
    packageName: 'com.google.android.apps.authenticator2',
    iconName: 'ShieldCheck',
    color: '#4285F4',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 31,
    lastUsedTime: Date.now() - 3600000 * 2
  },
  // B
  {
    id: 'app-bg-eraser',
    name: 'Background Eraser',
    packageName: 'com.handycloset.android.eraser',
    iconName: 'Sparkles',
    color: '#E91E63',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 6,
    lastUsedTime: Date.now() - 3600000 * 24
  },
  {
    id: 'app-bgmi',
    name: 'Battlegrounds India',
    packageName: 'com.pubg.imobile',
    iconName: 'Gamepad2',
    color: '#D97706',
    category: 'Games',
    isFavorite: false,
    isHidden: false,
    launchCount: 45,
    lastUsedTime: Date.now() - 3600000 * 8
  },
  {
    id: 'app-brave',
    name: 'Brave',
    packageName: 'com.brave.browser',
    iconName: 'Globe',
    color: '#FB542B',
    category: 'Productivity',
    isFavorite: false,
    isHidden: false,
    launchCount: 58,
    lastUsedTime: Date.now() - 3600000 * 1
  },
  // C
  {
    id: 'app-calculator',
    name: 'Calculator',
    packageName: 'com.google.android.calculator',
    iconName: 'Calculator',
    color: '#00838F',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 19,
    lastUsedTime: Date.now() - 3600000 * 12
  },
  {
    id: 'app-calendar',
    name: 'Calendar',
    packageName: 'com.google.android.calendar',
    iconName: 'Calendar',
    color: '#1A73E8',
    category: 'Productivity',
    isFavorite: false,
    isHidden: false,
    launchCount: 42,
    lastUsedTime: Date.now() - 3600000 * 4
  },
  {
    id: 'app-camera',
    name: 'Camera',
    packageName: 'com.google.android.GoogleCamera',
    iconName: 'Camera',
    color: '#34A853',
    category: 'Media',
    isFavorite: true,
    isHidden: false,
    launchCount: 77,
    lastUsedTime: Date.now() - 3600000 * 1
  },
  {
    id: 'app-chatgpt',
    name: 'ChatGPT',
    packageName: 'com.openai.chatgpt',
    iconName: 'Bot',
    color: '#10A37F',
    category: 'Productivity',
    isFavorite: false,
    isHidden: false,
    launchCount: 88,
    lastUsedTime: Date.now() - 3600000 * 2
  },
  {
    id: 'app-chrome',
    name: 'Chrome',
    packageName: 'com.android.chrome',
    iconName: 'Compass',
    color: '#EA4335',
    category: 'Productivity',
    isFavorite: true,
    isHidden: false,
    launchCount: 195,
    lastUsedTime: Date.now() - 1000 * 60 * 10
  },
  {
    id: 'app-clock',
    name: 'Clock',
    packageName: 'com.google.android.deskclock',
    iconName: 'Clock',
    color: '#3367D6',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 38,
    lastUsedTime: Date.now() - 3600000 * 6
  },
  {
    id: 'app-compass',
    name: 'Compass',
    packageName: 'com.google.android.apps.compass',
    iconName: 'Navigation',
    color: '#E65100',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 4,
    lastUsedTime: Date.now() - 3600000 * 72
  },
  {
    id: 'app-contacts',
    name: 'Contacts',
    packageName: 'com.google.android.contacts',
    iconName: 'Users',
    color: '#185ABC',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 26,
    lastUsedTime: Date.now() - 3600000 * 18
  },
  // D
  {
    id: 'app-discord',
    name: 'Discord',
    packageName: 'com.discord',
    iconName: 'MessagesSquare',
    color: '#5865F2',
    category: 'Social',
    isFavorite: false,
    isHidden: false,
    launchCount: 64,
    lastUsedTime: Date.now() - 3600000 * 5
  },
  {
    id: 'app-drive',
    name: 'Drive',
    packageName: 'com.google.android.apps.docs',
    iconName: 'HardDrive',
    color: '#0F9D58',
    category: 'Productivity',
    isFavorite: false,
    isHidden: false,
    launchCount: 33,
    lastUsedTime: Date.now() - 3600000 * 7
  },
  // N, R, S, W, Y
  {
    id: 'app-notes',
    name: 'Notes',
    packageName: 'com.google.android.keep',
    iconName: 'FileText',
    color: '#F4B400',
    category: 'Productivity',
    isFavorite: true,
    isHidden: false,
    launchCount: 65,
    lastUsedTime: Date.now() - 1000 * 60 * 30
  },
  {
    id: 'app-reddit',
    name: 'Reddit',
    packageName: 'com.reddit.frontpage',
    iconName: 'Flame',
    color: '#FF4500',
    category: 'Social',
    isFavorite: true,
    isHidden: false,
    launchCount: 110,
    lastUsedTime: Date.now() - 3600000 * 2
  },
  {
    id: 'app-settings',
    name: 'Settings',
    packageName: 'com.android.settings',
    iconName: 'Settings',
    color: '#5F6368',
    category: 'Tools',
    isFavorite: true,
    isHidden: false,
    launchCount: 48,
    lastUsedTime: Date.now() - 3600000 * 1
  },
  {
    id: 'app-spotify',
    name: 'Spotify',
    packageName: 'com.spotify.music',
    iconName: 'Headphones',
    color: '#1DB954',
    category: 'Media',
    isFavorite: true,
    isHidden: false,
    launchCount: 180,
    lastUsedTime: Date.now() - 1000 * 60 * 15
  },
  {
    id: 'app-whatsapp',
    name: 'WhatsApp',
    packageName: 'com.whatsapp',
    iconName: 'MessageSquare',
    color: '#25D366',
    category: 'Social',
    isFavorite: true,
    isHidden: false,
    launchCount: 240,
    lastUsedTime: Date.now() - 1000 * 60 * 5
  },
  {
    id: 'app-youtube',
    name: 'YouTube',
    packageName: 'com.google.android.youtube',
    iconName: 'PlaySquare',
    color: '#FF0000',
    category: 'Media',
    isFavorite: true,
    isHidden: false,
    launchCount: 210,
    lastUsedTime: Date.now() - 1000 * 60 * 8
  },
  {
    id: 'app-instagram',
    name: 'Instagram',
    packageName: 'com.instagram.android',
    iconName: 'Camera',
    color: '#E1306C',
    category: 'Social',
    isFavorite: false,
    isHidden: false,
    launchCount: 95,
    lastUsedTime: Date.now() - 3600000 * 3
  },
  {
    id: 'app-telegram',
    name: 'Telegram',
    packageName: 'org.telegram.messenger',
    iconName: 'Send',
    color: '#229ED9',
    category: 'Social',
    isFavorite: false,
    isHidden: false,
    launchCount: 75,
    lastUsedTime: Date.now() - 3600000 * 4
  },
  {
    id: 'app-x',
    name: 'X',
    packageName: 'com.twitter.android',
    iconName: 'Twitter',
    color: '#111827',
    category: 'Social',
    isFavorite: false,
    isHidden: false,
    launchCount: 82,
    lastUsedTime: Date.now() - 3600000 * 2
  },
  {
    id: 'app-finance-vault',
    name: 'Finance & Bank Vault',
    packageName: 'com.secure.banking.vault',
    iconName: 'Lock',
    color: '#1E3A8A',
    category: 'Tools',
    isFavorite: false,
    isHidden: true,
    launchCount: 15,
    lastUsedTime: Date.now() - 3600000 * 10
  }
];

export const INITIAL_AGENDA: AgendaItem[] = [
  { id: '1', title: 'Class 12 Chemistry', time: '10:00 AM', completed: false },
  { id: '2', title: 'Gym', time: '5:00 PM', completed: false },
  { id: '3', title: 'Read Book', time: '9:00 PM', completed: false }
];

export const INITIAL_NOTES: NoteItem[] = [
  { id: 'n1', text: 'Maths practice', completed: false },
  { id: 'n2', text: 'JanConnect work', completed: false },
  { id: 'n3', text: 'Edit video', completed: false }
];

export const INITIAL_SETTINGS: LauncherSettings = {
  theme: 'system',
  accentColor: '#38BDF8',
  iconShape: 'circle',
  iconStyle: 'default',
  gestures: {
    swipeUp: 'openAppList',
    swipeDown: 'openWidgets',
    swipeLeft: 'previousPage',
    swipeRight: 'nextPage',
    doubleTap: 'lockScreen',
    longPress: 'editHomeScreen'
  },
  focusModeEnabled: false,
  focusAllowedAppIds: ['app-chrome', 'app-notes', 'app-calendar', 'app-calculator'],
  focusSchedule: '9:00 AM – 12:00 PM',
  hiddenAppsPin: '1234',
  wallpaperIndex: 0,
  wallpaperDimOverlay: 0
};

export const WALLPAPERS = [
  {
    id: 'mountain-mist',
    name: 'Mountain Mist (Grace / Minimalist)',
    url: 'https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?auto=format&fit=crop&w=800&q=80',
    isReference: true
  },
  {
    id: 'warm-sand',
    name: 'Warm Minimalist Dune',
    url: 'https://images.unsplash.com/photo-1507525428034-b723cf961d3e?auto=format&fit=crop&w=800&q=80',
    isReference: false
  },
  {
    id: 'dark-nordic',
    name: 'Nordic Charcoal Pine',
    url: 'https://images.unsplash.com/photo-1511497584788-87676104235f?auto=format&fit=crop&w=800&q=80',
    isReference: false
  }
];

export const SOCIAL_STACK = [
  'app-whatsapp',
  'app-instagram',
  'app-telegram',
  'app-discord',
  'app-x',
  'app-reddit'
];

export const INITIAL_CONTEXT_RINGS: import('./types').ContextRingConfig = {
  defaultGlobalRingId: 'global_default',
  hapticIntensity: 'medium',
  gestureTrigger: 'long_press',
  showLocalSuggestions: true,
  appAssignments: {
    'com.brave.browser': 'browser_ring',
    'com.android.chrome': 'browser_ring',
    'com.spotify.music': 'music_ring'
  },
  workspaceAssignments: {
    'study': 'study_ring',
    'gaming': 'gaming_ring'
  },
  folderAssignments: {},
  rings: [
    {
      id: 'global_default',
      title: 'Global Command Ring',
      description: 'Launcher quick actions and navigation hub',
      slotCount: 6,
      radiusDp: 140,
      iconSizeDp: 44,
      isSystemPreset: true,
      actions: [
        { id: 'act_1', title: 'App Drawer', iconName: 'Grid', actionType: 'OPEN_APP_DRAWER', accentHex: '#2A9D8F' },
        { id: 'act_2', title: 'Command Bar', iconName: 'Terminal', actionType: 'OPEN_COMMAND_BAR', accentHex: '#457B9D' },
        { id: 'act_3', title: 'Focus Mode', iconName: 'Shield', actionType: 'TOGGLE_FOCUS_MODE', accentHex: '#E76F51' },
        { id: 'act_4', title: 'Widgets', iconName: 'Layers', actionType: 'OPEN_WIDGETS', accentHex: '#E9C46A' },
        { id: 'act_5', title: 'Constellations', iconName: 'Compass', actionType: 'OPEN_CONSTELLATIONS', accentHex: '#9B5DE5' },
        { id: 'act_6', title: 'Settings', iconName: 'Settings', actionType: 'OPEN_SETTINGS', accentHex: '#6C757D' }
      ]
    },
    {
      id: 'browser_ring',
      title: 'Browser Context Ring',
      description: 'Web browsing shortcuts and actions',
      slotCount: 6,
      radiusDp: 140,
      iconSizeDp: 44,
      isSystemPreset: true,
      actions: [
        { id: 'act_b1', title: 'Open Browser', iconName: 'Globe', actionType: 'LAUNCH_APP', payload: 'com.brave.browser', accentHex: '#FB542B' },
        { id: 'act_b2', title: 'Search Web', iconName: 'Search', actionType: 'OPEN_COMMAND_BAR', accentHex: '#2A9D8F' },
        { id: 'act_b3', title: 'Wi-Fi Config', iconName: 'Wifi', actionType: 'SYSTEM_SETTING', payload: 'wifi', accentHex: '#457B9D' },
        { id: 'act_b4', title: 'Focus Mode', iconName: 'Shield', actionType: 'TOGGLE_FOCUS_MODE', accentHex: '#E76F51' },
        { id: 'act_b5', title: 'Display Mode', iconName: 'Sun', actionType: 'SYSTEM_SETTING', payload: 'display', accentHex: '#E9C46A' },
        { id: 'act_b6', title: 'Settings', iconName: 'Settings', actionType: 'OPEN_SETTINGS', accentHex: '#6C757D' }
      ]
    },
    {
      id: 'music_ring',
      title: 'Music Context Ring',
      description: 'Direct playback controls and audio utilities',
      slotCount: 6,
      radiusDp: 140,
      iconSizeDp: 44,
      isSystemPreset: true,
      actions: [
        { id: 'act_m1', title: 'Play / Pause', iconName: 'Play', actionType: 'MEDIA_CONTROL', payload: 'play_pause', accentHex: '#1DB954' },
        { id: 'act_m2', title: 'Next Track', iconName: 'SkipForward', actionType: 'MEDIA_CONTROL', payload: 'next', accentHex: '#1DB954' },
        { id: 'act_m3', title: 'Previous Track', iconName: 'SkipBack', actionType: 'MEDIA_CONTROL', payload: 'prev', accentHex: '#1DB954' },
        { id: 'act_m4', title: 'Open Spotify', iconName: 'Music', actionType: 'LAUNCH_APP', payload: 'com.spotify.music', accentHex: '#1DB954' },
        { id: 'act_m5', title: 'Bluetooth', iconName: 'Bluetooth', actionType: 'SYSTEM_SETTING', payload: 'bluetooth', accentHex: '#457B9D' },
        { id: 'act_m6', title: 'Focus Mode', iconName: 'Shield', actionType: 'TOGGLE_FOCUS_MODE', accentHex: '#E76F51' }
      ]
    },
    {
      id: 'study_tools_nested',
      title: 'Study Tools',
      description: 'Sub-ring for calculator and study helpers',
      slotCount: 4,
      radiusDp: 130,
      iconSizeDp: 44,
      isSystemPreset: true,
      parentRingId: 'study_ring',
      actions: [
        { id: 'act_st1', title: 'Calculator', iconName: 'Hash', actionType: 'LAUNCH_APP', payload: 'com.google.android.calculator', accentHex: '#E9C46A' },
        { id: 'act_st2', title: 'Notes', iconName: 'Edit3', actionType: 'LAUNCH_APP', payload: 'com.google.android.keep', accentHex: '#F4A261' },
        { id: 'act_st3', title: 'Focus Mode', iconName: 'Shield', actionType: 'TOGGLE_FOCUS_MODE', accentHex: '#E76F51' },
        { id: 'act_st4', title: 'Back to Study', iconName: 'ArrowLeft', actionType: 'NESTED_RING', payload: 'study_ring', accentHex: '#2A9D8F' }
      ]
    },
    {
      id: 'study_ring',
      title: 'Study Workspace Ring',
      description: 'Dedicated tools for productive sessions',
      slotCount: 6,
      radiusDp: 145,
      iconSizeDp: 44,
      isSystemPreset: true,
      actions: [
        { id: 'act_s1', title: 'Study Tools', iconName: 'Folder', actionType: 'NESTED_RING', payload: 'study_tools_nested', accentHex: '#2A9D8F' },
        { id: 'act_s2', title: 'Start Study Macro', iconName: 'PlayCircle', actionType: 'EXECUTE_MACRO', payload: 'macro-study', requiresConfirmation: true, accentHex: '#F4A261' },
        { id: 'act_s3', title: 'Constellations', iconName: 'Compass', actionType: 'OPEN_CONSTELLATIONS', accentHex: '#9B5DE5' },
        { id: 'act_s4', title: 'Study Space', iconName: 'Layers', actionType: 'SWITCH_WORKSPACE', payload: 'study', accentHex: '#457B9D' },
        { id: 'act_s5', title: 'Focus Mode', iconName: 'Shield', actionType: 'TOGGLE_FOCUS_MODE', accentHex: '#E76F51' },
        { id: 'act_s6', title: 'Battery Check', iconName: 'Battery', actionType: 'SYSTEM_SETTING', payload: 'battery', accentHex: '#6C757D' }
      ]
    },
    {
      id: 'gaming_ring',
      title: 'Gaming Workspace Ring',
      description: 'Quick gaming shortcuts and display settings',
      slotCount: 6,
      radiusDp: 145,
      iconSizeDp: 44,
      isSystemPreset: true,
      actions: [
        { id: 'act_g1', title: 'Gaming Space', iconName: 'Play', actionType: 'SWITCH_WORKSPACE', payload: 'gaming', accentHex: '#E76F51' },
        { id: 'act_g2', title: 'Gaming Suite', iconName: 'Compass', actionType: 'OPEN_CONSTELLATIONS', accentHex: '#9B5DE5' },
        { id: 'act_g3', title: 'Discord', iconName: 'MessageCircle', actionType: 'LAUNCH_APP', payload: 'com.discord', accentHex: '#5865F2' },
        { id: 'act_g4', title: 'Display Refresh', iconName: 'Sun', actionType: 'SYSTEM_SETTING', payload: 'display', accentHex: '#E9C46A' },
        { id: 'act_g5', title: 'Game Wi-Fi', iconName: 'Wifi', actionType: 'SYSTEM_SETTING', payload: 'wifi', accentHex: '#457B9D' },
        { id: 'act_g6', title: 'Performance', iconName: 'Zap', actionType: 'SYSTEM_SETTING', payload: 'battery', accentHex: '#E76F51' }
      ]
    }
  ]
};
