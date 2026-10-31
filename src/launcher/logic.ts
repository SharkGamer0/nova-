import { AppItem, GestureAction, GestureConfig, GestureType, IconShape, IconStyle, LauncherTheme } from './types';

/**
 * Filter apps based on search query, category, and hidden/focus state.
 */
export function filterApps(
  apps: AppItem[],
  query: string,
  categoryFilter: string,
  focusModeActive: boolean,
  allowedAppIds: string[],
  showHidden: boolean
): AppItem[] {
  const cleanQuery = query.trim().toLowerCase();

  return apps.filter((app) => {
    // Hidden apps filter
    if (app.isHidden && !showHidden) {
      return false;
    }

    // Focus mode filter
    if (focusModeActive && !allowedAppIds.includes(app.id)) {
      return false;
    }

    // Category filter
    if (categoryFilter !== 'All' && categoryFilter !== 'Favorites') {
      if (app.category !== categoryFilter) return false;
    }
    if (categoryFilter === 'Favorites' && !app.isFavorite) {
      return false;
    }

    // Query match
    if (cleanQuery) {
      const matchName = app.name.toLowerCase().includes(cleanQuery);
      const matchCategory = app.category.toLowerCase().includes(cleanQuery);
      const matchPackage = app.packageName.toLowerCase().includes(cleanQuery);
      return matchName || matchCategory || matchPackage;
    }

    return true;
  });
}

/**
 * Group apps alphabetically.
 */
export function groupByAlphabet(apps: AppItem[]): Record<string, AppItem[]> {
  const sorted = [...apps].sort((a, b) => a.name.localeCompare(b.name));
  const groups: Record<string, AppItem[]> = {};

  for (const app of sorted) {
    const firstChar = app.name.charAt(0).toUpperCase();
    const key = /[A-Z]/.test(firstChar) ? firstChar : '#';
    if (!groups[key]) {
      groups[key] = [];
    }
    groups[key].push(app);
  }

  return groups;
}

/**
 * Smart favorites score: weighted by launch count and recency.
 */
export function calculateSmartFavorites(apps: AppItem[], limit: number = 8): AppItem[] {
  const now = Date.now();
  const scored = apps
    .filter((a) => !a.isHidden)
    .map((app) => {
      // Recency in hours (smaller is more recent)
      const hoursAgo = Math.max(1, (now - app.lastUsedTime) / (1000 * 60 * 60));
      const recencyScore = 100 / hoursAgo;
      const usageScore = app.launchCount * 10;
      const manualBoost = app.isFavorite ? 500 : 0;
      const score = recencyScore + usageScore + manualBoost;
      return { app, score };
    });

  scored.sort((a, b) => b.score - a.score);
  return scored.slice(0, limit).map((s) => s.app);
}

/**
 * Resolve CSS classes for an icon shape and style.
 */
export function getIconContainerClasses(
  shape: IconShape,
  style: IconStyle,
  theme: LauncherTheme,
  accentColor: string
): string {
  let shapeClass = 'rounded-2xl';
  switch (shape) {
    case 'circle':
      shapeClass = 'rounded-full';
      break;
    case 'squircle':
      shapeClass = 'rounded-[18px]';
      break;
    case 'rounded-square':
      shapeClass = 'rounded-xl';
      break;
    case 'square':
      shapeClass = 'rounded-md';
      break;
    case 'pebble':
      shapeClass = 'rounded-tl-2xl rounded-br-2xl rounded-tr-lg rounded-bl-lg';
      break;
  }

  let styleClass = '';
  switch (style) {
    case 'monochrome':
      styleClass = theme === 'warm-light' ? 'bg-[#3A3F3B] text-white' : 'bg-[#2A2E33] text-[#E0E2EC]';
      break;
    case 'outline':
      styleClass =
        theme === 'warm-light'
          ? 'bg-transparent border border-[#3A3F3B]/30 text-[#2D312E]'
          : 'bg-transparent border border-white/20 text-[#F1F3F4]';
      break;
    case 'custom':
      styleClass = 'text-white';
      break;
    case 'default':
    default:
      styleClass = 'shadow-xs';
      break;
  }

  return `${shapeClass} ${styleClass}`;
}

/**
 * Resolve gesture target action.
 */
export function matchGesture(gestures: GestureConfig, type: GestureType): GestureAction {
  return gestures[type];
}

/**
 * Check if app is permitted during focus mode.
 */
export function isAppAllowedInFocusMode(appId: string, allowedIds: string[], isFocusActive: boolean): boolean {
  if (!isFocusActive) return true;
  return allowedIds.includes(appId);
}

/**
 * Context Ring 4-level priority resolution:
 * 1. Explicit app assignment
 * 2. Active workspace assignment
 * 3. Folder assignment
 * 4. Default global ring
 */
export function resolveContextRing(
  config: import('./types').ContextRingConfig,
  targetPackage?: string,
  targetWorkspaceId?: string,
  targetFolderId?: string
): import('./types').ContextRingDefinition {
  if (targetPackage && config.appAssignments[targetPackage]) {
    const ring = config.rings.find((r) => r.id === config.appAssignments[targetPackage]);
    if (ring) return ring;
  }
  if (targetWorkspaceId && config.workspaceAssignments[targetWorkspaceId]) {
    const ring = config.rings.find((r) => r.id === config.workspaceAssignments[targetWorkspaceId]);
    if (ring) return ring;
  }
  if (targetFolderId && config.folderAssignments[targetFolderId]) {
    const ring = config.rings.find((r) => r.id === config.folderAssignments[targetFolderId]);
    if (ring) return ring;
  }
  return (
    config.rings.find((r) => r.id === config.defaultGlobalRingId) ||
    config.rings[0] || {
      id: 'fallback',
      title: 'Global Ring',
      slotCount: 6,
      radiusDp: 140,
      iconSizeDp: 44,
      actions: []
    }
  );
}

/**
 * Clamp center so ring nodes remain inside screen bounds with padding.
 */
export function calculateClampedCenter(
  originX: number,
  originY: number,
  screenWidth: number,
  screenHeight: number,
  radius: number,
  padding = 32
): [number, number] {
  const minX = radius + padding;
  const maxX = screenWidth - radius - padding;
  const minY = radius + padding;
  const maxY = screenHeight - radius - padding;

  const clampedX = minX < maxX ? Math.max(minX, Math.min(maxX, originX)) : screenWidth / 2;
  const clampedY = minY < maxY ? Math.max(minY, Math.min(maxY, originY)) : screenHeight / 2;

  return [clampedX, clampedY];
}

/**
 * Evaluate pointer coordinates to distance, angle, and active sector slot.
 */
export function evaluatePointerAngleAndSlot(
  pointerX: number,
  pointerY: number,
  centerX: number,
  centerY: number,
  slotCount: number,
  deadzone = 28
): { selectedIndex: number; distance: number; angleDeg: number } {
  const dx = pointerX - centerX;
  const dy = pointerY - centerY;
  const distance = Math.sqrt(dx * dx + dy * dy);

  if (distance < deadzone || slotCount <= 0) {
    return { selectedIndex: -1, distance, angleDeg: 0 };
  }

  // Angle in degrees from top (12 o'clock) clockwise
  let angleRad = Math.atan2(dy, dx);
  let angleDeg = (angleRad * 180) / Math.PI + 90;
  if (angleDeg < 0) angleDeg += 360;

  const sectorSize = 360 / slotCount;
  const shiftedAngle = (angleDeg + sectorSize / 2) % 360;
  const selectedIndex = Math.min(slotCount - 1, Math.max(0, Math.floor(shiftedAngle / sectorSize)));

  return { selectedIndex, distance, angleDeg };
}

/**
 * Validates nested ring navigation against maximum depth and circular loops.
 */
export function canNavigateNestedRing(
  targetRingId: string,
  breadcrumbs: string[],
  maxDepth = 3
): boolean {
  if (breadcrumbs.length >= maxDepth) return false;
  if (breadcrumbs.includes(targetRingId)) return false;
  return true;
}

