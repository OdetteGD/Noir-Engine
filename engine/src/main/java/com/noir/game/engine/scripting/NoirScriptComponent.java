package com.noir.game.engine.scripting;

import com.noir.game.engine.scene.NoirNode;

/** Script attachment stored on a target scene node. */
public final class NoirScriptComponent {
    public final NoirNode target;
    public String path;
    public boolean enabled=true;
    public NoirScriptComponent(NoirNode target,String path){this.target=target;this.path=path;}
}
