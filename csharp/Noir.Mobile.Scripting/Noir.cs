using System;
using System.Numerics;

namespace Noir;

/// <summary>Minimal mobile-safe C# scripting surface exposed by the Noir engine.</summary>
public static class Engine
{
    public static double DeltaTime { get; internal set; }
    public static double Time { get; internal set; }
    public static void Log(string message) => Console.WriteLine("[Noir] " + message);
}

public readonly record struct Vector2(float X, float Y)
{
    public static readonly Vector2 Zero = new(0, 0);
    public float Length() => MathF.Sqrt(X * X + Y * Y);
    public Vector2 Normalized() { var l=Length(); return l <= 0.00001f ? Zero : new(X/l,Y/l); }
}

public readonly record struct Vector3(float X, float Y, float Z)
{
    public static readonly Vector3 Zero = new(0, 0, 0);
    public static readonly Vector3 One = new(1, 1, 1);
    public float Length() => MathF.Sqrt(X * X + Y * Y + Z * Z);
    public Vector3 Normalized() { var l=Length(); return l <= 0.00001f ? Zero : new(X/l,Y/l,Z/l); }
    public static Vector3 operator +(Vector3 a,Vector3 b)=>new(a.X+b.X,a.Y+b.Y,a.Z+b.Z);
    public static Vector3 operator -(Vector3 a,Vector3 b)=>new(a.X-b.X,a.Y-b.Y,a.Z-b.Z);
    public static Vector3 operator *(Vector3 a,float b)=>new(a.X*b,a.Y*b,a.Z*b);
    public static Vector3 operator *(float b,Vector3 a)=>a*b;
    public static float Dot(Vector3 a,Vector3 b)=>a.X*b.X+a.Y*b.Y+a.Z*b.Z;
    public static Vector3 Cross(Vector3 a,Vector3 b)=>new(
        a.Y*b.Z-a.Z*b.Y, a.Z*b.X-a.X*b.Z, a.X*b.Y-a.Y*b.X);
}

public static class Input
{
    private static readonly System.Collections.Generic.HashSet<string> Pressed = new(StringComparer.Ordinal);
    public static bool IsActionPressed(string action) => Pressed.Contains(action);
    public static bool IsActionJustPressed(string action) => IsActionPressed(action);
    public static float Axis(string negative,string positive) =>
        (IsActionPressed(positive)?1f:0f)-(IsActionPressed(negative)?1f:0f);
    public static Vector2 Vector(string left,string right,string up,string down) =>
        new(Axis(left,right),Axis(down,up)).Normalized();
    internal static void Set(string action,bool pressed) { if(pressed) Pressed.Add(action); else Pressed.Remove(action); }
}

public static class Time
{
    public static double Delta => Engine.DeltaTime;
    public static double Now => Engine.Time;
}

public static class Mathf
{
    public const float Pi = MathF.PI;
    public static float Clamp(float v,float min,float max)=>Math.Clamp(v,min,max);
    public static float Lerp(float a,float b,float t)=>a+(b-a)*Clamp(t,0,1);
    public static float MoveToward(float current,float target,float maxDelta)
    {
        if(MathF.Abs(target-current)<=maxDelta)return target;
        return current + MathF.Sign(target-current)*maxDelta;
    }
    public static float DegToRad(float degrees)=>degrees*(MathF.PI/180f);
    public static float RadToDeg(float radians)=>radians*(180f/MathF.PI);
}

public abstract class Object
{
    public string Name { get; set; } = "Object";
    public bool IsValid { get; internal set; } = true;
}

public class Node : Object
{
    public Node? Parent { get; private set; }
    public System.Collections.Generic.IReadOnlyList<Node> Children => children;
    private readonly System.Collections.Generic.List<Node> children = new();

    public void AddChild(Node child)
    {
        if(child.Parent != null) child.Parent.RemoveChild(child);
        child.Parent=this; children.Add(child);
    }

    public void RemoveChild(Node child)
    {
        if(children.Remove(child)) child.Parent=null;
    }

    public T? GetNode<T>(string name) where T : Node =>
        Find(name) as T;

    public Node? Find(string name)
    {
        foreach(var c in children)
        {
            if(string.Equals(c.Name,name,StringComparison.Ordinal)) return c;
            var nested=c.Find(name);
            if(nested!=null)return nested;
        }
        return null;
    }

    public virtual void Start() { }
    public virtual void Update(float delta) { }
    public virtual void PhysicsUpdate(float delta) { }
}

public class Node3D : Node
{
    public Vector3 Position { get; set; }
    public Vector3 RotationDegrees { get; set; }
    public Vector3 Scale { get; set; } = Vector3.One;
    public Vector3 GlobalPosition => Position;
}

public class Character3D : Node3D
{
    public Vector3 Velocity { get; set; }
    public bool IsOnFloor { get; internal set; }
    public void MoveAndSlide() { /* Runtime bridge applies the velocity in the native engine. */ }
}

public class Camera3D : Node3D
{
    public float Fov { get; set; } = 70f;
}

public class Component : Object { public Node3D? Owner { get; internal set; } }

public static class Debug
{
    public static void Log(string message) => Engine.Log(message);
    public static void Warning(string message) => Engine.Log("WARNING: " + message);
}
