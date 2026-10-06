package com.noir.game.engine.editor;

import com.noir.game.engine.animation.AnimationSystem;
import com.noir.game.engine.assets.NoirAssetDatabase;
import com.noir.game.engine.scene.*;
import java.util.*;

/** Shared state for all mobile editor panels. No panel owns the scene; every panel
 * reads and mutates this model so selection, inspector, animation and script views stay synchronized. */
public final class EditorState {
    public enum Tool { SELECT,MOVE,ROTATE,SCALE,CREATE }
    public enum Panel { VIEWPORT,SCENE,ASSETS,INSPECTOR,ANIMATION,SCRIPT,CONSOLE,SHADER,PHYSICS }
    public final NoirScene scene;
    public final NoirAssetDatabase assets=new NoirAssetDatabase();
    public final AnimationSystem animations=new AnimationSystem();
    public NoirNode selected;
    public Tool tool=Tool.SELECT;
    public Panel panel=Panel.VIEWPORT;
    public boolean playing;
    public boolean grid=true;
    public boolean snapping=true;
    public float snapStep=0.25f;
    public final List<String> console=new ArrayList<>();
    public String scriptSource="";
    public String scriptPath="scripts/player.game";
    public EditorState(NoirScene s){scene=s;selected=s.selectedFallback();log("Noir editor state created");}
    public void select(NoirNode n){if(n!=null&&!n.locked){selected=n;log("Selected "+n.name);}}
    public void log(String s){console.add("[Noir] "+s);while(console.size()>120)console.remove(0);}
}
