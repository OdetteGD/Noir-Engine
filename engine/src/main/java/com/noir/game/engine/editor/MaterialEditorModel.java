package com.noir.game.engine.editor;

import java.util.*;

/** Node-based PBR material graph model for the mobile Material/Shader Lab. */
public final class MaterialEditorModel {
    public final List<Node> nodes=new ArrayList<>(); public final List<Link> links=new ArrayList<>();
    public String output="PBR_OUTPUT"; public boolean doubleSided=false,transparent=false;
    public Node add(String type,String name,float x,float y){Node n=new Node(type,name,x,y);nodes.add(n);return n;}
    public Link link(String a,String ap,String b,String bp){Link l=new Link(a,ap,b,bp);links.add(l);return l;}
    public boolean removeNode(String name){boolean changed=nodes.removeIf(n->n.name.equals(name));links.removeIf(l->l.from.equals(name)||l.to.equals(name));return changed;}
    public boolean validate(){if(output==null||output.isEmpty())return false;for(Link l:links)if(l.from.isEmpty()||l.to.isEmpty())return false;return true;}
    public static final class Node{public final String type,name;public float x,y;public final Map<String,String> params=new LinkedHashMap<>();Node(String t,String n,float x,float y){type=t;name=n;this.x=x;this.y=y;}}
    public static final class Link{public final String from,fromPort,to,toPort;Link(String f,String fp,String t,String tp){from=f;fromPort=fp;to=t;toPort=tp;}}
}
