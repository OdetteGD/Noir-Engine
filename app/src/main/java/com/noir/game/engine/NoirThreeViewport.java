package com.noir.game.engine;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.noir.game.engine.render.NoirRenderer;
import java.util.Locale;

/**
 * Three.js-backed editor viewport.
 *
 * The native editor remains responsible for scene tree, inspector, gizmos and
 * project actions. Three.js owns the actual WebGL2 3D canvas so the viewport
 * behaves like a real DCC/game-editor viewport instead of a painted preview.
 */
@SuppressLint("SetJavaScriptEnabled")
public final class NoirThreeViewport extends WebView implements NoirViewport {
    private final NoirRenderer renderer;
    private boolean pageReady;
    private boolean runtime;

    public NoirThreeViewport(Context context, NoirRenderer renderer) {
        super(context);
        this.renderer = renderer;

        setBackgroundColor(Color.TRANSPARENT);
        setLayerType(WebView.LAYER_TYPE_HARDWARE, null);

        WebSettings s = getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setAllowFileAccess(false);
        s.setAllowContentAccess(false);
        s.setMediaPlaybackRequiresUserGesture(false);
        s.setBuiltInZoomControls(false);
        s.setDisplayZoomControls(false);
        s.setSupportZoom(false);

        setWebViewClient(new WebViewClient() {
            @Override public void onPageFinished(WebView view, String url) {
                pageReady = true;
                syncFromRenderer();
            }
        });

        loadUrl("https://noir.local/");
        loadDataWithBaseURL(
                "https://noir.local/",
                readViewportHtml(),
                "text/html",
                "UTF-8",
                null
        );
    }

    @Override public void setRuntimeMode(boolean runtime) {
        this.runtime = runtime;
        evaluateJavascript("window.NoirViewport&&NoirViewport.setRuntime(" + runtime + ")", null);
        syncFromRenderer();
    }

    public void syncFromRenderer() {
        if (!pageReady || renderer == null) return;
        NoirRenderer.Camera c = renderer.camera();
        String js = String.format(
                Locale.US,
                "window.NoirViewport&&NoirViewport.setCamera(%.5f,%.5f,%.5f,%.5f,%.5f,%.5f)",
                c.yaw, c.pitch, c.distance, c.targetX, c.targetY, c.targetZ
        );
        evaluateJavascript(js, null);
    }

    @Override protected void onDetachedFromWindow() {
        pageReady = false;
        stopLoading();
        super.onDetachedFromWindow();
    }

    private String readViewportHtml() {
        try {
            java.io.InputStream in = getContext().getAssets().open("noir_three_viewport.html");
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int n;
            while ((n = in.read(buffer)) != -1) out.write(buffer, 0, n);
            in.close();
            return out.toString("UTF-8");
        } catch (Exception e) {
            return "<!doctype html><html><body style='margin:0;background:#172a46'></body></html>";
        }
    }
}
