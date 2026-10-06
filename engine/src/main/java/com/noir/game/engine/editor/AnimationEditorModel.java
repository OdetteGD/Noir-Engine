package com.noir.game.engine.editor;

import java.util.*;

/** Full animation editor state: clips, tracks, keyframes, layers, markers and onion-skin settings. */
public final class AnimationEditorModel {
    public float fps=30f,currentTime=0f;public boolean autoKey=false,loop=true,onionSkin=false;
    public final List<Clip> clips=new ArrayList<>();public final List<Marker> markers=new ArrayList<>();
    public Clip create(String name,float duration){Clip c=new Clip(name,duration);clips.add(c);return c;}
    public static final class Clip{public final String name;public float duration;public final List<Track> tracks=new ArrayList<>();Clip(String n,float d){name=n;duration=d;}}
    public static final class Track{public String nodePath,property;public final List<Key> keys=new ArrayList<>();Track(String n,String p){nodePath=n;property=p;}}
    public static final class Key{public float time;public String value;Key(float t,String v){time=t;value=v;}}
    public static final class Marker{public float time;public String name;Marker(float t,String n){time=t;name=n;}}
}
