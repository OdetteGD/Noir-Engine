namespace Noir;

[AttributeUsage(AttributeTargets.Class, Inherited=true)]
public sealed class NoirScriptAttribute : Attribute { }

[AttributeUsage(AttributeTargets.Property|AttributeTargets.Field)]
public sealed class ExportAttribute : Attribute
{
    public string? Category { get; }
    public ExportAttribute(string? category=null) => Category=category;
}

[AttributeUsage(AttributeTargets.Method|AttributeTargets.Event)]
public sealed class SignalAttribute : Attribute { }

[AttributeUsage(AttributeTargets.Class|AttributeTargets.Method)]
public sealed class ToolAttribute : Attribute { }

[AttributeUsage(AttributeTargets.Class|AttributeTargets.Method)]
public sealed class ExecuteInEditorAttribute : Attribute { }

public abstract class Script : Node
{
    public virtual void Ready() { }
    public virtual void Process(float delta) { Update(delta); }
    public virtual void PhysicsProcess(float delta) { PhysicsUpdate(delta); }
    public override void ExitTree() { base.ExitTree(); }
}