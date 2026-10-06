package com.noir.game.engine.editor;

import com.noir.game.engine.scene.NoirNode;
import java.util.*;

/**
 * Inspector data adapter. UI code consumes neutral fields instead of reaching into
 * scene nodes directly, which makes the inspector replaceable with a desktop dock
 * without changing the scene graph. Numeric transform edits use explicit parsing.
 */
public final class InspectorModel {
    public static final class Field {
        public final String group,key,value;
        public Field(String group,String key,String value){this.group=group;this.key=key;this.value=value;}
    }
    public List<Field> fields(NoirNode n){
        List<Field> f=new ArrayList<>(); if(n==null)return f;
        f.add(new Field("Node","Name",n.name));
        f.add(new Field("Node","Type",n.kind.name()));
        f.add(new Field("Node","Visible",Boolean.toString(n.visible)));
        f.add(new Field("Node","Locked",Boolean.toString(n.locked)));
        f.add(new Field("Transform","Position",vec(n.px,n.py,n.pz,2)));
        f.add(new Field("Transform","Rotation",vec(n.rx,n.ry,n.rz,1)));
        f.add(new Field("Transform","Scale",vec(n.sx,n.sy,n.sz,2)));
        for(Map.Entry<String,String> e:n.properties.entrySet())f.add(new Field("Properties",e.getKey(),e.getValue()));
        return f;
    }
    public void nudge(NoirNode n,float x,float y,float z){if(n==null||n.locked)return;n.px+=x;n.py+=y;n.pz+=z;}
    public void rotate(NoirNode n,float x,float y,float z){if(n==null||n.locked)return;n.rx+=x;n.ry+=y;n.rz+=z;}
    public void scale(NoirNode n,float x,float y,float z){if(n==null||n.locked)return;n.sx*=x;n.sy*=y;n.sz*=z;}
    public void setPosition(NoirNode n,String value){float[]v=parse(value);if(n!=null&&v!=null&&!n.locked){n.px=v[0];n.py=v[1];n.pz=v[2];}}
    public void setRotation(NoirNode n,String value){float[]v=parse(value);if(n!=null&&v!=null&&!n.locked){n.rx=v[0];n.ry=v[1];n.rz=v[2];}}
    public void setScale(NoirNode n,String value){float[]v=parse(value);if(n!=null&&v!=null&&!n.locked){n.sx=v[0];n.sy=v[1];n.sz=v[2];}}
    public void toggleVisible(NoirNode n){if(n!=null)n.visible=!n.visible;}
    public void toggleLock(NoirNode n){if(n!=null)n.locked=!n.locked;}
    private String vec(float a,float b,float c,int digits){return String.format(Locale.US,"%1$."+digits+"f, %2$."+digits+"f, %3$."+digits+"f",a,b,c);}
    private float[] parse(String s){try{String q=s.replace("("," ").replace(")"," ").replace(","," ").trim();String[]p=q.split("\\s+");if(p.length!=3)return null;return new float[]{Float.parseFloat(p[0]),Float.parseFloat(p[1]),Float.parseFloat(p[2])};}catch(Exception e){return null;}}
}
