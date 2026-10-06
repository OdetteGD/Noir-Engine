package com.noir.game.engine.editor;

import com.noir.game.engine.controller.*;
import com.noir.game.engine.navigation.NavigationWorld;
import com.noir.game.engine.particles.ParticleSystem;
import com.noir.game.engine.render.*;
import com.noir.game.engine.scripting.NoirNodeApiRegistry;
import com.noir.game.engine.terrain.TerrainSystem;
import java.util.*;

/**
 * Central mobile editor state coordinator. UI panels bind to these models rather than creating
 * disconnected mock state, which keeps scene editing, controllers, animation, materials and
 * world settings synchronized.
 */
public final class NoirMobileEditorController {
    public final EditorState editorState;
    public final EditorFeatureRegistry features=new EditorFeatureRegistry();
    public final ThreeDViewportSettings viewport=new ThreeDViewportSettings();
    public final ControllerEditorModel controller=new ControllerEditorModel();
    public final AnimationEditorModel animation=new AnimationEditorModel();
    public final MaterialEditorModel material=new MaterialEditorModel();
    public final TerrainSystem terrain=new TerrainSystem();
    public final NavigationWorld navigation=new NavigationWorld();
    public final ParticleSystem particles=new ParticleSystem();
    public final RenderFeatureSet renderFeatures=new RenderFeatureSet();
    public final WorldEnvironmentSettings world=new WorldEnvironmentSettings();
    public final ReflectionProbeSettings reflectionProbe=new ReflectionProbeSettings();
    public final NoirNodeApiRegistry api=new NoirNodeApiRegistry();
    public String activePanel="3D_VIEWPORT";
    public boolean gizmoTranslate=true,gizmoRotate=false,gizmoScale=false,snapEnabled=true;
    public float snapTranslation=.25f,snapRotation=15f,snapScale=.1f;

    public NoirMobileEditorController(EditorState state){editorState=Objects.requireNonNull(state);}
    public void selectPanel(String id){if(features.features.containsKey(id))activePanel=id;}
    public void setGizmo(String mode){gizmoTranslate="translate".equals(mode);gizmoRotate="rotate".equals(mode);gizmoScale="scale".equals(mode);}
    public void setQuality(RenderFeatureSet.Quality q){renderFeatures.apply(q);}
    public List<String> diagnostics(){
        List<String> d=new ArrayList<>(); d.addAll(controller.validate()); d.addAll(renderFeatures.diagnostics());
        if(!viewport.valid())d.add("Invalid 3D viewport configuration"); if(!reflectionProbe.valid())d.add("Invalid reflection probe configuration");
        if(!navigation.validate())d.add("Invalid navigation configuration"); if(!material.validate())d.add("Material graph is incomplete");
        return d;
    }
}
