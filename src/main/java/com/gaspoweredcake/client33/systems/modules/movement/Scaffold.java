/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.movement;

import com.gaspoweredcake.client33.events.world.TickEvent;
import com.gaspoweredcake.client33.renderer.ShapeMode;
import com.gaspoweredcake.client33.settings.*;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.utils.Utils;
import com.gaspoweredcake.client33.utils.misc.ListMode;
import com.gaspoweredcake.client33.utils.player.FindItemResult;
import com.gaspoweredcake.client33.utils.player.InvUtils;
import com.gaspoweredcake.client33.utils.player.PlayerUtils;
import com.gaspoweredcake.client33.utils.render.RenderUtils;
import com.gaspoweredcake.client33.utils.render.color.SettingColor;
import com.gaspoweredcake.client33.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Scaffold extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgRender = settings.createGroup("Render");

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Selected blocks.")
        .build()
    );

    private final Setting<ListMode> blocksFilter = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("blocks-filter")
        .description("How to use the block list setting")
        .defaultValue(ListMode.Blacklist)
        .build()
    );

    private final Setting<Boolean> fastTower = sgGeneral.add(new BoolSetting.Builder()
        .name("fast-tower")
        .description("Whether or not to scaffold upwards faster.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> towerSpeed = sgGeneral.add(new DoubleSetting.Builder()
        .name("tower-speed")
        .description("The speed at which to tower.")
        .defaultValue(0.5)
        .min(0)
        .sliderMax(1)
        .visible(fastTower::get)
        .build()
    );

    private final Setting<Boolean> whileMoving = sgGeneral.add(new BoolSetting.Builder()
        .name("while-moving")
        .description("Allows you to tower while moving.")
        .defaultValue(false)
        .visible(fastTower::get)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only places blocks when holding right click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> renderSwing = sgGeneral.add(new BoolSetting.Builder()
        .name("swing")
        .description("Renders your client-side swing.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Automatically swaps to a block before placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> rotate = sgGeneral.add(new BoolSetting.Builder()
        .name("rotate")
        .description("Rotates towards the blocks being placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> airPlace = sgGeneral.add(new BoolSetting.Builder()
        .name("air-place")
        .description("Allow air place. This also allows you to modify scaffold radius.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Double> aheadDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("ahead-distance")
        .description("How far ahead to place blocks.")
        .defaultValue(0)
        .min(0)
        .sliderMax(1)
        .visible(() -> !airPlace.get())
        .build()
    );

    private final Setting<Double> placeRange = sgGeneral.add(new DoubleSetting.Builder()
        .name("closest-block-range")
        .description("How far can scaffold place blocks when you are in air.")
        .defaultValue(4)
        .min(0)
        .sliderMax(8)
        .visible(() -> !airPlace.get())
        .build()
    );

    private final Setting<Double> radius = sgGeneral.add(new DoubleSetting.Builder()
        .name("radius")
        .description("Scaffold radius.")
        .defaultValue(0)
        .min(0)
        .max(6)
        .visible(airPlace::get)
        .build()
    );

    private final Setting<Integer> blocksPerTick = sgGeneral.add(new IntSetting.Builder()
        .name("blocks-per-tick")
        .description("How many blocks to place in one tick.")
        .defaultValue(3)
        .min(1)
        .visible(airPlace::get)
        .build()
    );

    // Render

    private final Setting<Boolean> render = sgRender.add(new BoolSetting.Builder()
        .name("render")
        .description("Whether to render blocks that have been placed.")
        .defaultValue(true)
        .build()
    );

    private final Setting<ShapeMode> shapeMode = sgRender.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> sideColor = sgRender.add(new ColorSetting.Builder()
        .name("side-color")
        .description("The side color of the target block rendering.")
        .defaultValue(new SettingColor(197, 137, 232, 10))
        .visible(render::get)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgRender.add(new ColorSetting.Builder()
        .name("line-color")
        .description("The line color of the target block rendering.")
        .defaultValue(new SettingColor(197, 137, 232))
        .visible(render::get)
        .build()
    );

    private final BlockPos.MutableBlockPos bp = new BlockPos.MutableBlockPos();

    public Scaffold() {
        super(Categories.Movement, "scaffold", "Automatically places blocks under you.");
    }


    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;
        if (onlyOnClick.get() && !mc.options.keyUse.isDown()) return;

        Vec3 vec = mc.player.position().add(mc.player.getDeltaMovement()).add(0, -0.75, 0);
        if (airPlace.get()) {
            bp.set(vec.x(), vec.y(), vec.z());
        } else {
            Vec3 pos = mc.player.position().add(mc.player.getDeltaMovement().multiply(1, 0, 1));
            BlockPos below = mc.player.blockPosition().below();
            if (aheadDistance.get() != 0 && !towering() && !mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) {
                Vec3 dir = Vec3.directionFromRotation(0, mc.player.getYRot()).multiply(aheadDistance.get(), 0, aheadDistance.get());
                if (mc.options.keyUp.isDown()) pos = pos.add(dir.x, 0, dir.z);
                if (mc.options.keyDown.isDown()) pos = pos.add(-dir.x, 0, -dir.z);
                if (mc.options.keyLeft.isDown()) pos = pos.add(dir.z, 0, -dir.x);
                if (mc.options.keyRight.isDown()) pos = pos.add(-dir.z, 0, dir.x);
            }
            bp.set(pos.x, vec.y, pos.z);
        }
        if (mc.options.keyShift.isDown() && !mc.options.keyJump.isDown() && bp.getY() > mc.level.getMinY()) {
            bp.setY(bp.getY() - 1);
        }
        if (bp.getY() >= mc.player.blockPosition().getY()) {
            bp.setY(mc.player.blockPosition().getY() - 1);
        }
        BlockPos targetBlock = bp.immutable();

        if (!airPlace.get() && BlockUtils.getPlaceSide(bp) == null) {
            double searchRange = placeRange.get();
            Vec3 eye = mc.player.getEyePosition();
            double reach = mc.player.blockInteractionRange();
            double reachSq = reach * reach;
            BlockPos best = null;
            double bestDistance = Double.MAX_VALUE;
            int minY = Math.max(mc.level.getMinY(), Mth.floor(mc.player.getY() - searchRange));
            int maxY = Math.min(mc.level.getMinY() + mc.level.getHeight() - 1, Mth.floor(mc.player.getY() + searchRange));

            for (int x = Mth.floor(mc.player.getX() - searchRange); x <= Mth.floor(mc.player.getX() + searchRange); x++) {
                for (int z = Mth.floor(mc.player.getZ() - searchRange); z <= Mth.floor(mc.player.getZ() + searchRange); z++) {
                    for (int y = minY; y <= maxY; y++) {
                        bp.set(x, y, z);
                        if (!BlockUtils.canPlace(bp)) continue;
                        Direction side = BlockUtils.getPlaceSide(bp);
                        if (side == null) continue;
                        Vec3 hitPos = Vec3.atCenterOf(bp).add(side.getStepX() * 0.5, side.getStepY() * 0.5, side.getStepZ() * 0.5);
                        if (eye.distanceToSqr(hitPos) > reachSq) continue;

                        double distance = bp.distSqr(targetBlock);
                        if (distance < bestDistance) {
                            bestDistance = distance;
                            best = bp.immutable();
                        }
                    }
                }
            }
            if (best == null) return;
            bp.set(best);
        }

        if (airPlace.get()) {
            List<BlockPos> blocks = new ArrayList<>();
            double radiusSq = radius.get() * radius.get();
            for (int x = Mth.ceil(bp.getX() - radius.get()); x <= Mth.floor(bp.getX() + radius.get()); x++) {
                for (int z = Mth.ceil(bp.getZ() - radius.get()); z <= Mth.floor(bp.getZ() + radius.get()); z++) {
                    BlockPos blockPos = BlockPos.containing(x, bp.getY(), z);
                    int dx = x - bp.getX();
                    int dz = z - bp.getZ();
                    if (dx * dx + dz * dz <= radiusSq) {
                        blocks.add(blockPos);
                    }
                }
            }

            if (!blocks.isEmpty()) {
                blocks.sort(Comparator.comparingDouble(PlayerUtils::squaredDistanceTo));
                int counter = 0;
                for (BlockPos block : blocks) {
                    if (place(block)) {
                        counter++;
                    }

                    if (counter >= blocksPerTick.get()) {
                        break;
                    }
                }
            }
        } else {
            place(bp);
        }

        FindItemResult result = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (fastTower.get() && mc.options.keyJump.isDown() && !mc.options.keyShift.isDown()
            && (whileMoving.get() || !PlayerUtils.isMoving()) && result.found()
            && (autoSwitch.get() || result.getHand() != null) && hasTowerSupport()) {
            Vec3 velocity = mc.player.getDeltaMovement();
            AABB playerBox = mc.player.getBoundingBox();
            if (!mc.level.getBlockCollisions(mc.player, playerBox.move(0, 1, 0)).iterator().hasNext()) {
                mc.player.setDeltaMovement(velocity.x, towerSpeed.get(), velocity.z);
            } else if (velocity.y > 0) {
                mc.player.setDeltaMovement(velocity.x, 0, velocity.z);
            }
        }
    }

    public boolean scaffolding() {
        return isActive() && (!onlyOnClick.get() || (onlyOnClick.get() && mc.options.keyUse.isDown()));
    }

    public boolean towering() {
        if (!Utils.canUpdate()) return false;
        FindItemResult result = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        return scaffolding() && fastTower.get() && mc.options.keyJump.isDown() && !mc.options.keyShift.isDown() &&
            (whileMoving.get() || !PlayerUtils.isMoving()) && result.found() && (autoSwitch.get() || result.getHand() != null) && hasTowerSupport();
    }

    private boolean hasTowerSupport() {
        BlockPos below = mc.player.blockPosition().below();
        if (!mc.level.getBlockState(below).getCollisionShape(mc.level, below).isEmpty()) return true;
        return BlockUtils.canPlace(below) && (airPlace.get() || BlockUtils.getPlaceSide(below) != null);
    }

    private boolean validItem(ItemStack itemStack, BlockPos pos) {
        if (!(itemStack.getItem() instanceof BlockItem)) return false;

        Block block = ((BlockItem) itemStack.getItem()).getBlock();

        if (!blocksFilter.get().allows(blocks.get().contains(block))) return false;

        if (!Block.isShapeFullBlock(block.defaultBlockState().getCollisionShape(mc.level, pos))) return false;
        return !(block instanceof FallingBlock) || !FallingBlock.isFree(mc.level.getBlockState(pos));
    }

    private boolean place(BlockPos bp) {
        FindItemResult item = InvUtils.findInHotbar(itemStack -> validItem(itemStack, bp));
        if (!item.found()) return false;

        if (item.getHand() == null && !autoSwitch.get()) return false;

        if (BlockUtils.place(bp, item, rotate.get(), 50, renderSwing.get(), true)) {
            // Render block if was placed
            if (render.get())
                RenderUtils.renderTickingBlock(bp.immutable(), sideColor.get(), lineColor.get(), shapeMode.get(), 0, 8, true, false);
            return true;
        }
        return false;
    }

}
