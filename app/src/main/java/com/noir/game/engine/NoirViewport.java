package com.noir.game.engine;

/** Common viewport contract shared by GLES and Vulkan editor/runtime surfaces. */
public interface NoirViewport {
    void setRuntimeMode(boolean runtime);
}
