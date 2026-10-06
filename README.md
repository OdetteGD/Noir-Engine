# Noir 3D Game Engine v1.0.0

Noir is a foreground Android 3D editor/runtime with a mobile project manager and native runtime.

## Engine app

Package ID: `com.noir.game.engine`

The installed engine is a normal launcher application:

- `ProjectManagerActivity` is the launcher entry point.
- It declares `ACTION_MAIN` + `CATEGORY_LAUNCHER`.
- It is explicitly `android:exported="true"`.
- The engine has an explicit launcher icon.
- `MainActivity` is opened by the Project Manager and is not the launcher.
- No background service is required to open the engine.

So after installing the APK, Noir appears in the Android app launcher and tapping its icon opens the Project Manager.

## v1.0.0

- Mobile Project Manager
- Create Noir projects
- Open/import Noir project packages
- Open projects in the 3D editor
- Export project packages
- Project-owned assets directories
- Native `libnoir3d.so` runtime
- CMake/NDK native build
- Explicit launcher APK configuration
- Release signing through local properties or GitHub Secrets
- Automatic tag-based APK release workflow
- Android Storage Access Framework for user-selected files

## Project package layout

```text
project/
  project.game
  scenes/
  scripts/
  assets/
  models/
  textures/
  materials/
  animations/
  shaders/
  audio/
runtime/
  noir-package.json
  lib/
    <ABI>/
      libnoir3d.so
```

The native library is built from `app/src/main/cpp`.

## APK signing

Do not commit private signing keys.

For GitHub Actions configure:

- `NOIR_KEYSTORE_B64`
- `NOIR_STORE_PASSWORD`
- `NOIR_KEY_ALIAS`
- `NOIR_KEY_PASSWORD`

The release workflow builds API 35 with JDK 17, Gradle 8.10.2 and NDK 28.1.13356709.

If no release keystore is configured, the Gradle release variant falls back to the Android debug key for development/testing only. It is not a production distribution identity.

## Release

A tag such as `v1.0.0` triggers the release workflow. The workflow verifies the launcher manifest, builds the native library and Android APK, uploads the APK artifact, and creates a GitHub Release containing the APK.

The project package is the portable editor/project handoff format. Building a final game APK still requires the Android build toolchain; the mobile editor itself is the launcher application distributed by this repository.

See `docs/SIGNING_AND_EXPORT.md`.
