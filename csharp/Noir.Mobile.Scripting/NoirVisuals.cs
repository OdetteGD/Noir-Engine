namespace Noir;

public enum ProjectionType{Perspective,Orthographic}
public enum FogMode{Disabled,Depth,Height,Volumetric}
public sealed class Decal:Node3D{public string TexturePath{get;set;}="";public Color Modulate{get;set;}=Color.White;public Vector3 Size{get;set;}=new(1,1,1);public float FadeNear{get;set;}public float FadeFar{get;set;}=10;public uint CullMask{get;set;}=0xffffffff;}
public sealed class ReflectionProbe:Node3D{public Vector3 Size{get;set;}=new(10,5,10);public float UpdateInterval{get;set;}=.5f;public bool Interior{get;set;}public bool Enabled{get;set;}=true;}
public sealed class FogVolume:Node3D{public FogMode Mode{get;set;}=FogMode.Volumetric;public Color Albedo{get;set;}=Color.White;public float Density{get;set;}=.02f;public float Height{get;set;}=10;}
public sealed class MeshInstance3D:Mesh3D{public int SurfaceCount{get;set;}=1;public bool GenerateCollision{get;set;}public bool GenerateNavigation{get;set;}}
public sealed class MultiMeshInstance3D:Mesh3D{public int InstanceCount{get;set;}public float VisibilityRange{get;set;}=100;public bool InstanceLodEnabled{get;set;}=true;}
public sealed class Sprite3D:Node3D{public string TexturePath{get;set;}="";public Color Modulate{get;set;}=Color.White;public bool Billboard{get;set;}=true;public Vector2 PixelSize{get;set;}=new(1,1);}
public sealed class Sky:Resource{public Color TopColor{get;set;}=new(.08f,.12f,.25f);public Color HorizonColor{get;set;}=new(.55f,.65f,.8f);public float SunAngle{get;set;}=35;public float CloudDensity{get;set;}=.35f;}
public sealed class ShaderMaterial:Material{public string ShaderSource{get;set;}="";public Dictionary<string,object?> Parameters{get;}=new(StringComparer.Ordinal);public void SetParameter(string name,object? value)=>Parameters[name]=value;public T? GetParameter<T>(string name)=>Parameters.TryGetValue(name,out var value)&&value is T typed?typed:default;}
public sealed class Viewport:Node{public Vector2 Size{get;set;}=new(1920,1080);public bool Hdr2D{get;set;}=true;public float RenderScale{get;set;}=1;public int MsaaSamples{get;set;}=4;public bool TransparentBg{get;set;}}
public sealed class LODGroup:Node3D{public float[] Distances{get;set;}=new[]{20,45,90};public int ActiveLevel{get;private set;}public void UpdateDistance(float distance){ActiveLevel=0;while(ActiveLevel<Distances.Length&&distance>Distances[ActiveLevel])ActiveLevel++;}}
