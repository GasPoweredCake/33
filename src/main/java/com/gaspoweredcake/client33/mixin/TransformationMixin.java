/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.Client33;
import com.gaspoweredcake.client33.events.render.ApplyTransformationEvent;
import net.minecraft.client.render.model.json.Transformation;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Transformation.class)
public abstract class TransformationMixin {
    @Inject(method = "apply", at = @At("HEAD"), cancellable = true)
    private void onApply(boolean leftHanded, MatrixStack.Entry entry, CallbackInfo info) {
        ApplyTransformationEvent event = Client33.EVENT_BUS.post(ApplyTransformationEvent.get((Transformation) (Object) this, leftHanded));
        if (event.isCancelled()) info.cancel();
    }
}
