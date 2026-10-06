package com.noir.game.engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import com.noir.game.engine.render.NoirRenderer;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/** Full-screen GPU viewport with editor orbit and runtime mobile look. */
public final class NoirSurface extends GLSurfaceView {
    public interface EditorTapListener { void onEditorTap(float x,float y); }
    private EditorTapListener editorTapListener;
    public void setEditorTapListener(EditorTapListener listener){editorTapListener=listener;}
    private final NoirRenderer renderer;
    private float lastX,lastY,startX,startY;
    private boolean dragging;
    private boolean runtimeMoveTouch;
    private float pinchDistance;

    public NoirSurface(Context c,NoirRenderer r){
        super(c);
        renderer=r;
        setEGLContextClientVersion(3);
        setEGLConfigChooser(new MultisampleChooser());
        setRenderer(r);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setFocusable(true);
    }

    public void setRuntimeMode(boolean runtime){
        queueEvent(() -> renderer.setMode(runtime ? NoirRenderer.Mode.RUNTIME : NoirRenderer.Mode.EDITOR));
    }

    private static final class MultisampleChooser implements EGLConfigChooser {
        @Override public EGLConfig chooseConfig(EGL10 egl,EGLDisplay display){
            int[] attrs={
                EGL10.EGL_RED_SIZE,8,EGL10.EGL_GREEN_SIZE,8,EGL10.EGL_BLUE_SIZE,8,
                EGL10.EGL_ALPHA_SIZE,8,EGL10.EGL_DEPTH_SIZE,24,EGL10.EGL_STENCIL_SIZE,8,
                EGL10.EGL_SAMPLE_BUFFERS,1,EGL10.EGL_SAMPLES,4,EGL10.EGL_NONE
            };
            EGLConfig[] configs=new EGLConfig[16];int[] count=new int[1];
            if(!egl.eglChooseConfig(display,attrs,configs,configs.length,count)||count[0]==0){
                int[] fallback={EGL10.EGL_RED_SIZE,8,EGL10.EGL_GREEN_SIZE,8,EGL10.EGL_BLUE_SIZE,8,
                    EGL10.EGL_ALPHA_SIZE,8,EGL10.EGL_DEPTH_SIZE,24,EGL10.EGL_STENCIL_SIZE,8,EGL10.EGL_NONE};
                if(!egl.eglChooseConfig(display,fallback,configs,configs.length,count)||count[0]==0)
                    throw new IllegalArgumentException("No compatible OpenGL ES 3 config");
            }
            return configs[0];
        }
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        int count=e.getPointerCount();
        if(count>=2){
            float dx=e.getX(0)-e.getX(1),dy=e.getY(0)-e.getY(1);
            float d=(float)Math.sqrt(dx*dx+dy*dy);
            if(e.getActionMasked()==MotionEvent.ACTION_POINTER_DOWN)pinchDistance=d;
            else if(e.getActionMasked()==MotionEvent.ACTION_MOVE && pinchDistance>1){
                renderer.zoom((pinchDistance-d)*0.015f);
                pinchDistance=d;
            }
            return true;
        }
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:
                lastX=e.getX();lastY=e.getY();startX=e.getX();startY=e.getY();
                runtimeMoveTouch=renderer.mode()==NoirRenderer.Mode.RUNTIME && e.getX()<getWidth()*0.42f;
                dragging=true;return true;
            case MotionEvent.ACTION_MOVE:
                if(dragging){
                    float dx=e.getX()-lastX,dy=e.getY()-lastY;
                    if(renderer.mode()==NoirRenderer.Mode.RUNTIME){
                        if(runtimeMoveTouch){
                            float sx=Math.max(-1f,Math.min(1f,(e.getX()-startX)/220f));
                            float sy=Math.max(-1f,Math.min(1f,(startY-e.getY())/220f));
                            renderer.runtimeMove(sy,sx,0.016f);
                        }else renderer.runtimeLook(dx,dy);
                    }else renderer.orbit(dx,dy);
                    lastX=e.getX();lastY=e.getY();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if(renderer.mode()==NoirRenderer.Mode.EDITOR && dragging && Math.abs(e.getX()-startX)<12f && Math.abs(e.getY()-startY)<12f && editorTapListener!=null)
                    editorTapListener.onEditorTap(e.getX(),e.getY());
                dragging=false;runtimeMoveTouch=false;pinchDistance=0;return true;
            case MotionEvent.ACTION_CANCEL:
                dragging=false;runtimeMoveTouch=false;pinchDistance=0;return true;
            default:return true;
        }
    }
}
