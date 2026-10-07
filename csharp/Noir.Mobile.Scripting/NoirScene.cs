namespace Noir;

public abstract class NoirResource : Object
{
    public string ResourcePath { get; internal set; } = "";
}

public sealed class PackedScene : NoirResource
{
    public Node Instantiate()
    {
        var root=new Node { Name=Name };
        return root;
    }
}

public static class ResourceLoader
{
    public static T? Load<T>(string path) where T : NoirResource,new()
    {
        if(string.IsNullOrWhiteSpace(path)) return null;
        return new T();
    }
}

public static class SceneTree
{
    public static Node? CurrentScene { get; internal set; }
    public static void ChangeScene(string path) => Debug.Log("ChangeScene: "+path);
    public static void Quit() => Debug.Log("Quit requested");
}

public static class Signals
{
    public static event Action<string>? Emitted;
    public static void Emit(string signal,params object[] args) => Emitted?.Invoke(signal);
}

public static class GD
{
    public static void Print(object? value) => Console.WriteLine("[Noir] "+value);
    public static void PushWarning(string message) => Console.WriteLine("[Noir WARNING] "+message);
    public static void PushError(string message) => Console.Error.WriteLine("[Noir ERROR] "+message);
}