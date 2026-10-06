package com.noir.game.engine.particles;

import java.util.*;

/**
 * Particle authoring/runtime model. The editor stores emitters, modules and curves;
 * GPU renderers can consume the same immutable configuration without changing scene files.
 */
public final class ParticleSystem {
    public int maxParticles=4096; public float lifetime=2f, emissionRate=120f;
    public boolean gpuPreferred=true, localSpace=false, depthCollision=false, softParticles=true;
    public final List<Emitter> emitters=new ArrayList<>();
    public final List<Module> modules=new ArrayList<>();
    public Emitter addEmitter(String name){Emitter e=new Emitter(name);emitters.add(e);return e;}
    public Module addModule(String type){Module m=new Module(type);modules.add(m);return m;}
    public int estimatedParticlesPerSecond(){return Math.max(0,Math.round(emissionRate));}
    public static final class Emitter{public final String name;public String material="particles/default";public float speed=2f,spread=25f,size=.15f;public boolean enabled=true;Emitter(String n){name=n;}}
    public static final class Module{public final String type;public boolean enabled=true;public final Map<String,Float> parameters=new LinkedHashMap<>();Module(String t){type=t;}}
}
