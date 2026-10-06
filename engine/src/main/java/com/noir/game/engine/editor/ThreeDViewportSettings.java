package com.noir.game.engine.editor;

/** 3D viewport configuration for mobile touch navigation and production/debug overlays. */
public final class ThreeDViewportSettings {
    public enum CameraMode{PERSPECTIVE,ORTHOGRAPHIC}
    public CameraMode cameraMode=CameraMode.PERSPECTIVE;
    public float fov=70f,near=.05f,far=5000f,orbitSensitivity=.35f,zoomSensitivity=.9f,panSensitivity=.8f;
    public boolean grid=true,axisGizmo=true,selectionOutline=true,wireframe=false;
    public boolean collisionDebug=false,navmeshDebug=false,lightDebug=false,boundsDebug=false,overdrawDebug=false;
    public boolean touchOrbit=true,touchPan=true,pinchZoom=true,twoFingerPan=true,stylusGizmos=true;
    public float gridSize=1f; public int gridSubdivisions=10;
    public void setCameraMode(CameraMode mode){cameraMode=mode;}
    public void resetCamera(){fov=70f;near=.05f;far=5000f;orbitSensitivity=.35f;zoomSensitivity=.9f;panSensitivity=.8f;}
    public boolean valid(){return fov>1&&fov<179&&near>0&&far>near&&gridSize>0&&gridSubdivisions>0;}
}
