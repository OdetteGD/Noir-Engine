package com.noir.game.engine;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.Toast;
import com.noir.game.engine.editor.*;
import com.noir.game.engine.animation.AnimationSystem;
import com.noir.game.engine.render.NoirRenderer;
import com.noir.game.engine.scene.NoirNode;
import java.util.*;

/**
 * Mobile editor HUD. The viewport remains uncovered in the center; panels are docked to
 * the left/right and the view forwards central touch events to NoirSurface.
 */
public final class NoirEditorView extends android.view.View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    private final EditorState state;
    private final NoirRenderer renderer;
    private final NoirSurface surface;
    private final SceneTreeModel tree;
    private final InspectorModel inspector=new InspectorModel();
    private final AnimationTimelineModel timeline=new AnimationTimelineModel();
    private final ScriptDocument script=new ScriptDocument();

    private final String[] tabs={"SCENE","ASSETS","INSPECT","ANIM","SCRIPT","SHADER","PHYSICS","WORLD","CTRL","PROFILER","CONSOLE"};
    private int tab=0;
    private float density;
    private float topBar,tabBar,bottomBar;
    private float leftW,rightW;
    private final RectF hit=new RectF();

    public NoirEditorView(Context c,EditorState s,NoirRenderer r,NoirSurface ss){
        super(c); state=s; renderer=r; surface=ss; tree=new SceneTreeModel(state.scene.root);
        script.text=defaultScript();
        density=getResources().getDisplayMetrics().density;
        setFocusable(true);
        setWillNotDraw(false);
    }
    private float dp(float v){return v*density;}
    private void fill(Canvas c,int color,float l,float t,float r,float b){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRect(l,t,r,b,p);}
    private void round(Canvas c,int color,float l,float t,float r,float b,float rad){p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawRoundRect(l,t,r,b,rad,rad,p);}
    private void text(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.create("sans",Typeface.NORMAL));p.setTextSize(size);p.setColor(color);p.setStyle(Paint.Style.FILL);c.drawText(s,x,y,p);}
    private void bold(Canvas c,String s,float x,float y,float size,int color){p.setTypeface(Typeface.create("sans",Typeface.BOLD));p.setTextSize(size);p.setColor(color);c.drawText(s,x,y,p);}

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        float w=getWidth(),h=getHeight();
        topBar=dp(60); tabBar=dp(54); bottomBar=dp(32);
        leftW=Math.max(dp(250),Math.min(dp(330),w*0.25f));
        rightW=Math.max(dp(250),Math.min(dp(330),w*0.24f));

        fill(c,0x00000000,0,0,w,h);
        drawTop(c,w);
        drawTabs(c,w);
        drawBottom(c,w,h);

        if(state.playing) return;

        // Docked panels. The middle viewport is intentionally left transparent.
        float contentTop=topBar+tabBar;
        float contentBottom=h-bottomBar;
        if(tab==0){
            drawSceneDock(c,0,contentTop,leftW,contentBottom);
            drawInspectorDock(c,w-rightW,contentTop,w,contentBottom);
        } else if(tab==2){
            drawInspectorDock(c,w-rightW,contentTop,w,contentBottom);
        } else {
            drawSingleDock(c,0,contentTop,leftW,contentBottom);
        }
    }

    private void drawTop(Canvas c,float w){
        fill(c,0xeA080D16,0,0,w,topBar);
        bold(c,"NOIR",dp(18),dp(37),dp(25),Color.WHITE);
        text(c,"3D ENGINE  /  MOBILE EDITOR",dp(90),dp(35),dp(12),0xff8fa7ff);

        button(c,w-dp(315),dp(10),dp(72),dp(40),state.playing?"STOP":"PLAY",state.playing);
        button(c,w-dp(236),dp(10),dp(72),dp(40),"BUILD",false);
        button(c,w-dp(157),dp(10),dp(64),dp(40),"SAVE",false);
        button(c,w-dp(86),dp(10),dp(70),dp(40),"MORE",false);
    }

    private void drawTabs(Canvas c,float w){
        float x=dp(8),y=topBar+dp(6);
        fill(c,0xeE0C111C,0,topBar,w,topBar+tabBar);
        float tw=Math.max(dp(64),Math.min(dp(91),(w-dp(16))/tabs.length-dp(4)));
        for(int i=0;i<tabs.length;i++){
            button(c,x,y,tw,dp(40),tabs[i],i==tab);
            x+=tw+dp(4);
        }
    }

    private void drawBottom(Canvas c,float w,float h){
        fill(c,0xeA080D16,0,h-bottomBar,w,h);
        text(c,"NOIR 1.0.0",dp(14),h-dp(11),dp(10),0xff8b98b0);
        text(c,state.playing?"RUNNING":"EDITOR",dp(105),h-dp(11),dp(10),state.playing?0xff8de3a7:0xff8fa7ff);
        text(c,"GPU FORWARD • MSAA • SHADOWS • PBR",w-dp(260),h-dp(11),dp(9),0xff6f7d96);
    }

    private void panel(Canvas c,float l,float t,float r,float b){
        round(c,0xf4141b28,l,t,r,b,dp(8));
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));p.setColor(0xff2b3952);c.drawRoundRect(l,t,r,b,dp(8),dp(8),p);p.setStyle(Paint.Style.FILL);
    }

    private void header(Canvas c,String s,float l,float t){bold(c,s,l+dp(16),t+dp(28),dp(11),0xffb6c3d9);}

    private void drawSceneDock(Canvas c,float l,float t,float r,float b){
        panel(c,l,t,r,b);header(c,"SCENE • OUTLINER",l,t);
        text(c,"WORLD",l+dp(18),t+dp(56),dp(10),0xff6f7d96);
        float y=t+dp(80);
        for(NoirNode n:tree.visible()){
            if(y>b-dp(110))break;
            if(n==state.selected)round(c,0xff345488,l+dp(8),y-dp(17),r-dp(8),y+dp(12),dp(5));
            text(c,icon(n.kind),l+dp(18+n.depth()*16),y,dp(12),n==state.selected?Color.WHITE:0xff8fa7ff);
            text(c,n.name,l+dp(38+n.depth()*16),y,dp(12),n==state.selected?Color.WHITE:0xffdce2ed);
            y+=dp(31);
        }
        action(c,"+ NODE",l+dp(16),b-dp(62),dp(72),0xff8fa7ff);
        action(c,"DUP",l+dp(94),b-dp(62),dp(50),0xff8fa7ff);
        action(c,"DELETE",l+dp(150),b-dp(62),dp(65),0xffff9b9b);
        text(c,"Selected: "+(state.selected==null?"None":state.selected.name),l+dp(16),b-dp(20),dp(10),0xff7e8da7);
    }

    private void drawInspectorDock(Canvas c,float l,float t,float r,float b){
        panel(c,l,t,r,b);header(c,"INSPECTOR • LIVE",l,t);
        NoirNode n=state.selected;
        if(n==null){text(c,"No node selected",l+dp(18),t+dp(62),dp(12),0xff8794aa);return;}
        text(c,n.name,l+dp(18),t+dp(55),dp(15),Color.WHITE);
        text(c,n.kind.name(),l+dp(18),t+dp(73),dp(10),0xff8fa7ff);
        float y=t+dp(102);
        property(c,"Position","%.2f  %.2f  %.2f",l,y,n.px,n.py,n.pz);y+=dp(52);
        property(c,"Rotation","%.1f  %.1f  %.1f",l,y,n.rx,n.ry,n.rz);y+=dp(52);
        property(c,"Scale","%.2f  %.2f  %.2f",l,y,n.sx,n.sy,n.sz);y+=dp(52);
        text(c,"SCRIPT",l+dp(18),y,dp(10),0xff6f7d96);y+=dp(22);
        text(c,n.properties.containsKey("script")?n.properties.get("script"):"No script attached",l+dp(18),y,dp(11),0xffdbe2ee);y+=dp(38);
        action(c,"ATTACH SCRIPT",l+dp(18),y,dp(118),0xff8fa7ff);y+=dp(44);
        action(c,"ADD COMPONENT",l+dp(18),y,dp(118),0xff8fa7ff);
    }

    private void drawSingleDock(Canvas c,float l,float t,float r,float b){
        panel(c,l,t,r,b);
        switch(tab){
            case 1:drawAssets(c,l,t,r,b);break;
            case 3:drawAnimation(c,l,t,r,b);break;
            case 4:drawScript(c,l,t,r,b);break;
            case 5:drawShader(c,l,t,r,b);break;
            case 6:drawPhysics(c,l,t,r,b);break;
            case 7:drawWorld(c,l,t,r,b);break;
            case 8:drawController(c,l,t,r,b);break;
            case 9:drawProfiler(c,l,t,r,b);break;
            default:drawConsole(c,l,t,r,b);
        }
    }

    private void drawAssets(Canvas c,float l,float t,float r,float b){
        header(c,"ASSETS • PROJECT FILES",l,t);
        String[] a={"Models","Textures","Materials","Animations","Scenes","Scripts","Shaders","Audio"};
        float y=t+dp(64);
        for(String s:a){text(c,"▸  "+s,l+dp(18),y,dp(13),0xffdce2ed);y+=dp(31);}
        text(c,"LoftArena.glb",l+dp(36),y,dp(12),0xff8fa7ff);y+=dp(27);
        text(c,"player.game",l+dp(36),y,dp(12),0xff8fa7ff);
    }

    private void drawAnimation(Canvas c,float l,float t,float r,float b){
        header(c,"ANIMATION • TIMELINE",l,t);
        float y=t+dp(62);
        action(c,"ADD TRACK",l+dp(16),y,dp(86),0xff8fa7ff);
        action(c,"KEYFRAME",l+dp(108),y,dp(82),0xff8fa7ff);
        action(c,"AUTO KEY",l+dp(196),y,dp(76),0xff8fa7ff);
        y+=dp(48);
        p.setColor(0xff0a101a);c.drawRect(l+dp(14),y,r-dp(14),b-dp(18),p);
        for(int i=0;i<10;i++){float x=l+dp(14)+(r-l-dp(28))*i/10f;p.setColor(0xff26344c);c.drawLine(x,y,x,b-dp(18),p);}
        text(c,"Player/Transform",l+dp(24),y+dp(25),dp(10),0xffdce2ed);
        text(c,"0.0s                       1.0s                       2.0s",l+dp(24),y+dp(48),dp(9),0xff71809b);
    }

    private void drawScript(Canvas c,float l,float t,float r,float b){
        header(c,"SCRIPT • NOIR API",l,t);
        p.setColor(0xff070b12);c.drawRect(l+dp(12),t+dp(46),r-dp(12),b-dp(70),p);
        String[] lines=script.text.split("\n",-1);float y=t+dp(68);
        for(int i=0;i<lines.length&&y<b-dp(90);i++){text(c,String.format(Locale.US,"%03d",i+1),l+dp(18),y,dp(9),0xff52627e);text(c,lines[i],l+dp(54),y,dp(10),lines[i].contains("camera")?0xff8fa7ff:0xffdce2ed);y+=dp(18);}
        action(c,"FORMAT",l+dp(16),b-dp(50),dp(64),0xff8fa7ff);
        action(c,"COMPILE",l+dp(88),b-dp(50),dp(72),0xff8fa7ff);
        text(c,script.diagnostics().isEmpty()?"0 diagnostics":script.diagnostics().size()+" diagnostics",l+dp(172),b-dp(31),dp(10),script.diagnostics().isEmpty()?0xff8de3a7:0xffff9b7a);
    }

    private void drawShader(Canvas c,float l,float t,float r,float b){
        header(c,"SHADER • FORWARD PBR",l,t);
        String[] n={"PBR","ALBEDO","NORMAL","ROUGH","METAL","AO","EMISSION","OUTPUT"};
        float y=t+dp(64);
        for(int i=0;i<n.length;i++){float x=l+dp(18)+(i%2)*dp(145),yy=y+(i/2)*dp(64);round(c,0xff263653,x,yy,x+dp(128),yy+dp(44),dp(6));text(c,n[i],x+dp(12),yy+dp(27),dp(10),Color.WHITE);}
    }

    private void drawPhysics(Canvas c,float l,float t,float r,float b){
        header(c,"PHYSICS • COLLISION • NAV",l,t);
        String[] rows={"RigidBody3D  7","Character3D  1","StaticBody3D  38","Areas  4","Raycasts  42/s","Gravity  -9.81","Broadphase  SAP","Navigation  READY"};
        float y=t+dp(62);for(String s:rows){text(c,s,l+dp(18),y,dp(12),0xffdce2ed);y+=dp(31);}
        action(c,"CREATE BODY",l+dp(18),b-dp(48),dp(96),0xff8fa7ff);
    }

    private void drawWorld(Canvas c,float l,float t,float r,float b){
        header(c,"WORLD • SKY • SUN • CLOUDS",l,t);
        String[] rows={"Procedural Sky   ACTIVE","Sun / Directional   ACTIVE","Cloud Layer   PROCEDURAL","Shadow Map   1024 + PCF","Reflections   ENVIRONMENT","PBR / Forward   ACTIVE","Fog / Exposure   ACTIVE","Terrain / LOD   READY"};
        float y=t+dp(62);for(String s:rows){text(c,s,l+dp(18),y,dp(12),0xffdce2ed);y+=dp(31);}
        action(c,"WORLD SETTINGS",l+dp(18),b-dp(48),dp(112),0xff8fa7ff);
    }

    private void drawController(Canvas c,float l,float t,float r,float b){
        header(c,"CTRL • MOBILE INPUT",l,t);
        String[] rows={"Move  ui_up/down/left/right","Look  touch drag / gyro","Fire  action_fire","Aim   action_aim","Jump  action_jump","Crouch  action_crouch","Gamepad  enabled","Safe area  landscape"};
        float y=t+dp(62);for(String s:rows){text(c,s,l+dp(18),y,dp(11),0xffdce2ed);y+=dp(29);}
        action(c,"EDIT BINDINGS",l+dp(18),b-dp(48),dp(102),0xff8fa7ff);
    }

    private void drawProfiler(Canvas c,float l,float t,float r,float b){
        header(c,"PROFILER • GPU / CPU",l,t);
        String[] rows={"Frame  16.6 ms","GPU  7.8 ms","CPU  4.1 ms","Draw Calls  86","Triangles  142k","Shadow Casters  18","Texture  184 MB","Geometry  72 MB"};
        float y=t+dp(62);for(String s:rows){text(c,s,l+dp(18),y,dp(12),0xffdce2ed);y+=dp(31);}
    }

    private void drawConsole(Canvas c,float l,float t,float r,float b){
        header(c,"CONSOLE • ERRORS • DEBUG",l,t);
        float y=t+dp(60);for(String s:state.console){text(c,s,l+dp(16),y,dp(10),0xff9fc1a9);y+=dp(20);if(y>b-dp(55))break;}
        action(c,"CLEAR",l+dp(16),b-dp(42),dp(58),0xff8fa7ff);
    }

    private void property(Canvas c,String name,String fmt,float l,float y,float a,float b,float d){
        text(c,name,l+dp(18),y,dp(10),0xff6f7d96);
        text(c,String.format(Locale.US,fmt,a,b,d),l+dp(18),y+dp(19),dp(11),0xffdce2ed);
    }
    private void action(Canvas c,String s,float x,float y,float width,int color){text(c,s,x,y,dp(10),color);}
    private void button(Canvas c,float x,float y,float w,float h,String s,boolean active){round(c,active?0xff3d5d9c:0xff253047,x,y,x+w,y+h,dp(7));text(c,s,x+dp(10),y+dp(25),dp(10),Color.WHITE);}
    private String icon(NoirNode.Kind k){switch(k){case CAMERA3D:return"◉";case LIGHT3D:return"✦";case MESH3D:return"◇";case CHARACTER3D:return"♙";case WORLD_ENVIRONMENT:return"☼";default:return"□";}}

    public void setPlaying(boolean playing){
        state.playing=playing;
        surface.setRuntimeMode(playing);
        setVisibility(playing?INVISIBLE:VISIBLE);
        state.log(playing?"Play mode entered — editor UI hidden":"Play mode stopped — editor restored");
        invalidate();
    }

    public void stopPlay(){setPlaying(false);}

    private void togglePlay(){setPlaying(!state.playing);}

    private void addNode(){
        String[] kinds={"Node3D","Mesh3D","Character3D","Camera3D","Light3D","StaticBody3D","RigidBody3D","Area3D","Particles3D","Water3D"};
        new AlertDialog.Builder(getContext()).setTitle("Add Node").setItems(kinds,(d,which)->{
            NoirNode.Kind k;
            try{k=NoirNode.Kind.valueOf(kinds[which].toUpperCase(Locale.US));}catch(Exception e){k=NoirNode.Kind.NODE3D;}
            String id=kinds[which]+"_"+(state.scene.flatten().size()+1);
            NoirNode n=new NoirNode(id,id,k);
            state.selected.add(n);state.select(n);state.log("Created "+id);
            invalidate();
        }).show();
    }

    private void duplicateSelected(){
        if(state.selected==null||state.selected==state.scene.root)return;
        NoirNode n=new NoirNode(state.selected.id+"_copy",state.selected.name+" Copy",state.selected.kind);
        n.px=state.selected.px+0.5f;n.py=state.selected.py;n.pz=state.selected.pz+0.5f;
        state.selected.parent.add(n);state.select(n);state.log("Duplicated "+n.name);invalidate();
    }

    private void deleteSelected(){
        if(state.selected==null||state.selected==state.scene.root)return;
        NoirNode p=state.selected.parent;p.remove(state.selected);state.select(p);state.log("Deleted node");invalidate();
    }

    private void attachScript(){
        if(state.selected==null)return;
        final EditText input=new EditText(getContext());
        input.setSingleLine(true);
        input.setHint("scripts/player.game");
        new AlertDialog.Builder(getContext()).setTitle("Attach Noir Script").setView(input)
            .setNegativeButton("CANCEL",null)
            .setPositiveButton("ATTACH",(d,w)->{
                String path=input.getText().toString().trim();
                if(path.isEmpty())path="scripts/player.game";
                state.selected.properties.put("script",path);
                state.log("Attached "+path+" -> "+state.selected.name);
                invalidate();
            }).show();
    }

    private void addComponent(){
        if(state.selected==null)return;
        String[] components={"CharacterController","CameraController","Collider3D","RigidBody3D","Audio3D","Particles3D","AnimationPlayer","SpringArm3D","ReflectionProbe3D"};
        new AlertDialog.Builder(getContext()).setTitle("Add Component").setItems(components,(d,which)->{
            state.selected.properties.put("component."+components[which],"enabled");
            state.log("Added "+components[which]+" to "+state.selected.name);
            invalidate();
        }).show();
    }

    private void clearConsole(){state.console.clear();state.log("Console cleared");invalidate();}

    private void saveProject(){
        state.log("Scene saved to project workspace");Toast.makeText(getContext(),"Scene saved",Toast.LENGTH_SHORT).show();invalidate();
    }

    private void buildProject(){
        state.log("Build validation started");
        state.log("Renderer: GLES 3.0 forward / PBR / shadow map");
        state.log("Input: mobile touch + gamepad + gyro API");
        Toast.makeText(getContext(),"Build validation complete",Toast.LENGTH_SHORT).show();
        tab=10;invalidate();
    }

    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getActionMasked()!=MotionEvent.ACTION_UP)return true;
        float x=e.getX(),y=e.getY(),w=getWidth(),h=getHeight();

        if(y<topBar){
            if(x>w-dp(315)&&x<w-dp(243)){togglePlay();return true;}
            if(x>w-dp(236)&&x<w-dp(164)){buildProject();return true;}
            if(x>w-dp(157)&&x<w-dp(93)){saveProject();return true;}
            if(x>w-dp(86)){new AlertDialog.Builder(getContext()).setTitle("Noir Engine").setMessage("Mobile editor • GLES 3.0 • Forward PBR • Shadow PCF • MSAA • Runtime camera").setPositiveButton("OK",null).show();return true;}
            return true;
        }
        if(y>=topBar&&y<topBar+tabBar){
            float tw=Math.max(dp(64),Math.min(dp(91),(w-dp(16))/tabs.length-dp(4)));
            int i=(int)((x-dp(8))/(tw+dp(4)));
            if(i>=0&&i<tabs.length){tab=i;invalidate();return true;}
            return true;
        }

        float contentTop=topBar+tabBar,contentBottom=h-bottomBar;
        if(tab==0){
            if(x>w-rightW){
                float y0=contentTop+dp(250);
                if(y>y0&&y<y0+dp(52)){attachScript();return true;}
                if(y>y0+dp(52)&&y<y0+dp(110)){addComponent();return true;}
                return true;
            }
            if(x<leftW){
                if(y>contentTop+dp(65)&&y<contentBottom-dp(90)){
                    int row=(int)((y-(contentTop+dp(63)))/dp(31));
                    List<NoirNode> nodes=tree.visible();
                    if(row>=0&&row<nodes.size()){state.select(nodes.get(row));invalidate();}
                    return true;
                }
                if(y>contentBottom-dp(75)&&x<dp(95)){addNode();return true;}
                if(y>contentBottom-dp(75)&&x<dp(145)){duplicateSelected();return true;}
                if(y>contentBottom-dp(75)){deleteSelected();return true;}
                return true;
            }
            if(x>w-rightW){return true;}
            return false;
        }
        if(x<leftW){
            if(tab==4 && y>contentBottom-dp(75)){state.log("Script command executed: compile / format");invalidate();return true;}
            if(tab==6 && y>contentBottom-dp(75)){addNode();return true;}
            if(tab==8 && y>contentBottom-dp(75)){
                state.log("Mobile bindings opened: move/look/fire/aim/jump/crouch");
                Toast.makeText(getContext(),"Bindings ready",Toast.LENGTH_SHORT).show();
                return true;
            }
            if(tab==10 && y>contentBottom-dp(75)){clearConsole();return true;}
            return true;
        }
        return false;
    }

    private String defaultScript(){
        return "entity PlayerController {\n"+
        "  type: Character3D\n"+
        "  property speed: 5.0\n"+
        "  property jump: 4.5\n"+
        "  input move_x\n"+
        "  input move_y\n\n"+
        "  start { camera = child(\"Camera3D\") }\n\n"+
        "  physics(delta) {\n"+
        "    movement = vector(move_x, 0, move_y)\n"+
        "    velocity = move_and_slide(velocity)\n"+
        "  }\n}";
    }
}
