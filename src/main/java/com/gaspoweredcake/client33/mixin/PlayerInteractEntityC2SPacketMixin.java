/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.mixininterface.IPlayerInteractEntityC2SPacket;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.NoSlow;
import com.gaspoweredcake.client33.systems.modules.movement.Sneak;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import static com.gaspoweredcake.client33.Client33.mc;

@Mixin(PlayerInteractEntityC2SPacket.class)
public abstract class PlayerInteractEntityC2SPacketMixin implements IPlayerInteractEntityC2SPacket {
    @Shadow @Final private PlayerInteractEntityC2SPacket.InteractTypeHandler type;
    @Shadow @Final private int entityId;

    @Override
    public PlayerInteractEntityC2SPacket.InteractType client33$getType() {
        return type.getType();
    }

    @Override
    public Entity client33$getEntity() {
        return mc.world.getEntityById(entityId);
    }

    @ModifyVariable(method = "<init>(IZLnet/minecraft/network/packet/c2s/play/PlayerInteractEntityC2SPacket$InteractTypeHandler;)V", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private static boolean setSneaking(boolean sneaking) {
        return Modules.get().get(Sneak.class).doPacket() || Modules.get().get(NoSlow.class).airStrict() || sneaking;
    }
}
