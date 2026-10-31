# NovaForge Launcher ProGuard Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* <methods>;
}
-dontwarn android.app.usage.**
