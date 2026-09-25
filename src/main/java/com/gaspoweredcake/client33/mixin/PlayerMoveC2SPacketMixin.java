/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.mixininterface.IPlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerMoveC2SPacket.class)
public abstract class PlayerMoveC2SPacketMixin implements IPlayerMoveC2SPacket {
    @Unique private int tag;

    @Override
    public void client33$setTag(int tag) { this.tag = tag; }

    @Override
    public int client33$getTag() { return this.tag; }
}
