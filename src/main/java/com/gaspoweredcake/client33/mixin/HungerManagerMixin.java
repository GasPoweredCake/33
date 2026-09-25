/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.NoSlow;
import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(HungerManager.class)
public class HungerManagerMixin {
    @ModifyExpressionValue(method = "canSprint()Z", at = @At(value = "CONSTANT", args = "floatValue=6.0f"))
    private float onHunger(float constant) {
        if (Modules.get().get(NoSlow.class).hunger()) return -1;
        return constant;
    }
}
