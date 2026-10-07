namespace Noir;

public sealed class Timer:Node
{
    public float WaitTime{get;set;}=1;public bool OneShot{get;set;}public bool Autostart{get;set;}public bool IsStopped{get;private set;}=true;public event Action? Timeout;
    public void Start(float time=-1){if(time>=0)WaitTime=time;IsStopped=false;}public void Stop(){IsStopped=true;}
    public void Tick(float delta){if(IsStopped)return;WaitTime-=delta;if(WaitTime<=0){Timeout?.Invoke();if(OneShot)IsStopped=true;else WaitTime=1;}}
}
public sealed class RandomNumberGenerator
{
    private readonly Random Random;public RandomNumberGenerator(int seed=0){Random=seed==0?new Random():new Random(seed);}
    public float Randf()=>Random.NextSingle();public float RandfRange(float min,float max)=>min+Random.NextSingle()*(max-min);public int RandiRange(int min,int max)=>Random.Next(min,max+1);
}
public readonly record struct Plane3D(Vector3 Normal,float D){public float DistanceTo(Vector3 p)=>Vector3.Dot(Normal,p)+D;public Vector3 Project(Vector3 p)=>p-Normal*DistanceTo(p);}
public readonly record struct Aabb(Vector3 Position,Vector3 Size)
{
    public Vector3 End=>Position+Size;public Vector3 Center=>Position+Size*.5f;
    public bool Contains(Vector3 p)=>p.X>=Position.X&&p.X<=End.X&&p.Y>=Position.Y&&p.Y<=End.Y&&p.Z>=Position.Z&&p.Z<=End.Z;
    public bool Intersects(Aabb b)=>Position.X<=b.End.X&&End.X>=b.Position.X&&Position.Y<=b.End.Y&&End.Y>=b.Position.Y&&Position.Z<=b.End.Z&&End.Z>=b.Position.Z;
}
public static class Geometry3D
{
    public static bool SegmentIntersectsSphere(Vector3 from,Vector3 to,Vector3 center,float radius){var d=to-from;var l=d.LengthSquared();if(l<1e-8f)return Vector3.Distance(from,center)<=radius;var t=Mathf.Clamp01(Vector3.Dot(center-from,d)/l);return Vector3.Distance(from+d*t,center)<=radius;}
}
public static class OS{public static string GetName()=>OperatingSystem.IsAndroid()?"Android":OperatingSystem.IsWindows()?"Windows":OperatingSystem.IsLinux()?"Linux":"Unknown";public static int ProcessorCount=>Environment.ProcessorCount;public static string VersionString=>Environment.OSVersion.VersionString;}
public static class ProjectSettings
{
    private static readonly Dictionary<string,object?> Values=new(StringComparer.Ordinal);
    public static void Set(string key,object? value)=>Values[key]=value;
    public static T Get<T>(string key,T fallback=default!)=>Values.TryGetValue(key,out var v)&&v is T t?t:fallback;
}
public static class DisplayServer{public static Vector2 WindowSize{get;internal set;}=new(1920,1080);public static bool IsTouchscreenAvailable=>true;}
