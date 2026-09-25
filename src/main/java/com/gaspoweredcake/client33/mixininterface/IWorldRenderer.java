/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixininterface;

import net.minecraft.client.gl.Framebuffer;

public interface IWorldRenderer {
    void client33$pushEntityOutlineFramebuffer(Framebuffer framebuffer);

    void client33$popEntityOutlineFramebuffer();
}
