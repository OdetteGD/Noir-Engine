package com.noir.game.engine;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;
import com.noir.game.engine.scripting.NoirScriptCompiler;

public final class NoirScriptIdeView {
    private NoirScriptIdeView(){}

    public static void open(Context context, File file){
        if(file==null || !file.isFile()){
            Toast.makeText(context,"Script file not found",Toast.LENGTH_SHORT).show();
            return;
        }
        final String source;
        try{source=read(file);}catch(Exception e){
            Toast.makeText(context,"Cannot read script: "+e.getMessage(),Toast.LENGTH_LONG).show();
            return;
        }

        final String lowerName=file.getName().toLowerCase(Locale.US);
        final boolean cs=lowerName.endsWith(".cs");
        final boolean shader=lowerName.endsWith(".shader")||lowerName.endsWith(".glsl")||lowerName.endsWith(".vert")||lowerName.endsWith(".frag");
        final String language=cs?"C#":(shader?"Noir Shader":"Noir .game");

        final EditText editor=new EditText(context);
        editor.setText(source);
        editor.setTextColor(Color.rgb(225,232,244));
        editor.setHintTextColor(Color.rgb(110,125,150));
        editor.setTextSize(13);
        editor.setGravity(Gravity.TOP|Gravity.START);
        editor.setTypeface(android.graphics.Typeface.MONOSPACE);
        editor.setSingleLine(false);
        editor.setHorizontallyScrolling(true);
        editor.setPadding(16,12,16,12);
        editor.setBackgroundColor(Color.rgb(7,11,18));

        final TextView diagnostics=new TextView(context);
        diagnostics.setTextColor(Color.rgb(255,199,102));
        diagnostics.setTextSize(11);
        diagnostics.setPadding(14,8,14,8);
        diagnostics.setTypeface(android.graphics.Typeface.MONOSPACE);

        LinearLayout root=new LinearLayout(context);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(9,13,21));

        LinearLayout bar=new LinearLayout(context);
        bar.setGravity(Gravity.CENTER_VERTICAL);
        bar.setPadding(10,6,10,6);
        TextView title=label(context,file.getName()+"  •  "+language,Color.WHITE,13);
        bar.addView(title,new LinearLayout.LayoutParams(0,48,1));

        Button check=button(context,"CHECK");
        Button save=button(context,"SAVE");
        Button close=button(context,"CLOSE");
        bar.addView(check);bar.addView(save);bar.addView(close);
        root.addView(bar,new LinearLayout.LayoutParams(-1,56));

        HorizontalScrollView hs=new HorizontalScrollView(context);
        hs.addView(editor,new HorizontalScrollView.LayoutParams(-1,-1));
        root.addView(hs,new LinearLayout.LayoutParams(-1,0,1));
        root.addView(diagnostics,new LinearLayout.LayoutParams(-1,76));

        AlertDialog dialog=new AlertDialog.Builder(context).setView(root).create();
        close.setOnClickListener(v->dialog.dismiss());

        Runnable validate=()->{
            List<String> errors=validate(editor.getText().toString(),cs,shader);
            if(errors.isEmpty()){
                diagnostics.setText("✓ No syntax diagnostics");
                diagnostics.setTextColor(Color.rgb(121,224,160));
            }else{
                diagnostics.setText("Diagnostics ("+errors.size()+")\n"+join(errors));
                diagnostics.setTextColor(Color.rgb(255,120,132));
            }
        };
        Runnable highlight=()->highlight(editor,cs,shader);

        check.setOnClickListener(v->{validate.run();highlight.run();});
        save.setOnClickListener(v->{
            // Never write an invalid source file. The previous editor wrote first and
            // only validated afterward, which made malformed C# appear "clean" after save.
            List<String> errors=validate(editor.getText().toString(),cs,shader);
            if(!errors.isEmpty()){
                diagnostics.setText("Save blocked — fix diagnostics first ("+errors.size()+")\n"+join(errors));
                diagnostics.setTextColor(Color.rgb(255,120,132));
                Toast.makeText(context,"Save blocked: fix script diagnostics first.",Toast.LENGTH_LONG).show();
                return;
            }
            try{
                write(file,editor.getText().toString());
                validate.run();highlight.run();
                Toast.makeText(context,"Saved "+file.getName(),Toast.LENGTH_SHORT).show();
            }catch(Exception e){
                Toast.makeText(context,"Save failed: "+e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });

        editor.addTextChangedListener(new TextWatcher(){
            public void beforeTextChanged(CharSequence s,int st,int c,int a){}
            public void onTextChanged(CharSequence s,int st,int before,int count){validate.run();}
            public void afterTextChanged(Editable e){}
        });

        dialog.setOnShowListener(v->{highlight.run();validate.run();});
        dialog.show();
        if(dialog.getWindow()!=null)dialog.getWindow().setLayout(-1,-1);
    }

    private static TextView label(Context c,String s,int color,float size){
        TextView v=new TextView(c);v.setText(s);v.setTextColor(color);v.setTextSize(size);
        v.setGravity(Gravity.CENTER_VERTICAL);return v;
    }
    private static Button button(Context c,String s){
        Button b=new Button(c);b.setText(s);b.setTextSize(10);b.setAllCaps(false);return b;
    }
    private static String read(File f)throws IOException{
        try(InputStream in=new FileInputStream(f)){
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            byte[] buf=new byte[8192];int n;long total=0;
            while((n=in.read(buf))!=-1){total+=n;if(total>4*1024*1024)throw new IOException("Script is too large");out.write(buf,0,n);}
            return out.toString(StandardCharsets.UTF_8.name());
        }
    }
    private static void write(File f,String s)throws IOException{
        File p=f.getParentFile();if(p!=null&&!p.exists()&&!p.mkdirs())throw new IOException("Cannot create "+p);
        try(OutputStream out=new FileOutputStream(f)){out.write(s.getBytes(StandardCharsets.UTF_8));}
    }
    private static String join(List<String> a){StringBuilder b=new StringBuilder();for(String s:a)b.append(s).append("\n");return b.toString().trim();}

    private static List<String> validate(String s,boolean cs,boolean shader){
        if(cs)return validateCSharp(s);
        ArrayList<String> out=new ArrayList<>();
        NoirScriptCompiler.CompileResult result=new NoirScriptCompiler().validate(s);
        for(NoirScriptCompiler.Diagnostic d:result.diagnostics)
            out.add("Line "+d.line+":"+d.column+" "+d.severity+": "+d.message);
        if(shader&&!s.contains("shader_type"))out.add("Line 1: ERROR: shader source should declare shader_type");
        return out;
    }

    /**
     * Lightweight C# lexer-level diagnostics used immediately on-device.
     * It deliberately rejects invalid source characters and malformed lexical state,
     * including Unicode symbols such as ÷/£ that the old bracket-only checker accepted.
     * Full Roslyn compilation remains the authoritative build-time compiler.
     */
    private static List<String> validateCSharp(String s){
        ArrayList<String> out=new ArrayList<>();
        String[] lines=s.replace("\r","").split("\n",-1);
        int braces=0,parens=0,brackets=0;
                    if(c=='\''){charLiteral=false;}

        for(int li=0;li<lines.length;li++){
            String line=lines[li];
            boolean lineComment=false;
            for(int i=0;i<line.length();i++){
                char c=line.charAt(i);
                char n=i+1<line.length()?line.charAt(i+1):0;

                if(blockComment){
                    if(c=='*'&&n=='/'){blockComment=false;i++;}
                    continue;
                }
                if(lineComment)continue;

                if(!string&&!charLiteral&&c=='/'&&n=='*'){blockComment=true;i++;continue;}
                if(!string&&!charLiteral&&c=='/'&&n=='/'){lineComment=true;continue;}

                if(string){
                    if(c=='\\'){i++;continue;}
                    if(c=='"')string=false;
                    continue;
                }
                if(charLiteral){
                    if(c=='\\'){i++;continue;}
                    if(c=='\''){charLiteral=false;}
                    continue;
                }
                if(c=='"'){string=true;continue;}
                if(c=='\''){charLiteral=true;}

                if(c=='#'){
                    int firstNonWs=0;
                    while(firstNonWs<line.length()&&Character.isWhitespace(line.charAt(firstNonWs)))firstNonWs++;
                    if(i!=firstNonWs)
                        out.add("Line "+(li+1)+":"+(i+1)+" unexpected '#' inside a C# statement");
                    continue;
                }

                if(c=='\u00f7'||c=='\u00a3'||c=='\u00d7'||c=='\u00a7'){
                    out.add(String.format(Locale.US,"Line %d:%d invalid C# character U+%04X '%c'",li+1,i+1,(int)c,c));
                    continue;
                }

                if(!Character.isWhitespace(c) && !Character.isLetterOrDigit(c) && c!='_' && !isCSharpPunctuation(c)){
                    out.add(String.format(Locale.US,"Line %d:%d invalid C# character U+%04X '%c'",li+1,i+1,(int)c,c));
                    continue;
                }

                if(c=='{')braces++;
                else if(c=='}')braces--;
                else if(c=='(')parens++;
                else if(c==')')parens--;
                else if(c=='[')brackets++;
                else if(c==']')brackets--;

                if(braces<0){out.add("Line "+(li+1)+":"+(i+1)+" unexpected '}'");braces=0;}
                if(parens<0){out.add("Line "+(li+1)+":"+(i+1)+" unexpected ')'");parens=0;}
                if(brackets<0){out.add("Line "+(li+1)+":"+(i+1)+" unexpected ']'");brackets=0;}
            }

            String t=line.trim();
            if(t.matches("^using\\s*;.*"))out.add("Line "+(li+1)+": using directive requires a namespace or type");
            if(t.matches("^(class|struct|interface|enum)\\s+[^A-Za-z_].*"))
                out.add("Line "+(li+1)+": type name must start with a letter or underscore");
            if(t.matches(".*\\b(if|for|while|switch|catch)\\s*\\([^)]*\\)\\s*[^\\{;].*"))
                out.add("Line "+(li+1)+": control statement is missing its block");

            // A line made mostly of symbol garbage is almost certainly accidental input.
            if(t.matches("^[!\\$%&*+\\-./:<=>?@\\[\\]\\^|~#]+[A-Za-z0-9_]*$") &&
               !t.endsWith(";") && !t.startsWith("//") && !t.startsWith("#")){
                out.add("Line "+(li+1)+": unexpected symbol-only expression");
            }
        }

        if(string)out.add("Line "+lines.length+": unclosed string literal");
        if(charLiteral)out.add("Line "+lines.length+": unclosed character literal");
        if(blockComment)out.add("Line "+lines.length+": unclosed block comment");
        if(braces!=0)out.add("End of file: unbalanced braces: "+braces);
        if(parens!=0)out.add("End of file: unbalanced parentheses: "+parens);
        if(brackets!=0)out.add("End of file: unbalanced brackets: "+brackets);
        return dedupe(out);
    }

    private static boolean isCSharpPunctuation(char c){
        return "{}()[];,.?:+-*/%&|^!<>=~@$'".indexOf(c)>=0;
    }

    private static List<String> dedupe(List<String> src){
        LinkedHashSet<String> set=new LinkedHashSet<>(src);
        return new ArrayList<>(set);
    }

    private static void highlight(EditText editor,boolean cs,boolean shader){
        int start=editor.getSelectionStart();
        int end=editor.getSelectionEnd();
        String s=editor.getText().toString();
        SpannableStringBuilder b=new SpannableStringBuilder(s);
        int keyword=Color.rgb(131,169,255),number=Color.rgb(255,199,102),comment=Color.rgb(105,135,112),string=Color.rgb(150,220,170),type=Color.rgb(196,150,255);
        Pattern p=shader
                ?Pattern.compile("\\b(shader_type|render_mode|uniform|varying|void|vertex|fragment|light|group_uniforms|group|return|if|else|for|true|false)\\b|\\b(float|vec2|vec3|vec4|mat3|mat4|sampler2D|COLOR|SCREEN_UV|TIME|NORMAL|UV)\\b|\\b\\d+(?:\\.\\d+)?\\b|//.*|\"(?:\\\\.|[^\"])*\"")
                :cs
                ?Pattern.compile("\\b(class|public|private|protected|sealed|using|namespace|override|return|if|else|for|while|new|float|double|int|bool|void|true|false|null|static|readonly|async|await)\\b|\\b(Character3D|Vector3|Input|Export)\\b|\\b\\d+(?:\\.\\d+)?f?\\b|//.*|\"(?:\\\\.|[^\"])*\"")
                :Pattern.compile("\\b(entity|type|property|input|start|physics|if|else|true|false)\\b|\\b(Character3D|Camera3D|vector|move_and_slide|child)\\b|\\b\\d+(?:\\.\\d+)?\\b|//.*|\"(?:\\\\.|[^\"])*\"");
        Matcher m=p.matcher(s);
        while(m.find()){
            String token=m.group();
            int color=token.startsWith("//")?comment:token.startsWith("\"")?string:
                    token.matches("\\d.*")?number:
                    token.matches(".*(Character3D|Vector3|Input|Export|Camera3D|vector|move_and_slide|child|shader_type|render_mode|uniform|vec2|vec3|vec4|sampler2D|SCREEN_UV|TIME).*")?type:keyword;
            b.setSpan(new ForegroundColorSpan(color),m.start(),m.end(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        editor.setText(b,TextView.BufferType.SPANNABLE);
        int safe=Math.max(0,Math.min(editor.length(),start<0?editor.length():start));
        if(end>=0){
            int safeEnd=Math.max(safe,Math.min(editor.length(),end));
            editor.setSelection(safe,safeEnd);
        }else editor.setSelection(safe);
    }
}
