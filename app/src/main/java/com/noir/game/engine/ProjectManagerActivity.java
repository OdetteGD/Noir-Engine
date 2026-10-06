package com.noir.game.engine;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.*;

public final class ProjectManagerActivity extends Activity {
    private NoirProjectWorkspace workspace;
    private LinearLayout list;
    private static final int OPEN_PACKAGE=1001;
    private static final int SAVE_PACKAGE=1002;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        workspace=new NoirProjectWorkspace(this);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32,32,32,32);
        root.setBackgroundColor(Color.rgb(8,11,18));

        TextView title=text("NOIR 3D ENGINE",28,Color.WHITE);
        root.addView(title,new LinearLayout.LayoutParams(-1,70));

        TextView sub=text("PROJECT MANAGER  •  FOREGROUND EDITOR",13,Color.rgb(143,167,255));
        root.addView(sub,new LinearLayout.LayoutParams(-1,45));

        LinearLayout actions=new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        addButton(actions,"NEW PROJECT",v->newProject());
        addButton(actions,"OPEN PROJECT",v->openPackage());
        addButton(actions,"EXPORT APK",v->exportSelected());
        root.addView(actions,new LinearLayout.LayoutParams(-1,70));

        TextView storage=text("Projects: "+workspace.root().getAbsolutePath(),11,Color.rgb(130,145,170));
        root.addView(storage,new LinearLayout.LayoutParams(-1,50));

        ScrollView scroll=new ScrollView(this);
        list=new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        setContentView(root);
        refresh();
    }

    private TextView text(String s,int size,int color) {
        TextView t=new TextView(this);
        t.setText(s); t.setTextSize(size); t.setTextColor(color); t.setGravity(Gravity.CENTER_VERTICAL);
        return t;
    }

    private void addButton(LinearLayout row,String label,View.OnClickListener click) {
        Button b=new Button(this); b.setText(label); b.setOnClickListener(click);
        row.addView(b,new LinearLayout.LayoutParams(0,-1,1));
    }

    private void refresh() {
        list.removeAllViews();
        for(File project:workspace.listProjects()) {
            LinearLayout row=new LinearLayout(this);
            row.setPadding(8,10,8,10);
            TextView name=text(project.getName()+"\n"+new File(project,"project.game").getAbsolutePath(),16,Color.WHITE);
            row.addView(name,new LinearLayout.LayoutParams(0,80,1));
            Button open=new Button(this); open.setText("OPEN"); open.setOnClickListener(v->openEditor(project));
            row.addView(open,new LinearLayout.LayoutParams(150,80));
            Button pack=new Button(this); pack.setText("PACKAGE"); pack.setOnClickListener(v->exportProject(project));
            row.addView(pack,new LinearLayout.LayoutParams(150,80));
            list.addView(row);
        }
        if(workspace.listProjects().isEmpty()) list.addView(text("No projects yet. Tap NEW PROJECT.",16,Color.LTGRAY));
    }

    private void newProject() {
        LinearLayout form=new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        EditText name=new EditText(this); name.setHint("Project name"); form.addView(name);
        EditText pkg=new EditText(this); pkg.setHint("Package e.g. com.example.mygame"); form.addView(pkg);
        new AlertDialog.Builder(this).setTitle("Create Noir Project").setView(form)
            .setNegativeButton("CANCEL",null)
            .setPositiveButton("CREATE",(d,w)->{
                try {
                    String n=name.getText().toString().trim();
                    String p=pkg.getText().toString().trim();
                    if(p.isEmpty()) p="com.noir.game."+NoirProjectWorkspace.safeName(n).toLowerCase(Locale.US);
                    File project=workspace.create(n,p);
                    Toast.makeText(this,"Project created: "+project.getName(),Toast.LENGTH_SHORT).show();
                    refresh();
                    openEditor(project);
                } catch(Exception e) { Toast.makeText(this,"Create failed: "+e.getMessage(),Toast.LENGTH_LONG).show(); }
            }).show();
    }

    private void openPackage() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        startActivityForResult(i,OPEN_PACKAGE);
    }

    private void exportSelected() {
        List<File> projects=workspace.listProjects();
        if(projects.isEmpty()) { Toast.makeText(this,"Create a project first.",Toast.LENGTH_SHORT).show(); return; }
        exportProject(projects.get(0));
    }

    private void exportProject(File project) {
        Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("application/zip");
        i.putExtra(Intent.EXTRA_TITLE,project.getName()+"-NoirAPKPackage.zip");
        selectedForExport=project;
        startActivityForResult(i,SAVE_PACKAGE);
    }

    private File selectedForExport;

    private void openEditor(File project) {
        Intent i=new Intent(this,MainActivity.class);
        i.putExtra("project_path",project.getAbsolutePath());
        startActivity(i);
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK||data==null)return;
        try {
            if(requestCode==OPEN_PACKAGE) {
                String base="ImportedProject";
                File dest=new File(workspace.root(),base);
                int n=1; while(dest.exists()) dest=new File(workspace.root(),base+"_"+(n++));
                NoirProjectPackage.importPackage(this,data.getData(),dest);
                Toast.makeText(this,"Project imported.",Toast.LENGTH_SHORT).show();
                refresh();
            } else if(requestCode==SAVE_PACKAGE && selectedForExport!=null) {
                try(OutputStream out=getContentResolver().openOutputStream(data.getData())) {
                    if(out==null) throw new IOException("Unable to create destination");
                    NoirProjectPackage.export(this,selectedForExport,out);
                }
                Toast.makeText(this,"Noir APK package exported.",Toast.LENGTH_LONG).show();
            }
        } catch(Exception e) {
            Toast.makeText(this,"Operation failed: "+e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }
}
