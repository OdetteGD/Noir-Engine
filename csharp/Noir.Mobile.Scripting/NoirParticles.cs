namespace Noir;

public enum ParticleDrawPass{Billboard,Mesh,Ribbon}
public sealed class ParticleProcessMaterial:NoirResource
{
    public Color Color{get;set;}=Color.White;public float Lifetime{get;set;}=1;public float InitialVelocityMin{get;set;}public float InitialVelocityMax{get;set;}
    public Vector3 Gravity{get;set;}=new(0,-9.81f,0);public float SpreadDegrees{get;set;}=45;
}
public sealed class GPUParticles3D:Node3D
{
    public int Amount{get;set;}=256;public float Lifetime{get;set;}=1;public bool Emitting{get;set;}public float Preprocess{get;set;}
    public ParticleProcessMaterial? ProcessMaterial{get;set;}public ParticleDrawPass DrawPass{get;set;}=ParticleDrawPass.Billboard;
    public void Restart(){Emitting=false;Emitting=true;}
}
public sealed class GPUParticles2D:Node2D{public int Amount{get;set;}=256;public float Lifetime{get;set;}=1;public bool Emitting{get;set;}public void Restart(){Emitting=false;Emitting=true;}}
