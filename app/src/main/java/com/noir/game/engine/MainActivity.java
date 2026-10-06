package com.noir.game.engine;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import com.noir.game.engine.core.GameFileParser;
import com.noir.game.engine.editor.EditorState;
import com.noir.game.engine.scene.NoirNode;
import com.noir.game.engine.scene.NoirScene;
import com.noir.game.engine.render.NoirRenderer;
import java.io.*;

public final class MainActivity extends Activity {
    private NoirRenderer renderer;
    private NoirEditorView editorUi;

    @Override public void onCreate(Bundle state){
        super.onCreate(state);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String projectPath=getIntent().getStringExtra("project_path");
        NoirScene scene=loadProjectScene(projectPath);
        EditorState editor=new EditorState(scene,projectPath);
        renderer=new NoirRenderer();

        NoirSurface surface=new NoirSurface(this,renderer);
        editorUi=new NoirEditorView(this,editor,renderer,surface);

        FrameLayout root=new FrameLayout(this);
        root.addView(surface,new FrameLayout.LayoutParams(-1,-1));
        root.addView(editorUi,new FrameLayout.LayoutParams(-1,-1));
        setContentView(root);
    }

    @Override public void onBackPressed(){
        if(editorUi!=null && editorUi.getVisibility()!=android.view.View.VISIBLE){
            editorUi.stopPlay();
            return;
        }
        super.onBackPressed();
    }

    private NoirScene loadProjectScene(String projectPath) {
        String src="scene Main\nnode World {\n type = NODE3D\n}\nnode MainCamera {\n type = CAMERA3D\n position = (0, 2, 6)\n}\n";
        if(projectPath!=null) {
            File f=new File(projectPath,"scenes/Main.game");
            if(f.isFile()) {
                try {
                    src=new String(java.nio.file.Files.readAllBytes(f.toPath()),java.nio.charset.StandardCharsets.UTF_8);
                } catch(Exception ignored) {}
            }
        }
        GameFileParser.Result r=new GameFileParser().parse(src,"scenes/Main.game");
        NoirScene s=r.scene;
        if(s==null) s=new NoirScene("Main");
        NoirNode root=s.root;
        if(root.find("WorldEnvironment")==null){
            NoirNode env=root.add(new NoirNode("WorldEnvironment","WorldEnvironment",NoirNode.Kind.WORLD_ENVIRONMENT));
            env.properties.put("sky","procedural");
            env.properties.put("clouds","procedural");
            env.properties.put("exposure","1.0");
        }
        if(root.find("Sun")==null){
            NoirNode sun=root.add(new NoirNode("Sun","Sun",NoirNode.Kind.LIGHT3D));
            sun.properties.put("type","DIRECTIONAL");
            sun.properties.put("energy","3.0");
            sun.properties.put("shadows","PCF");
        }
        if(root.find("Player3D")==null){
            NoirNode player=root.add(new NoirNode("Player3D","Player3D",NoirNode.Kind.PLAYER3D));
            player.properties.put("script","scripts/player.game");
            player.properties.put("speed","5.0");
            player.properties.put("jump","4.5");
            NoirNode cam=player.add(new NoirNode("Camera3D","Camera3D",NoirNode.Kind.CAMERA3D));
            cam.properties.put("mobile_look","touch+gyro");
        }
        if(root.find("MainCamera")==null) root.add(new NoirNode("MainCamera","MainCamera",NoirNode.Kind.CAMERA3D));
        return s;
    }
}
