namespace Noir;

public sealed class Terrain3D:Node3D{public string HeightmapPath{get;set;}="";public float HeightScale{get;set;}=20;public float ChunkSize{get;set;}=32;public int Resolution{get;set;}=64;public int LodCount{get;set;}=6;public bool CollisionEnabled{get;set;}=true;public bool CastShadows{get;set;}=true;}
public sealed class Foliage3D:Node3D{public string MeshPath{get;set;}="";public int InstanceCount{get;set;}public float Density{get;set;}=1;public float ViewDistance{get;set;}=80;public bool CastShadows{get;set;}=true;}
public sealed class Water3D:Node3D{public Color SurfaceColor{get;set;}=new(.05f,.25f,.35f,.8f);public float WaveHeight{get;set;}=.15f;public float WaveSpeed{get;set;}=1;public float FresnelPower{get;set;}=5;public bool Reflections{get;set;}=true;public bool Refractions{get;set;}=true;}
