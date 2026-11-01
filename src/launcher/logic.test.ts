import test from 'node:test';
import assert from 'node:assert/strict';
import {
  filterApps,
  groupByAlphabet,
  calculateSmartFavorites,
  isAppAllowedInFocusMode,
  matchGesture,
  resolveContextRing,
  calculateClampedCenter,
  evaluatePointerAngleAndSlot,
  canNavigateNestedRing
} from './logic';
import { AppItem, GestureConfig, ContextRingConfig } from './types';

const sampleApps: AppItem[] = [
  {
    id: '1',
    name: 'Amazon',
    packageName: 'com.amazon.mShop.android.shopping',
    iconName: 'shopping-bag',
    color: '#FF9900',
    category: 'Productivity',
    isFavorite: true,
    isHidden: false,
    launchCount: 15,
    lastUsedTime: Date.now() - 3600000
  },
  {
    id: '2',
    name: 'Brave',
    packageName: 'com.brave.browser',
    iconName: 'globe',
    color: '#FB542B',
    category: 'Tools',
    isFavorite: false,
    isHidden: false,
    launchCount: 40,
    lastUsedTime: Date.now() - 10000
  },
  {
    id: '3',
    name: 'Secret Vault',
    packageName: 'com.secret.vault',
    iconName: 'lock',
    color: '#4A5568',
    category: 'Tools',
    isFavorite: false,
    isHidden: true,
    launchCount: 5,
    lastUsedTime: Date.now() - 5000000
  },
  {
    id: '4',
    name: 'WhatsApp',
    packageName: 'com.whatsapp',
    iconName: 'message-circle',
    color: '#25D366',
    category: 'Social',
    isFavorite: true,
    isHidden: false,
    launchCount: 120,
    lastUsedTime: Date.now() - 500
  }
];

test('filterApps correctly hides hidden apps by default', () => {
  const result = filterApps(sampleApps, '', 'All', false, [], false);
  assert.equal(result.length, 3);
  assert.equal(result.some((a) => a.name === 'Secret Vault'), false);
});

test('filterApps shows hidden apps when showHidden is true', () => {
  const result = filterApps(sampleApps, '', 'All', false, [], true);
  assert.equal(result.length, 4);
  assert.equal(result.some((a) => a.name === 'Secret Vault'), true);
});

test('filterApps filters by text query', () => {
  const result = filterApps(sampleApps, 'brave', 'All', false, [], false);
  assert.equal(result.length, 1);
  assert.equal(result[0].name, 'Brave');
});

test('filterApps filters by category', () => {
  const result = filterApps(sampleApps, '', 'Social', false, [], false);
  assert.equal(result.length, 1);
  assert.equal(result[0].name, 'WhatsApp');
});

test('filterApps restricts during Focus Mode', () => {
  const allowed = ['1', '2'];
  const result = filterApps(sampleApps, '', 'All', true, allowed, false);
  assert.equal(result.length, 2);
  assert.equal(result.some((a) => a.id === '4'), false);
});

test('groupByAlphabet groups by initial letter', () => {
  const groups = groupByAlphabet(sampleApps);
  assert.ok(groups['A']);
  assert.ok(groups['B']);
  assert.ok(groups['W']);
  assert.equal(groups['A'][0].name, 'Amazon');
  assert.equal(groups['B'][0].name, 'Brave');
});

test('calculateSmartFavorites ranks pinned and high-frequency apps', () => {
  const favorites = calculateSmartFavorites(sampleApps, 2);
  assert.equal(favorites.length, 2);
  // WhatsApp is favorite + 120 launches
  assert.equal(favorites[0].name, 'WhatsApp');
});

test('gesture matcher routes to configured action', () => {
  const gestures: GestureConfig = {
    swipeUp: 'openAppList',
    swipeDown: 'openWidgets',
    swipeLeft: 'previousPage',
    swipeRight: 'nextPage',
    doubleTap: 'lockScreen',
    longPress: 'editHomeScreen'
  };

  assert.equal(matchGesture(gestures, 'swipeUp'), 'openAppList');
  assert.equal(matchGesture(gestures, 'doubleTap'), 'lockScreen');
});

test('isAppAllowedInFocusMode allows only whitelisted apps when active', () => {
  assert.equal(isAppAllowedInFocusMode('1', ['1', '2'], true), true);
  assert.equal(isAppAllowedInFocusMode('4', ['1', '2'], true), false);
  assert.equal(isAppAllowedInFocusMode('4', ['1', '2'], false), true);
});

test('Android project contains all critical source files and resources', async () => {
  const { ANDROID_PROJECT_FILES } = await import('./androidProjectFiles');
  
  const filePaths = new Set(ANDROID_PROJECT_FILES.map((f) => f.path));

  // Required classes
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/NovaForgeApp.kt'), 'Missing NovaForgeApp.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/data/AppIconManager.kt'), 'Missing AppIconManager.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/data/AppRepository.kt'), 'Missing AppRepository.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/data/LauncherPreferences.kt'), 'Missing LauncherPreferences.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/data/RealDataProviders.kt'), 'Missing RealDataProviders.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/widget/NovaForgeAppWidgetHost.kt'), 'Missing NovaForgeAppWidgetHost.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/MainActivity.kt'), 'Missing MainActivity.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/applist/AppListScreen.kt'), 'Missing AppListScreen.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/command/CommandScreen.kt'), 'Missing CommandScreen.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/spaces/SpacesScreen.kt'), 'Missing SpacesScreen.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/data/IntelligenceEngine.kt'), 'Missing IntelligenceEngine.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/intelligence/AutomationScreen.kt'), 'Missing AutomationScreen.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/icon/AnimatedIconModels.kt'), 'Missing AnimatedIconModels.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/icon/AnimationRepository.kt'), 'Missing AnimationRepository.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/icon/AnimationPlaybackController.kt'), 'Missing AnimationPlaybackController.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/icon/AnimationAssignmentManager.kt'), 'Missing AnimationAssignmentManager.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/icon/AnimatedIconView.kt'), 'Missing AnimatedIconView.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/icon/AnimatedIconLibraryScreen.kt'), 'Missing AnimatedIconLibraryScreen.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/icon/IconAssignmentDialog.kt'), 'Missing IconAssignmentDialog.kt');

  // Context Rings system
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/contextring/ContextRingModels.kt'), 'Missing ContextRingModels.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/contextring/ContextRingRepository.kt'), 'Missing ContextRingRepository.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/contextring/ContextRingEngine.kt'), 'Missing ContextRingEngine.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/contextring/ContextRingRenderer.kt'), 'Missing ContextRingRenderer.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/contextring/ContextRingActionExecutor.kt'), 'Missing ContextRingActionExecutor.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/contextring/ContextRingOverlay.kt'), 'Missing ContextRingOverlay.kt');
  assert.ok(filePaths.has('app/src/main/java/com/novaforge/launcher/ui/contextring/ContextRingDesignerScreen.kt'), 'Missing ContextRingDesignerScreen.kt');

  // Required resources & themes
  assert.ok(filePaths.has('app/src/main/res/values/themes.xml'), 'Missing themes.xml');
  const themesFile = ANDROID_PROJECT_FILES.find((f) => f.path === 'app/src/main/res/values/themes.xml');
  assert.ok(themesFile?.content.includes('Theme.NovaForge'), 'Theme.NovaForge must be declared in themes.xml');

  assert.ok(filePaths.has('app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml'), 'Missing ic_launcher.xml');
  assert.ok(filePaths.has('app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml'), 'Missing ic_launcher_round.xml');
  assert.ok(filePaths.has('app/proguard-rules.pro'), 'Missing proguard-rules.pro');

  // Wrapper files
  assert.ok(filePaths.has('gradlew'), 'Missing gradlew');
  assert.ok(filePaths.has('gradlew.bat'), 'Missing gradlew.bat');
  assert.ok(filePaths.has('gradle/wrapper/gradle-wrapper.properties'), 'Missing gradle-wrapper.properties');
  assert.ok(filePaths.has('gradle/wrapper/gradle-wrapper.jar'), 'Missing gradle-wrapper.jar');
  const jarFile = ANDROID_PROJECT_FILES.find((f) => f.path === 'gradle/wrapper/gradle-wrapper.jar');
  assert.equal(jarFile?.isBinary, true, 'gradle-wrapper.jar should be flagged as binary');
  assert.ok(jarFile?.binaryBase64 && jarFile.binaryBase64.length > 1000, 'gradle-wrapper.jar must have base64 payload');

  // Dependencies
  const tomlFile = ANDROID_PROJECT_FILES.find((f) => f.path === 'gradle/libs.versions.toml');
  assert.ok(tomlFile?.content.includes('lifecycle-viewmodel-compose'), 'Missing lifecycle-viewmodel-compose dependency in toml');
  const appGradle = ANDROID_PROJECT_FILES.find((f) => f.path === 'app/build.gradle.kts');
  assert.ok(appGradle?.content.includes('lifecycle.viewmodel.compose'), 'Missing lifecycle compose dependency in build.gradle.kts');

  // Policy and architectural documentation
  assert.ok(filePaths.has('POLICY.md'), 'Missing POLICY.md');
  assert.ok(filePaths.has('README.md'), 'Missing README.md');
});

test('resolveContextRing respects strict 4-level priority hierarchy', () => {
  const dummyConfig: ContextRingConfig = {
    defaultGlobalRingId: 'global',
    hapticIntensity: 'medium',
    gestureTrigger: 'long_press',
    showLocalSuggestions: true,
    appAssignments: { 'com.browser': 'browser_ring' },
    workspaceAssignments: { 'study': 'study_ring' },
    folderAssignments: { 'folder1': 'folder_ring' },
    rings: [
      { id: 'global', title: 'Global Ring', slotCount: 6, radiusDp: 140, iconSizeDp: 44, actions: [] },
      { id: 'browser_ring', title: 'Browser Ring', slotCount: 6, radiusDp: 140, iconSizeDp: 44, actions: [] },
      { id: 'study_ring', title: 'Study Ring', slotCount: 6, radiusDp: 140, iconSizeDp: 44, actions: [] },
      { id: 'folder_ring', title: 'Folder Ring', slotCount: 4, radiusDp: 130, iconSizeDp: 44, actions: [] }
    ]
  };

  // Priority 1: Explicit app assignment wins over workspace and folder
  const r1 = resolveContextRing(dummyConfig, 'com.browser', 'study', 'folder1');
  assert.equal(r1.id, 'browser_ring');

  // Priority 2: Workspace assignment wins when app has no binding
  const r2 = resolveContextRing(dummyConfig, 'com.other', 'study', 'folder1');
  assert.equal(r2.id, 'study_ring');

  // Priority 3: Folder assignment wins when app & workspace have no binding
  const r3 = resolveContextRing(dummyConfig, 'com.other', 'other_ws', 'folder1');
  assert.equal(r3.id, 'folder_ring');

  // Priority 4: Default global ring fallback
  const r4 = resolveContextRing(dummyConfig, 'com.other', 'other_ws', 'other_fld');
  assert.equal(r4.id, 'global');
});

test('calculateClampedCenter prevents radial ring edge clipping', () => {
  const screenWidth = 360;
  const screenHeight = 800;
  const radius = 140;

  // Near top-left (0, 0) -> clamped to stay fully on screen
  const [cx1, cy1] = calculateClampedCenter(10, 10, screenWidth, screenHeight, radius, 24);
  assert.ok(cx1 >= radius + 24, `cx1 ${cx1} must be >= radius + padding`);
  assert.ok(cy1 >= radius + 24, `cy1 ${cy1} must be >= radius + padding`);

  // Near bottom-right (360, 800) -> clamped
  const [cx2, cy2] = calculateClampedCenter(355, 790, screenWidth, screenHeight, radius, 24);
  assert.ok(cx2 <= screenWidth - radius - 24, `cx2 ${cx2} must be <= screenWidth - radius - padding`);
  assert.ok(cy2 <= screenHeight - radius - 24, `cy2 ${cy2} must be <= screenHeight - radius - padding`);
});

test('evaluatePointerAngleAndSlot deadzone and sector mapping', () => {
  const centerX = 200;
  const centerY = 300;
  const slotCount = 6;

  // Pointer directly at center within 20px deadzone
  const resDeadzone = evaluatePointerAngleAndSlot(205, 305, centerX, centerY, slotCount, 28);
  assert.equal(resDeadzone.selectedIndex, -1);

  // Pointer directly straight up (12 o'clock, dx=0, dy=-100) -> Sector 0
  const resTop = evaluatePointerAngleAndSlot(200, 200, centerX, centerY, slotCount, 28);
  assert.equal(resTop.selectedIndex, 0);

  // Pointer directly straight right (3 o'clock, dx=100, dy=0) -> Sector (slotCount/4)
  const resRight = evaluatePointerAngleAndSlot(300, 300, centerX, centerY, 4, 28);
  assert.equal(resRight.selectedIndex, 1);
});

test('canNavigateNestedRing protects against circular references and max depth', () => {
  // Max depth 3
  assert.equal(canNavigateNestedRing('ring3', ['root', 'ring1']), true);
  assert.equal(canNavigateNestedRing('ring4', ['root', 'ring1', 'ring2', 'ring3']), false);

  // Circular reference detection
  assert.equal(canNavigateNestedRing('ring1', ['root', 'ring1']), false);
});

test('Semantic theme system correctly resolves light and dark modes with high-contrast tokens', async () => {
  const { resolveIsDark, getSemanticColors } = await import('./theme');

  // Explicit modes
  assert.equal(resolveIsDark('dark'), true);
  assert.equal(resolveIsDark('charcoal-dark'), true);
  assert.equal(resolveIsDark('monochrome'), true);
  assert.equal(resolveIsDark('light'), false);
  assert.equal(resolveIsDark('warm-light'), false);

  // Dark tokens
  const darkColors = getSemanticColors('dark', '#38BDF8');
  assert.equal(darkColors.isDark, true);
  assert.equal(darkColors.textPrimary, '#F8FAFC');
  assert.equal(darkColors.bgPrimary, '#07090E');
  assert.equal(darkColors.textSecondary, '#94A3B8');
  assert.ok(darkColors.borderPrimary.includes('255, 255, 255'));

  // Light tokens
  const lightColors = getSemanticColors('light', '#38BDF8');
  assert.equal(lightColors.isDark, false);
  assert.equal(lightColors.textPrimary, '#1E293B');
  assert.equal(lightColors.bgPrimary, '#F8F9FA');
  assert.equal(lightColors.textSecondary, '#475569');
  assert.ok(lightColors.borderPrimary.includes('0, 0, 0'));
  assert.equal(lightColors.tabSelectedBg, '#38BDF8');
});

