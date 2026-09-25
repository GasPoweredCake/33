/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.mixininterface.ISlot;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Slot.class)
public abstract class SlotMixin implements ISlot {
    @Shadow public int id;
    @Shadow @Final private int index;

    @Override
    public int client33$getId() {
        return id;
    }

    @Override
    public int client33$getIndex() {
        return index;
    }
}
