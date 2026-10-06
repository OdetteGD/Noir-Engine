namespace Noir;

public abstract class PhysicsBody3D : Node3D
{
    public bool Sleeping { get; set; }
    public float Mass { get; set; } = 1f;
    public Vector3 LinearVelocity { get; set; }
    public Vector3 AngularVelocity { get; set; }
    public void AddForce(Vector3 force) => LinearVelocity += force * (1f/MathF.Max(Mass,0.001f)) * (float)Time.Delta;
    public void AddImpulse(Vector3 impulse) => LinearVelocity += impulse * (1f/MathF.Max(Mass,0.001f));
}

public sealed class RigidBody3D : PhysicsBody3D
{
    public float LinearDamp { get; set; } = 0.05f;
    public float AngularDamp { get; set; } = 0.05f;
}

public sealed class StaticBody3D : PhysicsBody3D
{
    public StaticBody3D(){ Sleeping=true; }
}

public sealed class CollisionShape3D : Node3D
{
    public string ShapeType { get; set; } = "box";
    public Vector3 Extents { get; set; } = Vector3.One;
}

public readonly record struct RaycastHit3D(bool Hit,Vector3 Position,Vector3 Normal,float Distance,Node3D? Collider);

public static class Physics
{
    public static RaycastHit3D Raycast(Vector3 origin,Vector3 direction,float maxDistance=100f)
        => new(false,origin,Vector3.Zero,maxDistance,null);

    public static bool SphereCast(Vector3 origin,float radius,Vector3 direction,float maxDistance=100f)
        => false;
}