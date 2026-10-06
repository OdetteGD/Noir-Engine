package com.noir.game.engine.animation;

import java.util.*;

/** Keyframe animation data model used by the mobile animation maker. Supports
 * transform/property tracks, looping, interpolation and editor scrubbing. */
public final class AnimationSystem {
    public enum Interpolation { STEP, LINEAR, CUBIC }
    public static final class Key { public float time; public float[] value; public Interpolation interpolation=Interpolation.LINEAR; public Key(float t,float...v){time=t;value=v;} }
    public static final class Track { public String path; public final List<Key> keys=new ArrayList<>(); public Track(String p){path=p;} public void add(Key k){keys.add(k);keys.sort(Comparator.comparingDouble(x->x.time));} }
    public static final class Clip { public String name; public float duration; public float fps=30; public boolean loop=true; public final List<Track> tracks=new ArrayList<>(); public Clip(String n,float d){name=n;duration=d;} }
    private Clip current; private float time;
    public void setClip(Clip clip){current=clip;time=0;}
    public void seek(float t){if(current==null)return; time=Math.max(0,Math.min(current.duration,t));}
    public void update(float dt){if(current==null)return;time+=dt;if(current.loop&&current.duration>0)time%=current.duration;else time=Math.min(time,current.duration);}
    public float getTime(){return time;}
    public float[] sample(Track track){if(track.keys.isEmpty())return new float[0]; if(time<=track.keys.get(0).time)return track.keys.get(0).value; if(time>=track.keys.get(track.keys.size()-1).time)return track.keys.get(track.keys.size()-1).value; Key a=track.keys.get(0),b=track.keys.get(1);for(int i=1;i<track.keys.size();i++){if(time<=track.keys.get(i).time){a=track.keys.get(i-1);b=track.keys.get(i);break;}}float f=(time-a.time)/(b.time-a.time);if(a.interpolation==Interpolation.STEP)return a.value;float[] out=new float[Math.min(a.value.length,b.value.length)];for(int i=0;i<out.length;i++)out[i]=a.value[i]+(b.value[i]-a.value[i])*f;return out;}
}
