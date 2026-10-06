package com.noir.game.engine.render;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import java.nio.*;
import java.util.*;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Noir mobile forward renderer.
 *
 * Features:
 * - OpenGL ES 3.0 forward PBR-style lighting
 * - procedural sky + animated cloud layer
 * - directional sun
 * - depth-tested shadow map with 3x3 PCF filtering
 * - environment/specular reflections from the procedural sky
 * - MSAA is requested by NoirSurface
 * - editor orbit camera and runtime FPS/mobile look
 * - cached GPU buffers; no per-frame mesh allocation
 */
public final class NoirRenderer implements GLSurfaceView.Renderer {
    public enum Mode { EDITOR, RUNTIME }

    public static final class Camera {
        public float yaw = -90f, pitch = 12f, distance = 18f;
        public float targetX = 0f, targetY = 1.4f, targetZ = 0f;
        public float x, y, z;
        public void updateOrbit() {
            float yr=(float)Math.toRadians(yaw), pr=(float)Math.toRadians(pitch);
            x=targetX+(float)(Math.cos(pr)*Math.cos(yr)*distance);
            y=targetY+(float)(Math.sin(pr)*distance);
            z=targetZ+(float)(Math.cos(pr)*Math.sin(yr)*distance);
        }
    }

    public static final class RuntimeCamera {
        public float x=0f,y=1.7f,z=6f;
        public float yaw=-90f,pitch=0f;
    }

    public static final class Quality {
        public int shadowSize=1024;
        public boolean shadows=true;
        public boolean reflections=true;
        public boolean clouds=true;
        public boolean fog=true;
        public float exposure=1.0f;
        public float renderScale=1.0f;
    }

    private final Camera editorCamera=new Camera();
    private final RuntimeCamera runtimeCamera=new RuntimeCamera();
    private final Quality quality=new Quality();
    private Mode mode=Mode.EDITOR;

    private int width=1,height=1;
    private int mainProgram, skyProgram, shadowProgram;
    private int cubeVbo, groundVbo;
    private int shadowFbo, shadowTexture;
    private int uModel,uViewProj,uNormal,uCamera,uSunDir,uSunColor,uSky,uBaseColor,uRough,uMetal,uShadow,uLightVP,uExposure;
    private int sModel,sLightVP;
    private int skyTime, skyForward, skyRight, skyUp, skyAspect, skyClouds;
    private long lastNanos;
    private float frameTimeMs;

    private final float[] cubeModels=new float[7*16];
    private final float[] groundModel=new float[16];
    private float time;

    public NoirRenderer(){ editorCamera.updateOrbit(); }

    @Override public void onSurfaceCreated(GL10 gl,EGLConfig config){
        GLES30.glClearColor(0.02f,0.03f,0.055f,1f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA,GLES30.GL_ONE_MINUS_SRC_ALPHA);

        mainProgram=link(mainVertex(),mainFragment());
        skyProgram=link(skyVertex(),skyFragment());
        shadowProgram=link(shadowVertex(),shadowFragment());

        uModel=GLES30.glGetUniformLocation(mainProgram,"uModel");
        uViewProj=GLES30.glGetUniformLocation(mainProgram,"uViewProj");
        uNormal=GLES30.glGetUniformLocation(mainProgram,"uNormal");
        uCamera=GLES30.glGetUniformLocation(mainProgram,"uCamera");
        uSunDir=GLES30.glGetUniformLocation(mainProgram,"uSunDir");
        uSunColor=GLES30.glGetUniformLocation(mainProgram,"uSunColor");
        uSky=GLES30.glGetUniformLocation(mainProgram,"uSky");
        uBaseColor=GLES30.glGetUniformLocation(mainProgram,"uBaseColor");
        uRough=GLES30.glGetUniformLocation(mainProgram,"uRough");
        uMetal=GLES30.glGetUniformLocation(mainProgram,"uMetal");
        uShadow=GLES30.glGetUniformLocation(mainProgram,"uShadow");
        uLightVP=GLES30.glGetUniformLocation(mainProgram,"uLightVP");
        uExposure=GLES30.glGetUniformLocation(mainProgram,"uExposure");
        sModel=GLES30.glGetUniformLocation(shadowProgram,"uModel");
        sLightVP=GLES30.glGetUniformLocation(shadowProgram,"uLightVP");
        skyTime=GLES30.glGetUniformLocation(skyProgram,"uTime");
        skyForward=GLES30.glGetUniformLocation(skyProgram,"uForward");
        skyRight=GLES30.glGetUniformLocation(skyProgram,"uRight");
        skyUp=GLES30.glGetUniformLocation(skyProgram,"uUp");
        skyAspect=GLES30.glGetUniformLocation(skyProgram,"uAspect");
        skyClouds=GLES30.glGetUniformLocation(skyProgram,"uClouds");

        createMeshes();
        createShadowMap();
        buildSceneModels();
        lastNanos=System.nanoTime();
        frameTimeMs=0f;
    }

    @Override public void onSurfaceChanged(GL10 gl,int w,int h){
        width=Math.max(1,w); height=Math.max(1,h);
        GLES30.glViewport(0,0,width,height);
    }

    @Override public void onDrawFrame(GL10 gl){
        long now=System.nanoTime();
        float dt=Math.min(0.05f,(now-lastNanos)/1_000_000_000f);
        lastNanos=now;
        frameTimeMs=dt*1000f;
        time+=dt;
        if(mode==Mode.EDITOR) editorCamera.updateOrbit();

        if(quality.shadows && shadowFbo!=0) renderShadowPass();
        renderMainPass();
    }

    public Camera camera(){return editorCamera;}
    public RuntimeCamera runtimeCamera(){return runtimeCamera;}
    public Quality quality(){return quality;}

    public void setMode(Mode m){mode=m;}
    public Mode mode(){return mode;}

    public void orbit(float dx,float dy){
        if(mode!=Mode.EDITOR)return;
        // One-finger editor orbit: drag right = rotate right, drag up = orbit upward.
        editorCamera.yaw+=dx*0.24f;
        editorCamera.pitch=Math.max(-82f,Math.min(82f,editorCamera.pitch-dy*0.24f));
        editorCamera.distance=Math.max(2.0f,Math.min(100f,editorCamera.distance));
    }

    public void pan(float dx,float dy){
        if(mode!=Mode.EDITOR)return;
        float[] forward=cameraForward();
        float[] right=normalize(cross(forward,new float[]{0,1,0}));
        float[] up=normalize(cross(right,forward));
        float scale=editorCamera.distance*0.0026f;
        editorCamera.targetX+=(-right[0]*dx+up[0]*dy)*scale;
        editorCamera.targetY+=(-right[1]*dx+up[1]*dy)*scale;
        editorCamera.targetZ+=(-right[2]*dx+up[2]*dy)*scale;
    }

    public void zoom(float amount){
        if(mode==Mode.EDITOR) editorCamera.distance=Math.max(2.0f,Math.min(100f,editorCamera.distance+amount));
    }

    public void resetEditorCamera(){
        editorCamera.yaw=-90f;
        editorCamera.pitch=12f;
        editorCamera.distance=18f;
        editorCamera.targetX=0f;editorCamera.targetY=1.4f;editorCamera.targetZ=0f;
        editorCamera.updateOrbit();
    }

    public float frameTimeMs(){return frameTimeMs;}

    public float gizmoWorldSize(){
        return Math.max(0.8f,Math.min(4.5f,editorCamera.distance*0.10f));
    }

    public float gizmoPixelSize(){return Math.max(54f,Math.min(120f,gizmoWorldSize()*42f));}

    public float[] cameraForward(){
        float yaw=mode==Mode.RUNTIME?runtimeCamera.yaw:editorCamera.yaw;
        float pitch=mode==Mode.RUNTIME?runtimeCamera.pitch:editorCamera.pitch;
        float yr=(float)Math.toRadians(yaw),pr=(float)Math.toRadians(pitch);
        return normalize(new float[]{
            (float)(Math.cos(pr)*Math.cos(yr)),
            (float)Math.sin(pr),
            (float)(Math.cos(pr)*Math.sin(yr))
        });
    }

    /** Returns origin xyz + normalized direction xyz for a viewport pixel. */
    public float[] screenRay(float sx,float sy){
        if(width<=0||height<=0)return null;
        float nx=(sx/(float)width)*2f-1f;
        float ny=1f-(sy/(float)height)*2f;
        float tan=(float)Math.tan(Math.toRadians(64f)*0.5);
        float aspect=(float)width/(float)Math.max(1,height);
        float[] forward=cameraForward();
        float[] right=normalize(cross(forward,new float[]{0,1,0}));
        float[] up=normalize(cross(right,forward));
        float[] dir=normalize(new float[]{
            forward[0]+right[0]*nx*aspect*tan+up[0]*ny*tan,
            forward[1]+right[1]*nx*aspect*tan+up[1]*ny*tan,
            forward[2]+right[2]*nx*aspect*tan+up[2]*ny*tan
        });
        float ox,oy,oz;
        if(mode==Mode.RUNTIME){ox=runtimeCamera.x;oy=runtimeCamera.y;oz=runtimeCamera.z;}
        else {ox=editorCamera.x;oy=editorCamera.y;oz=editorCamera.z;}
        return new float[]{ox,oy,oz,dir[0],dir[1],dir[2]};
    }

    public void runtimeLook(float dx,float dy){
        if(mode!=Mode.RUNTIME)return;
        runtimeCamera.yaw+=dx*0.16f;
        runtimeCamera.pitch=Math.max(-85f,Math.min(85f,runtimeCamera.pitch+dy*0.16f));
    }

    public void runtimeMove(float forward,float strafe,float dt){
        if(mode!=Mode.RUNTIME)return;
        float yr=(float)Math.toRadians(runtimeCamera.yaw);
        float fx=(float)Math.cos(yr), fz=(float)Math.sin(yr);
        runtimeCamera.x+=(fx*forward-fz*strafe)*dt*5f;
        runtimeCamera.z+=(fz*forward+fx*strafe)*dt*5f;
    }

    private void renderShadowPass(){
        int s=quality.shadowSize;
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,shadowFbo);
        GLES30.glViewport(0,0,s,s);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        GLES30.glUseProgram(shadowProgram);

        float[] lightVP=lightViewProj();
        GLES30.glUniformMatrix4fv(sLightVP,1,false,lightVP,0);
        drawShadowModel(groundModel);
        for(int i=0;i<cubeModels.length/16;i++)drawShadowModel(cubeModels,i);

        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
        GLES30.glViewport(0,0,width,height);
    }

    private void renderMainPass(){
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
        GLES30.glViewport(0,0,width,height);
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT|GLES30.GL_DEPTH_BUFFER_BIT);

        drawSky();

        GLES30.glUseProgram(mainProgram);
        float[] vp=viewProj();
        float[] lightVP=lightViewProj();
        GLES30.glUniformMatrix4fv(uViewProj,1,false,vp,0);
        GLES30.glUniformMatrix4fv(uLightVP,1,false,lightVP,0);
        GLES30.glUniform3f(uSunDir,-0.42f,-0.82f,-0.34f);
        GLES30.glUniform3f(uSunColor,1.0f,0.93f,0.82f);
        GLES30.glUniform3f(uSky,0.22f,0.38f,0.62f);
        GLES30.glUniform1f(uExposure,quality.exposure);
        int uReflectionsLoc=GLES30.glGetUniformLocation(mainProgram,"uReflections");
        int uFogLoc=GLES30.glGetUniformLocation(mainProgram,"uFog");
        GLES30.glUniform1f(uReflectionsLoc,quality.reflections?1f:0f);
        GLES30.glUniform1f(uFogLoc,quality.fog?0.18f:0f);
        GLES30.glUniform1i(uShadow,0);
        GLES30.glUniform3f(uBaseColor,0.28f,0.31f,0.34f);

        float cx,cy,cz;
        if(mode==Mode.RUNTIME){cx=runtimeCamera.x;cy=runtimeCamera.y;cz=runtimeCamera.z;}
        else {cx=editorCamera.x;cy=editorCamera.y;cz=editorCamera.z;}
        GLES30.glUniform3f(uCamera,cx,cy,cz);

        GLES30.glActiveTexture(GLES30.GL_TEXTURE0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,shadowTexture);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,cubeVbo);
        enableMainAttributes();
        GLES30.glUniform1f(uRough,0.62f);
        GLES30.glUniform1f(uMetal,0.02f);
        GLES30.glUniform3f(uBaseColor,0.18f,0.21f,0.24f);
        drawModel(groundModel);
        for(int i=0;i<cubeModels.length/16;i++){
            float rough= i==1 ? 0.24f : (i==3||i==4 ? 0.38f : 0.52f);
            float metal= i==1 ? 0.26f : 0.05f;
            if(i==0)GLES30.glUniform3f(uBaseColor,0.58f,0.60f,0.63f);
            else if(i==1)GLES30.glUniform3f(uBaseColor,0.22f,0.30f,0.36f);
            else if(i==2)GLES30.glUniform3f(uBaseColor,0.48f,0.44f,0.38f);
            else if(i==3||i==4)GLES30.glUniform3f(uBaseColor,0.32f,0.37f,0.42f);
            else if(i==5)GLES30.glUniform3f(uBaseColor,0.12f,0.15f,0.18f);
            else GLES30.glUniform3f(uBaseColor,0.36f,0.40f,0.44f);
            GLES30.glUniform1f(uRough,rough);
            GLES30.glUniform1f(uMetal,metal);
            drawModel(cubeModels,i);
        }
        disableMainAttributes();
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
    }

    private void drawSky(){
        GLES30.glDepthMask(false);
        GLES30.glDisable(GLES30.GL_CULL_FACE);
        GLES30.glUseProgram(skyProgram);
        GLES30.glUniform1f(skyTime,time);
        float yaw=mode==Mode.RUNTIME?runtimeCamera.yaw:editorCamera.yaw;
        float pitch=mode==Mode.RUNTIME?runtimeCamera.pitch:editorCamera.pitch;
        float yr=(float)Math.toRadians(yaw),pr=(float)Math.toRadians(pitch);
        float[] forward={(float)(Math.cos(pr)*Math.cos(yr)),(float)Math.sin(pr),(float)(Math.cos(pr)*Math.sin(yr))};
        float[] right=normalize(cross(forward,new float[]{0f,1f,0f}));
        float[] up=normalize(cross(right,forward));
        GLES30.glUniform3f(skyForward,forward[0],forward[1],forward[2]);
        GLES30.glUniform3f(skyRight,right[0],right[1],right[2]);
        GLES30.glUniform3f(skyUp,up[0],up[1],up[2]);
        GLES30.glUniform1f(skyAspect,(float)width/Math.max(1,height));
        GLES30.glUniform1f(skyClouds,quality.clouds?1f:0f);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,3);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glDepthMask(true);
    }

    private void createMeshes(){
        float[] v={
            // position, normal, uv
            -1,-1,-1, 0,0,-1, 0,0, 1,-1,-1,0,0,-1,1,0, 1,1,-1,0,0,-1,1,1,
            -1,-1,-1,0,0,-1,0,0, 1,1,-1,0,0,-1,1,1, -1,1,-1,0,0,-1,0,1,
            -1,-1,1,0,0,1,0,0, -1,1,1,0,0,1,0,1, 1,1,1,0,0,1,1,1,
            -1,-1,1,0,0,1,0,0, 1,1,1,0,0,1,1,1, 1,-1,1,0,0,1,1,0,
            -1,-1,-1,-1,0,0,0,0, -1,1,-1,-1,0,0,0,1, -1,1,1,-1,0,0,1,1,
            -1,-1,-1,-1,0,0,0,0, -1,1,1,-1,0,0,1,1, -1,-1,1,-1,0,0,1,0,
            1,-1,-1,1,0,0,0,0, 1,-1,1,1,0,0,1,0, 1,1,1,1,0,0,1,1,
            1,-1,-1,1,0,0,0,0, 1,1,1,1,0,0,1,1, 1,1,-1,1,0,0,0,1,
            -1,1,-1,0,1,0,0,0, 1,1,-1,0,1,0,1,0, 1,1,1,0,1,0,1,1,
            -1,1,-1,0,1,0,0,0, 1,1,1,0,1,0,1,1, -1,1,1,0,1,0,0,1,
            -1,-1,-1,0,-1,0,0,0, -1,-1,1,0,-1,0,0,1, 1,-1,1,0,-1,0,1,1,
            -1,-1,-1,0,-1,0,0,0, 1,-1,1,0,-1,0,1,1, 1,-1,-1,0,-1,0,1,0
        };
        FloatBuffer buf=ByteBuffer.allocateDirect(v.length*4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        buf.put(v).flip();
        int[] ids=new int[2];
        GLES30.glGenBuffers(2,ids,0);
        cubeVbo=ids[0]; groundVbo=ids[1];
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,cubeVbo);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,v.length*4,buf,GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,0);
    }

    private void createShadowMap(){
        int[] t=new int[1],f=new int[1];
        GLES30.glGenTextures(1,t,0); shadowTexture=t[0];
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,shadowTexture);
        GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D,0,GLES30.GL_DEPTH_COMPONENT16,quality.shadowSize,quality.shadowSize,0,GLES30.GL_DEPTH_COMPONENT,GLES30.GL_UNSIGNED_SHORT,null);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MIN_FILTER,GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_WRAP_S,GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D,GLES30.GL_TEXTURE_WRAP_T,GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D,0);
        GLES30.glGenFramebuffers(1,f,0); shadowFbo=f[0];
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,shadowFbo);
        GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER,GLES30.GL_DEPTH_ATTACHMENT,GLES30.GL_TEXTURE_2D,shadowTexture,0);
        GLES30.glDrawBuffers(0,new int[0],0);
        GLES30.glReadBuffer(GLES30.GL_NONE);
        if(GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER)!=GLES30.GL_FRAMEBUFFER_COMPLETE){
            GLES30.glDeleteFramebuffers(1,f,0); shadowFbo=0;
        }
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER,0);
    }

    private void buildSceneModels(){
        setIdentity(groundModel); scale(groundModel,14,0.1f,18); translate(groundModel,0,-0.1f,0);
        model(cubeModels,0,0,1,0,0,0,0,2.5f,1.5f,2.5f);
        model(cubeModels,1,-5,1.5f,-3,0,0,0,2,1.5f,2);
        model(cubeModels,2,5,1.5f,-3,0,0,0,2,1.5f,2);
        model(cubeModels,3,-7,2.0f,5,0,0,0,1,2,1);
        model(cubeModels,4,7,2.0f,5,0,0,0,1,2,1);
        model(cubeModels,5,0,1.5f,-9,0,0,0,6,1.5f,0.6f);
        model(cubeModels,6,0,2.5f,9,0,0,0,6,2.5f,0.6f);
    }

    private void model(float[] dst,int i,float x,float y,float z,float rx,float ry,float rz,float sx,float sy,float sz){
        float[] m=identity(); translate(m,x,y,z); rotateXYZ(m,rx,ry,rz); scale(m,sx,sy,sz); System.arraycopy(m,0,dst,i*16,16);
    }

    private void drawShadowModel(float[] m){
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER,cubeVbo);
        GLES30.glEnableVertexAttribArray(0);
        GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,8*4,0);
        GLES30.glUniformMatrix4fv(sModel,1,false,m,0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,36);
        GLES30.glDisableVertexAttribArray(0);
    }

    private void drawShadowModel(float[] all,int i){
        drawShadowModel(Arrays.copyOfRange(all,i*16,i*16+16));
    }

    private void enableMainAttributes(){
        GLES30.glEnableVertexAttribArray(0); GLES30.glVertexAttribPointer(0,3,GLES30.GL_FLOAT,false,8*4,0);
        GLES30.glEnableVertexAttribArray(1); GLES30.glVertexAttribPointer(1,3,GLES30.GL_FLOAT,false,8*4,3*4);
        GLES30.glEnableVertexAttribArray(2); GLES30.glVertexAttribPointer(2,2,GLES30.GL_FLOAT,false,8*4,6*4);
    }

    private void disableMainAttributes(){GLES30.glDisableVertexAttribArray(0);GLES30.glDisableVertexAttribArray(1);GLES30.glDisableVertexAttribArray(2);}

    private void drawModel(float[] m){
        GLES30.glUniformMatrix4fv(uModel,1,false,m,0);
        float[] n=normalMatrix(m);
        GLES30.glUniformMatrix3fv(uNormal,1,false,n,0);
        GLES30.glDrawArrays(GLES30.GL_TRIANGLES,0,36);
    }
    private void drawModel(float[] all,int i){drawModel(Arrays.copyOfRange(all,i*16,i*16+16));}

    private float[] viewProj(){
        float[] v;
        if(mode==Mode.RUNTIME){
            float yr=(float)Math.toRadians(runtimeCamera.yaw), pr=(float)Math.toRadians(runtimeCamera.pitch);
            float fx=(float)(Math.cos(pr)*Math.cos(yr)), fy=(float)Math.sin(pr), fz=(float)(Math.cos(pr)*Math.sin(yr));
            v=lookAt(runtimeCamera.x,runtimeCamera.y,runtimeCamera.z,runtimeCamera.x+fx,runtimeCamera.y+fy,runtimeCamera.z+fz,0,1,0);
        }else v=lookAt(editorCamera.x,editorCamera.y,editorCamera.z,editorCamera.targetX,editorCamera.targetY,editorCamera.targetZ,0,1,0);
        float[] p=perspective(64f,(float)width/Math.max(1,height),0.05f,160f);
        return multiply(p,v);
    }

    private float[] lightViewProj(){
        float[] v=lookAt(18,24,16,0,0,0,0,1,0);
        float[] p=ortho(-28,28,-28,28,1,70);
        return multiply(p,v);
    }

    private int link(String vs,String fs){
        int a=compile(GLES30.GL_VERTEX_SHADER,vs),b=compile(GLES30.GL_FRAGMENT_SHADER,fs);
        int p=GLES30.glCreateProgram();GLES30.glAttachShader(p,a);GLES30.glAttachShader(p,b);GLES30.glLinkProgram(p);
        int[] ok=new int[1];GLES30.glGetProgramiv(p,GLES30.GL_LINK_STATUS,ok,0);
        if(ok[0]==0) throw new RuntimeException("Noir shader link failed: "+GLES30.glGetProgramInfoLog(p));
        GLES30.glDeleteShader(a);GLES30.glDeleteShader(b);return p;
    }
    private int compile(int type,String src){
        int s=GLES30.glCreateShader(type);GLES30.glShaderSource(s,src);GLES30.glCompileShader(s);
        int[] ok=new int[1];GLES30.glGetShaderiv(s,GLES30.GL_COMPILE_STATUS,ok,0);
        if(ok[0]==0) throw new RuntimeException("Noir shader compile failed: "+GLES30.glGetShaderInfoLog(s));
        return s;
    }

    private String mainVertex(){return "#version 300 es\nlayout(location=0)in vec3 aPos;layout(location=1)in vec3 aNormal;layout(location=2)in vec2 aUV;uniform mat4 uModel,uViewProj,uLightVP;uniform mat3 uNormal;out vec3 vPos,vNormal;out vec4 vLight;out vec2 vUV;void main(){vec4 w=uModel*vec4(aPos,1.0);vPos=w.xyz;vNormal=normalize(uNormal*aNormal);vLight=uLightVP*w;vUV=aUV;gl_Position=uViewProj*w;}";}
    private String mainFragment(){return "#version 300 es\nprecision highp float;in vec3 vPos,vNormal;in vec4 vLight;in vec2 vUV;uniform vec3 uCamera,uSunDir,uSunColor,uSky,uBaseColor;uniform float uRough,uMetal,uExposure,uReflections,uFog;uniform sampler2D uShadow;out vec4 frag;float shadow(){vec3 p=vLight.xyz/max(vLight.w,0.0001);p=p*0.5+0.5;if(p.x<0.0||p.x>1.0||p.y<0.0||p.y>1.0||p.z>1.0)return 1.0;float bias=0.0015;float s=0.0;float texel=1.0/1024.0;for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++){float d=texture(uShadow,p.xy+vec2(x,y)*texel).r;s+=p.z-bias<=d?1.0:0.0;}return s/9.0;}vec3 fresnel(float c,vec3 f0){return f0+(1.0-f0)*pow(1.0-c,5.0);}void main(){vec3 N=normalize(vNormal),V=normalize(uCamera-vPos),L=normalize(-uSunDir),H=normalize(V+L);float NoL=max(dot(N,L),0.0),NoV=max(dot(N,V),0.0),NoH=max(dot(N,H),0.0),VoH=max(dot(V,H),0.0);float a=max(0.045,uRough*uRough);float a2=a*a;float d=(NoH*NoH*(a2-1.0)+1.0);float D=a2/(3.14159265*d*d);float k=(a+1.0);k=k*k/8.0;float Gv=NoV/(NoV*(1.0-k)+k);float Gl=NoL/(NoL*(1.0-k)+k);vec3 F0=mix(vec3(0.04),vec3(0.86),uMetal);vec3 F=fresnel(VoH,F0);vec3 spec=(D*Gv*Gl*F)/max(4.0*NoV*NoL,0.001);vec3 base=max(uBaseColor,vec3(0.001));vec3 kd=(1.0-F)*(1.0-uMetal);float sh=shadow();vec3 direct=(kd*base/3.14159265+spec)*uSunColor*NoL*sh;vec3 env=mix(uSky,vec3(0.75,0.82,0.92),pow(1.0-NoV,5.0));vec3 color=(direct+kd*base*0.16+env*spec*0.35*uReflections);float fogAmount=uFog*clamp(length(uCamera-vPos)/70.0,0.0,1.0);color=mix(color,uSky,fogAmount);color=vec3(1.0)-exp(-color*uExposure);color=pow(color,vec3(1.0/2.2));frag=vec4(color,1.0);}";}
    private String shadowVertex(){return "#version 300 es\nlayout(location=0)in vec3 aPos;uniform mat4 uModel,uLightVP;void main(){gl_Position=uLightVP*uModel*vec4(aPos,1.0);}";}
    private String shadowFragment(){return "#version 300 es\nprecision mediump float;void main(){}";}
    private String skyVertex(){return "#version 300 es\nout vec2 vSkyUV;void main(){vec2 p=vec2(float((gl_VertexID<<1)&2),float(gl_VertexID&2));vSkyUV=p;gl_Position=vec4(p*2.0-1.0,0.999,1.0);}";}
    private String skyFragment(){return "#version 300 es\nprecision highp float;in vec2 vSkyUV;uniform float uTime,uAspect;uniform vec3 uForward,uRight,uUp;uniform float uClouds;out vec4 frag;float hash(vec3 p){return fract(sin(dot(p,vec3(127.1,311.7,74.7)))*43758.5453);}float noise(vec3 p){vec3 i=floor(p),f=fract(p);f=f*f*(3.0-2.0*f);return mix(mix(mix(hash(i),hash(i+vec3(1,0,0)),f.x),mix(hash(i+vec3(0,1,0)),hash(i+vec3(1,1,0)),f.x),f.y),mix(mix(hash(i+vec3(0,0,1)),hash(i+vec3(1,0,1)),f.x),mix(hash(i+vec3(0,1,1)),hash(i+vec3(1,1,1)),f.x),f.y),f.z);}void main(){vec2 ndc=vSkyUV*2.0-1.0;float aspect=uAspect;vec3 ray=normalize(uForward+uRight*ndc.x*aspect+uUp*ndc.y);float h=clamp(ray.y*0.5+0.5,0.0,1.0);vec3 horizon=vec3(0.72,0.82,0.96),zenith=vec3(0.025,0.08,0.20);vec3 c=mix(horizon,zenith,pow(h,0.75));float cloudBand=smoothstep(0.02,0.18,ray.y)*smoothstep(0.72,0.30,ray.y);vec3 q=ray*3.2+vec3(uTime*0.006,0.0,uTime*0.004);float n=noise(q)*0.62+noise(q*2.1)*0.25+noise(q*4.0)*0.13;float clouds=smoothstep(0.57,0.74,n)*cloudBand*uClouds;c=mix(c,vec3(0.94,0.96,0.985),clouds*0.48);vec3 sunDir=normalize(vec3(-0.42,-0.82,-0.34));float sunDot=max(dot(ray,-sunDir),0.0);float sun=pow(sunDot,720.0)+0.12*pow(sunDot,18.0);c+=vec3(1.0,0.72,0.40)*sun;frag=vec4(c,1.0);}";}
    public float[] projectWorldToScreen(float x,float y,float z){
        float[] vp=viewProj();
        float cx=vp[0]*x+vp[4]*y+vp[8]*z+vp[12];
        float cy=vp[1]*x+vp[5]*y+vp[9]*z+vp[13];
        float cw=vp[3]*x+vp[7]*y+vp[11]*z+vp[15];
        if(cw<=0.0001f)return null;
        float nx=cx/cw,ny=cy/cw;
        return new float[]{(nx*0.5f+0.5f)*width,(1f-(ny*0.5f+0.5f))*height,cw};
    }
    private float[] cross(float[] a,float[] b){return new float[]{a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0]};}
    private float[] normalize(float[] v){float l=(float)Math.sqrt(v[0]*v[0]+v[1]*v[1]+v[2]*v[2]);if(l<0.00001f)return new float[]{0,1,0};return new float[]{v[0]/l,v[1]/l,v[2]/l};}
    private float[] identity(){float[]m=new float[16];m[0]=m[5]=m[10]=m[15]=1;return m;}
    private void setIdentity(float[]m){Arrays.fill(m,0);m[0]=m[5]=m[10]=m[15]=1;}
    private void translate(float[]m,float x,float y,float z){m[12]+=x;m[13]+=y;m[14]+=z;}
    private void scale(float[]m,float x,float y,float z){m[0]*=x;m[5]*=y;m[10]*=z;}
    private void rotateXYZ(float[]m,float x,float y,float z){if(x!=0)mulInPlace(m,rotationX(x));if(y!=0)mulInPlace(m,rotationY(y));if(z!=0)mulInPlace(m,rotationZ(z));}
    private float[] rotationX(float d){float c=(float)Math.cos(d),s=(float)Math.sin(d);float[]m=identity();m[5]=c;m[6]=s;m[9]=-s;m[10]=c;return m;}
    private float[] rotationY(float d){float c=(float)Math.cos(d),s=(float)Math.sin(d);float[]m=identity();m[0]=c;m[2]=-s;m[8]=s;m[10]=c;return m;}
    private float[] rotationZ(float d){float c=(float)Math.cos(d),s=(float)Math.sin(d);float[]m=identity();m[0]=c;m[1]=s;m[4]=-s;m[5]=c;return m;}
    private void mulInPlace(float[]a,float[]b){float[]r=multiply(a,b);System.arraycopy(r,0,a,0,16);}
    private float[] normalMatrix(float[]m){return new float[]{m[0],m[1],m[2],m[4],m[5],m[6],m[8],m[9],m[10]};}
    private float[] perspective(float f,float a,float n,float fa){float t=(float)(1.0/Math.tan(Math.toRadians(f)*0.5));float[]m=new float[16];m[0]=t/a;m[5]=t;m[10]=(fa+n)/(n-fa);m[11]=-1;m[14]=(2*fa*n)/(n-fa);return m;}
    private float[] ortho(float l,float r,float b,float t,float n,float f){float[]m=identity();m[0]=2/(r-l);m[5]=2/(t-b);m[10]=-2/(f-n);m[12]=-(r+l)/(r-l);m[13]=-(t+b)/(t-b);m[14]=-(f+n)/(f-n);return m;}
    private float[] lookAt(float ex,float ey,float ez,float cx,float cy,float cz,float ux,float uy,float uz){float zx=ex-cx,zy=ey-cy,zz=ez-cz;float zl=(float)Math.sqrt(zx*zx+zy*zy+zz*zz);zx/=zl;zy/=zl;zz/=zl;float xx=uy*zz-uz*zy,xy=uz*zx-ux*zz,xz=ux*zy-uy*zx;float xl=(float)Math.sqrt(xx*xx+xy*xy+xz*xz);xx/=xl;xy/=xl;xz/=xl;float yx=zy*xz-zz*xy,yy=zz*xx-zx*xz,yz=zx*xy-zy*xx;float[]m=identity();m[0]=xx;m[1]=yx;m[2]=zx;m[4]=xy;m[5]=yy;m[6]=zy;m[8]=xz;m[9]=yz;m[10]=zz;m[12]=-(xx*ex+xy*ey+xz*ez);m[13]=-(yx*ex+yy*ey+yz*ez);m[14]=-(zx*ex+zy*ey+zz*ez);return m;}
    private float[] multiply(float[]a,float[]b){float[]r=new float[16];for(int c=0;c<4;c++)for(int row=0;row<4;row++)r[c*4+row]=a[row]*b[c*4]+a[4+row]*b[c*4+1]+a[8+row]*b[c*4+2]+a[12+row]*b[c*4+3];return r;}
}
