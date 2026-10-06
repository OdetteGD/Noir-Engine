package com.noir.game.engine.terrain;

import java.util.*;

/**
 * Runtime/editor terrain model with streamed chunks, deterministic LOD selection and
 * layer metadata. Rendering backends can map these records to clipmaps or tiled meshes.
 */
public final class TerrainSystem {
    public int size = 1024, chunkSize = 64, lodLevels = 6;
    public float heightScale = 80f, worldScale = 1f;
    public boolean streaming = true, collision = true, foliage = true, virtualTextures = false;
    public final Map<Long, Chunk> chunks = new LinkedHashMap<>();
    public final Map<String, Layer> layers = new LinkedHashMap<>();

    public Chunk ensure(int x, int z) {
        long key = (((long)x) << 32) ^ (z & 0xffffffffL);
        return chunks.computeIfAbsent(key, q -> new Chunk(x, z));
    }

    public void markLoaded(int x, int z, boolean loaded) { ensure(x, z).loaded = loaded; }

    public int selectLod(float distance) {
        if (distance < 40) return 0;
        if (distance < 90) return Math.min(1, lodLevels - 1);
        if (distance < 180) return Math.min(2, lodLevels - 1);
        if (distance < 350) return Math.min(3, lodLevels - 1);
        if (distance < 700) return Math.min(4, lodLevels - 1);
        return Math.max(0, Math.min(5, lodLevels - 1));
    }

    public Layer addLayer(String id, String material) {
        Layer l = new Layer(id, material); layers.put(id, l); return l;
    }

    public static final class Chunk {
        public final int x, z; public int lod; public boolean loaded, dirty;
        Chunk(int x, int z) { this.x = x; this.z = z; }
    }
    public static final class Layer {
        public final String id; public String material; public float tiling = 1f, roughness = .8f;
        Layer(String id, String material) { this.id = id; this.material = material; }
    }
}
