/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.utils.misc;

import net.minecraft.nbt.CompoundTag;

public interface ISerializable<T> {
    CompoundTag toTag();

    T fromTag(CompoundTag tag);
}
