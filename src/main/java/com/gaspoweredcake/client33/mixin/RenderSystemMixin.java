/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.gaspoweredcake.client33.renderer.MeshUniforms;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.misc.InventoryTweaks;
import com.gaspoweredcake.client33.utils.render.postprocess.ChamsShader;
import com.gaspoweredcake.client33.utils.render.postprocess.OutlineUniforms;
import com.gaspoweredcake.client33.utils.render.postprocess.PostProcessShader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.gaspoweredcake.client33.Client33.mc;

@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {
    @Inject(method = "flipFrame", at = @At("TAIL"))
    private static void client33$flipFrame(CallbackInfo info) {
        MeshUniforms.flipFrame();
        PostProcessShader.flipFrame();
        ChamsShader.flipFrame();
        OutlineUniforms.flipFrame();

        if (Modules.get() == null || mc.player == null) return;
        if (Modules.get().get(InventoryTweaks.class).frameInput()) ((MinecraftClientAccessor) mc).client33$handleInputEvents();
    }
}
