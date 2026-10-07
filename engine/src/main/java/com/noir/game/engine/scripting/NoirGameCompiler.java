package com.noir.game.engine.scripting;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Builds Noir .game scripts into validated, cacheable NGBC bytecode artifacts. */
public final class NoirGameCompiler {
    public static final String BYTECODE_EXTENSION=".ngbc";

    public static final class Diagnostic {
        public final String path;
        public final int line;
        public final String severity;
        public final String message;
        public Diagnostic(String p,int l,String s,String m){path=p;line=l;severity=s;message=m;}
        @Override public String toString(){return severity+" "+path+":"+line+" "+message;}
    }

    public static final class Result {
        public final List<Diagnostic> diagnostics=new ArrayList<>();
        public int compiledScripts;
        public int validatedScenes;
        public boolean success(){
            for(Diagnostic d:diagnostics)if("ERROR".equals(d.severity))return false;
            return true;
        }
    }

    private final NoirScriptCompiler scriptCompiler=new NoirScriptCompiler();
    private final com.noir.game.engine.core.GameFileParser sceneParser=new com.noir.game.engine.core.GameFileParser();

    public Result compileProject(File root)throws IOException{
        Result result=new Result();
        if(root==null||!root.isDirectory()){
            result.diagnostics.add(new Diagnostic("<project>",1,"ERROR","project root does not exist"));
            return result;
        }
        compileTree(root,result);
        return result;
    }

    private void compileTree(File dir,Result result)throws IOException{
        File[] files=dir.listFiles();
        if(files==null)return;
        for(File file:files){
            if(file.isDirectory()){
                if(!file.getName().equals("bin")&&!file.getName().equals("obj")&&!file.getName().equals(".git"))
                    compileTree(file,result);
                continue;
            }
            String name=file.getName().toLowerCase(Locale.US);
            if(!name.endsWith(".game"))continue;
            String source=read(file);
            if(name.contains("scene")||file.getParentFile().getName().equalsIgnoreCase("scenes")||source.trim().startsWith("scene ")){
                com.noir.game.engine.core.GameFileParser.Result parsed=sceneParser.parse(source,file.getPath());
                for(com.noir.game.engine.core.GameFileParser.Diagnostic d:parsed.diagnostics)
                    result.diagnostics.add(new Diagnostic(file.getPath(),d.line,d.severity,d.message));
                if(parsed.ok())result.validatedScenes++;
            }else{
                NoirScriptCompiler.CompileResult checked=scriptCompiler.validate(source);
                for(NoirScriptCompiler.Diagnostic d:checked.diagnostics)
                    result.diagnostics.add(new Diagnostic(file.getPath(),d.line,d.severity.name(),d.message));
                if(checked.success()){
                    String baseName=file.getName();
                    if(baseName.toLowerCase(Locale.US).endsWith(".game"))
                        baseName=baseName.substring(0,baseName.length()-5);
                    File out=new File(file.getParentFile(),baseName+BYTECODE_EXTENSION);
                    write(out,NoirGameBytecode.compileText(source).serialize());
                    result.compiledScripts++;
                }
            }
        }
    }

    private static String read(File file)throws IOException{
        try(InputStream in=new FileInputStream(file)){return new String(in.readAllBytes(),StandardCharsets.UTF_8);}
    }
    private static void write(File file,byte[] data)throws IOException{
        File parent=file.getParentFile();
        if(parent!=null&&!parent.isDirectory()&&!parent.mkdirs())throw new IOException("Cannot create "+parent);
        try(OutputStream out=new FileOutputStream(file)){out.write(data);}
    }
}
