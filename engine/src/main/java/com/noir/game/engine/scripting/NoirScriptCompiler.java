package com.noir.game.engine.scripting;

import java.util.*;
import java.util.regex.*;

/** Front-end validator for the Noir .game scripting language. It intentionally
 * preserves source text instead of generating hidden code, allowing the mobile IDE
 * to display exact diagnostics and later compiler backends to target different runtimes. */
public final class NoirScriptCompiler {
    public enum Severity { INFO, WARNING, ERROR }
    public static final class Diagnostic { public int line,column; public Severity severity; public String message; public String toString(){return severity+" L"+line+":"+column+" "+message;} }
    public static final class CompileResult { public final List<Diagnostic> diagnostics=new ArrayList<>(); public boolean success(){return diagnostics.stream().noneMatch(d->d.severity==Severity.ERROR);} }

    private static final Set<String> KEYWORDS=new HashSet<>(Arrays.asList("entity","scene","component","property","input","signal","start","update","process","physics","if","else","for","while","return","true","false","null","type","extends","function"));

    public CompileResult validate(String source){
        CompileResult out=new CompileResult(); String[] lines=source.replace("\r","").split("\n",-1); int braces=0; boolean inEntity=false;
        for(int i=0;i<lines.length;i++){
            String line=lines[i].trim(); if(line.isEmpty()||line.startsWith("#"))continue;
            for(char c:line.toCharArray()){if(c=='{')braces++; if(c=='}')braces--; if(braces<0){add(out,i+1,1,Severity.ERROR,"unexpected '}'");braces=0;}}
            if(line.startsWith("entity ")) inEntity=true;
            if(line.startsWith("update(")||line.startsWith("physics(")) if(!inEntity)add(out,i+1,1,Severity.ERROR,"lifecycle function must belong to an entity");
            if(line.matches(".*\\b(property|input)\\s+[^A-Za-z_].*"))add(out,i+1,1,Severity.ERROR,"identifier must start with a letter or underscore");
            Matcher m=Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b").matcher(line);
            while(m.find()){String token=m.group(1); if(token.equals("todo"))add(out,i+1,m.start()+1,Severity.WARNING,"TODO remains in script");}
        }
        if(braces!=0)add(out,lines.length,1,Severity.ERROR,"unbalanced braces: "+braces);
        if(!source.contains("entity "))add(out,1,1,Severity.WARNING,"script contains no entity declaration");
        return out;
    }
    private void add(CompileResult r,int line,int col,Severity s,String msg){Diagnostic d=new Diagnostic();d.line=line;d.column=col;d.severity=s;d.message=msg;r.diagnostics.add(d);}
    public List<String> complete(String prefix){
        List<String> result=new ArrayList<>(); for(String k:KEYWORDS)if(k.startsWith(prefix))result.add(k); Collections.sort(result); return result;
    }
}
