package com.noir.game.engine;

import android.content.Context;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class NoirProjectWorkspace {
    private final File root;

    public NoirProjectWorkspace(Context context) {
        root = new File(context.getExternalFilesDir(null), "NoirProjects");
        if (!root.exists()) root.mkdirs();
    }

    public File root() { return root; }

    public File create(String name, String packageName) throws IOException {
        String safe = safeName(name);
        File project = new File(root, safe);
        if (project.exists()) throw new IOException("Project already exists: " + safe);
        createTree(project);
        write(new File(project, "project.game"),
                "project " + safe + "\n" +
                "name = " + safe + "\n" +
                "package = " + packageName + "\n" +
                "main_scene = scenes/Main.game\n" +
                "renderer = mobile_pbr\n" +
                "version = 1\n");
        write(new File(project, "scenes/Main.game"),
                "scene Main\n" +
                "node World {\n" +
                " type = NODE3D\n" +
                "}\n" +
                "node MainCamera {\n" +
                " type = CAMERA3D\n" +
                " position = (0, 2, 6)\n" +
                "}\n");
        write(new File(project, "csharp/Noir.Game/Noir.Game.csproj"),
                "<Project Sdk=\"Microsoft.NET.Sdk\">\n" +
                "  <PropertyGroup>\n" +
                "    <TargetFramework>net10.0-android36.1</TargetFramework>\n" +
                "    <Nullable>enable</Nullable>\n" +
                "    <ImplicitUsings>enable</ImplicitUsings>\n" +
                "    <LangVersion>14.0</LangVersion>\n" +
                "  </PropertyGroup>\n" +
                "  <ItemGroup>\n" +
                "    <Compile Include=\"PlayerController.cs\" />\n" +
                "  </ItemGroup>\n" +
                "</Project>\n");
        write(new File(project, "csharp/Noir.Game/PlayerController.cs"),
                "using Noir;\n\n" +
                "public sealed class PlayerController : Character3D {\n" +
                "    [Export] public float Speed { get; set; } = 5f;\n" +
                "    public override void _PhysicsProcess(float delta) {\n" +
                "        MoveAndSlide();\n" +
                "    }\n" +
                "}\n");
        write(new File(project, "csharp/README.md"),
                "# Noir C# Project\n\n" +
                "C# scripts live in `csharp/Noir.Game/`. The engine C# SDK is shipped with the Noir Engine source/SDK package.\n");
        write(new File(project, "scripts/player.game"),
                "entity PlayerController {\n" +
                " type: Character3D\n" +
                " property speed: 5.0\n" +
                " physics(delta) {\n" +
                "   move_and_slide()\n" +
                " }\n" +
                "}\n");
        write(new File(project, "assets/.gitkeep"), "");
        write(new File(project, "materials/.gitkeep"), "");
        write(new File(project, "textures/.gitkeep"), "");
        write(new File(project, "models/.gitkeep"), "");
        write(new File(project, "animations/.gitkeep"), "");
        write(new File(project, "shaders/.gitkeep"), "");
        write(new File(project, "audio/.gitkeep"), "");
        write(new File(project, "export/.gitkeep"), "");
        return project;
    }

    public List<File> listProjects() {
        File[] files = root.listFiles(File::isDirectory);
        List<File> result = new ArrayList<>();
        if (files != null) {
            Arrays.sort(files, Comparator.comparing(File::getName, String.CASE_INSENSITIVE_ORDER));
            for (File f : files) if (new File(f, "project.game").isFile()) result.add(f);
        }
        return result;
    }

    public File projectFile(File project, String relative) {
        File target = new File(project, relative);
        try {
            String base = project.getCanonicalPath() + File.separator;
            String path = target.getCanonicalPath();
            if (!path.startsWith(base)) throw new SecurityException("Path escapes project");
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
        return target;
    }

    private void createTree(File p) {
        new File(p, "scenes").mkdirs();
        new File(p, "scripts").mkdirs();
        new File(p, "csharp").mkdirs();
        new File(p, "csharp/Noir.Game").mkdirs();
        new File(p, "assets").mkdirs();
        new File(p, "materials").mkdirs();
        new File(p, "textures").mkdirs();
        new File(p, "models").mkdirs();
        new File(p, "animations").mkdirs();
        new File(p, "shaders").mkdirs();
        new File(p, "audio").mkdirs();
        new File(p, "export").mkdirs();
    }

    public static void write(File file, String text) throws IOException {
        File parent=file.getParentFile();
        if(parent!=null) parent.mkdirs();
        try(FileOutputStream out=new FileOutputStream(file)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
        }
    }

    public static String safeName(String value) {
        String s=value==null?"Untitled":value.trim();
        s=s.replaceAll("[^A-Za-z0-9._-]+","_");
        if(s.isEmpty()) s="Untitled";
        return s;
    }
}
