package com.noir.game.engine.render;

import java.util.*;

/** Device-tier rendering contract. The editor can expose these values while the renderer
 * maps them to actual GPU resources. Profiles prevent high-end settings from being silently
 * enabled on low-memory Android devices. */
public final class RenderProfile {
    public enum Tier { PERFORMANCE,BALANCED,QUALITY,CUSTOM }
    public Tier tier=Tier.BALANCED;
    public int shadowMap=1024;
    public int msaa=2;
    public int maxLights=32;
    public boolean hdr=true;
    public boolean bloom=false;
    public boolean ssao=true;
    public boolean reflections=true;
    public boolean fog=true;
    public int textureBudgetMb=384;
    public int meshBudgetMb=256;
    public void apply(Tier t){tier=t;switch(t){case PERFORMANCE:shadowMap=512;msaa=0;maxLights=12;hdr=false;bloom=false;ssao=false;reflections=false;textureBudgetMb=192;meshBudgetMb=128;break;case BALANCED:shadowMap=1024;msaa=2;maxLights=24;hdr=true;bloom=false;ssao=true;reflections=true;textureBudgetMb=384;meshBudgetMb=256;break;case QUALITY:shadowMap=2048;msaa=4;maxLights=48;hdr=true;bloom=true;ssao=true;reflections=true;textureBudgetMb=768;meshBudgetMb=512;break;default:break;}}
    public Map<String,String> asMap(){Map<String,String>m=new LinkedHashMap<>();m.put("tier",tier.name());m.put("shadow_map",Integer.toString(shadowMap));m.put("msaa",Integer.toString(msaa));m.put("max_lights",Integer.toString(maxLights));m.put("hdr",Boolean.toString(hdr));m.put("bloom",Boolean.toString(bloom));m.put("ssao",Boolean.toString(ssao));m.put("reflections",Boolean.toString(reflections));m.put("fog",Boolean.toString(fog));return m;}
}
