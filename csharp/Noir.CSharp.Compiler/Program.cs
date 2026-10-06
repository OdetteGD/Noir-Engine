using System.Reflection;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;

static int Main(string[] args)
{
    if(args.Length == 0 || args[0] is "--help" or "-h")
    {
        Console.WriteLine("Noir C# compiler host");
        Console.WriteLine("  validate <file.cs> [file.cs ...]");
        Console.WriteLine("  compile <output.dll> <file.cs> [file.cs ...]");
        return 0;
    }

    var mode=args[0].ToLowerInvariant();
    if(mode!="validate" && mode!="compile")
    {
        Console.Error.WriteLine("Unknown command: "+mode);
        return 2;
    }

    var output = mode=="compile" ? args.ElementAtOrDefault(1) : null;
    var start = mode=="compile" ? 2 : 1;
    if(args.Length<=start)
    {
        Console.Error.WriteLine("No C# source files supplied.");
        return 2;
    }

    var sources=args.Skip(start).Select(File.ReadAllText).ToArray();
    var trees=sources.Select((s,i)=>CSharpSyntaxTree.ParseText(
        s,
        CSharpParseOptions.Default.WithLanguageVersion(LanguageVersion.CSharp12),
        path:Path.GetFileName(args[start+i]))).ToArray();

    var refs=new List<MetadataReference>();
    foreach(var asm in AppDomain.CurrentDomain.GetAssemblies())
    {
        if(!asm.IsDynamic && !string.IsNullOrWhiteSpace(asm.Location))
            refs.Add(MetadataReference.CreateFromFile(asm.Location));
    }

    var noir=typeof(Noir.Engine).Assembly;
    if(!string.IsNullOrWhiteSpace(noir.Location))
        refs.Add(MetadataReference.CreateFromFile(noir.Location));

    var compilation=CSharpCompilation.Create(
        assemblyName:output==null?"NoirScriptValidation":Path.GetFileNameWithoutExtension(output),
        syntaxTrees:trees,
        references:refs.DistinctBy(r=>r.Display,StringComparer.OrdinalIgnoreCase),
        options:new CSharpCompilationOptions(OutputKind.DynamicallyLinkedLibrary,
            optimizationLevel:OptimizationLevel.Release,
            nullableContextOptions:NullableContextOptions.Enable));

    var errors=compilation.GetDiagnostics()
        .Where(d=>d.Severity is DiagnosticSeverity.Error or DiagnosticSeverity.Warning)
        .ToArray();

    foreach(var d in errors)
        Console.WriteLine($"{d.Severity}: {d.Id}: {d.GetMessage()}");

    if(errors.Any(d=>d.Severity==DiagnosticSeverity.Error)) return 1;

    if(mode=="compile")
    {
        Directory.CreateDirectory(Path.GetDirectoryName(Path.GetFullPath(output!))!);
        using var stream=File.Create(output!);
        var emit=compilation.Emit(stream);
        if(!emit.Success)
        {
            foreach(var d in emit.Diagnostics) Console.WriteLine($"{d.Severity}: {d.Id}: {d.GetMessage()}");
            return 1;
        }
        Console.WriteLine("Compiled "+output);
    }
    return 0;
}
