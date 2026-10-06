package com.noir.game.engine.editor;

import com.noir.game.engine.animation.AnimationSystem;
import java.util.*;

/** Editor-facing timeline model: clip management, track selection, key creation,
 * frame snapping, duration changes and display timecode. Rendering of the timeline
 * remains separate so the same animation data can power a future desktop editor. */
public final class AnimationTimelineModel {
    public final List<AnimationSystem.Clip> clips=new ArrayList<>();
    public AnimationSystem.Clip clip;
    public int selectedTrack=-1;
    public float playhead;
    public boolean autoKey=true;
    public boolean loop=true;
    public AnimationTimelineModel(){clip=new AnimationSystem.Clip("Player_Idle",4.0f);clip.loop=true;clips.add(clip);AnimationSystem.Track pos=new AnimationSystem.Track("Player/position");pos.add(new AnimationSystem.Key(0,0,1.7f,0));pos.add(new AnimationSystem.Key(2,0,1.72f,0));pos.add(new AnimationSystem.Key(4,0,1.7f,0));clip.tracks.add(pos);}
    public void newClip(String name,float duration){clip=new AnimationSystem.Clip(name,Math.max(.01f,duration));clip.loop=loop;clips.add(clip);selectedTrack=-1;playhead=0;}
    public void addTrack(String path){if(clip==null)return;clip.tracks.add(new AnimationSystem.Track(path));selectedTrack=clip.tracks.size()-1;}
    public void removeTrack(int index){if(clip==null||index<0||index>=clip.tracks.size())return;clip.tracks.remove(index);if(selectedTrack>=clip.tracks.size())selectedTrack=clip.tracks.size()-1;}
    public void addKey(float time,float...value){if(clip==null||selectedTrack<0||selectedTrack>=clip.tracks.size())return;float t=snap(Math.max(0,Math.min(clip.duration,time)));clip.tracks.get(selectedTrack).add(new AnimationSystem.Key(t,value));playhead=t;}
    public void scrub(float normalized){if(clip==null)return;playhead=Math.max(0,Math.min(1,normalized))*clip.duration;}
    public float snap(float time){float step=1f/Math.max(1,clip==null?30:Math.round(clip.fps));return Math.round(time/step)*step;}
    public String timecode(float t){return String.format(Locale.US,"%02d:%05.2f",(int)(t/60f),t%60f);}
}
