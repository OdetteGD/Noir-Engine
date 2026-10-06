package com.noir.game.engine.editor;

import com.noir.game.engine.controller.*;
import java.util.*;

/** Mobile controller editor: layout, action binding, sensitivity and device preview. */
public final class ControllerEditorModel {
    public final MobileControllerProfile profile=new MobileControllerProfile();
    public final TouchControlLayout layout=new TouchControlLayout();
    public String devicePreview="LANDSCAPE_16_9";
    public ControllerEditorModel(){
        layout.add("left_stick","move",.04f,.68f,.20f,.25f).visual="stick";
        layout.add("right_stick","look",.76f,.68f,.20f,.25f).visual="stick";
        layout.add("jump","jump",.80f,.48f,.07f,.07f);
        layout.add("crouch","crouch",.72f,.55f,.06f,.06f);
        layout.add("fire","fire",.88f,.50f,.08f,.08f);
        layout.add("aim","aim",.80f,.59f,.06f,.06f);
        layout.add("reload","reload",.70f,.44f,.06f,.06f);
        layout.add("pause","pause",.94f,.04f,.04f,.06f);
    }
    public List<String> validate(){List<String> e=new ArrayList<>();if(layout.controls.isEmpty())e.add("No controls configured");for(TouchControlLayout.Control c:layout.controls)if(c.w<=0||c.h<=0)e.add("Invalid control size: "+c.id);return e;}
}
