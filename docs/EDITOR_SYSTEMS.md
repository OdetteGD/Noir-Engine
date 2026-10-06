# Noir 3D Editor Systems

Noir is a native Android application, not a Godot project. The editor is composed of a GPU 3D viewport plus a synchronized mobile UI model.

## Panels
- Scene/Outliner: hierarchical Node3D tree with creation, deletion and duplication hooks.
- Assets: typed asset database for GLB/GLTF, textures, materials, shaders, animation, audio and .game documents.
- Inspector: transform and metadata fields generated from the selected node.
- Animation Maker: clips, tracks, keyframes, interpolation and scrubbing model.
- Scripting IDE: .game source document, diagnostics and completion API.
- Shader Graph/Material Lab: PBR parameter surface and shader source asset support.
- Physics/Navigation: runtime body statistics, raycast diagnostics and navmesh status surface.
- Console/Profiler: runtime messages and GPU/CPU frame counters.

## Runtime/editor boundary
The editor state is independent from the renderer. A scene can be parsed without an Android Activity. The renderer receives only render data and camera state. This separation is intentional so future desktop/headless builds can reuse the same engine core.

## Next native backend milestones
1. GLB/GLTF binary importer with vertex/index/skin buffers.
2. Texture decoder and KTX2/Basis GPU upload.
3. PBR IBL, shadow maps, reflection probes and post-processing.
4. Skeletal animation GPU skinning and animation graph.
5. Character controller, collision shapes, broadphase and navmesh.
6. Terrain/foliage/LOD streaming.
7. Material/shader graph compiler.
8. Frame capture and GPU memory profiler.
