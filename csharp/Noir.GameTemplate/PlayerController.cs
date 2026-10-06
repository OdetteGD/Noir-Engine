using Noir;

namespace Game;

public sealed class PlayerController : Character3D
{
    public float Speed { get; set; } = 5f;
    public float JumpVelocity { get; set; } = 4.5f;

    public override void Start()
    {
        Name = "PlayerController";
        Debug.Log("C# PlayerController started");
    }

    public override void PhysicsUpdate(float delta)
    {
        var move = Input.Vector("ui_left","ui_right","ui_up","ui_down");
        Velocity = new Vector3(move.X * Speed, Velocity.Y, move.Y * Speed);
        if(IsOnFloor && Input.IsActionJustPressed("action_jump"))
            Velocity = new Vector3(Velocity.X,JumpVelocity,Velocity.Z);
        MoveAndSlide();
    }
}
