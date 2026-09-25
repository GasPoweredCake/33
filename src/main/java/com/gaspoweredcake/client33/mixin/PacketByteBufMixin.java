/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.misc.AntiPacketKick;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.network.PacketByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(PacketByteBuf.class)
public abstract class PacketByteBufMixin {
    @ModifyArg(method = "readNbt(Lio/netty/buffer/ByteBuf;)Lnet/minecraft/nbt/NbtCompound;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/PacketByteBuf;readNbt(Lio/netty/buffer/ByteBuf;Lnet/minecraft/nbt/NbtSizeTracker;)Lnet/minecraft/nbt/NbtElement;"))
    private static NbtSizeTracker xlPackets(NbtSizeTracker sizeTracker) {
        return Modules.get().isActive(AntiPacketKick.class) ? NbtSizeTracker.ofUnlimitedBytes() : sizeTracker;
    }
}
