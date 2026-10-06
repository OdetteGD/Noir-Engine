package com.noir.game.engine.editor;

import com.noir.game.engine.scene.NoirNode;
import java.util.*;

public final class SceneTreeModel {
    private final NoirNode root;
    public SceneTreeModel(NoirNode root){this.root=root;}
    public List<NoirNode> visible(){List<NoirNode> out=new ArrayList<>();flatten(root,out);return out;}
    private void flatten(NoirNode n,List<NoirNode> out){out.add(n);for(NoirNode c:n.children)flatten(c,out);}
    public NoirNode addChild(NoirNode parent,String name,NoirNode.Kind kind){String id=name+"_"+System.nanoTime();return parent.add(new NoirNode(id,name,kind));}
    public boolean delete(NoirNode node){if(node==null||node.parent==null)return false;return node.parent.remove(node);}
    public void duplicate(NoirNode node){if(node==null||node.parent==null)return;NoirNode c=copy(node);c.name=node.name+"_Copy";node.parent.add(c);}
    private NoirNode copy(NoirNode n){NoirNode c=new NoirNode(n.id+"_copy",n.name,n.kind);c.setTransform(n.px,n.py,n.pz,n.rx,n.ry,n.rz,n.sx);c.sy=n.sy;c.sz=n.sz;c.properties.putAll(n.properties);for(NoirNode x:n.children)c.add(copy(x));return c;}
}
