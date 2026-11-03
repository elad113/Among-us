# Among Us Companion Launcher (Android)

An original Android companion launcher for the official Google Play Among Us app.

## What it does
- Launches the installed official game (package: com.innersloth.spacemafia).
- Provides an optional draggable Android overlay with a small companion panel.
- Includes companion-only UI toggles for notes, timer and HUD preview.

## Important limitation
Android isolates apps from one another. This launcher does not inject code into Among Us, change game state, reveal hidden roles, or provide public-match cheats. The menu is a companion overlay, not an in-game mod.

## Build
GitHub Actions builds a debug APK on every push. Download it from the workflow run's **Artifacts** section as `among-us-companion-debug-apk`.

Manual build: open the repository in Android Studio and run `gradle assembleDebug`.
