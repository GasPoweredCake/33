/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.NoSlow;
import net.minecraft.block.BlockState;
import net.minecraft.block.SlimeBlock;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gaspoweredcake.client33.Client33.mc;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin {
    @Inject(method = "onSteppedOn", at = @At("HEAD"), cancellable = true)
    private void onSteppedOn(World world, BlockPos pos, BlockState state, Entity entity, CallbackInfo info) {
        if (Modules.get().get(NoSlow.class).slimeBlock() && entity == mc.player) info.cancel();
    }
}
