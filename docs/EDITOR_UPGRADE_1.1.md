# Noir 3D Game Engine — Editor/Renderer Upgrade

This upgrade focuses on mobile editor interaction and world-space rendering.

## Editor interaction
- Corrected editor orbit direction so drag motion is no longer inverted.
- Added viewport tap callbacks for editor object selection.
- Added Select / Move / Rotate / Scale tool buttons.
- Added an editor gizmo overlay for the selected node.
- Added a real project-root file browser backed by the opened project directory.
- Animation Maker now has a Play/Pause control and a moving playhead.
- Existing scene, inspector, script, shader, physics, world, controller, profiler and console panels remain available.

## World rendering
- Procedural sky is now evaluated from camera/world direction instead of raw screen coordinates.
- Procedural clouds are sampled in 3D direction space, so dragging the camera no longer drags the cloud texture with the screen.
- Procedural sun disc is tied to the same world-space sun direction used by the forward lighting.
- Forward PBR baseline now includes GGX-style distribution/geometry terms, Fresnel, roughness/metal response and an environment/specular contribution.
- Existing depth shadow pass and PCF filtering remain enabled.
- MSAA is still requested by the mobile surface.

## Native C++
- Added noir_physics.cpp to the Android CMake target.
- Added JNI rigid-body integration and ray/sphere intersection helpers.
- Added a native mobile PBR shader source accessor.
- Java NoirNative exposes these native hooks.

## Runtime architecture
The native additions are backend hooks, not a claim that every engine system has been migrated to C++ yet. The current renderer still uses the Java OpenGL ES 3.0 path and the project remains designed for mobile performance.

## Verification note
The repository has been edited and the Android CMake configuration now includes the new C++ source. A full Android Gradle/NDK build must still be run in GitHub Actions or Android Studio to validate device/toolchain-specific compilation and runtime behavior.
