package com.noir.game.engine.assets;

import java.util.*;

/** Asset database with typed records, import state and stable editor IDs. */
public final class NoirAssetDatabase {
    public enum Type { SCENE, SCRIPT, MODEL, TEXTURE, MATERIAL, ANIMATION, AUDIO, SHADER, UNKNOWN }
    public enum State { DISCOVERED, IMPORTED, FAILED, MISSING }
    public static final class Asset { public String id,path,name; public Type type; public State state=State.DISCOVERED; public long size; public String error=""; public Asset(String i,String p,Type t){id=i;path=p;name=p.substring(p.lastIndexOf('/')+1);type=t;} }
    private final Map<String,Asset> assets=new LinkedHashMap<>();
    public Asset register(String path){String id=Integer.toHexString(path.hashCode());Type t=typeFor(path);Asset a=assets.get(id);if(a==null){a=new Asset(id,path,t);assets.put(id,a);}return a;}
    public Collection<Asset> all(){return Collections.unmodifiableCollection(assets.values());}
    public List<Asset> search(String q){List<Asset> out=new ArrayList<>();String s=q.toLowerCase(Locale.US);for(Asset a:assets.values())if(a.path.toLowerCase(Locale.US).contains(s))out.add(a);return out;}
    private Type typeFor(String p){String s=p.toLowerCase(Locale.US);if(s.endsWith(".game"))return s.contains("script")||s.contains("player")?Type.SCRIPT:Type.SCENE;if(s.endsWith(".glb")||s.endsWith(".gltf")||s.endsWith(".obj"))return Type.MODEL;if(s.endsWith(".png")||s.endsWith(".jpg")||s.endsWith(".ktx2"))return Type.TEXTURE;if(s.endsWith(".anim"))return Type.ANIMATION;if(s.endsWith(".shader")||s.endsWith(".glsl"))return Type.SHADER;if(s.endsWith(".ogg")||s.endsWith(".wav"))return Type.AUDIO;if(s.endsWith(".mat"))return Type.MATERIAL;return Type.UNKNOWN;}
}
