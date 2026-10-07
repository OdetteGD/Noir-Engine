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
import android.view.View;
import android.widget.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.*;

public final class NoirScriptIdeView {
    private NoirScriptIdeView(){}

    public static void open(Context context, File file){
        if(file==null || !file.isFile()){
            Toast.makeText(context,"Script file not found",Toast.LENGTH_SHORT).show();
            return;
        }
        String source;
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
        root.addView(diagnostics,new LinearLayout.LayoutParams(-1,56));

        AlertDialog dialog=new AlertDialog.Builder(context).setView(root).create();
        close.setOnClickListener(v->dialog.dismiss());
        Runnable validate=()->{
            List<String> errors=validate(editor.getText().toString(),cs,shader);
            diagnostics.setText(errors.isEmpty()?"✓ No syntax diagnostics":
                    "Diagnostics ("+errors.size()+")\n"+join(errors));
            diagnostics.setTextColor(errors.isEmpty()?Color.rgb(121,224,160):Color.rgb(255,120,132));
        };
        Runnable highlight=()->highlight(editor,cs,shader);
        check.setOnClickListener(v->{validate.run();highlight.run();});
        save.setOnClickListener(v->{
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
        dialog.getWindow().setLayout(-1,-1);
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
        File p=f.getParentFile();if(p!=null)p.mkdirs();
        try(OutputStream out=new FileOutputStream(f)){out.write(s.getBytes(StandardCharsets.UTF_8));}
    }
    private static String join(List<String> a){StringBuilder b=new StringBuilder();for(String s:a)b.append(s).append("\n");return b.toString().trim();}

    private static List<String> validate(String s,boolean cs,boolean shader){
        ArrayList<String> e=new ArrayList<>();
        int braces=0,parens=0;boolean lineComment=false,string=false;
        String[] lines=s.split("\n",-1);
        for(int li=0;li<lines.length;li++){
            String line=lines[li];
            for(int i=0;i<line.length();i++){
                char ch=line.charAt(i),next=i+1<line.length()?line.charAt(i+1):0;
                if(!string && ch=='/' && next=='/'){lineComment=true;break;}
                if(!lineComment && ch=='"'){string=!string;continue;}
                if(lineComment)continue;
                if(ch=='{')braces++;else if(ch=='}')braces--;
                else if(ch=='(')parens++;else if(ch==')')parens--;
                if(braces<0){e.add("Line "+(li+1)+": unexpected '}'");braces=0;}
                if(parens<0){e.add("Line "+(li+1)+": unexpected ')'");parens=0;}
            }
            lineComment=false;
            if(line.length()>180)e.add("Line "+(li+1)+": long line may overflow the mobile editor");
            if(!cs && !shader && line.trim().startsWith("entity ") && !line.contains("{"))
                e.add("Line "+(li+1)+": entity declaration needs '{'");
        }
        if(braces!=0)e.add("Unbalanced braces: "+braces);
        if(parens!=0)e.add("Unbalanced parentheses: "+parens);
        if(shader && !s.contains("shader_type"))e.add("Shader source should declare shader_type");
        if(string)e.add("Unclosed string literal");
        return e;
    }

    private static void highlight(EditText editor,boolean cs,boolean shader){
        String s=editor.getText().toString();
        SpannableStringBuilder b=new SpannableStringBuilder(s);
        int keyword=Color.rgb(131,169,255),number=Color.rgb(255,199,102),comment=Color.rgb(105,135,112),string=Color.rgb(150,220,170),type=Color.rgb(196,150,255);
        Pattern p=shader
                ?Pattern.compile("\\b(shader_type|render_mode|uniform|varying|void|vertex|fragment|light|group_uniforms|group|return|if|else|for|true|false)\\b|\\b(float|vec2|vec3|vec4|mat3|mat4|sampler2D|COLOR|SCREEN_UV|TIME|NORMAL|UV)\\b|\\b\\d+(?:\\.\\d+)?\\b|//.*|"(?:\\\\.|[^"])*"")
                :cs
                ?Pattern.compile("\\b(class|public|private|protected|sealed|using|namespace|override|return|if|else|for|while|new|float|int|bool|void|true|false)\\b|\\b(Character3D|Vector3|Input|Export)\\b|\\b\\d+(?:\\.\\d+)?f?\\b|//.*|\"(?:\\\\.|[^\"])*\"")
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
        editor.setSelection(Math.min(editor.length(),editor.getSelectionStart()<0?editor.length():editor.getSelectionStart()));
    }
}
