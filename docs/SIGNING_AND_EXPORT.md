# Noir 3D Game Engine v1.0.0 signing and export

## Engine APK

The engine is a normal Android launcher application with package ID `com.noir.game.engine`.

The launcher entry point is `.ProjectManagerActivity`. It is explicitly exported and declares both `ACTION_MAIN` and `CATEGORY_LAUNCHER`, so an installed engine APK has a launcher icon and opens the Project Manager when tapped.

The engine does not depend on a background service for startup.

## Release signing

Never commit a release keystore or passwords.

Local builds can use:

```text
NOIR_KEYSTORE=/absolute/path/noir-release.jks
NOIR_STORE_PASSWORD=...
NOIR_KEY_ALIAS=NoirEngine
NOIR_KEY_PASSWORD=...
```

Or create an untracked root file named `keystore.properties`:

```properties
storeFile=/absolute/path/noir-release.jks
storePassword=...
keyAlias=NoirEngine
keyPassword=...
```

GitHub Actions uses these repository secrets:

- `NOIR_KEYSTORE_B64`
- `NOIR_STORE_PASSWORD`
- `NOIR_KEY_ALIAS`
- `NOIR_KEY_PASSWORD`

When the keystore secret is present, the release APK is signed with that keystore. Without it, the Gradle release variant falls back to the Android debug key for development/testing only; that is not a production signing identity.

## v1.0.0 release pipeline

Pushing a tag such as `v1.0.0` starts the release workflow. The workflow:

1. Checks the launcher manifest.
2. Installs Android API 35, build-tools 35.0.0 and NDK 28.1.13356709.
3. Builds the native `libnoir3d.so`.
4. Builds the Android release APK.
5. Uploads the APK as a workflow artifact.
6. Creates a GitHub Release and attaches the APK.

The workflow can also be started manually with a release version.

## Project package

The mobile editor exports a portable Noir project package. Its structure includes:

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

The native library is the same engine runtime family used by the editor APK. The project package is the project handoff format; final project APK compilation still runs through an Android build environment.
