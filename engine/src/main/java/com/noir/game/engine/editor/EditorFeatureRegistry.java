package com.noir.game.engine.editor;

import java.util.*;

/** Registry used by the mobile editor to expose feature panels without hard-coding every tab. */
public final class EditorFeatureRegistry {
    public final Map<String,Feature> features=new LinkedHashMap<>();
    public EditorFeatureRegistry(){
        add("3D_VIEWPORT","3D Viewport",true);add("SCENE_TREE","Scene Tree",true);add("INSPECTOR","Inspector",true);add("ASSETS","Asset Browser",true);
        add("ANIMATION","Animation Maker",true);add("SCRIPT_IDE","Noir Script IDE",true);add("MATERIAL","Material Lab",true);add("TERRAIN","Terrain",true);
        add("NAVIGATION","Navigation",true);add("PHYSICS","Physics",true);add("PARTICLES","Particles",true);add("LIGHTING","Lighting",true);
        add("WORLD","World Environment",true);add("CONTROLLER","Mobile Controller Editor",true);add("PROFILER","GPU/CPU Profiler",true);add("BUILD","Build/Export",true);
    }
    public Feature add(String id,String title,boolean enabled){Feature f=new Feature(id,title,enabled);features.put(id,f);return f;}
    public static final class Feature{public final String id,title;public boolean enabled;Feature(String i,String t,boolean e){id=i;title=t;enabled=e;}}
}
