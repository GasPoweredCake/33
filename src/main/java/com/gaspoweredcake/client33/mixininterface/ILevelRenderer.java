/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixininterface;

import com.mojang.blaze3d.pipeline.RenderTarget;

public interface ILevelRenderer {
    void client33$pushEntityOutlineFramebuffer(RenderTarget framebuffer);

    void client33$popEntityOutlineFramebuffer();
}
