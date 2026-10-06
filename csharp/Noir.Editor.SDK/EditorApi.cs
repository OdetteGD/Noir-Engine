using Noir;

namespace Noir.Editor;

public sealed class EditorSelection
{
    private readonly List<Node> items=new();
    public IReadOnlyList<Node> Items=>items;
    public Node? Active=>items.Count==0?null:items[^1];
    public void Set(Node node){items.Clear();items.Add(node);}
    public void Add(Node node){if(!items.Contains(node))items.Add(node);}
    public void Clear()=>items.Clear();
}

public sealed class UndoRedo
{
    private readonly Stack<Action> undo=new();
    private readonly Stack<Action> redo=new();
    public void Record(Action undoAction,Action redoAction){undo.Push(undoAction);redo.Clear();}
    public bool Undo(){if(undo.Count==0)return false;undo.Pop().Invoke();return true;}
    public bool Redo(){if(redo.Count==0)return false;redo.Pop().Invoke();return true;}
}

public enum GizmoMode { Select, Move, Rotate, Scale }

public sealed class TransformGizmo
{
    public GizmoMode Mode { get; set; }=GizmoMode.Select;
    public float Snap { get; set; }=0.25f;
    public bool Snapping { get; set; }=true;
    public void Move(Node3D node,Vector3 delta)
    {
        var d=Snapping?new Vector3(SnapValue(delta.X),SnapValue(delta.Y),SnapValue(delta.Z)):delta;
        node.Position+=d;
    }
    private float SnapValue(float v)=>Snap<=0?v:MathF.Round(v/Snap)*Snap;
}

public abstract class EditorPlugin
{
    public virtual void OnEnable() { }
    public virtual void OnDisable() { }
    public virtual void OnSelectionChanged(EditorSelection selection) { }
    public virtual void OnProcess(float delta) { }
}
