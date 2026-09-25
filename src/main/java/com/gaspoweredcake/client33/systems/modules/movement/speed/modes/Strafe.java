/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.movement.speed.modes;

import com.gaspoweredcake.client33.Client33;
import com.gaspoweredcake.client33.events.entity.player.PlayerMoveEvent;
import com.gaspoweredcake.client33.mixininterface.IVec3d;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.Anchor;
import com.gaspoweredcake.client33.systems.modules.movement.speed.SpeedMode;
import com.gaspoweredcake.client33.systems.modules.movement.speed.SpeedModes;
import com.gaspoweredcake.client33.utils.player.PlayerUtils;
import org.joml.Vector2d;

public class Strafe extends SpeedMode {

    public Strafe() {
        super(SpeedModes.Strafe);
    }

    private long timer = 0L;

    @Override
    public void onMove(PlayerMoveEvent event) {
        switch (stage) {
            case 0: //Reset
                if (PlayerUtils.isMoving()) {
                    stage++;
                    speed = 1.18f * getDefaultSpeed() - 0.01;
                }
            case 1: //Jump
                if (!PlayerUtils.isMoving() || !mc.player.isOnGround()) break;

                ((IVec3d) event.movement).client33$setY(getHop(0.40123128));
                speed *= settings.ncpSpeed.get();
                stage++;
                break;
            case 2: speed = distance - 0.76 * (distance - getDefaultSpeed()); stage++; break; //Slowdown after jump
            case 3: //Reset on collision or predict and update speed
                if (!mc.world.isSpaceEmpty(mc.player.getBoundingBox().offset(0.0, mc.player.getVelocity().y, 0.0)) || mc.player.verticalCollision && stage > 0) {
                    stage = 0;
                }
                speed = distance - (distance / 159.0);
                break;
        }

        speed = Math.max(speed, getDefaultSpeed());

        if (settings.ncpSpeedLimit.get()) {
            if (System.currentTimeMillis() - timer > 2500L) {
                timer = System.currentTimeMillis();
            }

            speed = Math.min(speed, System.currentTimeMillis() - timer > 1250L ? 0.44D : 0.43D);
        }

        Vector2d change = transformStrafe(speed);

        Anchor anchor = Modules.get().get(Anchor.class);
        if (anchor.isActive() && anchor.controlMovement) {
            change.set(anchor.deltaX, anchor.deltaZ);
        }

        ((IVec3d) event.movement).client33$setXZ(change.x, change.y);
    }

    public static Vector2d transformStrafe(double speed) {
        float forward = Math.signum(Client33.mc.player.input.getMovementInput().y);
        float side = Math.signum(Client33.mc.player.input.getMovementInput().x);
        float yaw = Client33.mc.player.getLerpedYaw(Client33.mc.getRenderTickCounter().getTickProgress(true));

        if (forward == 0.0f && side == 0.0f) return new Vector2d();

        float strafe = 90 * side;
        if (forward != 0) strafe *= forward * 0.5f;

        yaw = yaw - strafe;
        if (forward < 0) yaw -= 180;
        double yawRadians = Math.toRadians(yaw);

        return new Vector2d(-Math.sin(yawRadians) * speed, Math.cos(yawRadians) * speed);
    }

    @Override
    public void onTick() {
        distance = Math.sqrt((mc.player.getX() - mc.player.lastX) * (mc.player.getX() - mc.player.lastX) + (mc.player.getZ() - mc.player.lastZ) * (mc.player.getZ() - mc.player.lastZ));
    }
}
