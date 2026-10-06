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

public final class MainActivity extends Activity {
    private NoirRenderer renderer;
    @Override public void onCreate(Bundle state){super.onCreate(state);requestWindowFeature(Window.FEATURE_NO_TITLE);getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        NoirScene scene=defaultScene(); EditorState editor=new EditorState(scene); renderer=new NoirRenderer();
        NoirSurface surface=new NoirSurface(this,renderer); NoirEditorView ui=new NoirEditorView(this,editor,renderer,surface);
        FrameLayout root=new FrameLayout(this); root.addView(surface,new FrameLayout.LayoutParams(-1,-1));root.addView(ui,new FrameLayout.LayoutParams(-1,-1));setContentView(root);
    }
    private NoirScene defaultScene(){String src="scene LoftArena\nnode WorldEnvironment {\n type = WORLD_ENVIRONMENT\n}\nnode MainLight {\n type = LIGHT3D\n position = (0, 8, 2)\n}\nnode Player {\n type = CHARACTER3D\n position = (0, 1.7, 8)\n}\n";GameFileParser.Result r=new GameFileParser().parse(src,"scenes/LoftArena.game");NoirScene s=r.scene;NoirNode p=s.root.find("Player");if(p!=null){p.add(new NoirNode("Camera3D","Camera3D",NoirNode.Kind.CAMERA3D));p.add(new NoirNode("Weapon","Weapon",NoirNode.Kind.MESH3D));}return s;}
}
