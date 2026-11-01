import { LauncherTheme } from './types';

export interface SemanticThemeColors {
  isDark: boolean;
  // Backgrounds
  bgPrimary: string;
  bgSecondary: string;
  bgSurface: string;
  bgCard: string;
  bgCardHover: string;
  bgInput: string;
  bgOverlay: string;
  
  // Text
  textPrimary: string;
  textSecondary: string;
  textMuted: string;
  textInverse: string;
  textOnAccent: string;
  
  // Borders & Dividers
  borderPrimary: string;
  borderSubtle: string;
  borderDivider: string;
  borderInput: string;

  // Icons
  iconPrimary: string;
  iconSecondary: string;
  iconMuted: string;

  // Components & Tabs
  tabSelectedBg: string;
  tabSelectedText: string;
  tabUnselectedBg: string;
  tabUnselectedText: string;
  
  // Command Pill
  commandPillBg: string;
  commandPillBorder: string;
  commandPillText: string;
  commandPillPlaceholder: string;

  // Buttons
  buttonPrimaryBg: string;
  buttonPrimaryText: string;
  buttonSecondaryBg: string;
  buttonSecondaryText: string;

  // Rail & Indicators
  railActiveText: string;
  railInactiveText: string;
  indicatorActive: string;
  indicatorInactive: string;

  // Wallpaper Scrim
  scrimColor: string;

  // Status
  statusSuccess: string;
  statusWarning: string;
  statusError: string;
  statusInfo: string;
}

export function resolveIsDark(theme: LauncherTheme): boolean {
  if (theme === 'dark' || theme === 'charcoal-dark' || theme === 'monochrome') {
    return true;
  }
  if (theme === 'light' || theme === 'warm-light') {
    return false;
  }
  // 'system' or undefined: check system preference
  if (typeof window !== 'undefined' && window.matchMedia) {
    return window.matchMedia('(prefers-color-scheme: dark)').matches;
  }
  return true; // Default fallback to dark
}

export function getSemanticColors(theme: LauncherTheme, accentColor = '#38BDF8'): SemanticThemeColors {
  const isDark = resolveIsDark(theme);

  if (isDark) {
    return {
      isDark: true,
      bgPrimary: '#07090E',
      bgSecondary: '#0D1117',
      bgSurface: '#151B26',
      bgCard: 'rgba(13, 17, 23, 0.72)',
      bgCardHover: 'rgba(255, 255, 255, 0.08)',
      bgInput: 'rgba(255, 255, 255, 0.08)',
      bgOverlay: 'rgba(0, 0, 0, 0.65)',

      textPrimary: '#F8FAFC',
      textSecondary: '#94A3B8',
      textMuted: '#64748B',
      textInverse: '#0F172A',
      textOnAccent: '#FFFFFF',

      borderPrimary: 'rgba(255, 255, 255, 0.15)',
      borderSubtle: 'rgba(255, 255, 255, 0.08)',
      borderDivider: 'rgba(255, 255, 255, 0.10)',
      borderInput: 'rgba(255, 255, 255, 0.15)',

      iconPrimary: '#F1F5F9',
      iconSecondary: '#94A3B8',
      iconMuted: '#64748B',

      tabSelectedBg: accentColor,
      tabSelectedText: '#FFFFFF',
      tabUnselectedBg: 'rgba(255, 255, 255, 0.08)',
      tabUnselectedText: '#94A3B8',

      commandPillBg: 'rgba(255, 255, 255, 0.10)',
      commandPillBorder: 'rgba(255, 255, 255, 0.14)',
      commandPillText: '#F1F5F9',
      commandPillPlaceholder: '#94A3B8',

      buttonPrimaryBg: accentColor,
      buttonPrimaryText: '#FFFFFF',
      buttonSecondaryBg: 'rgba(255, 255, 255, 0.10)',
      buttonSecondaryText: '#F1F5F9',

      railActiveText: accentColor,
      railInactiveText: '#64748B',
      indicatorActive: accentColor,
      indicatorInactive: 'rgba(255, 255, 255, 0.25)',

      scrimColor: 'rgba(0, 0, 0, 0.40)',

      statusSuccess: '#10B981',
      statusWarning: '#F59E0B',
      statusError: '#EF4444',
      statusInfo: '#38BDF8'
    };
  }

  // Light Mode
  return {
    isDark: false,
    bgPrimary: '#F8F9FA',
    bgSecondary: '#F1F3F5',
    bgSurface: '#FFFFFF',
    bgCard: 'rgba(255, 255, 255, 0.88)',
    bgCardHover: 'rgba(0, 0, 0, 0.04)',
    bgInput: 'rgba(0, 0, 0, 0.05)',
    bgOverlay: 'rgba(0, 0, 0, 0.35)',

    textPrimary: '#1E293B',
    textSecondary: '#475569',
    textMuted: '#64748B',
    textInverse: '#F8FAFC',
    textOnAccent: '#FFFFFF',

    borderPrimary: 'rgba(0, 0, 0, 0.14)',
    borderSubtle: 'rgba(0, 0, 0, 0.07)',
    borderDivider: 'rgba(0, 0, 0, 0.08)',
    borderInput: 'rgba(0, 0, 0, 0.12)',

    iconPrimary: '#1E293B',
    iconSecondary: '#475569',
    iconMuted: '#94A3B8',

    tabSelectedBg: accentColor,
    tabSelectedText: '#FFFFFF',
    tabUnselectedBg: 'rgba(0, 0, 0, 0.05)',
    tabUnselectedText: '#475569',

    commandPillBg: 'rgba(255, 255, 255, 0.90)',
    commandPillBorder: 'rgba(0, 0, 0, 0.10)',
    commandPillText: '#1E293B',
    commandPillPlaceholder: '#64748B',

    buttonPrimaryBg: accentColor,
    buttonPrimaryText: '#FFFFFF',
    buttonSecondaryBg: 'rgba(0, 0, 0, 0.06)',
    buttonSecondaryText: '#1E293B',

    railActiveText: accentColor,
    railInactiveText: '#94A3B8',
    indicatorActive: accentColor,
    indicatorInactive: 'rgba(0, 0, 0, 0.20)',

    scrimColor: 'rgba(0, 0, 0, 0.20)',

    statusSuccess: '#059669',
    statusWarning: '#D97706',
    statusError: '#DC2626',
    statusInfo: '#0284C7'
  };
}
