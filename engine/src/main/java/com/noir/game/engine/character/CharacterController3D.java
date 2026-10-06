package com.noir.game.engine.character;

import java.util.*;

/** Deterministic mobile-friendly character controller state. Physics backends consume this state. */
public final class CharacterController3D {
    public enum Stance { STAND, CROUCH, PRONE }
    public float speed=5f, sprintSpeed=7.5f, acceleration=24f, airControl=0.25f, jumpVelocity=4.5f;
    public float gravity=-9.81f, capsuleRadius=0.35f, capsuleHeight=1.8f;
    public boolean grounded, sprinting, jumpQueued;
    public Stance stance=Stance.STAND;
    public float x,y,z,vx,vy,vz;
    public final float[] moveInput=new float[2];
    public void setMoveInput(float x,float y){moveInput[0]=clamp(x,-1,1);moveInput[1]=clamp(y,-1,1);}
    public void queueJump(){jumpQueued=true;}
    public void setStance(Stance s){stance=Objects.requireNonNull(s);}
    public void tick(float dt){
        dt=Math.max(0,Math.min(dt,0.05f));
        float len=(float)Math.sqrt(moveInput[0]*moveInput[0]+moveInput[1]*moveInput[1]);
        float ix=len>1?moveInput[0]/len:moveInput[0], iz=len>1?moveInput[1]/len:moveInput[1];
        float target= sprinting?sprintSpeed:speed;
        float control=grounded?1f:airControl;
        vx=approach(vx,ix*target,acceleration*control*dt);
        vz=approach(vz,iz*target,acceleration*control*dt);
        if(jumpQueued && grounded){vy=jumpVelocity;grounded=false;} jumpQueued=false;
        vy+=gravity*dt; x+=vx*dt;y+=vy*dt;z+=vz*dt;
        if(y<0){y=0;vy=0;grounded=true;}
    }
    private static float approach(float a,float b,float d){if(Math.abs(b-a)<=d)return b;return a+Math.copySign(d,b-a);}
    private static float clamp(float v,float a,float b){return Math.max(a,Math.min(b,v));}
}
