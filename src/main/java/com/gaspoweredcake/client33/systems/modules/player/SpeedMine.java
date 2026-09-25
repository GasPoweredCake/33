/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.player;

import com.gaspoweredcake.client33.events.packets.PacketEvent;
import com.gaspoweredcake.client33.events.world.TickEvent;
import com.gaspoweredcake.client33.mixin.ClientPlayerInteractionManagerAccessor;
import com.gaspoweredcake.client33.settings.*;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.utils.Utils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;

import java.util.List;

import static net.minecraft.entity.effect.StatusEffects.HASTE;

public class SpeedMine extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .defaultValue(Mode.Damage)
        .onChanged(mode -> removeHaste())
        .build()
    );

    private final Setting<List<Block>> blocks = sgGeneral.add(new BlockListSetting.Builder()
        .name("blocks")
        .description("Selected blocks.")
        .filter(block -> block.getHardness() > 0)
        .visible(() -> mode.get() != Mode.Haste)
        .build()
    );

    private final Setting<ListMode> blocksFilter = sgGeneral.add(new EnumSetting.Builder<ListMode>()
        .name("blocks-filter")
        .description("How to use the blocks setting.")
        .defaultValue(ListMode.Blacklist)
        .visible(() -> mode.get() != Mode.Haste)
        .build()
    );

    public final Setting<Double> modifier = sgGeneral.add(new DoubleSetting.Builder()
        .name("modifier")
        .description("Mining speed modifier. An additional value of 0.2 is equivalent to one haste level (1.2 = haste 1).")
        .defaultValue(1.4)
        .visible(() -> mode.get() == Mode.Normal)
        .min(0)
        .build()
    );

    private final Setting<Double> finishThreshold = sgGeneral.add(new DoubleSetting.Builder()
        .name("finish-threshold")
        .description("Mining progress required before Damage finishes a block. Higher values are safer on strict servers.")
        .defaultValue(0.7)
        .range(0.7, 1.0)
        .sliderRange(0.7, 1.0)
        .visible(() -> mode.get() == Mode.Damage)
        .build()
    );

    private final Setting<Integer> hasteAmplifier = sgGeneral.add(new IntSetting.Builder()
        .name("haste-amplifier")
        .description("What value of haste to give you. Above 2 not recommended.")
        .defaultValue(2)
        .min(1)
        .visible(() -> mode.get() == Mode.Haste)
        .onChanged(i -> removeHaste())
        .build()
    );

    private final Setting<Boolean> instamine = sgGeneral.add(new BoolSetting.Builder()
        .name("instamine")
        .description("Whether or not to instantly mine blocks under certain conditions.")
        .defaultValue(true)
        .visible(() -> mode.get() == Mode.Damage)
        .build()
    );

    private final Setting<Boolean> grimBypass = sgGeneral.add(new BoolSetting.Builder()
        .name("grim-bypass")
        .description("Bypasses Grim's fastbreak check, working as of 2.3.58")
        .defaultValue(false)
        .visible(() -> mode.get() == Mode.Damage)
        .build()
    );

    public SpeedMine() {
        super(Categories.Player, "speed-mine", "Allows you to quickly mine blocks.");
    }

    @Override
    public void onDeactivate() {
        removeHaste();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!Utils.canUpdate()) return;

        if (mode.get() == Mode.Haste) {
            StatusEffectInstance haste = mc.player.getStatusEffect(HASTE);

            if (haste == null || haste.getAmplifier() <= hasteAmplifier.get() - 1) {
                mc.player.setStatusEffect(new StatusEffectInstance(HASTE, -1, hasteAmplifier.get() - 1, false, false, false), null);
            }
        }
        else if (mode.get() == Mode.Damage) {
            ClientPlayerInteractionManagerAccessor im = (ClientPlayerInteractionManagerAccessor) mc.interactionManager;
            float progress = im.client33$getBreakingProgress();
            BlockPos pos = im.client33$getCurrentBreakingBlockPos();

            if (pos == null || progress <= 0 || !mc.interactionManager.isBreakingBlock()) return;

            BlockState state = mc.world.getBlockState(pos);
            if (!filter(state.getBlock())) return;

            float delta = state.calcBlockBreakingDelta(mc.player, mc.world, pos);
            if (delta > 0 && progress + delta >= finishThreshold.get().floatValue())
                im.client33$setCurrentBreakingProgress(1f);
        }
    }

    @EventHandler
    private void onPacket(PacketEvent.Send event) {
        if (!(mode.get() == Mode.Damage) || !grimBypass.get()) return;

        // https://github.com/GrimAnticheat/Grim/issues/1296
        if (event.packet instanceof PlayerActionC2SPacket packet && packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK) {
            mc.getNetworkHandler().sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos().up(), packet.getDirection()));
        }
    }

    private void removeHaste() {
        if (!Utils.canUpdate()) return;

        StatusEffectInstance haste = mc.player.getStatusEffect(HASTE);
        if (haste != null && !haste.shouldShowIcon()) mc.player.removeStatusEffect(HASTE);
    }

    public boolean filter(Block block) {
        if (blocksFilter.get() == ListMode.Blacklist && !blocks.get().contains(block)) return true;
        return blocksFilter.get() == ListMode.Whitelist && blocks.get().contains(block);
    }

    public boolean instamine() {
        return isActive() && mode.get() == Mode.Damage && instamine.get();
    }

    public boolean canInstantMine(BlockPos pos, BlockState state) {
        return instamine() && filter(state.getBlock())
            && state.calcBlockBreakingDelta(mc.player, mc.world, pos) >= finishThreshold.get().floatValue();
    }

    public enum Mode {
        Normal,
        Haste,
        Damage
    }

    public enum ListMode {
        Whitelist,
        Blacklist
    }
}
