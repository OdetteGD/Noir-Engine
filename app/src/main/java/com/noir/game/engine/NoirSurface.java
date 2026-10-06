package com.noir.game.engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import com.noir.game.engine.render.NoirRenderer;

/** Full-screen GPU viewport with editor orbit and runtime mobile look. */
public final class NoirSurface extends GLSurfaceView {
    private final NoirRenderer renderer;
    private float lastX,lastY;
    private boolean dragging;
    private float pinchDistance;

    public NoirSurface(Context c,NoirRenderer r){
        super(c);
        renderer=r;
        setEGLContextClientVersion(3);
        try {
            setEGLConfigChooser(8,8,8,8,24,8,4);
        } catch(Exception ignored) {
            setEGLConfigChooser(8,8,8,8,24,8);
        }
        setRenderer(r);
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        setFocusable(true);
    }

    public void setRuntimeMode(boolean runtime){
        queueEvent(() -> renderer.setMode(runtime ? NoirRenderer.Mode.RUNTIME : NoirRenderer.Mode.EDITOR));
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
                lastX=e.getX();lastY=e.getY();dragging=true;return true;
            case MotionEvent.ACTION_MOVE:
                if(dragging){
                    float dx=e.getX()-lastX,dy=e.getY()-lastY;
                    if(renderer.mode()==NoirRenderer.Mode.RUNTIME) renderer.runtimeLook(dx,dy);
                    else renderer.orbit(dx,dy);
                    lastX=e.getX();lastY=e.getY();
                }
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragging=false;pinchDistance=0;return true;
            default:return true;
        }
    }
}
