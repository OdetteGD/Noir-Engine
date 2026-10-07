namespace Noir;

public enum GizmoAxis{None,X,Y,Z,XY,XZ,YZ}
public enum EditorToolMode{Select,Move,Rotate,Scale}
public sealed class EditorToolState
{
    public EditorToolMode Mode{get;set;}=EditorToolMode.Select;
    public GizmoAxis Axis{get;set;}=GizmoAxis.None;
    public float GridSnap{get;set;}=.25f;
    public float AngleSnapDegrees{get;set;}=15;
    public bool SnapEnabled{get;set;}=true;
}
public static class Editor
{
    public static bool IsPlaying=>Engine.IsPlaying;
    public static bool IsEditor=>Engine.IsEditor;
    public static void MarkDirty(string reason)=>Debug.Log("Editor dirty: "+reason);
    public static void PrintInspector(Node node)=>Debug.Log("Inspector: "+node.Name);
}
