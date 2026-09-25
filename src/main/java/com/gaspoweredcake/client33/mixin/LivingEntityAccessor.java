/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Invoker("swimUpward")
    void client33$swimUpwards(TagKey<Fluid> fluid);

    @Accessor("jumping")
    boolean client33$isJumping();

    @Accessor("jumpingCooldown")
    int client33$getJumpCooldown();

    @Accessor("jumpingCooldown")
    void client33$setJumpCooldown(int cooldown);
}
