# Noir 3D Game Engine

Noir is a foreground Android 3D editor/runtime with a mobile project manager and native runtime.

## App

The launcher opens **Project Manager**, not a background service.

Features:
- Create Noir projects
- Open/import Noir project packages
- Open projects in the 3D editor
- Package/export projects
- Project-owned assets directory
- Native libnoir3d.so runtime
- CMake/NDK native build
- Release signing through local properties or GitHub Secrets
- Android Storage Access Framework for user-selected files

## Project package layout

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

The native library is built from app/src/main/cpp.

## APK signing

Do not commit private signing keys.

For GitHub Actions configure:
- NOIR_KEYSTORE_B64
- NOIR_STORE_PASSWORD
- NOIR_KEY_ALIAS
- NOIR_KEY_PASSWORD

The release workflow builds API 35 with JDK 17, Gradle 8.10.2 and NDK 28.1.13356709.

If no release keystore is configured, the Gradle release variant falls back to the Android debug key for development/testing only. It is not a production distribution identity.

## Project export vs APK build

The mobile editor can create a complete Noir project package containing project files/assets and the libnoir3d.so ABI available in the running engine. Building a final project APK still requires an Android build environment/Gradle toolchain; the package is the handoff format for that build step.

See docs/SIGNING_AND_EXPORT.md.
