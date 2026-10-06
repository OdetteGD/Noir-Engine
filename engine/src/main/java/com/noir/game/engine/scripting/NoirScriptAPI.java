package com.noir.game.engine.scripting;

import com.noir.game.engine.render.NoirRenderer;
import com.noir.game.engine.scene.NoirNode;
import java.util.*;

/** Stable high-level API exposed to Noir .game scripts. */
public final class NoirScriptAPI {
    private NoirScriptAPI(){}

    public static final class Input {
        public float moveX,moveY,lookX,lookY;
        private final Set<String> actions=new HashSet<>();
        public boolean pressed(String action){return actions.contains(action);}
        public void set(String action,boolean down){if(down)actions.add(action);else actions.remove(action);}
        public void clearFrameLook(){lookX=lookY=0;}
    }

    public static final class Transform {
        public static void position(NoirNode n,float x,float y,float z){n.px=x;n.py=y;n.pz=z;}
        public static void translate(NoirNode n,float x,float y,float z){n.px+=x;n.py+=y;n.pz+=z;}
        public static void rotation(NoirNode n,float pitch,float yaw,float roll){n.rx=pitch;n.ry=yaw;n.rz=roll;}
        public static void scale(NoirNode n,float x,float y,float z){n.sx=x;n.sy=y;n.sz=z;}
    }

    public static final class Character {
        public static void move(NoirNode n,float x,float y,float z,float speed,float dt){
            n.px+=x*speed*dt;n.py+=y*speed*dt;n.pz+=z*speed*dt;
        }
        public static void gravity(NoirNode n,float[] velocity,float dt){
            velocity[1]-=9.81f*dt;n.py+=velocity[1]*dt;
            if(n.py<0){n.py=0;velocity[1]=0;}
        }
    }

    public static final class Camera {
        public static void look(NoirRenderer renderer,float dx,float dy){
            renderer.runtimeLook(dx,dy);
        }
        public static void move(NoirRenderer renderer,float forward,float strafe,float dt){
            renderer.runtimeMove(forward,strafe,dt);
        }
    }

    public static final class Debug {
        public interface Sink { void log(String level,String message); }
        private static Sink sink;
        public static void setSink(Sink s){sink=s;}
        public static void log(String m){if(sink!=null)sink.log("INFO",m);}
        public static void warn(String m){if(sink!=null)sink.log("WARN",m);}
        public static void error(String m){if(sink!=null)sink.log("ERROR",m);}
    }

    public static final class Time {
        public static float delta;
        public static float elapsed;
    }
}
