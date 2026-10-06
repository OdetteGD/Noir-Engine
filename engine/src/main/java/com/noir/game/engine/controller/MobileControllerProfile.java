package com.noir.game.engine.controller;

import java.util.*;

/** Complete data model for touch, tilt and gamepad control profiles used by the mobile editor. */
public final class MobileControllerProfile {
    public String name="Noir FPS Mobile";
    public boolean leftStick=true,rightStick=true,buttons=true,gyro=false,gamepad=true;
    public float moveDeadzone=.12f,lookDeadzone=.08f,lookSensitivity=1.0f,aimSensitivity=.65f;
    public final Map<String,Action> actions=new LinkedHashMap<>();
    public MobileControllerProfile(){
        add("move"); add("look"); add("jump"); add("crouch"); add("sprint"); add("interact"); add("fire"); add("aim"); add("reload"); add("pause");
    }
    public Action add(String id){Action a=new Action(id);actions.put(id,a);return a;}
    public static final class Action { public final String id; public float x,y,w,h; public String icon=""; public boolean hold,toggle;
        Action(String id){this.id=id;}
        public Action rect(float x,float y,float w,float h){this.x=x;this.y=y;this.w=w;this.h=h;return this;}
    }
}
