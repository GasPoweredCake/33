/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.render.Fullbright;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.world.LightType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(WorldRenderer.BrightnessGetter.class)
public interface BrightnessGetterMixin {

    @ModifyVariable(method = "method_68890", at = @At(value = "STORE"), ordinal = 0)
    private static int getLightmapCoordinatesModifySkyLight(int sky) {
        return Math.max(Modules.get().get(Fullbright.class).getLuminance(LightType.SKY), sky);
    }

    @ModifyVariable(method = "method_68890", at = @At(value = "STORE"), ordinal = 1)
    private static int getLightmapCoordinatesModifyBlockLight(int sky) {
        return Math.max(Modules.get().get(Fullbright.class).getLuminance(LightType.BLOCK), sky);
    }
}
