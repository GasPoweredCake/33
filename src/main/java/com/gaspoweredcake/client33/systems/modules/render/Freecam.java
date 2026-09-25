/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.render;


import com.gaspoweredcake.client33.events.game.GameLeftEvent;
import com.gaspoweredcake.client33.events.game.OpenScreenEvent;
import com.gaspoweredcake.client33.events.client33.KeyEvent;
import com.gaspoweredcake.client33.events.client33.MouseClickEvent;
import com.gaspoweredcake.client33.events.client33.MouseScrollEvent;
import com.gaspoweredcake.client33.events.entity.player.DoItemUseEvent;
import com.gaspoweredcake.client33.events.packets.PacketEvent;
import com.gaspoweredcake.client33.events.world.ChunkOcclusionEvent;
import com.gaspoweredcake.client33.events.world.TickEvent;
import com.gaspoweredcake.client33.pathing.PathManagers;
import com.gaspoweredcake.client33.settings.BoolSetting;
import com.gaspoweredcake.client33.settings.DoubleSetting;
import com.gaspoweredcake.client33.settings.Setting;
import com.gaspoweredcake.client33.settings.SettingGroup;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.GUIMove;
import com.gaspoweredcake.client33.utils.Utils;
import com.gaspoweredcake.client33.utils.misc.input.Input;
import com.gaspoweredcake.client33.utils.misc.input.KeyAction;
import com.gaspoweredcake.client33.utils.player.Rotations;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.DeathMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.lwjgl.glfw.GLFW;

public class Freecam extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgPathing = settings.createGroup("Pathing");

    private final Setting<Double> speed = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed")
        .description("Your speed while in freecam.")
        .onChanged(aDouble -> speedValue = aDouble)
        .defaultValue(1.0)
        .min(0.0)
        .build()
    );

    private final Setting<Double> speedScrollSensitivity = sgGeneral.add(new DoubleSetting.Builder()
        .name("speed-scroll-sensitivity")
        .description("Allows you to change speed value using scroll wheel. 0 to disable.")
        .defaultValue(0)
        .min(0)
        .sliderMax(2)
        .build()
    );

    private final Setting<Boolean> staySneaking = sgGeneral.add(new BoolSetting.Builder()
        .name("stay-sneaking")
        .description("If you are sneaking when you enter freecam, whether your player should remain sneaking.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> toggleOnDamage = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-damage")
        .description("Disables freecam when you take damage.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOnDeath = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-death")
        .description("Disables freecam when you die.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> toggleOnLog = sgGeneral.add(new BoolSetting.Builder()
        .name("toggle-on-log")
        .description("Disables freecam when you disconnect from a server.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> reloadChunks = sgGeneral.add(new BoolSetting.Builder()
        .name("reload-chunks")
        .description("Disables cave culling.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> renderHands = sgGeneral.add(new BoolSetting.Builder()
        .name("show-hands")
        .description("Whether or not to render your hands in freecam.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates to the block or entity you are looking at.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> interactThroughWalls = sgGeneral.add(new BoolSetting.Builder()
        .name("interact-through-walls")
        .description("Opens containers behind walls while your real player is within interaction reach.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Double> containerRayRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("container-ray-range")
        .description("How far the freecam view searches for a container through walls.")
        .defaultValue(6)
        .min(1)
        .sliderMax(12)
        .visible(interactThroughWalls::get)
        .build()
    );

    private final Setting<Boolean> staticView = sgGeneral.add(new BoolSetting.Builder()
        .name("static")
        .description("Disables settings that move the view.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> baritoneClick = sgPathing.add(new BoolSetting.Builder()
        .name("click-to-path")
        .description("Sets a pathfinding goal to any block/entity you click at.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> requireDoubleClick = sgPathing.add(new BoolSetting.Builder()
        .name("double-click")
        .description("Require two clicks to start pathing.")
        .defaultValue(false)
        .build()
    );

    public final Vector3d pos = new Vector3d();
    public final Vector3d prevPos = new Vector3d();

    private Perspective perspective;
    private double speedValue;

    public float yaw, pitch;
    public float lastYaw, lastPitch;

    private double fovScale;
    private boolean bobView;

    private boolean forward, backward, right, left, up, down, isSneaking;

    private long clickTs = 0;

    public Freecam() {
        super(Categories.Render, "freecam", "Allows the camera to move away from the player.");
    }

    @Override
    public void onActivate() {
        fovScale = mc.options.getFovEffectScale().getValue();
        bobView = mc.options.getBobView().getValue();
        if (staticView.get()) {
            mc.options.getFovEffectScale().setValue((double)0);
            mc.options.getBobView().setValue(false);
        }
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        perspective = mc.options.getPerspective();
        speedValue = speed.get();

        Utils.set(pos, mc.gameRenderer.getCamera().getCameraPos());
        Utils.set(prevPos, mc.gameRenderer.getCamera().getCameraPos());

        if (mc.options.getPerspective() == Perspective.THIRD_PERSON_FRONT) {
            yaw += 180;
            pitch *= -1;
        }

        lastYaw = yaw;
        lastPitch = pitch;

        isSneaking = mc.options.sneakKey.isPressed();

        forward = Input.isPressed(mc.options.forwardKey);
        backward = Input.isPressed(mc.options.backKey);
        right = Input.isPressed(mc.options.rightKey);
        left = Input.isPressed(mc.options.leftKey);
        up = Input.isPressed(mc.options.jumpKey);
        down = Input.isPressed(mc.options.sneakKey);

        unpress();
        if (reloadChunks.get()) mc.worldRenderer.reload();
    }

    @Override
    public void onDeactivate() {
        if (reloadChunks.get()) {
            mc.execute(mc.worldRenderer::reload);
        }

        mc.options.setPerspective(perspective);

        if (staticView.get()) {
            mc.options.getFovEffectScale().setValue(fovScale);
            mc.options.getBobView().setValue(bobView);
        }

        isSneaking = false;
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        unpress();

        prevPos.set(pos);
        lastYaw = yaw;
        lastPitch = pitch;
    }

    private void unpress() {
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(false);
        mc.options.rightKey.setPressed(false);
        mc.options.leftKey.setPressed(false);
        mc.options.jumpKey.setPressed(false);
        mc.options.sneakKey.setPressed(false);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onUse(DoItemUseEvent event) {
        if (!interactThroughWalls.get() || event.isCancelled() || mc.player == null || mc.world == null
            || mc.interactionManager == null || mc.currentScreen != null || mc.player.isSneaking()
            || mc.crosshairTarget instanceof EntityHitResult) return;

        if (mc.crosshairTarget instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
            && isContainer(hit.getBlockPos())) return;

        BlockHitResult hit = findContainerThroughWalls();
        if (hit == null || !mc.player.canInteractWithBlockAt(hit.getBlockPos(), 1)) return;

        for (Hand hand : Hand.values()) {
            ActionResult result = mc.interactionManager.interactBlock(mc.player, hand, hit);
            if (result instanceof ActionResult.Success success) {
                if (success.swingSource() == ActionResult.SwingSource.CLIENT) mc.player.swingHand(hand);
                event.cancel();
                return;
            }
            if (result instanceof ActionResult.Fail) {
                event.cancel();
                return;
            }
        }
    }

    @Nullable
    private BlockHitResult findContainerThroughWalls() {
        Vec3d look = Vec3d.fromPolar(pitch, yaw);
        BlockPos.Mutable blockPos = new BlockPos.Mutable();
        int lastX = Integer.MIN_VALUE;
        int lastY = Integer.MIN_VALUE;
        int lastZ = Integer.MIN_VALUE;

        for (double distance = 0; distance <= containerRayRange.get(); distance += 0.1) {
            int x = MathHelper.floor(pos.x + look.x * distance);
            int y = MathHelper.floor(pos.y + look.y * distance);
            int z = MathHelper.floor(pos.z + look.z * distance);
            if (x == lastX && y == lastY && z == lastZ) continue;

            lastX = x;
            lastY = y;
            lastZ = z;
            blockPos.set(x, y, z);

            if (isContainer(blockPos)) {
                BlockPos target = blockPos.toImmutable();
                return new BlockHitResult(Vec3d.ofCenter(target), Direction.UP, target, true);
            }
        }

        return null;
    }

    private boolean isContainer(BlockPos blockPos) {
        BlockState state = mc.world.getBlockState(blockPos);
        return state.hasBlockEntity() && (state.createScreenHandlerFactory(mc.world, blockPos) != null || state.isOf(Blocks.ENDER_CHEST));
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.getCameraEntity().isInsideWall()) mc.getCameraEntity().noClip = true;
        if (!perspective.isFirstPerson()) mc.options.setPerspective(Perspective.FIRST_PERSON);

        Vec3d forward = Vec3d.fromPolar(0, yaw);
        Vec3d right = Vec3d.fromPolar(0, yaw + 90);
        double velX = 0;
        double velY = 0;
        double velZ = 0;

        if (rotate.get()) {
            BlockPos crossHairPos;
            Vec3d crossHairPosition;

            if (mc.crosshairTarget instanceof EntityHitResult) {
                crossHairPos = ((EntityHitResult) mc.crosshairTarget).getEntity().getBlockPos();
                Rotations.rotate(Rotations.getYaw(crossHairPos), Rotations.getPitch(crossHairPos), 0, null);
            } else {
                crossHairPosition = mc.crosshairTarget.getPos();
                crossHairPos = ((BlockHitResult) mc.crosshairTarget).getBlockPos();

                if (!mc.world.getBlockState(crossHairPos).isAir()) {
                    Rotations.rotate(Rotations.getYaw(crossHairPosition), Rotations.getPitch(crossHairPosition), 0, null);
                }
            }
        }

        double s = 0.5;
        if (Input.isPressed(mc.options.sprintKey)) s = 1;

        boolean a = false;
        if (this.forward) {
            velX += forward.x * s * speedValue;
            velZ += forward.z * s * speedValue;
            a = true;
        }
        if (this.backward) {
            velX -= forward.x * s * speedValue;
            velZ -= forward.z * s * speedValue;
            a = true;
        }

        boolean b = false;
        if (this.right) {
            velX += right.x * s * speedValue;
            velZ += right.z * s * speedValue;
            b = true;
        }
        if (this.left) {
            velX -= right.x * s * speedValue;
            velZ -= right.z * s * speedValue;
            b = true;
        }

        if (a && b) {
            double diagonal = 1 / Math.sqrt(2);
            velX *= diagonal;
            velZ *= diagonal;
        }

        if (this.up) {
            velY += s * speedValue;
        }
        if (this.down) {
            velY -= s * speedValue;
        }

        prevPos.set(pos);
        pos.set(pos.x + velX, pos.y + velY, pos.z + velZ);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onKey(KeyEvent event) {
        if (Input.isKeyPressed(GLFW.GLFW_KEY_F3)) return;
        if (checkGuiMove()) return;

        if (onInput(event.key(), event.action)) event.cancel();
    }

    @Nullable
    private BlockPos rayCastEntity(Vec3d posVec, Vec3d max, short maxDist) {
        EntityHitResult res = ProjectileUtil.raycast(
            mc.player,
            posVec,
            max,
            Box.enclosing(BlockPos.ofFloored(posVec.x, posVec.y, posVec.z), BlockPos.ofFloored(max.x, max.y, max.z)),
            (entity) -> true,
            maxDist
        );

        if (res == null) return null;

        Vec3d vec = res.getPos();

        return BlockPos.ofFloored(vec.x, vec.y, vec.z);
    }

    @Nullable
    private BlockPos rayCastBlock(Vec3d posVec, Vec3d max) {
        RaycastContext ctx = new RaycastContext(
            posVec,
            max,
            RaycastContext.ShapeType.VISUAL,
            RaycastContext.FluidHandling.SOURCE_ONLY,
            ShapeContext.absent()
        );

        BlockHitResult res = mc.world.raycast(ctx);
        if (res.getType() == HitResult.Type.MISS) return null;

        // Don't move inside block
        return res.getBlockPos().add(res.getSide().getVector());
    }

    private void setGoal() {
        long prevClick = clickTs;
        clickTs = System.currentTimeMillis();

        if (requireDoubleClick.get() && clickTs - prevClick > 500) return;

        Camera cam = mc.gameRenderer.getCamera();
        Vec3d posVec = cam.getCameraPos();
        Vec3d lookVec = Vec3d.fromPolar(cam.getPitch(), cam.getYaw());
        short maxDist = 256;
        Vec3d max = posVec.add(lookVec.multiply(maxDist));

        BlockPos pos = rayCastEntity(posVec, max, maxDist);
        if (pos == null) {
            pos = rayCastBlock(posVec, max);
        }

        if (pos == null) return;

        PathManagers.get().moveTo(pos);
    }

    @EventHandler(priority = EventPriority.HIGH)
    private void onMouseClick(MouseClickEvent event) {
        if (checkGuiMove()) return;

        if (baritoneClick.get() && event.action == KeyAction.Press && mc.options.attackKey.matchesMouse(event.click)) {
            setGoal();
        }

        if (onInput(event.button(), event.action)) event.cancel();
    }

    private boolean onInput(int key, KeyAction action) {
        if (Input.getKey(mc.options.forwardKey) == key) {
            forward = action != KeyAction.Release;
            mc.options.forwardKey.setPressed(false);
        }
        else if (Input.getKey(mc.options.backKey) == key) {
            backward = action != KeyAction.Release;
            mc.options.backKey.setPressed(false);
        }
        else if (Input.getKey(mc.options.rightKey) == key) {
            right = action != KeyAction.Release;
            mc.options.rightKey.setPressed(false);
        }
        else if (Input.getKey(mc.options.leftKey) == key) {
            left = action != KeyAction.Release;
            mc.options.leftKey.setPressed(false);
        }
        else if (Input.getKey(mc.options.jumpKey) == key) {
            up = action != KeyAction.Release;
            mc.options.jumpKey.setPressed(false);
        }
        else if (Input.getKey(mc.options.sneakKey) == key) {
            down = action != KeyAction.Release;
            mc.options.sneakKey.setPressed(false);
        }
        else {
            return false;
        }

        return true;
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onMouseScroll(MouseScrollEvent event) {
        if (speedScrollSensitivity.get() > 0 && mc.currentScreen == null) {
            speedValue += event.value * 0.25 * (speedScrollSensitivity.get() * speedValue);
            if (speedValue < 0.1) speedValue = 0.1;

            event.cancel();
        }
    }

    @EventHandler
    private void onChunkOcclusion(ChunkOcclusionEvent event) {
        event.cancel();
    }

    @EventHandler
    private void onGameLeft(GameLeftEvent event) {
        if (!toggleOnLog.get()) return;

        toggle();
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event)  {
        if (event.packet instanceof DeathMessageS2CPacket packet) {
            Entity entity = mc.world.getEntityById(packet.playerId());
            if (entity == mc.player && toggleOnDeath.get()) {
                toggle();
                info("Toggled off because you died.");
            }
        }
        else if (event.packet instanceof HealthUpdateS2CPacket packet) {
            if (mc.player.getHealth() - packet.getHealth() > 0 && toggleOnDamage.get()) {
                toggle();
                info("Toggled off because you took damage.");
            }
        }
        else if (event.packet instanceof PlayerRespawnS2CPacket) {
            if (isActive()) {
                toggle();
                info("Toggled off because you changed dimensions.");
            }
        }
    }

    private boolean checkGuiMove() {
        GUIMove guiMove = Modules.get().get(GUIMove.class);
        if (mc.currentScreen != null && !guiMove.isActive()) return true;
        return (mc.currentScreen != null && guiMove.isActive() && guiMove.skip());
    }

    public void changeLookDirection(double deltaX, double deltaY) {
        lastYaw = yaw;
        lastPitch = pitch;

        yaw += (float) deltaX;
        pitch += (float) deltaY;

        pitch = MathHelper.clamp(pitch, -90, 90);
    }

    public boolean renderHands() {
        return !isActive() || renderHands.get();
    }

    public boolean handlesContainerInteraction() {
        return isActive() && interactThroughWalls.get();
    }

    public boolean staySneaking() {
        return isActive() && !mc.player.getAbilities().flying && staySneaking.get() && isSneaking;
    }

    public double getX(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.x, pos.x);
    }
    public double getY(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.y, pos.y);
    }
    public double getZ(float tickDelta) {
        return MathHelper.lerp(tickDelta, prevPos.z, pos.z);
    }

    public double getYaw(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastYaw, yaw);
    }
    public double getPitch(float tickDelta) {
        return MathHelper.lerp(tickDelta, lastPitch, pitch);
    }
}
