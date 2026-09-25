/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.events.world;

import com.gaspoweredcake.client33.events.Cancellable;
import net.minecraft.particle.ParticleEffect;

public class ParticleEvent extends Cancellable {
    private static final ParticleEvent INSTANCE = new ParticleEvent();

    public ParticleEffect particle;

    public static ParticleEvent get(ParticleEffect particle) {
        INSTANCE.setCancelled(false);
        INSTANCE.particle = particle;
        return INSTANCE;
    }
}
