package com.noir.game.engine.scripting;

import com.noir.game.engine.scene.NoirNode;
import java.util.*;

/** Single source of truth for node names, properties and callable .game API methods. */
public final class NoirNodeApiRegistry {
    public static final class ApiNode {
        public final NoirNode.Kind kind; public final String description;
        public final Set<String> properties=new LinkedHashSet<>(); public final Set<String> methods=new LinkedHashSet<>();
        ApiNode(NoirNode.Kind k,String d){kind=k;description=d;}
        ApiNode prop(String... p){Collections.addAll(properties,p);return this;}
        ApiNode method(String... m){Collections.addAll(methods,m);return this;}
    }
    private final Map<String,ApiNode> nodes=new LinkedHashMap<>();
    public NoirNodeApiRegistry(){
        register(NoirNode.Kind.NODE3D,"Base transform and hierarchy node").prop("position","rotation","scale","visible","locked").method("child","find","add_child","remove_child","look_at");
        register(NoirNode.Kind.CHARACTER3D,"Capsule-based mobile character").prop("speed","jump_velocity","gravity","stance","sprint_speed").method("move_and_slide","jump","is_on_floor","set_stance");
        register(NoirNode.Kind.PLAYER3D,"Player gameplay controller").prop("input_profile","camera","aim_sensitivity").method("fire","aim","reload","interact");
        register(NoirNode.Kind.CAMERA3D,"Perspective or orthographic camera").prop("fov","near","far","projection").method("look_at","set_active","screen_ray");
        register(NoirNode.Kind.MESH3D,"Static render mesh").prop("mesh","material","cast_shadows","receive_shadows").method("set_material");
        register(NoirNode.Kind.SKINNED_MESH3D,"Skeletal render mesh").prop("skeleton","skin","material").method("set_bone_transform");
        register(NoirNode.Kind.LIGHT3D,"Directional, point or spot light").prop("type","energy","range","color","shadow_enabled").method("set_energy");
        register(NoirNode.Kind.WORLD_ENVIRONMENT,"Global environment").prop("sky","fog","exposure","ambient_energy").method("capture_environment");
        register(NoirNode.Kind.TERRAIN3D,"Streamed terrain and LOD").prop("height_scale","chunk_size","lod_levels").method("stream","paint_height","paint_layer");
        register(NoirNode.Kind.FOLIAGE3D,"GPU-instanced foliage").prop("density","wind_strength","mesh").method("paint","clear");
        register(NoirNode.Kind.WATER3D,"Water surface").prop("wave_height","wave_speed","foam","refraction").method("sample_height");
        register(NoirNode.Kind.PARTICLES3D,"Particle emitter").prop("max_particles","lifetime","emission_rate").method("emit","burst","clear");
        register(NoirNode.Kind.NAVMESH3D,"Navigation surface").prop("agent_radius","agent_height","max_slope").method("bake","rebuild");
        register(NoirNode.Kind.NAV_AGENT3D,"Navigation agent").prop("speed","avoidance","target").method("set_target","get_next_path_point");
        register(NoirNode.Kind.REFLECTION_PROBE3D,"Local reflection capture").prop("resolution","update_mode","box_projection").method("capture");
        register(NoirNode.Kind.LIGHT_PROBE3D,"Indirect lighting sample").prop("resolution","intensity").method("bake","sample");
        register(NoirNode.Kind.DECAL3D,"Projected material").prop("texture","normal","roughness","size").method("set_texture");
        register(NoirNode.Kind.FOG_VOLUME3D,"Localized volumetric fog").prop("density","albedo","anisotropy","size").method("set_density");
        register(NoirNode.Kind.POST_PROCESS3D,"Post-processing stack").prop("bloom","ssao","tonemap","color_grading").method("set_effect_enabled");
        register(NoirNode.Kind.LOD_GROUP3D,"Distance-based LOD").prop("levels","screen_ratio","fade").method("force_level");
        register(NoirNode.Kind.OCCLUDER3D,"Visibility occluder").prop("mesh","enabled").method("set_enabled");
        register(NoirNode.Kind.RIGID_BODY3D,"Dynamic rigid body").prop("mass","linear_velocity","angular_velocity").method("apply_force","apply_impulse");
        register(NoirNode.Kind.STATIC_BODY3D,"Static collision body").prop("collision_layer","collision_mask","shape").method("set_shape");
        register(NoirNode.Kind.COLLIDER3D,"Collision shape").prop("shape","layer","mask","enabled").method("set_enabled");
        register(NoirNode.Kind.RAYCAST3D,"Collision query").prop("target","collision_mask","enabled").method("get_hit","get_hit_position");
        register(NoirNode.Kind.ANIMATION_PLAYER,"Animation clip player").prop("clip","speed","loop").method("play","stop","pause","seek","blend");
        register(NoirNode.Kind.ANIMATION_TREE,"Animation graph").prop("active","parameters").method("travel","set_parameter","blend_space");
        register(NoirNode.Kind.BONE_ATTACHMENT3D,"Bone-following attachment").prop("bone","follow_pose").method("set_bone");
        register(NoirNode.Kind.IK_TARGET3D,"Inverse-kinematics target").prop("target","weight","pole").method("set_target");
        register(NoirNode.Kind.VEHICLE3D,"Vehicle controller").prop("mass","engine_force","brake","steering").method("accelerate","brake","steer");
        register(NoirNode.Kind.SPRING_ARM3D,"Collision-aware camera arm").prop("length","collision_mask").method("set_length");
    }
    private ApiNode register(NoirNode.Kind k,String d){ApiNode a=new ApiNode(k,d);nodes.put(k.name(),a);return a;}
    public ApiNode get(String name){return nodes.get(name.toUpperCase(Locale.US));}
    public Collection<ApiNode> all(){return Collections.unmodifiableCollection(nodes.values());}
    public List<String> completions(String prefix){String p=prefix==null?"":prefix.toUpperCase(Locale.US);List<String> out=new ArrayList<>();for(ApiNode n:nodes.values())if(n.kind.name().startsWith(p))out.add(n.kind.name());Collections.sort(out);return out;}
}
