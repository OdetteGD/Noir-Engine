package com.noir.game.engine.scene;

import com.noir.game.engine.character.CharacterController3D;
import java.util.*;

/**
 * Character3D scene node: capsule movement, stance, animation selection and camera target metadata.
 * The controller is deliberately backend-neutral so the same data is usable by the editor,
 * deterministic tests and a future native physics implementation.
 */
public final class NoirCharacterNode {
    public final NoirNode node;
    public final CharacterController3D controller = new CharacterController3D();
    public String skeletonAsset = "";
    public String idleAnimation = "idle", walkAnimation = "walk", runAnimation = "run", jumpAnimation = "jump";
    public boolean rootMotion = false, cameraRelativeMovement = true;
    public float cameraHeight = 1.55f, cameraDistance = 3.5f;
    public final List<String> animationEvents = new ArrayList<>();

    public NoirCharacterNode(String id, String name) {
        node = new NoirNode(id, name, NoirNode.Kind.CHARACTER3D);
    }

    public void setInput(float x, float z, boolean sprint, boolean jump) {
        controller.setMoveInput(x, z);
        controller.sprinting = sprint;
        if (jump) controller.queueJump();
    }

    public void setStance(CharacterController3D.Stance stance) {
        controller.setStance(stance);
    }

    public void update(float dt) {
        controller.tick(dt);
        node.px = controller.x; node.py = controller.y; node.pz = controller.z;
    }

    public String locomotionState() {
        float speed = (float)Math.sqrt(controller.vx * controller.vx + controller.vz * controller.vz);
        if (!controller.grounded) return jumpAnimation;
        if (speed < 0.05f) return idleAnimation;
        return controller.sprinting ? runAnimation : walkAnimation;
    }

    public void addAnimationEvent(String event) { if (event != null && !event.isEmpty()) animationEvents.add(event); }
}
