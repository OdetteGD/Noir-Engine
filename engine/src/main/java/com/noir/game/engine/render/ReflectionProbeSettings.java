package com.noir.game.engine.render;

/** Reflection probe capture/update policy and box-projection parameters. */
public final class ReflectionProbeSettings {
    public int resolution=256; public boolean boxProjection=true,parallaxCorrection=true,convolution=true;
    public float near=.1f,far=50f; public float sizeX=10,sizeY=5,sizeZ=10;
    public enum UpdateMode{ON_LOAD,MANUAL,EVERY_FRAME,INTERVAL} public UpdateMode updateMode=UpdateMode.ON_LOAD;
    public float interval=2f;
    public boolean valid(){return resolution>=32&&resolution<=2048&&near>0&&far>near&&sizeX>0&&sizeY>0&&sizeZ>0;}
    public void clamp(){resolution=Math.max(32,Math.min(2048,resolution));far=Math.max(near+.01f,far);interval=Math.max(.1f,interval);sizeX=Math.max(.1f,sizeX);sizeY=Math.max(.1f,sizeY);sizeZ=Math.max(.1f,sizeZ);}
    public void setBounds(float x,float y,float z){sizeX=Math.max(.1f,x);sizeY=Math.max(.1f,y);sizeZ=Math.max(.1f,z);}
    public void setUpdateMode(UpdateMode mode){updateMode=mode==null?UpdateMode.ON_LOAD:mode;}
    public boolean shouldUpdate(float elapsed,boolean sceneChanged){switch(updateMode){case EVERY_FRAME:return true;case MANUAL:return false;case INTERVAL:return elapsed>=interval;default:return sceneChanged;}}
    public long estimatedBytes(){return (long)resolution*resolution*6*8;}
}
