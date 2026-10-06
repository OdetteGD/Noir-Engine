package com.noir.game.engine.editor;

import com.noir.game.engine.scripting.NoirScriptCompiler;
import java.util.*;

/** Mutable source document for the Noir mobile IDE. It tracks edits, cursor-friendly
 * insert/replace operations, diagnostics and keyword completion without Android UI coupling. */
public final class ScriptDocument {
    public String path="scripts/player.game";
    public String text="";
    public boolean dirty;
    public int cursor;
    public int selectionStart;
    public int selectionEnd;
    public final NoirScriptCompiler compiler=new NoirScriptCompiler();
    public List<NoirScriptCompiler.Diagnostic> diagnostics(){return compiler.validate(text).diagnostics;}
    public List<String> completion(String prefix){return compiler.complete(prefix==null?"":prefix);}
    public void setText(String source){text=source==null?"":source;cursor=text.length();selectionStart=selectionEnd=cursor;dirty=false;}
    public void insert(int index,String s){if(s==null)return;index=Math.max(0,Math.min(text.length(),index));text=text.substring(0,index)+s+text.substring(index);cursor=index+s.length();selectionStart=selectionEnd=cursor;dirty=true;}
    public void type(String s){replaceSelection(s==null?"":s);}
    public void replaceSelection(String s){int a=Math.min(selectionStart,selectionEnd),b=Math.max(selectionStart,selectionEnd);text=text.substring(0,a)+(s==null?"":s)+text.substring(b);cursor=a+(s==null?0:s.length());selectionStart=selectionEnd=cursor;dirty=true;}
    public void select(int start,int end){selectionStart=Math.max(0,Math.min(start,text.length()));selectionEnd=Math.max(0,Math.min(end,text.length()));cursor=selectionEnd;}
    public void replace(String old,String next){if(old==null||old.isEmpty())return;text=text.replace(old,next==null?"":next);dirty=true;cursor=Math.min(cursor,text.length());selectionStart=selectionEnd=cursor;}
    public int lineOfCursor(){int line=1;for(int i=0;i<Math.min(cursor,text.length());i++)if(text.charAt(i)=='\n')line++;return line;}
    public int columnOfCursor(){int i=Math.min(cursor,text.length())-1;while(i>=0&&text.charAt(i)!='\n')i--;return cursor-i-1;}
}
