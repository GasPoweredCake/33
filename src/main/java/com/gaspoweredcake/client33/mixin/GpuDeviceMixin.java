/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.gaspoweredcake.client33.mixininterface.IGpuDevice;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(GpuDevice.class)
public abstract class GpuDeviceMixin implements IGpuDevice {
    @Shadow
    @Final
    private GpuDeviceBackend backend;

    @Override
    public void client33$pushScissor(int x, int y, int width, int height) {
        ((IGpuDevice) backend).client33$pushScissor(x, y, width, height);
    }

    @Override
    public void client33$popScissor() {
        ((IGpuDevice) backend).client33$popScissor();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void client33$onCreateRenderPass(RenderPassBackend backend) {
        ((IGpuDevice) this.backend).client33$onCreateRenderPass(backend);
    }
}
