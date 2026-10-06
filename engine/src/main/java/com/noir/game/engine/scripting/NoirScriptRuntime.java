package com.noir.game.engine.scripting;

import com.noir.game.engine.render.NoirRenderer;
import com.noir.game.engine.scene.NoirNode;
import java.util.*;

/**
 * Deterministic script host for the built-in mobile controller API.
 * Custom .game scripts can bind to these same primitives without coupling to Android UI.
 */
public final class NoirScriptRuntime {
    private final Map<String,NoirScriptComponent> components=new LinkedHashMap<>();
    private final Map<String,float[]> velocities=new HashMap<>();
    private final NoirScriptAPI.Input input=new NoirScriptAPI.Input();
    private final NoirRenderer renderer;
    private float elapsed;

    public NoirScriptRuntime(NoirRenderer renderer){this.renderer=renderer;}

    public NoirScriptAPI.Input input(){return input;}

    public NoirScriptComponent attach(NoirNode target,String scriptPath){
        if(target==null)throw new IllegalArgumentException("target node is null");
        NoirScriptComponent c=new NoirScriptComponent(target,scriptPath);
        components.put(target.id,c);
        target.properties.put("script",scriptPath);
        return c;
    }

    public void detach(NoirNode target){
        if(target==null)return;
        components.remove(target.id);
        velocities.remove(target.id);
        target.properties.remove("script");
    }

    public Collection<NoirScriptComponent> attachments(){return Collections.unmodifiableCollection(components.values());}

    public void update(float dt){
        elapsed+=dt;
        NoirScriptAPI.Time.delta=dt;
        NoirScriptAPI.Time.elapsed=elapsed;

        for(NoirScriptComponent c:components.values()){
            if(!c.enabled)continue;
            NoirNode n=c.target;
            if(n.kind==NoirNode.Kind.CHARACTER3D || n.kind==NoirNode.Kind.PLAYER3D){
                float[] v=velocities.computeIfAbsent(n.id,k->new float[3]);
                float len=(float)Math.sqrt(input.moveX*input.moveX+input.moveY*input.moveY);
                float mx=input.moveX,my=input.moveY;
                if(len>1f){mx/=len;my/=len;}
                float speed=parseFloat(n.properties.get("speed"),5f);
                float yaw=(float)Math.toRadians(renderer.runtimeCamera().yaw);
                float fx=(float)Math.cos(yaw),fz=(float)Math.sin(yaw);
                v[0]=(fx*my-fz*mx)*speed;
                v[2]=(fz*my+fx*mx)*speed;
                NoirScriptAPI.Character.move(n,v[0],0,v[2],1f,dt);
                NoirScriptAPI.Character.gravity(n,v,dt);
                if(input.pressed("jump")&&n.py<=0.001f)v[1]=parseFloat(n.properties.get("jump"),4.5f);
            }
        }
    }

    private float parseFloat(String s,float fallback){
        if(s==null)return fallback;
        try{return Float.parseFloat(s);}catch(Exception e){return fallback;}
    }
}
