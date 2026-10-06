# Noir 3D Node API Reference

This document is generated from the v0.4 node registry and is intended to be the stable public API index for the mobile editor.

## Scene / gameplay
- **Node3D** — base transform/hierarchy node.
- **Character3D** — capsule character movement, grounding, stance and jump state.
- **Player3D** — player input, camera ownership and gameplay actions.
- **Vehicle3D** — wheel/engine/brake/steering authoring surface.
- **Area3D** — trigger volumes and overlap events.
- **Raycast3D** — deterministic collision query.

## Rendering
- **Mesh3D** — static mesh + material.
- **SkinnedMesh3D** — skeletal mesh + skinning.
- **Light3D** — directional, point and spot lights.
- **WorldEnvironment** — sky, fog, exposure and ambient lighting.
- **Sky3D** — procedural/HDRI/cubemap sky.
- **ReflectionProbe3D** — local environment capture.
- **LightProbe3D** — indirect lighting samples.
- **Decal3D** — projected material/decal.
- **FogVolume3D** — localized volumetric fog.
- **PostProcess3D** — tone mapping, SSAO, bloom and color grading.
- **Particles3D** — GPU-preferred particle authoring.
- **Water3D** — wave, foam, refraction and shoreline settings.
- **LODGroup3D** — distance/screen-size LOD selection.
- **Occluder3D** — visibility/occlusion geometry.

## World building
- **Terrain3D** — streamed terrain chunks and clipmap-style LOD.
- **Foliage3D** — GPU-instanced vegetation authoring.
- **Spline3D** — path/rail/road authoring.
- **Navigation3D** — navmesh surfaces.
- **NavigationAgent3D** — path-following and avoidance.

## Physics
- **Collider3D** — collision shape/layer/mask.
- **RigidBody3D** — dynamic rigid body.
- **StaticBody3D** — static collision body.
- **SpringArm3D** — collision-aware third-person camera arm.

## Animation
- **AnimationPlayer** — clip playback and blending.
- **AnimationTree** — state-machine/blend-tree evaluation.
- **BoneAttachment3D** — follows a skeleton bone.
- **IKTarget3D** — inverse-kinematics target.

Every node must expose its properties through the Inspector and its callable methods through the `.game` API registry.
