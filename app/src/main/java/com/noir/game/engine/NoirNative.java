package com.noir.game.engine;

public final class NoirNative {
    private static boolean loaded;

    static {
        try {
            System.loadLibrary("noir3d");
            loaded=true;
        } catch (UnsatisfiedLinkError ignored) {
            loaded=false;
        }
    }

    private NoirNative() {}

    public static boolean isLoaded() { return loaded; }

    public static native String engineVersion();
    public static native long engineBuildId();
    public static native void stepRigidBody(float[] state,float dt,float gravity);
    public static native void stepRigidBodyAdvanced(float[] state,float dt,float gravity,float damping,float floorY);
    public static native float raySphereHit(float[] rayOrigin,float[] rayDir,float[] center,float radius);
    public static native float rayAabbHit(float[] rayOrigin,float[] rayDir,float[] min,float[] max);
    public static native float springDamper(float current,float velocity,float target,float stiffness,float damping,float dt);
    public static native String mobilePbrShader();
}
