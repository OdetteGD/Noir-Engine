namespace Noir;

public enum HorizontalAlignment{Left,Center,Right,Fill}
public enum VerticalAlignment{Top,Center,Bottom,Fill}
public enum MouseFilter{Stop,Pass,Ignore}

public class Control:Node3D
{
    public Vector2 Size{get;set;}=new(100,40);
    public Vector2 MinSize{get;set;}=Vector2.Zero;
    public Vector2 AnchorMin{get;set;}=Vector2.Zero;
    public Vector2 AnchorMax{get;set;}=Vector2.Zero;
    public Vector2 Offset{get;set;}=Vector2.Zero;
    public MouseFilter MouseFilter{get;set;}=MouseFilter.Stop;
    public bool ClipContents{get;set;}
    public Rect2 Rect=>new(Position.X,Position.Y,Size.X,Size.Y);
    public event Action? Resized;
    public void SetSize(Vector2 size){Size=size;Resized?.Invoke();}
}

public class CanvasLayer:Node{public int Layer{get;set;}=1;public bool FollowViewportEnabled{get;set;}=true;}
public class Canvas:Control{public bool Transparent{get;set;}=true;}
public class Label:Control
{
    public string Text{get;set;}="";
    public float FontSize{get;set;}=16;
    public Color FontColor{get;set;}=Color.White;
    public HorizontalAlignment HorizontalAlignment{get;set;}=HorizontalAlignment.Left;
    public VerticalAlignment VerticalAlignment{get;set;}=VerticalAlignment.Center;
}
public class Button:Control
{
    public string Text{get;set;}="";
    public bool Disabled{get;set;}
    public bool ToggleMode{get;set;}
    public bool ButtonPressed{get;set;}
    public event Action? Pressed;
    public void Click(){if(Disabled)return;if(ToggleMode)ButtonPressed=!ButtonPressed;Pressed?.Invoke();}
}
public sealed class CheckButton:Button{public bool Checked{get=>ButtonPressed;set=>ButtonPressed=value;}}
public sealed class TextureRect:Control{public string TexturePath{get;set;}="";public bool ExpandMode{get;set;}=true;}
public sealed class ColorRect:Control{public Color Color{get;set;}=Color.White;}
public class Panel:Control{public Color Modulate{get;set;}=Color.White;}
public sealed class ProgressBar:Control
{
    public float MinValue{get;set;}=0;public float MaxValue{get;set;}=1;public float Value{get;set;}=1;public bool ShowPercentage{get;set;}=true;
}
public sealed class Slider:Control
{
    public float MinValue{get;set;}=0;public float MaxValue{get;set;}=1;public float Step{get;set;}=.01f;public float Value{get;set;}
    public event Action<float>? ValueChanged;
    public void SetValue(float value){Value=Mathf.Clamp(value,MinValue,MaxValue);ValueChanged?.Invoke(Value);}
}
public abstract class Container:Control{public float Separation{get;set;}=4;public void SortChildren(){}}
public sealed class VBoxContainer:Container{}
public sealed class HBoxContainer:Container{}
public sealed class GridContainer:Container{public int Columns{get;set;}=2;}
public sealed class ScrollContainer:Container{public Vector2 ScrollOffset{get;set;}}
public sealed class MarginContainer:Container{public float Margin{get;set;}=8;}
public sealed class CenterContainer:Container{}
public sealed class AspectRatioContainer:Container{public float Ratio{get;set;}=1.7777f;}

public sealed class TouchScreenButton:Button
{
    public string Action{get;set;}="";
    public bool PassByTouch{get;set;}=true;
    public bool TouchPressed{get;private set;}
    public void SetTouchState(bool pressed){TouchPressed=pressed;if(!string.IsNullOrEmpty(Action))Input.SetAction(Action,pressed);}
}
public sealed class VirtualJoystick:Control
{
    public string LeftAction{get;set;}="ui_left";public string RightAction{get;set;}="ui_right";
    public string UpAction{get;set;}="ui_up";public string DownAction{get;set;}="ui_down";
    public Vector2 Value{get;private set;}
    public void SetValue(Vector2 value)
    {
        Value=new(Mathf.Clamp(value.X,-1,1),Mathf.Clamp(value.Y,-1,1));
        Input.SetAction(LeftAction,Value.X<-.1f);Input.SetAction(RightAction,Value.X>.1f);
        Input.SetAction(UpAction,Value.Y<-.1f);Input.SetAction(DownAction,Value.Y>.1f);
    }
}
