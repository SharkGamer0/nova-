# NovaForge Launcher — Project-Wide Freedom & Privacy Policy

## Core Philosophy
NovaForge Launcher is an independent, completely free, privacy-first Android launcher designed for productivity, intentionality, and minimalism. It is developed with the permanent principle that users should own their device experience without financial barriers, data extraction, or intrusive monetization.

---

## 10 Permanent Development Principles

1. **100% Free Forever**
   - Every feature in NovaForge Launcher is free for every user.
   - All current and future capabilities (workspaces, command engine, widgets, automation) are included for all users equally.

2. **Zero Subscriptions or Paywalls**
   - No subscriptions, premium upgrades, "Pro" licenses, unlock keys, or feature paywalls will ever be introduced.

3. **Zero Advertisements**
   - No banner ads, interstitial ads, native promotional units, sponsored app suggestions, or affiliate links.

4. **Zero Paid APIs & Zero Paid Cloud Infrastructure**
   - The launcher does not rely on paid external cloud APIs, paid AI backend models, or recurring hosted infrastructure.
   - External data integrations must use free, open-access public APIs (such as Open-Meteo for weather, requiring no API key or payment) with graceful offline and unavailable fallbacks.

5. **Native Android API First**
   - Features leverage Android platform APIs directly:
     - App discovery & management: Android `PackageManager`
     - Calendar integration: Android `CalendarContract`
     - Screen time & digital wellbeing: Android `UsageStatsManager`
     - Music & playback controls: Android `MediaSessionManager`
     - Widgets: Android `AppWidgetHost` & `AppWidgetManager`
     - Persistence: Jetpack DataStore / Room

6. **Local-First On-Device Intelligence**
   - The NovaForge Intelligence Engine, command macro evaluation, schedule heuristics, and workspace recommendations run 100% on-device in local memory.
   - No telemetry, analytics SDKs, user tracking, or behavioral profiling.

7. **No Mandatory Accounts or Cloud Logins**
   - Users are never required to register an account, log in, or link cloud services to use any part of the launcher.

8. **Efficient & Battery-Friendly**
   - Avoid unnecessary background polling, battery-draining foreground services, or heavy persistent background tasks.
   - Automation rules and schedule checks evaluate passively during launcher lifecycle events.

9. **Local Data Ownership & Privacy**
   - User settings, custom macros, app constellations, and workspace configurations are stored strictly on local device storage.
   - Backups are performed via user-directed local export/import.

10. **Transparent Permission Model**
    - Runtime permissions (Calendar, Usage Access, Notification/Media listener, Widgets) are only requested with explicit user initiation and context.
    - If a permission is denied or revoked, the launcher provides clear fallback messaging and remains fully functional.

---

## Future Voluntary Donations Policy

If voluntary donation options are introduced in the future:
- **Zero Entitlement**: Donations will strictly be voluntary tips; they will never unlock features or restrict non-donors.
- **No Exclusive Perks**: Donors will receive no preferential functionality, badges, or tiers.
- **No In-App Intrusions**: Users will never be prompted, nagged, or interrupted with donation requests.
- **No Unnecessary Dependencies**: Payment SDKs or Google Play Billing libraries are not included unless and until explicitly required, and will never gate core launcher functionality.
