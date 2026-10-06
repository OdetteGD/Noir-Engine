package com.noir.game.engine;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import com.noir.game.engine.render.NoirRenderer;

/** GPU viewport surface. One-finger orbit is kept separate from the editor overlay so
 * the same surface can later expose pinch zoom, two-finger pan and touch gizmos. */
public final class NoirSurface extends GLSurfaceView {
    private final NoirRenderer renderer; private float lastX,lastY; private boolean dragging;
    public NoirSurface(Context c,NoirRenderer r){super(c);renderer=r;setEGLContextClientVersion(3);setRenderer(r);setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);setFocusable(true);}
    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getPointerCount()>1){dragging=false;return true;}
        switch(e.getActionMasked()){
            case MotionEvent.ACTION_DOWN:lastX=e.getX();lastY=e.getY();dragging=true;return true;
            case MotionEvent.ACTION_MOVE:if(dragging){float dx=e.getX()-lastX,dy=e.getY()-lastY;renderer.orbit(dx,dy);lastX=e.getX();lastY=e.getY();}return true;
            case MotionEvent.ACTION_UP:case MotionEvent.ACTION_CANCEL:dragging=false;return true;
            default:return true;
        }
    }
}
