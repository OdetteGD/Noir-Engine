package com.noir.game.engine;

import android.content.Context;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/** Native Vulkan presentation surface. Backend is selected before Activity creation. */
public final class NoirVulkanSurface extends SurfaceView implements SurfaceHolder.Callback, NoirViewport {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean attached;
    private boolean running;
    private final Runnable frameLoop = new Runnable() {
        @Override public void run() {
            if (!running || !attached) return;
            try { NoirNative.vulkanDrawFrame(); } catch (Throwable ignored) {}
            handler.postDelayed(this, 16L);
        }
    };

    public NoirVulkanSurface(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(10,15,24));
        getHolder().addCallback(this);
        setFocusable(true);
    }

    @Override public void surfaceCreated(SurfaceHolder holder) {
        try {
            boolean ok = NoirNative.vulkanAttachSurface(holder.getSurface());
            attached = ok;
            running = ok;
            if (ok) handler.post(frameLoop);
            else Toast.makeText(getContext(),"Vulkan surface failed; restart with GLES.",Toast.LENGTH_LONG).show();
        } catch (Throwable ignored) {
            attached=false; running=false;
            Toast.makeText(getContext(),"Vulkan initialization failed safely.",Toast.LENGTH_LONG).show();
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder,int format,int width,int height) {}

    @Override public void surfaceDestroyed(SurfaceHolder holder) {
        running=false; attached=false; handler.removeCallbacks(frameLoop);
        try { NoirNative.vulkanDetachSurface(); } catch (Throwable ignored) {}
    }

    @Override public void setRuntimeMode(boolean runtime) {}

    @Override public boolean onTouchEvent(MotionEvent event) { return true; }
}
