namespace Noir;

public readonly record struct Color(float R,float G,float B,float A=1f)
{
    public static readonly Color White=new(1,1,1,1);
    public static readonly Color Black=new(0,0,0,1);
    public static readonly Color Clear=new(0,0,0,0);
    public Color WithAlpha(float a)=>new(R,G,B,a);
}

public readonly record struct Quaternion(float X,float Y,float Z,float W)
{
    public static readonly Quaternion Identity=new(0,0,0,1);
}

public readonly record struct Basis(Quaternion Rotation)
{
    public static readonly Basis Identity=new(Quaternion.Identity);
}

public readonly record struct Transform3D(Basis Basis,Vector3 Origin)
{
    public static readonly Transform3D Identity=new(Basis.Identity,Vector3.Zero);
}

public enum ToneMapper { None, Reinhard, ACES, AgX }

public enum LightType { Directional, Point, Spot }

public class Mesh3D : Node3D
{
    public string MeshPath { get; set; } = "";
    public Material? Material { get; set; }
    public bool CastShadows { get; set; } = true;
    public bool ReceiveShadows { get; set; } = true;
    public float LodBias { get; set; } = 1f;
}

public class Material : Object
{
    public Color Albedo { get; set; } = Color.White;
    public float Metallic { get; set; }
    public float Roughness { get; set; } = 0.5f;
    public float EmissionStrength { get; set; }
}

public sealed class StandardMaterial3D : Material
{
    public Color Emission { get; set; } = Color.Black;
    public bool Transparent { get; set; }
    public bool DoubleSided { get; set; }
}

public class Light3D : Node3D
{
    public LightType Type { get; set; }
    public Color LightColor { get; set; } = Color.White;
    public float Energy { get; set; } = 1f;
    public float Range { get; set; } = 20f;
    public bool CastShadow { get; set; } = true;
}

public sealed class DirectionalLight3D : Light3D
{
    public float AngularSize { get; set; } = 0.5f;
    public DirectionalLight3D(){ Type=LightType.Directional; }
}

public sealed class WorldEnvironment : Node3D
{
    public Environment Environment { get; set; } = new();
}

public sealed class Environment : Object
{
    public Color BackgroundColor { get; set; } = new(0.05f,0.08f,0.14f);
    public Color AmbientColor { get; set; } = new(0.25f,0.3f,0.4f);
    public float Exposure { get; set; } = 1f;
    public ToneMapper ToneMapper { get; set; } = ToneMapper.ACES;
    public bool VolumetricFog { get; set; } = true;
    public float FogDensity { get; set; } = 0.02f;
    public bool Glow { get; set; } = true;
    public bool SunRays { get; set; } = true;
}