package com.noir.game.engine.scripting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

public final class NoirCSharpProjectService {
    private static final Pattern PACKAGE=Pattern.compile("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*$");

    public static final class Result {
        public final File root,project,script;
        public final String packageName;
        public Result(File r,File p,File s,String n){root=r;project=p;script=s;packageName=n;}
    }

    private NoirCSharpProjectService(){}

    public static Result ensure(File projectRoot,String packageName) throws IOException {
        if(projectRoot==null) throw new IOException("No Noir project root");
        File root=new File(projectRoot,"scripts/csharp");
        File sdk=new File(root,"NoirSdk");
        if(!sdk.mkdirs() && !sdk.isDirectory()) throw new IOException("Unable to create C# SDK directory");
        String pkg=validPackage(packageName) ? packageName : "com.noir.game.scripts";

        File csproj=new File(root,"Noir.Game.csproj");
        File script=new File(root,"PlayerController.cs");
        File sdkSource=new File(sdk,"Noir.cs");
        write(csproj,csprojText());
        write(new File(root,"Directory.Build.props"),propsText(pkg));
        if(!sdkSource.isFile()) write(sdkSource,NOIR_API);
        if(!script.isFile()) write(script,PLAYER_SCRIPT);
        write(new File(root,"README.md"),readmeText());
        return new Result(root,csproj,script,pkg);
    }

    public static boolean validPackage(String packageName){
        return packageName!=null && PACKAGE.matcher(packageName).matches();
    }

    private static void write(File file,String text)throws IOException{
        File parent=file.getParentFile(); if(parent!=null)parent.mkdirs();
        try(FileOutputStream out=new FileOutputStream(file)){
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    private static String csprojText(){
        return "<Project Sdk="Microsoft.NET.Sdk">\n"+
        "  <PropertyGroup>\n"+
        "    <TargetFramework>net8.0-android</TargetFramework>\n"+
        "    <SupportedOSPlatformVersion>26</SupportedOSPlatformVersion>\n"+
        "    <OutputType>Library</OutputType>\n"+
        "    <RootNamespace>Game</RootNamespace>\n"+
        "    <AssemblyName>Noir.Game</AssemblyName>\n"+
        "    <Nullable>enable</Nullable>\n"+
        "    <ImplicitUsings>enable</ImplicitUsings>\n"+
        "    <EnableDefaultCompileItems>false</EnableDefaultCompileItems>\n"+
        "  </PropertyGroup>\n"+
        "  <ItemGroup>\n"+
        "    <Compile Include="NoirSdk/Noir.cs" />\n"+
        "    <Compile Include="PlayerController.cs" />\n"+
        "  </ItemGroup>\n"+
        "</Project>\n";
    }

    private static String propsText(String packageName){
        return "<Project>\n  <PropertyGroup>\n"+
        "    <NoirPackageName>"+packageName+"</NoirPackageName>\n"+
        "    <NoirEngineApi>Mobile</NoirEngineApi>\n"+
        "    <NoirCSharpLanguage>12</NoirCSharpLanguage>\n"+
        "  </PropertyGroup>\n</Project>\n";
    }

    private static String readmeText(){
        return "# Noir C# Mobile Scripting\n\n"+
        "This project targets net8.0-android and exposes the engine API through using Noir;.\n"+
        "It is self-contained so a Noir project can be copied or exported without desktop paths.\n\n"+
        "The Roslyn host in the engine repository performs validation and assembly emission on a machine with the .NET SDK.\n"+
        "The Java Android editor creates this project but does not pretend that the Android ART process is a CLR.\n";
    }

    private static final String PLAYER_SCRIPT=
        "using Noir;\n\nnamespace Game;\n\n"+
        "public sealed class PlayerController : Character3D\n{\n"+
        "    public float Speed { get; set; } = 5f;\n"+
        "    public float JumpVelocity { get; set; } = 4.5f;\n\n"+
        "    public override void Start() => Debug.Log("C# PlayerController started");\n\n"+
        "    public override void PhysicsUpdate(float delta)\n    {\n"+
        "        var move = Input.Vector("ui_left","ui_right","ui_up","ui_down");\n"+
        "        Velocity = new Vector3(move.X * Speed, Velocity.Y, move.Y * Speed);\n"+
        "        if(IsOnFloor && Input.IsActionJustPressed("action_jump"))\n"+
        "            Velocity = new Vector3(Velocity.X,JumpVelocity,Velocity.Z);\n"+
        "        MoveAndSlide();\n"+
        "    }\n}\n";

    private static final String NOIR_API=
        "using System;\nusing System.Collections.Generic;\n\nnamespace Noir;\n\n"+
        "public static class Engine { public static double DeltaTime { get; internal set; } public static double Time { get; internal set; } public static void Log(string message)=>Console.WriteLine("[Noir] "+message); }\n"+
        "public readonly record struct Vector2(float X,float Y) { public static readonly Vector2 Zero=new(0,0); public Vector2 Normalized(){var l=MathF.Sqrt(X*X+Y*Y);return l<0.00001f?Zero:new(X/l,Y/l);} }\n"+
        "public readonly record struct Vector3(float X,float Y,float Z) { public static readonly Vector3 Zero=new(0,0,0); public static readonly Vector3 One=new(1,1,1); public static Vector3 operator +(Vector3 a,Vector3 b)=>new(a.X+b.X,a.Y+b.Y,a.Z+b.Z); public static Vector3 operator *(Vector3 a,float b)=>new(a.X*b,a.Y*b,a.Z*b); }\n"+
        "public static class Input { static readonly HashSet<string> Pressed=new(StringComparer.Ordinal); public static bool IsActionPressed(string a)=>Pressed.Contains(a); public static bool IsActionJustPressed(string a)=>IsActionPressed(a); public static float Axis(string n,string p)=>(IsActionPressed(p)?1:0)-(IsActionPressed(n)?1:0); public static Vector2 Vector(string l,string r,string u,string d){var x=Axis(l,r);var y=Axis(d,u);var m=MathF.Sqrt(x*x+y*y);return m<0.00001f?Vector2.Zero:new Vector2(x/m,y/m);} }\n"+
        "public class Node { public string Name{get;set;}="Node"; public virtual void Start(){} public virtual void Update(float delta){} public virtual void PhysicsUpdate(float delta){} }\n"+
        "public class Node3D:Node { public Vector3 Position{get;set;} public Vector3 RotationDegrees{get;set;} public Vector3 Scale{get;set;}=Vector3.One; }\n"+
        "public class Character3D:Node3D { public Vector3 Velocity{get;set;} public bool IsOnFloor{get;internal set;} public void MoveAndSlide(){} }\n"+
        "public class Camera3D:Node3D { public float Fov{get;set;}=70f; }\n"+
        "public static class Debug { public static void Log(string message)=>Engine.Log(message); public static void Warning(string message)=>Engine.Log("WARNING: "+message); }\n";
}
