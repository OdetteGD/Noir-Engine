namespace Noir;

public static class ResourceCache
{
    private static readonly Dictionary<string,NoirResource> Cache=new(StringComparer.Ordinal);
    public static T? Get<T>(string path) where T:NoirResource=>Cache.TryGetValue(path,out var value)?value as T:null;
    public static void Put(string path,NoirResource resource)=>Cache[path]=resource;
    public static void Clear()=>Cache.Clear();
}
public static class Performance
{
    public static float TargetFps{get;set;}=60;
    public static float FrameTimeMs=>Engine.DeltaTime<=0?0:(float)(Engine.DeltaTime*1000);
    public static float Fps=>Engine.DeltaTime<=0?0:(float)(1.0/Engine.DeltaTime);
    public static int ManagedMemoryMb=>(int)(GC.GetTotalMemory(false)/1024/1024);
}
public static class GCControl{public static void Collect(){GC.Collect();GC.WaitForPendingFinalizers();}}
