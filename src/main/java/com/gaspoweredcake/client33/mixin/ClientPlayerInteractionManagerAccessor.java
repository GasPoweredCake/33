/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {
    @Accessor("currentBreakingProgress")
    float client33$getBreakingProgress();

    @Accessor("currentBreakingProgress")
    void client33$setCurrentBreakingProgress(float progress);

    @Accessor("currentBreakingPos")
    BlockPos client33$getCurrentBreakingBlockPos();
}
