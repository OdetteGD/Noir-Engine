package com.noir.game.engine.physics;

import java.util.*;

/**
 * Deterministic mobile 3D physics core.
 * Uses swept-style fixed-step integration, gravity, damping, ground contacts,
 * static AABB contacts and ray queries. The scene/editor API stays backend-neutral.
 */
public final class PhysicsWorld {
    public static final class Body {
        public final int id;
        public float x,y,z;
        public float radius=0.5f;
        public float halfX=0.5f,halfY=0.5f,halfZ=0.5f;
        public float vx,vy,vz;
        public float mass=1f;
        public float damping=0.04f;
        public boolean dynamic=true;
        public boolean grounded;
        Body(int i){id=i;}
    }
    public static final class StaticBox {
        public final float minX,minY,minZ,maxX,maxY,maxZ;
        public StaticBox(float x,float y,float z,float hx,float hy,float hz){
            minX=x-hx;maxX=x+hx;minY=y-hy;maxY=y+hy;minZ=z-hz;maxZ=z+hz;
        }
        boolean contains(float x,float y,float z,float r){
            return x+r>minX&&x-r<maxX&&y+r>minY&&y-r<maxY&&z+r>minZ&&z-r<maxZ;
        }
    }

    private final List<Body> bodies=new ArrayList<>();
    private final List<StaticBox> staticBoxes=new ArrayList<>();
    private float accumulator;
    private float fixedStep=1f/60f;
    private float gravity=-9.81f;

    public Body create(int id){Body b=new Body(id);bodies.add(b);return b;}
    public StaticBox createStaticBox(float x,float y,float z,float hx,float hy,float hz){
        StaticBox b=new StaticBox(x,y,z,hx,hy,hz);staticBoxes.add(b);return b;
    }
    public void setGravity(float g){gravity=g;}
    public float gravity(){return gravity;}
    public void setFixedStep(float seconds){fixedStep=Math.max(1f/240f,Math.min(1f/20f,seconds));}

    public void step(float dt){
        accumulator+=Math.min(dt,0.1f);
        while(accumulator>=fixedStep){integrate(fixedStep);accumulator-=fixedStep;}
    }

    private void integrate(float dt){
        for(Body b:bodies){
            if(!b.dynamic)continue;
            b.grounded=false;
            b.vy+=gravity*dt;
            float damp=Math.max(0f,1f-b.damping*dt*60f);
            b.vx*=damp;b.vz*=damp;
            b.x+=b.vx*dt;b.y+=b.vy*dt;b.z+=b.vz*dt;

            if(b.y-b.halfY<0f){
                b.y=b.halfY;b.vy=0f;b.grounded=true;
            }
            for(StaticBox box:staticBoxes){
                if(!box.contains(b.x,b.y,b.z,Math.max(b.halfX,Math.max(b.halfY,b.halfZ))))continue;
                float pushTop=box.maxY+b.halfY;
                if(b.y>=box.maxY){
                    b.y=pushTop;
                    if(b.vy<0)b.vy=0;
                    b.grounded=true;
                }else{
                    float left=Math.abs((box.minX-b.x)-b.halfX);
                    float right=Math.abs((box.maxX-b.x)+b.halfX);
                    float front=Math.abs((box.minZ-b.z)-b.halfZ);
                    float back=Math.abs((box.maxZ-b.z)+b.halfZ);
                    float min=Math.min(Math.min(left,right),Math.min(front,back));
                    if(min==left)b.x=box.minX-b.halfX;
                    else if(min==right)b.x=box.maxX+b.halfX;
                    else if(min==front)b.z=box.minZ-b.halfZ;
                    else b.z=box.maxZ+b.halfZ;
                }
            }
        }
    }

    public int raycastCount(float ox,float oy,float oz,float dx,float dy,float dz,float max){
        float len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);
        if(len<0.0001f)return 0;
        dx/=len;dy/=len;dz/=len;
        int hits=0;
        for(Body b:bodies){
            float t=(b.x-ox)*dx+(b.y-oy)*dy+(b.z-oz)*dz;
            if(t<0||t>max)continue;
            float px=ox+dx*t,py=oy+dy*t,pz=oz+dz*t;
            float d=(b.x-px)*(b.x-px)+(b.y-py)*(b.y-py)+(b.z-pz)*(b.z-pz);
            if(d<=b.radius*b.radius)hits++;
        }
        for(StaticBox box:staticBoxes){
            float t=0;
            for(int i=0;i<120;i++){
                float q=max*i/119f;
                float x=ox+dx*q,y=oy+dy*q,z=oz+dz*q;
                if(x>=box.minX&&x<=box.maxX&&y>=box.minY&&y<=box.maxY&&z>=box.minZ&&z<=box.maxZ){hits++;break;}
            }
        }
        return hits;
    }

    public List<Body> bodies(){return Collections.unmodifiableList(bodies);}
    public List<StaticBox> staticBoxes(){return Collections.unmodifiableList(staticBoxes);}
}
