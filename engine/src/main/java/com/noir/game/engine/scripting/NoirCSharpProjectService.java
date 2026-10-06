package com.noir.game.engine.scripting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

/** Creates a portable, mobile-first C# project inside a Noir .game project. */
public final class NoirCSharpProjectService {
    private static final Pattern PACKAGE =
            Pattern.compile("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*$");

    public static final class Result {
        public final File root, project, script;
        public final String packageName;
        public Result(File r, File p, File s, String n) {
            root=r; project=p; script=s; packageName=n;
        }
    }

    private NoirCSharpProjectService() {}

    public static Result ensure(File projectRoot, String packageName) throws IOException {
        if(projectRoot==null) throw new IOException("No Noir project root");
        File root=new File(projectRoot,"scripts/csharp");
        File sdk=new File(root,"NoirSdk");
        if(!sdk.mkdirs() && !sdk.isDirectory())
            throw new IOException("Unable to create C# SDK directory");

        String pkg=validPackage(packageName) ? packageName : "com.noir.game.scripts";
        File csproj=new File(root,"Noir.Game.csproj");
        File script=new File(root,"PlayerController.cs");

        write(csproj, csprojText());
        write(new File(root,"Directory.Build.props"), propsText(pkg));
        write(new File(root,"NoirSdk/Noir.cs"), NOIR_API);
        if(!script.isFile()) write(script, PLAYER_SCRIPT);
        write(new File(root,"README.md"), readmeText());
        return new Result(root,csproj,script,pkg);
    }

    public static boolean validPackage(String packageName) {
        return packageName!=null && PACKAGE.matcher(packageName).matches();
    }

    private static void write(File file,String text)throws IOException {
        File parent=file.getParentFile();
        if(parent!=null) parent.mkdirs();
        try(FileOutputStream out=new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String csprojText() {
        return """
<Project Sdk="Microsoft.NET.Sdk">
  <PropertyGroup>
    <TargetFramework>net10.0-android35.0</TargetFramework>
    <SupportedOSPlatformVersion>26</SupportedOSPlatformVersion>
    <AndroidUseLatestPlatformSdk>true</AndroidUseLatestPlatformSdk>
    <OutputType>Library</OutputType>
    <RootNamespace>Game</RootNamespace>
    <AssemblyName>Noir.Game</AssemblyName>
    <Nullable>enable</Nullable>
    <ImplicitUsings>enable</ImplicitUsings>
    <LangVersion>14.0</LangVersion>
    <EnableDefaultCompileItems>false</EnableDefaultCompileItems>
  </PropertyGroup>
  <ItemGroup>
    <Compile Include="NoirSdk/Noir.cs" />
    <Compile Include="PlayerController.cs" />
  </ItemGroup>
</Project>
""";
    }

    private static String propsText(String packageName) {
        return "<Project>\n"+
               "  <PropertyGroup>\n"+
               "    <NoirPackageName>"+packageName+"</NoirPackageName>\n"+
               "    <NoirEngineApi>Mobile</NoirEngineApi>\n"+
               "    <NoirCSharpLanguage>14</NoirCSharpLanguage>\n"+
               "    <NoirRendererApi>ForwardPlusMobile</NoirRendererApi>\n"+
               "  </PropertyGroup>\n"+
               "</Project>\n";
    }

    private static String readmeText() {
        return """
# Noir C# Mobile Scripting

This project targets net10.0-android and exposes the engine through using Noir;.

The API covers nodes, transforms, cameras, input, time, rendering materials,
lights, world environment, physics bodies, ray queries, resources, scene changes,
signals and mobile editor metadata.

The repository Roslyn host can validate and emit managed assemblies on a machine
with the .NET SDK. The Java Android editor creates the project and does not claim
that Android ART can execute arbitrary C# assemblies without a managed runtime
bridge.
""";
    }

    private static final String PLAYER_SCRIPT = """
using Noir;

namespace Game;

[NoirScript]
public sealed class PlayerController : Character3D
{
    [Export("Movement")]
    public float Speed { get; set; } = 5f;

    [Export("Movement")]
    public float JumpVelocity { get; set; } = 4.5f;

    public override void Start()
    {
        Name = "PlayerController";
        Debug.Log("C# PlayerController started");
    }

    public override void PhysicsUpdate(float delta)
    {
        ApplyGravity(delta);
        var move = Input.Vector("ui_left","ui_right","ui_up","ui_down");
        Velocity = new Vector3(move.X * Speed, Velocity.Y, move.Y * Speed);

        if(IsOnFloor && Input.IsActionJustPressed("action_jump"))
            Velocity = new Vector3(Velocity.X,JumpVelocity,Velocity.Z);

        MoveAndSlide();
    }
}
""";

    private static final String NOIR_API = """
using System;
using System.Collections.Generic;

namespace Noir;

public static class Engine
{
    public static double DeltaTime { get; internal set; }
    public static double Time { get; internal set; }
    public static void Log(string message)=>Console.WriteLine("[Noir] "+message);
}

public readonly record struct Vector2(float X,float Y)
{
    public static readonly Vector2 Zero=new(0,0);
    public float Length()=>MathF.Sqrt(X*X+Y*Y);
    public Vector2 Normalized(){var l=Length();return l<0.00001f?Zero:new(X/l,Y/l);}
}

public readonly record struct Vector3(float X,float Y,float Z)
{
    public static readonly Vector3 Zero=new(0,0,0);
    public static readonly Vector3 One=new(1,1,1);
    public float Length()=>MathF.Sqrt(X*X+Y*Y+Z*Z);
    public Vector3 Normalized(){var l=Length();return l<0.00001f?Zero:new(X/l,Y/l,Z/l);}
    public static Vector3 operator +(Vector3 a,Vector3 b)=>new(a.X+b.X,a.Y+b.Y,a.Z+b.Z);
    public static Vector3 operator -(Vector3 a,Vector3 b)=>new(a.X-b.X,a.Y-b.Y,a.Z-b.Z);
    public static Vector3 operator *(Vector3 a,float b)=>new(a.X*b,a.Y*b,a.Z*b);
    public static Vector3 operator /(Vector3 a,float b)=>new(a.X/b,a.Y/b,a.Z/b);
    public static float Dot(Vector3 a,Vector3 b)=>a.X*b.X+a.Y*b.Y+a.Z*b.Z;
}

public static class Input
{
    static readonly HashSet<string> Pressed=new(StringComparer.Ordinal);
    public static bool IsActionPressed(string a)=>Pressed.Contains(a);
    public static bool IsActionJustPressed(string a)=>IsActionPressed(a);
    public static float Axis(string n,string p)=>(IsActionPressed(p)?1:0)-(IsActionPressed(n)?1:0);
    public static Vector2 Vector(string l,string r,string u,string d)
    {
        var x=Axis(l,r);var y=Axis(d,u);var m=MathF.Sqrt(x*x+y*y);
        return m<0.00001f?Vector2.Zero:new Vector2(x/m,y/m);
    }
}

public static class Time
{
    public static double Delta=>Engine.DeltaTime;
    public static double Now=>Engine.Time;
}

public static class Mathf
{
    public static float Clamp(float v,float min,float max)=>Math.Clamp(v,min,max);
    public static float Lerp(float a,float b,float t)=>a+(b-a)*Clamp(t,0,1);
    public static float MoveToward(float a,float b,float d)=>MathF.Abs(b-a)<=d?b:a+MathF.Sign(b-a)*d;
}

public abstract class Object
{
    public string Name{get;set;}="Object";
    public bool IsValid{get;internal set;}=true;
}

public class Node:Object
{
    public Node? Parent{get;private set;}
    readonly List<Node> children=new();
    public IReadOnlyList<Node> Children=>children;
    public void AddChild(Node child){child.Parent?.RemoveChild(child);child.Parent=this;children.Add(child);}
    public void RemoveChild(Node child){if(children.Remove(child))child.Parent=null;}
    public Node? Find(string name){foreach(var c in children){if(c.Name==name)return c;var n=c.Find(name);if(n!=null)return n;}return null;}
    public virtual void Start(){}
    public virtual void Update(float delta){}
    public virtual void PhysicsUpdate(float delta){}
}

public class Node3D:Node
{
    public Vector3 Position{get;set;}
    public Vector3 RotationDegrees{get;set;}
    public Vector3 Scale{get;set;}=Vector3.One;
    public Vector3 GlobalPosition=>Position;
    public bool Visible{get;set;}=true;
}

public class Character3D:Node3D
{
    public Vector3 Velocity{get;set;}
    public bool IsOnFloor{get;internal set;}
    public float Gravity{get;set;}=9.81f;
    public void ApplyGravity(float delta){if(!IsOnFloor)Velocity+=new Vector3(0,-Gravity*delta,0);}
    public void MoveAndSlide(){}
}

public class Camera3D:Node3D
{
    public float Fov{get;set;}=70f;
    public float Near{get;set;}=0.05f;
    public float Far{get;set;}=500f;
    public bool Current{get;set;}
}

public abstract class Component:Object{public Node3D? Owner{get;internal set;}}

public sealed class Mesh3D:Node3D
{
    public string MeshPath{get;set;}="";
    public Material? Material{get;set;}
}

public class Material:Object
{
    public Color Albedo{get;set;}=Color.White;
    public float Metallic{get;set;}
    public float Roughness{get;set;}=.5f;
}

public readonly record struct Color(float R,float G,float B,float A=1f)
{
    public static readonly Color White=new(1,1,1,1);
    public static readonly Color Black=new(0,0,0,1);
}

public enum LightType{Directional,Point,Spot}

public class Light3D:Node3D
{
    public LightType Type{get;set;}
    public Color LightColor{get;set;}=Color.White;
    public float Energy{get;set;}=1f;
    public float Range{get;set;}=20f;
    public bool CastShadow{get;set;}=true;
}

public sealed class DirectionalLight3D:Light3D
{
    public DirectionalLight3D(){Type=LightType.Directional;}
}

public sealed class WorldEnvironment:Node3D
{
    public Environment Environment{get;set;}=new();
}

public sealed class Environment:Object
{
    public float Exposure{get;set;}=1f;
    public bool SunRays{get;set;}=true;
    public bool Glow{get;set;}=true;
}

public abstract class PhysicsBody3D:Node3D
{
    public float Mass{get;set;}=1f;
    public Vector3 LinearVelocity{get;set;}
    public void AddImpulse(Vector3 impulse)=>LinearVelocity+=impulse/MathF.Max(Mass,.001f);
}

public sealed class RigidBody3D:PhysicsBody3D{}
public sealed class StaticBody3D:PhysicsBody3D{}

public readonly record struct RaycastHit3D(bool Hit,Vector3 Position,Vector3 Normal,float Distance,Node3D? Collider);

public static class Physics
{
    public static RaycastHit3D Raycast(Vector3 origin,Vector3 direction,float maxDistance=100f)
        =>new(false,origin,Vector3.Zero,maxDistance,null);
}

public abstract class Resource:Object{public string ResourcePath{get;internal set;}="";}
public sealed class PackedScene:Resource{public Node Instantiate()=>new Node{Name=Name};}
public static class ResourceLoader
{
    public static T? Load<T>(string path) where T:Resource=>string.IsNullOrWhiteSpace(path)?null:Activator.CreateInstance<T>();
}

public static class SceneTree
{
    public static Node? CurrentScene{get;internal set;}
    public static void ChangeScene(string path)=>Debug.Log("ChangeScene: "+path);
    public static void Quit()=>Debug.Log("Quit requested");
}

public static class Debug
{
    public static void Log(string message)=>Engine.Log(message);
    public static void Warning(string message)=>Engine.Log("WARNING: "+message);
}
""";

}