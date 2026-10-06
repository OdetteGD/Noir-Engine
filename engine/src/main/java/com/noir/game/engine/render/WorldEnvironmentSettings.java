package com.noir.game.engine.render;

/** Environment/sky/atmosphere configuration consumed by the 3D renderer and editor inspector. */
public final class WorldEnvironmentSettings {
    public enum SkyMode{COLOR,GRADIENT,CUBEMAP,PROCEDURAL_SKY,HDRI}
    public SkyMode skyMode=SkyMode.PROCEDURAL_SKY; public String skyAsset="";
    public float skyBrightness=1f,sunEnergy=2f,ambientEnergy=.7f,fogDensity=.008f,fogHeight=12f;
    public boolean fogEnabled=true,volumetricEnabled=false,tonemap=true,autoExposure=false;
    public float exposure=1f,whitePoint=1f,cloudCoverage=.15f,cloudDensity=.2f;
    public final float[] horizon={.32f,.40f,.55f,1f},zenith={.04f,.08f,.16f,1f};
    public void setSkyMode(SkyMode mode){skyMode=mode;}
    public void setFog(float density,float height){fogDensity=Math.max(0,density);fogHeight=Math.max(0,height);fogEnabled=fogDensity>0;}
    public void setExposure(float value){exposure=Math.max(.01f,Math.min(8f,value));}
    public void setSkyBrightness(float value){skyBrightness=Math.max(0f,Math.min(16f,value));}
    public void setAmbientEnergy(float value){ambientEnergy=Math.max(0f,Math.min(8f,value));}
    public void setSunEnergy(float value){sunEnergy=Math.max(0f,Math.min(32f,value));}
    public void setClouds(float coverage,float density){cloudCoverage=Math.max(0f,Math.min(1f,coverage));cloudDensity=Math.max(0f,Math.min(1f,density));}
    public boolean valid(){return skyBrightness>=0&&sunEnergy>=0&&ambientEnergy>=0&&fogDensity>=0&&fogHeight>=0&&exposure>0;}
    public float ambientLuminance(){return ambientEnergy*skyBrightness;}
}
