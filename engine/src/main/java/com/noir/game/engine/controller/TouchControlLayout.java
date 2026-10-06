package com.noir.game.engine.controller;

import java.util.*;

/** Resolution-independent touch control layout with safe-area support and hit testing. */
public final class TouchControlLayout {
    public float designWidth=1920,designHeight=1080,safeLeft=.02f,safeRight=.02f,safeTop=.02f,safeBottom=.03f;
    public final List<Control> controls=new ArrayList<>();
    public Control add(String id,String action,float x,float y,float w,float h){Control c=new Control(id,action,x,y,w,h);controls.add(c);return c;}
    public Control hit(float nx,float ny){for(int i=controls.size()-1;i>=0;i--)if(controls.get(i).contains(nx,ny))return controls.get(i);return null;}
    public static final class Control {public final String id,action;public float x,y,w,h,opacity=0.82f,scale=1f;public String visual="circle";public Control(String id,String action,float x,float y,float w,float h){this.id=id;this.action=action;this.x=x;this.y=y;this.w=w;this.h=h;}boolean contains(float px,float py){return px>=x&&py>=y&&px<=x+w&&py<=y+h;}}
}
