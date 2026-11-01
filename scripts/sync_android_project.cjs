const fs = require('fs');
const path = require('path');

const androidDir = path.resolve(__dirname, '../android');
const outputFile = path.resolve(__dirname, '../src/launcher/androidProjectFiles.ts');

function walk(dir) {
  let results = [];
  const list = fs.readdirSync(dir);
  for (const item of list) {
    if (item === '.gradle' || item === 'build' || item.endsWith('.bin') || item.endsWith('.lock')) {
      continue;
    }
    const fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);
    if (stat && stat.isDirectory()) {
      results = results.concat(walk(fullPath));
    } else {
      results.push(fullPath);
    }
  }
  return results;
}

const allPaths = walk(androidDir);

const files = allPaths.map((filePath) => {
  const relPath = path.relative(androidDir, filePath).replace(/\\/g, '/');
  const ext = path.extname(filePath).toLowerCase();

  let category = 'doc';
  if (relPath.endsWith('AndroidManifest.xml')) {
    category = 'manifest';
  } else if (ext === '.kt') {
    category = 'kotlin';
  } else if (ext === '.xml') {
    category = 'xml';
  } else if (ext === '.kts' || ext === '.properties' || ext === '.toml' || ext === '.pro' || relPath.startsWith('gradlew')) {
    category = 'gradle';
  }

  let description = `Android source file: ${relPath}`;
  if (relPath.includes('NovaForgeApp.kt')) {
    description = 'Application subclass with singleton & dependency injection';
  } else if (relPath.includes('ContextRingModels.kt')) {
    description = 'Context Rings data models, action types, definitions and active session state';
  } else if (relPath.includes('ContextRingRepository.kt')) {
    description = 'Context Rings repository with DataStore persistence, default presets & export/import';
  } else if (relPath.includes('ContextRingEngine.kt')) {
    description = 'Context Rings priority resolution (app > workspace > folder > global), edge clamping & pointer geometry';
  } else if (relPath.includes('ContextRingRenderer.kt')) {
    description = 'Context Rings radial geometry renderer with smooth animations and circular action nodes';
  } else if (relPath.includes('ContextRingActionExecutor.kt')) {
    description = 'Context Rings action executor for apps, system settings, media keys & macros';
  } else if (relPath.includes('ContextRingOverlay.kt')) {
    description = 'Context Rings full-screen touch-and-drag overlay with haptics & edge clamping';
  } else if (relPath.includes('ContextRingDesignerScreen.kt')) {
    description = 'Context Rings visual designer with live preview, slot count & action ordering';
  } else if (relPath.includes('AnimatedIconModels.kt')) {
    description = 'Animated Icon Engine data models (GIF, WebP, Lottie, BatteryEngineMode)';
  } else if (relPath.includes('AnimationRepository.kt')) {
    description = 'Local animation library, disk file manager & assignment persistence';
  } else if (relPath.includes('AnimationPlaybackController.kt')) {
    description = 'Battery & performance engine with lifecycle and screen-state throttling';
  } else if (relPath.includes('AnimationAssignmentManager.kt')) {
    description = 'App animation assignment manager with batch assignment and filter helpers';
  } else if (relPath.includes('AnimatedIconView.kt')) {
    description = 'AnimatedAppIcon & AnimatedIconPreview Jetpack Compose components';
  } else if (relPath.includes('AnimatedIconLibraryScreen.kt')) {
    description = 'Animated Icon Library with document picker, collections & speed preview';
  } else if (relPath.includes('IconAssignmentDialog.kt')) {
    description = 'App icon assignment dialog with live preview and filter controls';
  } else if (relPath.includes('AppRepository.kt')) {
    description = 'App package manager repository for installed app querying';
  } else if (relPath.includes('LauncherPreferences.kt')) {
    description = 'DataStore preferences for theme, focus mode & widget layouts';
  } else if (relPath.includes('RealDataProviders.kt')) {
    description = 'Open-Meteo weather, Calendar Provider & UsageStats screen time';
  } else if (relPath.includes('NovaForgeAppWidgetHost.kt')) {
    description = 'AppWidgetHost, binding manager & resizable widget container';
  } else if (relPath.includes('MainActivity.kt')) {
    description = 'Main Activity with runtime permissions, widget launchers & home intent';
  } else if (relPath.includes('WidgetsScreen.kt')) {
    description = 'Widget configuration screen with live resize, move & delete';
  } else if (relPath.includes('themes.xml')) {
    description = 'Android Material 3 theme definition for NovaForge';
  } else if (relPath.includes('gradlew')) {
    description = 'Official Gradle wrapper execution script';
  } else if (relPath.endsWith('.jar')) {
  } else if (relPath.includes('NovaForgeLogo.kt')) {
    description = 'Robust memory-safe NovaForge logo with multi-tier fail-safe rendering';
  } else if (relPath.includes('ic_novaforge_vector_logo.xml')) {
    description = 'Official NovaForge cosmic identity vector drawable asset';
  } else if (ext === '.png') {
    description = `Official NovaForge image asset: ${relPath}`;
  }

  const isBinary = ext === '.jar' || ext === '.png' || ext === '.webp' || ext === '.class';
  if (isBinary) {
    const base64Content = fs.readFileSync(filePath).toString('base64');
    return {
      path: relPath,
      category: ext === '.png' ? 'xml' : category,
      description,
      isBinary: true,
      binaryBase64: base64Content,
      content: `[Binary asset (${(fs.statSync(filePath).size / 1024).toFixed(1)} KB)]`
    };
  } else {
    const textContent = fs.readFileSync(filePath, 'utf8');
    return {
      path: relPath,
      category,
      description,
      isBinary: false,
      content: textContent
    };
  }
});

// Sort files logically: gradle, manifest, kotlin, res, wrapper
const sortedFiles = files.sort((a, b) => {
  const getWeight = (p) => {
    if (p.includes('AndroidManifest.xml')) return 1;
    if (p.includes('build.gradle.kts')) return 2;
    if (p.includes('libs.versions.toml')) return 3;
    if (p.includes('NovaForgeApp.kt')) return 10;
    if (p.includes('MainActivity.kt')) return 11;
    if (p.includes('AppRepository.kt')) return 12;
    if (p.includes('LauncherPreferences.kt')) return 13;
    if (p.includes('RealDataProviders.kt')) return 14;
    if (p.includes('NovaForgeAppWidgetHost.kt')) return 15;
    if (p.includes('WidgetsScreen.kt')) return 16;
    if (p.endsWith('.kt')) return 20;
    if (p.includes('themes.xml')) return 30;
    if (p.endsWith('.xml')) return 35;
    if (p.includes('gradlew')) return 40;
    return 50;
  };
  return getWeight(a.path) - getWeight(b.path) || a.path.localeCompare(b.path);
});

const tsCode = `export interface AndroidFileDefinition {
  path: string;
  category: 'manifest' | 'gradle' | 'kotlin' | 'xml' | 'doc';
  description: string;
  content: string;
  isBinary?: boolean;
  binaryBase64?: string;
}

export const ANDROID_PROJECT_FILES: AndroidFileDefinition[] = ${JSON.stringify(sortedFiles, null, 2)};
`;

fs.writeFileSync(outputFile, tsCode, 'utf8');
console.log(`Successfully generated ${outputFile} with ${sortedFiles.length} files.`);
