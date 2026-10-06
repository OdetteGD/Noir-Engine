# Noir 3D Game Engine v0.4

A native Android-first 3D game engine and mobile editor with a proprietary `.game` scene/scripting format.

**Application ID:** `com.noir.game.engine`

## v0.3 editor
The project now contains actual engine/editor subsystems rather than placeholder panels: scene graph, asset database, inspector model, animation system, script compiler/diagnostics, physics preview backend, OpenGL ES 3 viewport renderer, mobile editor UI, shader material surface, console and profiler surface.

## Build
Use Android Studio or a GitHub Actions runner with Android SDK 35 and JDK 17. GitHub workflows use Gradle 8.10.2. The repository intentionally does not contain a fake APK or a fake Gradle wrapper binary.

## GitHub Actions
- `android.yml`: build debug APK on push/PR.
- `release.yml`: build release APK when a version tag is pushed.

## Important engineering note
This is a real foundation, not a claim that every AAA subsystem is already complete. Unsupported asset formats should report diagnostics instead of silently pretending to import them. Future rendering/physics/animation backends plug into the existing contracts.


## v0.4 mobile-next-gen additions
- Expanded Node3D registry including Character3D, Player3D, Vehicle3D, Terrain3D, Water3D, Foliage3D, Navigation, reflection/light probes, decals, fog, post-processing, LOD and occlusion.
- Mobile controller editor models with FPS/third-person/vehicle action layouts, safe-area support, touch hit testing, sensitivity and deadzones.
- Animation Maker model with clips, tracks, keyframes, markers, auto-key, looping and onion-skin configuration.
- Material/Shader Lab graph model.
- Terrain streaming/LOD model, particle authoring model and navigation world model.
- Versioned Noir `.game` encryption container using PBKDF2-HMAC-SHA256 + AES-256-GCM. This is a custom Noir file format built from standard cryptography, not Base64 and not an invented cipher.
- `.game` bytecode container with source hashing.
- Public API documentation for all registered node families.
