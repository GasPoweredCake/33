/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {
    @Accessor("KEYS_BY_ID")
    static Map<String, KeyBinding> getKeysById() { return null; }

    @Accessor("boundKey")
    InputUtil.Key client33$getKey();

    @Accessor("timesPressed")
    int client33$getTimesPressed();

    @Accessor("timesPressed")
    void client33$setTimesPressed(int timesPressed);

    @Invoker("reset")
    void client33$invokeReset();
}
