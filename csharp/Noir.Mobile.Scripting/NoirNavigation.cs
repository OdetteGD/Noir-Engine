namespace Noir;

public enum NavigationPathStatus{Empty,Partial,Complete}
public sealed class NavigationAgent3D:Node3D
{
    public float PathDesiredDistance{get;set;}=.5f;public float TargetDesiredDistance{get;set;}=.5f;public float Radius{get;set;}=.5f;public float Height{get;set;}=2;
    public Vector3 TargetPosition{get;set;}public Vector3 NextPathPosition{get;private set;}public NavigationPathStatus PathStatus{get;private set;}=NavigationPathStatus.Empty;
    public void SetTargetPosition(Vector3 target){TargetPosition=target;NextPathPosition=target;PathStatus=NavigationPathStatus.Complete;}
    public void Advance(float delta){Position=Vector3.Lerp(Position,NextPathPosition,Mathf.Clamp01(delta*6));}
}
public sealed class NavigationRegion3D:Node3D{public bool Enabled{get;set;}=true;public string NavigationMeshPath{get;set;}="";public float CellSize{get;set;}=.25f;public float CellHeight{get;set;}=.25f;}
public sealed class NavigationObstacle3D:Node3D{public bool AvoidanceEnabled{get;set;}=true;public float Radius{get;set;}=.5f;public float Height{get;set;}=2;}
