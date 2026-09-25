/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.Velocity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static com.gaspoweredcake.client33.Client33.mc;

@Mixin(FishingBobberEntity.class)
public abstract class FishingBobberEntityMixin {
    @WrapOperation(method = "handleStatus", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/FishingBobberEntity;pullHookedEntity(Lnet/minecraft/entity/Entity;)V"))
    private void preventFishingRodPull(FishingBobberEntity instance, Entity entity, Operation<Void> original) {
        if (!instance.getEntityWorld().isClient() || entity != mc.player) original.call(instance, entity);

        Velocity velocity = Modules.get().get(Velocity.class);
        if (!velocity.isActive() || !velocity.fishing.get()) original.call(instance, entity);
    }
}
