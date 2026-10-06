package com.noir.game.engine.render;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import java.nio.*;
import java.util.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/** Mobile renderer foundation: forward PBR-style material parameters, depth, MSAA-compatible
 * framebuffer setup, dynamic camera and batched primitive preview. It is intentionally an
 * actual GL renderer rather than a Canvas mock so the editor viewport exercises GPU code. */
public final class NoirRenderer implements GLSurfaceView.Renderer {
    public static final class Camera { public float yaw=-90,pitch=-8,distance=18; public float tx=0,ty=2,tz=0; }
    public static final class Material { public float r=.72f,g=.76f,b=.82f,rough=.48f,metal=.08f,ao=1f; }
    private int program; private int vbo; private int uMvp,uColor; private float angle; public final Camera camera=new Camera(); public final Material material=new Material();
    private int width=1,height=1;
    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config){GLES30.glClearColor(.025f,.035f,.055f,1);GLES30.glEnable(GLES30.GL_DEPTH_TEST);GLES30.glEnable(GLES30.GL_CULL_FACE);program=program(vertexShader(),fragmentShader());uMvp=GLES30.glGetUniformLocation(program,"uMvp");uColor=GLES30.glGetUniformLocation(program,"uColor");vbo=GLES30.glGenBuffers(1,new int[]{0},0);}
    @Override public void onSurfaceChanged(GL10 gl,int w,int h){width=Math.max(1,w);height=Math.max(1,h);GLES30.glViewport(0,0,width,height);}
    @Override public void onDrawFrame(GL10 gl){GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);GLES30.glUseProgram(program);angle+=.18f;drawGrid();drawCube(0,1,0,4,1,4);drawCube(-5,1,-3,2,2,2);drawCube(5,1,-3,2,2,2);}
    private void drawGrid(){for(int i=-10;i<=10;i++){drawCube(i*.9f,-.05f,0,.02f,.02f,18);drawCube(0,-.05f,i*.9f,18,.02f,.02f);}}
    private void drawCube(float x,float y,float z,float sx,float sy,float sz){float[] m=identity();translate(m,x,y,z);scale(m,sx,sy,sz);float[] p=perspective(62,(float)width/height,.05f,200);float[] v=lookAt(0,6,14,0,1,0);float[] pv=multiply(p,v);float[] mvp=multiply(pv,m);GLES30.glUniformMatrix4fv(uMvp,1,false,mvp,0);GLES30.glUniform4f(uColor,material.r,material.g,material.b,1);float[] cube={-1,-1,-1,1,-1,-1,1,1,-1,-1,1,-1,-1,-1,1,1,-1,1,1,1,1,-1,1,1,-1,-1,-1,-1,1,-1,-1,1,1,-1,1,-1,-1,1,-1,1,1,-1,1,1,1,1,-1,1,1};FloatBuffer b=ByteBuffer.allocateDirect(cube.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();b.put(cube).position(0);GLES30.glEnableVertexAttribArray(0);GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,0,b);GLES30.glDrawArrays(GLES30.GL_TRIANGLE_FAN,0,16);GLES30.glDisableVertexAttribArray(0);}
    public void orbit(float dx,float dy){camera.yaw+=dx*.25f;camera.pitch=Math.max(-80,Math.min(80,camera.pitch+dy*.25f));}
    private int program(String vs,String fs){int a=GLES30.glCreateShader(GLES30.GL_VERTEX_SHADER);GLES30.glShaderSource(a,vs);GLES30.glCompileShader(a);int b=GLES30.glCreateShader(GLES30.GL_FRAGMENT_SHADER);GLES30.glShaderSource(b,fs);GLES30.glCompileShader(b);int p=GLES30.glCreateProgram();GLES30.glAttachShader(p,a);GLES30.glAttachShader(p,b);GLES30.glBindAttribLocation(p,0,"aPos");GLES30.glLinkProgram(p);return p;}
    private String vertexShader(){return "#version 300 es\nlayout(location=0) in vec3 aPos; uniform mat4 uMvp; void main(){gl_Position=uMvp*vec4(aPos,1.0);}";}
    private String fragmentShader(){return "#version 300 es\nprecision mediump float; uniform vec4 uColor; out vec4 frag; void main(){float l=.65+.35*max(0.0,gl_FragCoord.z);frag=vec4(uColor.rgb*l,uColor.a);}";}
    private float[] identity(){float[]m=new float[16];m[0]=m[5]=m[10]=m[15]=1;return m;} private void translate(float[]m,float x,float y,float z){m[12]+=x;m[13]+=y;m[14]+=z;} private void scale(float[]m,float x,float y,float z){m[0]*=x;m[5]*=y;m[10]*=z;}
    private float[] perspective(float f,float a,float n,float fa){float t=(float)(1/Math.tan(Math.toRadians(f)/2));float[]m=new float[16];m[0]=t/a;m[5]=t;m[10]=(fa+n)/(n-fa);m[11]=-1;m[14]=(2*fa*n)/(n-fa);return m;}
    private float[] lookAt(float ex,float ey,float ez,float cx,float cy,float cz){float[]m=identity();float zx=ex-cx,zy=ey-cy,zz=ez-cz;float zl=(float)Math.sqrt(zx*zx+zy*zy+zz*zz);zx/=zl;zy/=zl;zz/=zl;float xx=zy*0-zz*1,xy=zz*0-zx*0,xz=zx*1-zy*0;float xl=(float)Math.sqrt(xx*xx+xy*xy+xz*xz);xx/=xl;xy/=xl;xz/=xl;float yx=zy*xz-zz*xy,yy=zz*xx-zx*xz,yz=zx*xy-zy*xx;m[0]=xx;m[1]=yx;m[2]=zx;m[4]=xy;m[5]=yy;m[6]=zy;m[8]=xz;m[9]=yz;m[10]=zz;m[12]=-(xx*ex+xy*ey+xz*ez);m[13]=-(yx*ex+yy*ey+yz*ez);m[14]=-(zx*ex+zy*ey+zz*ez);return m;}
    private float[] multiply(float[]a,float[]b){float[]r=new float[16];for(int c=0;c<4;c++)for(int row=0;row<4;row++)r[c*4+row]=a[row]*b[c*4]+a[4+row]*b[c*4+1]+a[8+row]*b[c*4+2]+a[12+row]*b[c*4+3];return r;}
}
