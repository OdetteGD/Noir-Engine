using Noir;

namespace Noir.Editor;

public sealed record ScriptDiagnostic(string Severity, string Message);

public sealed class NoirEditor
{
    public string ProjectRoot { get; }
    public string ScriptRoot => Path.Combine(ProjectRoot, "scripts", "csharp");

    public NoirEditor(string projectRoot)
    {
        ProjectRoot = Path.GetFullPath(projectRoot);
        Directory.CreateDirectory(ScriptRoot);
    }

    public IReadOnlyList<string> EnumerateScripts() =>
        Directory.Exists(ScriptRoot)
            ? Directory.EnumerateFiles(ScriptRoot, "*.cs", SearchOption.AllDirectories).ToArray()
            : Array.Empty<string>();

    public string CreateScript(string name, string? source = null)
    {
        if (string.IsNullOrWhiteSpace(name)) throw new ArgumentException("Script name is required.", nameof(name));
        var typeName = Path.GetFileNameWithoutExtension(name);
        var safe = typeName + ".cs";
        var path = Path.Combine(ScriptRoot, safe);
        if (!File.Exists(path))
        {
            source ??= "using Noir;\n\npublic sealed class " + typeName +
                       " : Character3D\n{\n    public override void _Process(float delta) { }\n}\n";
            File.WriteAllText(path, source);
        }
        return path;
    }
}
