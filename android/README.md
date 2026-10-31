# NovaForge Launcher (Android Native)

NovaForge Launcher is a minimalist, intelligent, workspace-based Android launcher built with Kotlin and Jetpack Compose.

## Architectural Pillars

- **Minimalist Workspace Architecture**:
  - **HOME**: Clean, essential apps, animated icon engine, native Android widgets, and adaptive suggestion banner.
  - **COMMAND**: Universal command bar (`/start-study`, `/gaming-mode`, `/icons`, etc.), fuzzy app search, and quick settings.
  - **SPACES**: Dedicated workspaces (Study, Work, Gaming, Travel, Personal) with full workspace memory.
- **Animated Icon Engine**:
  - Replace ordinary app icons with custom animated icons (GIF, Animated WebP, Lottie JSON, Static PNG/WebP fallback).
  - Dedicated **Animated Icon Library**: Import animations via Android document picker, preview speeds (0.5x–2x), rename, organize into collections, and delete.
  - **Intuitive App Icon Assignment**: Assign animations to installed apps, filter by assigned/unassigned, preview live, and restore original icons.
  - **Battery & Performance Engine**: Three modes (`Battery Saver`, `Balanced`, `Performance`), auto-pauses when screen is off, when another app is open, or during low-power states. Zero background CPU consumption.
  - Rendered across Home essentials, Predictive Dock, App Drawer, and Constellation orbits.
- **Native Android Integration**:
  - `AppWidgetHost` and `AppWidgetManager` for live, resizable home-screen widgets.
  - `PackageManager` for launchable app discovery and intent resolution.
  - `CalendarContract` for native calendar agenda integration.
  - `UsageStatsManager` for real digital wellbeing screen-time statistics.
  - `MediaSessionManager` for active media playback integration.
- **Context Rings — Advanced Radial Command System**:
  - **Radial Action Interface**: Circular radial menu with 4, 6, 8, or 10 customizable action slots arranged around a central target hub.
  - **Fluid Touch Mechanics**: Smooth radial expansion animation, circular geometry, touch-and-drag sector highlighting with haptic feedback, release-to-activate, center-cancel hub, and screen-edge clamping that prevents ring clipping on any device screen or orientation.
  - **4-Level Context Priority**:
    1. Explicit app-specific ring assignment (e.g. Browser ring on Chrome/Brave, Music ring on Spotify).
    2. Active workspace ring (e.g. Study ring in Study workspace, Gaming ring in Gaming workspace).
    3. Folder-specific ring.
    4. Default global ring (`global_default`).
  - **Dedicated Context Ring Designer**: Visual editor allowing users to create, duplicate, rename, reorder, adjust radius (100–180 dp) and icon sizes (36–52 dp), customize action labels, inspect live geometry, and export/import configurations locally via JSON.
  - **Rich Action Categories**: Native app launches, launcher workspace switching, focus mode toggles, standard Android system settings (Wi-Fi, Bluetooth, Display, Battery Saver), media session controls (play/pause, next, prev), and command macro execution with safety confirmation prompts.
  - **Nested Context Rings**: Actions can navigate to sub-rings (e.g. Study Tools) with back navigation, bounded to a maximum depth of 3 with circular reference loop protection.
- **On-Device Intelligence Engine**:
  - Command macros executing multi-step launcher actions.
  - Local heuristic schedule recommendations with zero telemetry.
  - Visual automation rule editor (IF/THEN triggers).
  - App constellations for orbit grouping.

## Freedom & Privacy Policy
NovaForge is 100% free, ad-free, account-free, and privacy-first.
See [POLICY.md](POLICY.md) for our permanent 10 development principles.

## Building the Project

Requirements:
- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17 or higher
- Android SDK 35 (compileSdk 35, minSdk 26, targetSdk 35)

Run in terminal:
```bash
./gradlew assembleDebug
```
The compiled debug APK will be generated at:
`app/build/outputs/apk/debug/app-debug.apk`
