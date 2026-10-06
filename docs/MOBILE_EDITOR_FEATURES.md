# Noir Mobile Editor v0.4

Noir is designed around touch-first authoring rather than shrinking a desktop editor onto a phone.

## 3D viewport
- One-finger orbit, two-finger pan and pinch zoom.
- Perspective and orthographic cameras.
- Translate, rotate and scale gizmo contracts.
- Grid snapping and axis constraints.
- Selection outlines and node bounds.
- Collision, navmesh, wireframe, light and overdraw debug modes.
- Production camera bookmarks.

## Controllers
- FPS, third-person and vehicle profiles.
- Resolution-independent safe-area controls.
- Left/right virtual sticks.
- Fire, aim, reload, jump, crouch, sprint, interact and pause actions.
- Deadzones, sensitivity, gyro flag and gamepad flag.
- Touch hit testing and custom control placement.

## Animation Maker
- Animation clips, tracks and keyframes.
- Auto-key and loop modes.
- Timeline markers.
- Onion-skin configuration.
- Skeletal animation hooks.
- Animation state-machine parameters.

## World authoring
Terrain streaming, terrain layers, foliage painting, water settings, spline paths, navigation surfaces/agents, reflection probes, light probes, decals, particles, fog volumes, post-processing and LOD groups.

## Material / Shader Lab
The graph model supports PBR inputs, texture nodes, normal maps, roughness, metallic, AO, emission and output links. The renderer can compile graph output into a mobile GLSL material backend.

## Script IDE
The IDE supports diagnostics, completion, compilation and encrypted `.game` storage. Encrypted files use a versioned Noir binary envelope around AES-256-GCM with PBKDF2-HMAC-SHA256 key derivation. It is not Base64. The encryption format is custom to Noir, while the cryptographic primitives remain established standards.

## Build / profiling
The editor is intended to expose device render profiles, GPU/CPU timings, draw-call counts, triangle counts, memory budgets and APK/export settings.
