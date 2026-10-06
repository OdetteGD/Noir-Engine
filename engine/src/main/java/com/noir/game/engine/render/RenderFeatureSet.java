package com.noir.game.engine.render;

import java.util.*;

/** Mobile render feature matrix. Each option has a performance-safe default and can be edited in the renderer panel. */
public final class RenderFeatureSet {
    public boolean pbr=true,normalMaps=true,shadowMaps=true,softShadows=true,ibl=true,reflectionProbes=true;
    public boolean ssao=true,bloom=false,chromaticAberration=false,fog=true,volumetricFog=false,decals=true;
    public boolean particles=true,water=true,terrainLod=true,occlusionCulling=true,gpuInstancing=true,skinning=true;
    public boolean taa=false,msaa=true,wireframeDebug=false;
    public int shadowMapSize=2048,msaaSamples=2,maxLightsPerObject=8;
    public float renderScale=1.0f,environmentIntensity=1.0f,exposure=1.0f;
    public enum Quality{PERFORMANCE,BALANCED,QUALITY,ULTRA}
    public Quality quality=Quality.BALANCED;
    public void apply(Quality q){quality=q;switch(q){case PERFORMANCE:shadowMapSize=1024;msaaSamples=1;ssao=false;softShadows=false;volumetricFog=false;renderScale=.75f;maxLightsPerObject=4;break;case BALANCED:shadowMapSize=1536;msaaSamples=2;ssao=true;softShadows=true;volumetricFog=false;renderScale=.9f;maxLightsPerObject=6;break;case QUALITY:shadowMapSize=2048;msaaSamples=4;ssao=true;softShadows=true;volumetricFog=true;renderScale=1f;maxLightsPerObject=8;break;case ULTRA:shadowMapSize=4096;msaaSamples=4;ssao=true;softShadows=true;volumetricFog=true;renderScale=1f;maxLightsPerObject=12;break;}}
    public List<String> diagnostics(){List<String> d=new ArrayList<>();if(shadowMapSize>2048)d.add("Large shadow maps may exceed mobile memory");if(volumetricFog&&renderScale<.8f)d.add("Volumetric fog with low render scale may be unstable visually");if(msaaSamples>4)d.add("Unsupported MSAA level");return d;}
}
