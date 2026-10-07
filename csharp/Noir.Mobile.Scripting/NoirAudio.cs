namespace Noir;

public enum AudioBus{Master,Music,Sfx,Voice,UI,Ambient}
public class AudioStream:NoirResource{public float Length{get;set;}public int SampleRate{get;set;}=48000;public int Channels{get;set;}=2;}
public class AudioStreamPlayer:Node
{
    public AudioStream? Stream{get;set;}public float VolumeDb{get;set;}public float PitchScale{get;set;}=1;
    public bool Playing{get;private set;}public AudioBus Bus{get;set;}=AudioBus.Master;public bool Autoplay{get;set;}
    public event Action? Finished;
    public void Play(float fromPosition=0){Playing=true;}public void Stop(){Playing=false;}public void Pause(){Playing=false;}
    public void Finish(){Playing=false;Finished?.Invoke();}
}
public sealed class AudioStreamPlayer3D:AudioStreamPlayer{public float MaxDistance{get;set;}=20;public float UnitSize{get;set;}=1;public float Attenuation{get;set;}=1;public bool DopplerTracking{get;set;}}
public sealed class AudioListener3D:Node3D{public bool Current{get;set;}=true;}
public static class AudioServer
{
    private static readonly Dictionary<AudioBus,float> Volumes=new();
    public static void SetBusVolumeDb(AudioBus bus,float db)=>Volumes[bus]=db;
    public static float GetBusVolumeDb(AudioBus bus)=>Volumes.TryGetValue(bus,out var v)?v:0;
}
