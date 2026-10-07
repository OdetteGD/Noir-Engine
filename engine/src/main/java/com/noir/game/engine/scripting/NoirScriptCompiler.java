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

    private static final Pattern DECLARATION=Pattern.compile("^(entity|component|scene|function|property|input|signal)\\s+([A-Za-z_][A-Za-z0-9_]*)\\s*(.*)$");
    private static final Set<String> KEYWORDS=new HashSet<>(Arrays.asList("entity","scene","component","property","input","signal","start","update","process","physics","if","else","for","while","return","true","false","null","type","extends","function"));

    public CompileResult validate(String source){
        CompileResult out=new CompileResult(); String[] lines=source.replace("\r","").split("\n",-1);
        int braces=0,parens=0,brackets=0; boolean inEntity=false,inString=false;
        for(int i=0;i<lines.length;i++){
            String raw=lines[i], line=raw.trim(); if(line.isEmpty()||line.startsWith("#"))continue;
            boolean comment=false;
            for(int col=0;col<raw.length();col++){
                char c=raw.charAt(col);
                if(!inString&&c=='#'){comment=true;break;}
                if(!comment&&c=='"'&&(col==0||raw.charAt(col-1)!='\\'))inString=!inString;
                if(comment||inString)continue;
                if(c=='{')braces++; else if(c=='}')braces--; else if(c=='(')parens++; else if(c==')')parens--; else if(c=='[')brackets++; else if(c==']')brackets--;
                if(braces<0){add(out,i+1,col+1,Severity.ERROR,"unexpected '}'");braces=0;}
                if(parens<0){add(out,i+1,col+1,Severity.ERROR,"unexpected ')'");parens=0;}
                if(brackets<0){add(out,i+1,col+1,Severity.ERROR,"unexpected ']'");brackets=0;}
            }
            Matcher decl=DECLARATION.matcher(line);
            if(line.startsWith("entity "))inEntity=true;
            if(decl.matches()){
                String kind=decl.group(1),rest=decl.group(3).trim();
                if((kind.equals("entity")||kind.equals("component")||kind.equals("scene"))&&!rest.endsWith("{"))add(out,i+1,1,Severity.ERROR,kind+" declaration must end with '{'");
                if((kind.equals("property")||kind.equals("input")||kind.equals("signal"))&&rest.isEmpty())add(out,i+1,1,Severity.ERROR,kind+" requires a name");
            }
            if((line.startsWith("update(")||line.startsWith("physics("))&&!inEntity)add(out,i+1,1,Severity.ERROR,"lifecycle function must belong to an entity");
            if(line.matches(".*\\b(property|input|signal)\\s+[^A-Za-z_].*"))add(out,i+1,1,Severity.ERROR,"identifier must start with a letter or underscore");
            if(line.matches(".*\\b(if|for|while)\\s*\\([^)]*\\)\\s*[^\\{].*"))add(out,i+1,1,Severity.ERROR,"control statement must use a block '{ ... }'");
            if(line.endsWith("="))add(out,i+1,Math.max(1,raw.length()),Severity.ERROR,"incomplete assignment");
            Matcher m=Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\b").matcher(line);
            while(m.find())if(m.group(1).equals("todo"))add(out,i+1,m.start()+1,Severity.WARNING,"TODO remains in script");
        }
        if(inString)add(out,lines.length,1,Severity.ERROR,"unclosed string literal");
        if(braces!=0)add(out,lines.length,1,Severity.ERROR,"unbalanced braces: "+braces);
        if(parens!=0)add(out,lines.length,1,Severity.ERROR,"unbalanced parentheses: "+parens);
        if(brackets!=0)add(out,lines.length,1,Severity.ERROR,"unbalanced brackets: "+brackets);
        if(!source.contains("entity "))add(out,1,1,Severity.WARNING,"script contains no entity declaration");
        return out;
    }
    private void add(CompileResult r,int line,int col,Severity s,String msg){Diagnostic d=new Diagnostic();d.line=line;d.column=col;d.severity=s;d.message=msg;r.diagnostics.add(d);}
    public List<String> complete(String prefix){
        List<String> result=new ArrayList<>(); for(String k:KEYWORDS)if(k.startsWith(prefix))result.add(k); Collections.sort(result); return result;
    }
}
