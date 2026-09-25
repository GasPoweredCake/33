/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.combat;

import com.gaspoweredcake.client33.events.game.OpenScreenEvent;
import com.gaspoweredcake.client33.events.world.TickEvent;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.utils.player.InvUtils;
import com.gaspoweredcake.client33.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.AnvilBlock;
import net.minecraft.block.Block;
import net.minecraft.client.gui.screen.ingame.AnvilScreen;

public class SelfAnvil extends Module {
    public SelfAnvil() {
        super(Categories.Combat, "self-anvil", "Automatically places an anvil on you to prevent other players from going into your hole.");
    }

    @EventHandler
    private void onOpenScreen(OpenScreenEvent event) {
        if (event.screen instanceof AnvilScreen) event.cancel();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (BlockUtils.place(mc.player.getBlockPos().add(0, 2, 0), InvUtils.findInHotbar(itemStack -> Block.getBlockFromItem(itemStack.getItem()) instanceof AnvilBlock), 0)) {
            toggle();
        }
    }
}
