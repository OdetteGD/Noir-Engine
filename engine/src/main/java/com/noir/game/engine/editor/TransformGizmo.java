package com.noir.game.engine.editor;

import com.noir.game.engine.render.NoirRenderer;
import com.noir.game.engine.scene.NoirNode;
import java.util.Locale;

/**
 * Touch-first 3D transform gizmo.
 * X/Y/Z are real world axes; movement uses a camera-facing constraint plane,
 * rotation uses the selected axis plane, and scale uses the projected handle.
 */
public final class TransformGizmo {
    public enum Axis { NONE, X, Y, Z }

    private final float[] axis = new float[3];
    private final float[] startPos = new float[3];
    private final float[] startScale = new float[3];
    private float startRotation;
    private float startAxisParameter;
    private float startAngle;
    private float startRadius;
    private float centerX, centerY;
    private Axis active = Axis.NONE;
    private EditorState.Tool tool = EditorState.Tool.SELECT;
    private boolean dragging;

    public Axis activeAxis(){ return active; }
    public boolean dragging(){ return dragging; }

    public void cancel(){ active=Axis.NONE; dragging=false; }

    public boolean begin(NoirRenderer renderer, NoirNode node, EditorState.Tool selectedTool,
                         float sx, float sy, float hitRadiusPx) {
        cancel();
        if(node==null || selectedTool==EditorState.Tool.SELECT) return false;
        float[] center=renderer.projectWorldToScreen(node.px,node.py,node.pz);
        if(center==null) return false;
        centerX=center[0]; centerY=center[1];
        float size=renderer.gizmoWorldSize();

        Axis best=Axis.NONE;
        float bestDistance=Float.MAX_VALUE;
        if(selectedTool==EditorState.Tool.ROTATE){
            for(Axis candidate:new Axis[]{Axis.X,Axis.Y,Axis.Z}){
                float d=ringDistance(renderer,node,candidate,size,sx,sy);
                if(d<bestDistance){bestDistance=d;best=candidate;}
            }
            if(bestDistance>hitRadiusPx) return false;
        } else {
            for(Axis candidate:new Axis[]{Axis.X,Axis.Y,Axis.Z}){
                float d=axisDistance(renderer,node,candidate,size,sx,sy);
                if(d<bestDistance){bestDistance=d;best=candidate;}
            }
            if(bestDistance>hitRadiusPx) return false;
        }

        active=best;
        tool=selectedTool;
        axisFor(best,axis);
        startPos[0]=node.px;startPos[1]=node.py;startPos[2]=node.pz;
        startScale[0]=node.sx;startScale[1]=node.sy;startScale[2]=node.sz;
        startRotation=rotationFor(node,best);

        if(tool==EditorState.Tool.MOVE){
            float[] hit=axisConstraintHit(renderer,sx,sy,startPos,axis);
            if(hit==null){cancel();return false;}
            startAxisParameter=dot(sub(hit,startPos),axis);
        } else if(tool==EditorState.Tool.ROTATE){
            float[] hit=axisPlaneHit(renderer,sx,sy,startPos,axis);
            if(hit==null){cancel();return false;}
            float[] v=sub(hit,startPos);
            float[] b1=rotationBasisA(best),b2=rotationBasisB(best);
            startAngle=(float)Math.atan2(dot(v,b2),dot(v,b1));
        } else if(tool==EditorState.Tool.SCALE){
            startRadius=(float)Math.sqrt((sx-centerX)*(sx-centerX)+(sy-centerY)*(sy-centerY));
            if(startRadius<4f)startRadius=4f;
        }
        dragging=true;
        return true;
    }

    public boolean update(NoirRenderer renderer, NoirNode node, float sx, float sy){
        if(!dragging||node==null)return false;
        if(tool==EditorState.Tool.MOVE){
            float[] hit=axisConstraintHit(renderer,sx,sy,startPos,axis);
            if(hit==null)return false;
            float parameter=dot(sub(hit,startPos),axis);
            float delta=parameter-startAxisParameter;
            node.px=startPos[0]+axis[0]*delta;
            node.py=startPos[1]+axis[1]*delta;
            node.pz=startPos[2]+axis[2]*delta;
            snapPosition(node);
            return true;
        }
        if(tool==EditorState.Tool.ROTATE){
            float[] hit=axisPlaneHit(renderer,sx,sy,startPos,axis);
            if(hit==null)return false;
            float[] v=sub(hit,startPos);
            float[] b1=rotationBasisA(active),b2=rotationBasisB(active);
            float angle=(float)Math.atan2(dot(v,b2),dot(v,b1));
            float delta=normalizeAngle(angle-startAngle);
            setRotation(node,active,startRotation+(float)Math.toDegrees(delta));
            return true;
        }
        if(tool==EditorState.Tool.SCALE){
            float radius=(float)Math.sqrt((sx-centerX)*(sx-centerX)+(sy-centerY)*(sy-centerY));
            float factor=1f+(radius-startRadius)/(renderer.gizmoPixelSize()*1.15f);
            factor=Math.max(0.05f,Math.min(20f,factor));
            node.sx=Math.max(0.01f,startScale[0]*factor);
            node.sy=Math.max(0.01f,startScale[1]*factor);
            node.sz=Math.max(0.01f,startScale[2]*factor);
            return true;
        }
        return false;
    }

    public void end(){dragging=false;active=Axis.NONE;}

    public String status(){
        return String.format(Locale.US,"%s %s",tool.name(),active.name());
    }

    public static int axisColor(Axis a){
        switch(a){
            case X:return 0xffff4f5f;
            case Y:return 0xff62e88a;
            case Z:return 0xff58a7ff;
            default:return 0xffb7c2d6;
        }
    }

    private float axisDistance(NoirRenderer r,NoirNode n,Axis a,float size,float sx,float sy){
        float[] p=r.projectWorldToScreen(n.px,n.py,n.pz);
        float[] q=r.projectWorldToScreen(n.px+axisComponent(a,0)*size,n.py+axisComponent(a,1)*size,n.pz+axisComponent(a,2)*size);
        if(p==null||q==null)return Float.MAX_VALUE;
        return pointSegmentDistance(sx,sy,p[0],p[1],q[0],q[1]);
    }

    private float ringDistance(NoirRenderer r,NoirNode n,Axis a,float size,float sx,float sy){
        float[] b1=rotationBasisA(a),b2=rotationBasisB(a);
        float previousX=0,previousY=0;
        boolean have=false;
        float best=Float.MAX_VALUE;
        for(int i=0;i<=48;i++){
            float t=(float)(Math.PI*2.0*i/48.0);
            float x=n.px+(b1[0]*(float)Math.cos(t)+b2[0]*(float)Math.sin(t))*size;
            float y=n.py+(b1[1]*(float)Math.cos(t)+b2[1]*(float)Math.sin(t))*size;
            float z=n.pz+(b1[2]*(float)Math.cos(t)+b2[2]*(float)Math.sin(t))*size;
            float[] s=r.projectWorldToScreen(x,y,z);
            if(s==null){have=false;continue;}
            if(have)best=Math.min(best,pointSegmentDistance(sx,sy,previousX,previousY,s[0],s[1]));
            previousX=s[0];previousY=s[1];have=true;
        }
        return best;
    }

    private float[] axisConstraintHit(NoirRenderer r,float sx,float sy,float[] point,float[] a){
        float[] ray=r.screenRay(sx,sy);
        if(ray==null)return null;
        float[] forward=r.cameraForward();
        float[] normal=cross(a,forward);
        if(length(normal)<0.05f)normal=cross(a,new float[]{0,1,0});
        if(length(normal)<0.05f)normal=cross(a,new float[]{1,0,0});
        normalizeInPlace(normal);
        return rayPlane(ray,point,normal);
    }

    private float[] axisPlaneHit(NoirRenderer r,float sx,float sy,float[] point,float[] a){
        float[] ray=r.screenRay(sx,sy);
        if(ray==null)return null;
        return rayPlane(ray,point,a);
    }

    private float[] rayPlane(float[] ray,float[] point,float[] normal){
        float denom=dot(new float[]{ray[3],ray[4],ray[5]},normal);
        if(Math.abs(denom)<0.00001f)return null;
        float[] origin={ray[0],ray[1],ray[2]};
        float t=dot(sub(point,origin),normal)/denom;
        if(t<0f)return null;
        return new float[]{origin[0]+ray[3]*t,origin[1]+ray[4]*t,origin[2]+ray[5]*t};
    }

    private float[] rotationBasisA(Axis a){
        switch(a){
            case X:return new float[]{0,1,0};
            case Y:return new float[]{0,0,1};
            default:return new float[]{1,0,0};
        }
    }
    private float[] rotationBasisB(Axis a){
        switch(a){
            case X:return new float[]{0,0,1};
            case Y:return new float[]{1,0,0};
            default:return new float[]{0,1,0};
        }
    }

    private void axisFor(Axis a,float[] out){
        switch(a){
            case X:out[0]=1;out[1]=0;out[2]=0;break;
            case Y:out[0]=0;out[1]=1;out[2]=0;break;
            default:out[0]=0;out[1]=0;out[2]=1;break;
        }
    }

    private float axisComponent(Axis a,int i){
        if(a==Axis.X)return i==0?1:0;
        if(a==Axis.Y)return i==1?1:0;
        return i==2?1:0;
    }

    private float rotationFor(NoirNode n,Axis a){
        if(a==Axis.X)return n.rx;
        if(a==Axis.Y)return n.ry;
        return n.rz;
    }
    private void setRotation(NoirNode n,Axis a,float v){
        if(a==Axis.X)n.rx=v;
        else if(a==Axis.Y)n.ry=v;
        else n.rz=v;
    }

    private void snapPosition(NoirNode n){
        // The editor applies the scene snap setting outside this low-level math object.
        n.px=clean(n.px);n.py=clean(n.py);n.pz=clean(n.pz);
    }
    private float clean(float v){return Math.abs(v)<0.00001f?0f:v;}

    private float[] sub(float[]a,float[]b){return new float[]{a[0]-b[0],a[1]-b[1],a[2]-b[2]};}
    private float dot(float[]a,float[]b){return a[0]*b[0]+a[1]*b[1]+a[2]*b[2];}
    private float[] cross(float[]a,float[]b){return new float[]{a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]};}
    private float length(float[]a){return (float)Math.sqrt(dot(a,a));}
    private void normalizeInPlace(float[]a){float l=length(a);if(l<0.00001f)return;a[0]/=l;a[1]/=l;a[2]/=l;}
    private float normalizeAngle(float a){while(a>Math.PI)a-=2f*(float)Math.PI;while(a<-Math.PI)a+=2f*(float)Math.PI;return a;}
    private float pointSegmentDistance(float px,float py,float ax,float ay,float bx,float by){
        float dx=bx-ax,dy=by-ay;float len=dx*dx+dy*dy;
        if(len<0.0001f)return (float)Math.hypot(px-ax,py-ay);
        float t=((px-ax)*dx+(py-ay)*dy)/len;t=Math.max(0f,Math.min(1f,t));
        return (float)Math.hypot(px-(ax+dx*t),py-(ay+dy*t));
    }
}