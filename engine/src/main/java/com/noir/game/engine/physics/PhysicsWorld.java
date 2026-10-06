package com.noir.game.engine.physics;

import java.util.*;

/** Lightweight deterministic broad-phase used by the editor preview. Production
 * physics backends can implement the same contract without changing scene/editor code. */
public final class PhysicsWorld {
    public static final class Body { public int id; public float x,y,z; public float radius=0.5f; public boolean dynamic=true; public float vx,vy,vz; public Body(int i){id=i;} }
    private final List<Body> bodies=new ArrayList<>();
    public Body create(int id){Body b=new Body(id);bodies.add(b);return b;}
    public void step(float dt){for(Body b:bodies)if(b.dynamic){b.vy-=9.81f*dt;b.x+=b.vx*dt;b.y+=b.vy*dt;b.z+=b.vz*dt;if(b.y<b.radius){b.y=b.radius;b.vy=0;}}}
    public int raycastCount(float ox,float oy,float oz,float dx,float dy,float dz,float max){int hits=0;float len=(float)Math.sqrt(dx*dx+dy*dy+dz*dz);if(len<0.0001f)return 0;dx/=len;dy/=len;dz/=len;for(Body b:bodies){float t=(b.x-ox)*dx+(b.y-oy)*dy+(b.z-oz)*dz;if(t<0||t>max)continue;float px=ox+dx*t,py=oy+dy*t,pz=oz+dz*t;float d=(b.x-px)*(b.x-px)+(b.y-py)*(b.y-py)+(b.z-pz)*(b.z-pz);if(d<=b.radius*b.radius)hits++;}return hits;}
    public List<Body> bodies(){return Collections.unmodifiableList(bodies);}
}
