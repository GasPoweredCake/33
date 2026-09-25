/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.movement;

import com.gaspoweredcake.client33.events.entity.player.PlayerMoveEvent;
import com.gaspoweredcake.client33.events.packets.PacketEvent;
import com.gaspoweredcake.client33.events.world.TickEvent;
import com.gaspoweredcake.client33.mixin.ClientPlayerEntityAccessor;
import com.gaspoweredcake.client33.mixin.PlayerMoveC2SPacketAccessor;
import com.gaspoweredcake.client33.mixininterface.IVec3d;
import com.gaspoweredcake.client33.settings.*;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.utils.Utils;
import com.gaspoweredcake.client33.utils.entity.EntityUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.entity.MovementType;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.math.Vec3d;

public class Flight extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgAntiKick = settings.createGroup("Anti Kick"); //Pog

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("The mode for Flight.")
        .defaultValue(Mode.Auto)
        .onChanged(mode -> {
            if (!isActive() || !Utils.canUpdate()) return;
            restoreAbilities();
            resetAntiKick();
            if (usesAbilities()) applyAbilities();
        })
        .build()
    );

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Your speed when flying.")
        .defaultValue(0.1)
        .min(0.0)
        .build()
    );

    private final Setting<Boolean> verticalSpeedMatch = sgGeneral.add(new BoolSetting.Builder()
        .name("vertical-speed-match")
        .description("Matches your vertical speed to your horizontal speed, otherwise uses vanilla ratio.")
        .defaultValue(false)
        .build()
    );

    private final Setting<InputMode> inputMode = sgGeneral.add(new EnumSetting.Builder<InputMode>()
        .name("input-mode")
        .description("Player Input follows processed movement controls; Keybinds reads the bound keys directly.")
        .defaultValue(InputMode.PlayerInput)
        .visible(() -> mode.get() != Mode.Abilities)
        .build()
    );

    private final Setting<Boolean> noSneak = sgGeneral.add(new BoolSetting.Builder()
        .name("no-sneak")
        .description("Prevents you from sneaking while flying.")
        .defaultValue(false)
        .visible(() -> mode.get() != Mode.Abilities)
        .build()
    );

    private final Setting<AntiKickMode> antiKickMode = sgAntiKick.add(new EnumSetting.Builder<AntiKickMode>()
        .name("mode")
        .description("The mode for anti kick.")
        .defaultValue(AntiKickMode.Packet)
        .build()
    );

    private final Setting<Integer> delay = sgAntiKick.add(new IntSetting.Builder()
        .name("delay")
        .description("The amount of delay, in ticks, between flying down a bit and return to original position")
        .defaultValue(20)
        .min(1)
        .sliderMax(200)
        .build()
    );

    // Anti Kick
    private final Setting<Integer> offTime = sgAntiKick.add(new IntSetting.Builder()
        .name("off-time")
        .description("The amount of delay, in ticks, to fly down a bit to reset floating ticks.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 20)
        .build()
    );

    private int delayLeft;
    private int offLeft;
    private boolean flip;
    private float lastYaw;
    private double lastPacketY = Double.MAX_VALUE;
    private boolean savedAbilities;
    private boolean savedAllowFlying;
    private boolean savedFlying;
    private float savedFlySpeed;

    public Flight() {
        super(Categories.Movement, "flight", "Fly with server abilities or direct movement control.");
    }

    @Override
    public void onActivate() {
        if (!Utils.canUpdate()) return;
        savedAllowFlying = mc.player.getAbilities().allowFlying;
        savedFlying = mc.player.getAbilities().flying;
        savedFlySpeed = mc.player.getAbilities().getFlySpeed();
        savedAbilities = true;
        resetAntiKick();
        if (usesAbilities()) applyAbilities();
    }

    @Override
    public void onDeactivate() {
        if (Utils.canUpdate()) restoreAbilities();
        savedAbilities = false;
    }

    @EventHandler
    private void onPreTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (!usesAbilities()) {
            mc.player.getAbilities().flying = false;
            mc.player.setVelocity(getFlightVelocity());
        }

        float currentYaw = mc.player.getYaw();
        if (usesAbilities() && mc.player.fallDistance >= 3f && currentYaw == lastYaw && mc.player.getVelocity().length() < 0.003d) {
            mc.player.setYaw(currentYaw + (flip ? 1 : -1));
            flip = !flip;
        }
        lastYaw = currentYaw;
    }

    @EventHandler
    private void onPostTick(TickEvent.Post event) {
        if (!Utils.canUpdate()) return;
        if (delayLeft > 0) delayLeft--;

        if (offLeft <= 0 && delayLeft <= 0) {
            delayLeft = delay.get();
            offLeft = offTime.get();

            if (antiKickMode.get() == AntiKickMode.Packet) {
                // Resend movement packets
                ((ClientPlayerEntityAccessor) mc.player).client33$setTicksSinceLastPositionPacketSent(20);
            }
        } else if (delayLeft <= 0) {
            boolean shouldReturn = false;

            if (antiKickMode.get() == AntiKickMode.Normal) {
                if (usesAbilities()) {
                    mc.player.getAbilities().flying = false;
                    shouldReturn = true;
                }
            } else if (antiKickMode.get() == AntiKickMode.Packet && offLeft == offTime.get()) {
                // Resend movement packets
                ((ClientPlayerEntityAccessor) mc.player).client33$setTicksSinceLastPositionPacketSent(20);
            }

            offLeft--;

            if (shouldReturn) return;
        }

        if (mc.player.getYaw() != lastYaw) mc.player.setYaw(lastYaw);

        if (usesAbilities()) applyAbilities();
        else {
            mc.player.getAbilities().flying = false;
            mc.player.setVelocity(getFlightVelocity());
            if (noSneak.get()) mc.player.setOnGround(false);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    private void onPlayerMove(PlayerMoveEvent event) {
        if (event.type != MovementType.SELF || usesAbilities()) return;
        Vec3d velocity = getFlightVelocity();
        ((IVec3d) event.movement).client33$set(velocity.x, velocity.y, velocity.z);
    }

    private Vec3d getFlightVelocity() {
        boolean forward = inputMode.get() == InputMode.Keybinds ? mc.options.forwardKey.isPressed() : mc.player.input.playerInput.forward();
        boolean backward = inputMode.get() == InputMode.Keybinds ? mc.options.backKey.isPressed() : mc.player.input.playerInput.backward();
        boolean left = inputMode.get() == InputMode.Keybinds ? mc.options.leftKey.isPressed() : mc.player.input.playerInput.left();
        boolean right = inputMode.get() == InputMode.Keybinds ? mc.options.rightKey.isPressed() : mc.player.input.playerInput.right();
        boolean jump = inputMode.get() == InputMode.Keybinds ? mc.options.jumpKey.isPressed() : mc.player.input.playerInput.jump();
        boolean sneak = inputMode.get() == InputMode.Keybinds ? mc.options.sneakKey.isPressed() : mc.player.input.playerInput.sneak();

        Vec3d horizontal = Vec3d.fromPolar(0, mc.player.getYaw()).multiply((forward ? 1 : 0) - (backward ? 1 : 0))
            .add(Vec3d.fromPolar(0, mc.player.getYaw() + 90).multiply((right ? 1 : 0) - (left ? 1 : 0)));
        if (horizontal.lengthSquared() > 1) horizontal = horizontal.normalize();

        double horizontalSpeed = speed.get() * (mc.player.isSprinting() ? 15 : 10);
        double verticalSpeed = speed.get() * (verticalSpeedMatch.get() ? 10 : 5);
        return new Vec3d(horizontal.x * horizontalSpeed, ((jump ? 1 : 0) - (sneak ? 1 : 0)) * verticalSpeed, horizontal.z * horizontalSpeed);
    }

    private boolean usesAbilities() {
        return mode.get() == Mode.Abilities || (mode.get() == Mode.Auto && savedAllowFlying);
    }

    private void resetAntiKick() {
        delayLeft = delay.get();
        offLeft = offTime.get();
        lastPacketY = Double.MAX_VALUE;
        flip = false;
        lastYaw = mc.player.getYaw();
    }

    private void applyAbilities() {
        if (mc.player.isSpectator()) return;
        mc.player.getAbilities().allowFlying = true;
        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(speed.get().floatValue());
    }

    private void restoreAbilities() {
        if (!savedAbilities) return;
        mc.player.getAbilities().allowFlying = savedAllowFlying;
        mc.player.getAbilities().flying = savedFlying;
        mc.player.getAbilities().setFlySpeed(savedFlySpeed);
    }

    private void antiKickPacket(PlayerMoveC2SPacket packet, double currentY) {
        // maximum time we can be "floating" is 80 ticks, so 4 seconds max
        if (this.delayLeft <= 0 && this.lastPacketY != Double.MAX_VALUE &&
            shouldFlyDown(currentY, this.lastPacketY) && EntityUtils.isOnAir(mc.player)) {
            // actual check is for >= -0.03125D, but we have to do a bit more than that
            // due to the fact that it's a bigger or *equal* to, and not just a bigger than
            ((PlayerMoveC2SPacketAccessor) packet).client33$setY(lastPacketY - 0.03130D);
        } else {
            lastPacketY = currentY;
        }
    }

    /**
     * @see ServerPlayNetworkHandler#onPlayerMove(PlayerMoveC2SPacket)
     */
    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (!(event.packet instanceof PlayerMoveC2SPacket packet) || antiKickMode.get() != AntiKickMode.Packet) return;

        double currentY = packet.getY(Double.MAX_VALUE);
        if (currentY != Double.MAX_VALUE) {
            antiKickPacket(packet, currentY);
        } else {
            // if the packet is a LookAndOnGround packet or an OnGroundOnly packet then we need to
            // make it a Full packet or a PositionAndOnGround packet respectively, so it has a Y value
            PlayerMoveC2SPacket fullPacket;
            if (packet.changesLook()) {
                fullPacket = new PlayerMoveC2SPacket.Full(
                    mc.player.getX(),
                    mc.player.getY(),
                    mc.player.getZ(),
                    packet.getYaw(0),
                    packet.getPitch(0),
                    packet.isOnGround(),
                    mc.player.horizontalCollision
                );
            } else {
                fullPacket = new PlayerMoveC2SPacket.PositionAndOnGround(
                    mc.player.getX(),
                    mc.player.getY(),
                    mc.player.getZ(),
                    packet.isOnGround(),
                    mc.player.horizontalCollision
                );
            }
            event.cancel();
            antiKickPacket(fullPacket, mc.player.getY());
            mc.getNetworkHandler().sendPacket(fullPacket);
        }
    }

    @EventHandler
    private void onReceivePacket(PacketEvent.Receive event) {
        if (!Utils.canUpdate() || !(event.packet instanceof PlayerAbilitiesS2CPacket packet)) return;
        if (savedAbilities) {
            savedAllowFlying = packet.allowFlying();
            savedFlying = packet.isFlying();
            savedFlySpeed = packet.getFlySpeed();
        }
        if (!usesAbilities()) return;
        event.cancel(); // Cancel packet, so fly won't be toggled

        mc.player.getAbilities().invulnerable = packet.isInvulnerable();
        mc.player.getAbilities().creativeMode = packet.isCreativeMode();
        mc.player.getAbilities().setWalkSpeed(packet.getWalkSpeed());
        applyAbilities();
    }

    private boolean shouldFlyDown(double currentY, double lastY) {
        if (currentY >= lastY) {
            return true;
        } else return lastY - currentY < 0.03130D;
    }

    public float getOffGroundSpeed() {
        // Velocity mode now sets movement directly rather than relying on airborne acceleration.
        return -1;
    }

    public boolean noSneak() {
        return isActive() && !usesAbilities() && noSneak.get();
    }

    public enum Mode {
        Auto,
        Abilities,
        Velocity
    }

    public enum InputMode {
        PlayerInput,
        Keybinds
    }

    public enum AntiKickMode {
        Normal,
        Packet,
        None
    }
}
