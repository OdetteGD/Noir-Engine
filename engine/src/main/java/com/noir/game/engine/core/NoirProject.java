package com.noir.game.engine.core;

import java.util.*;

/** Project-level state for the Noir editor/runtime. The object is deliberately
 * independent of Android UI so the same scene can be loaded by editor and play mode. */
public final class NoirProject {
    public String name = "Untitled Noir Project";
    public String packageName = "com.noir.game.engine";
    public String mainScene = "scenes/Main.game";
    public int rendererVersion = 3;
    public final Map<String,String> settings = new LinkedHashMap<>();
    public final List<String> recentFiles = new ArrayList<>();

    public NoirProject() {
        settings.put("render.pipeline", "mobile_pbr");
        settings.put("render.shadows", "cascaded");
        settings.put("render.msaa", "4");
        settings.put("render.hdr", "true");
        settings.put("render.fog", "true");
        settings.put("physics.backend", "noir_character");
        settings.put("input.profile", "mobile_fps");
        settings.put("editor.grid", "1.0");
    }

    public void touchFile(String path) {
        recentFiles.remove(path);
        recentFiles.add(0, path);
        while (recentFiles.size() > 16) recentFiles.remove(recentFiles.size() - 1);
    }

    public String summary() {
        return name + " | " + packageName + " | " + mainScene + " | renderer=" + rendererVersion;
    }
}
