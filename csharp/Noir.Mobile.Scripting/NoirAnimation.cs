namespace Noir;

public enum AnimationLoopMode{None,Linear,PingPong}
public sealed class AnimationClip:Resource{public float Length{get;set;}public AnimationLoopMode LoopMode{get;set;}public bool Loop{get=>LoopMode!=AnimationLoopMode.None;set=>LoopMode=value?AnimationLoopMode.Linear:AnimationLoopMode.None;}}
public sealed class AnimationPlayer:Node
{
    public string CurrentAnimation{get;private set;}="";public float CurrentTime{get;private set;}public float SpeedScale{get;set;}=1;public bool Playing{get;private set;}
    public void Play(string animationName=""){CurrentAnimation=animationName;CurrentTime=0;Playing=true;}public void Stop(){Playing=false;CurrentTime=0;}public void Pause(){Playing=false;}
    public void Seek(float seconds,bool update=true){CurrentTime=MathF.Max(0,seconds);}public bool IsPlaying()=>Playing;
}
public sealed class AnimationTree:Node{public bool Active{get;set;}public float TimeScale{get;set;}=1;public string State{get;set;}="";public void Travel(string state){State=state;Active=true;}}
public sealed class Tween:Node{public bool IsRunning{get;private set;}public void Kill(){IsRunning=false;}public void Start(){IsRunning=true;}}
