# Noir `.game` Scripting API

## Language model
`.game` is Noir's high-level scene/gameplay language. The source can be compiled to Noir bytecode and optionally stored in an encrypted `.game` container. Encryption is **not Base64**: Noir uses a versioned binary envelope with PBKDF2-HMAC-SHA256 key derivation and AES-256-GCM authenticated encryption.

> Security note: Noir deliberately uses established cryptographic primitives rather than inventing a custom cipher. The custom part is the Noir file/container format and scripting toolchain.

## Core syntax
```game
entity Player {
    type: Character3D
    property speed = 5.0
    input move_x
    input move_y

    start {
        camera = child("Camera3D")
    }

    physics(delta) {
        movement = vector(move_x, 0, move_y)
        velocity.x = movement.x * speed
        velocity.z = movement.z * speed
        move_and_slide()
    }
}
```

## Built-in node API

| Node | Main API |
|---|---|
| Node3D | `child`, `find`, `set_position`, `set_rotation`, `set_scale`, `add_child`, `remove_child` |
| Character3D | `velocity`, `move_and_slide`, `jump`, `is_on_floor`, `set_stance` |
| Player3D | `input`, `look`, `camera`, `aim`, `fire` |
| Camera3D | `fov`, `near`, `far`, `look_at`, `set_active` |
| Mesh3D | `mesh`, `material`, `visible`, `cast_shadows` |
| SkinnedMesh3D | `skeleton`, `skin`, `set_bone_transform` |
| Light3D | `type`, `energy`, `range`, `color`, `shadow_enabled` |
| WorldEnvironment | `sky`, `fog`, `exposure`, `ambient_energy` |
| ReflectionProbe3D | `size`, `resolution`, `update_mode`, `capture` |
| LightProbe3D | `bake`, `sample` |
| Terrain3D | `height_scale`, `chunk_size`, `lod_levels`, `stream` |
| Foliage3D | `density`, `instance_mesh`, `wind_strength`, `paint` |
| Water3D | `wave_height`, `wave_speed`, `foam`, `refraction` |
| Particles3D | `emit`, `burst`, `lifetime`, `emission_rate`, `material` |
| Navigation3D | `bake`, `rebuild`, `set_area` |
| NavigationAgent3D | `target`, `velocity`, `path`, `avoidance` |
| Collider3D | `shape`, `layer`, `mask`, `enabled` |
| RigidBody3D | `mass`, `apply_force`, `apply_impulse`, `linear_velocity` |
| StaticBody3D | `shape`, `freeze`, `collision_layer` |
| Raycast3D | `enabled`, `target`, `collision_mask`, `get_hit` |
| AnimationPlayer | `play`, `stop`, `pause`, `seek`, `blend` |
| AnimationTree | `set_parameter`, `travel`, `blend_space` |
| BoneAttachment3D | `bone`, `follow_pose` |
| IKTarget3D | `target`, `weight`, `pole` |
| Decal3D | `texture`, `normal`, `roughness`, `size` |
| Sky3D | `mode`, `texture`, `brightness` |
| FogVolume3D | `density`, `albedo`, `anisotropy`, `size` |
| PostProcess3D | `bloom`, `ssao`, `tonemap`, `color_grading` |
| LODGroup3D | `levels`, `screen_ratio`, `fade` |
| Occluder3D | `mesh`, `enabled` |
| Vehicle3D | `mass`, `engine_force`, `brake`, `steering` |
| SpringArm3D | `length`, `collision_mask`, `camera` |

## Mobile input API
`input.axis("move_x")`, `input.axis("move_y")`, `input.action_pressed("jump")`, `input.action_just_pressed("fire")`, `input.look_delta()`, `input.touch_position()`.

## Math API
`vector(x,y,z)`, `lerp(a,b,t)`, `clamp(v,min,max)`, `normalize(v)`, `dot(a,b)`, `cross(a,b)`, `distance(a,b)`.

## Runtime callbacks
`start()`, `update(delta)`, `physics(delta)`, `input(event)`, `late_update(delta)`, `destroy()`.

## Error policy
Unknown nodes, properties, and malformed expressions generate diagnostics with source locations. The editor must not silently ignore an invalid gameplay statement.
