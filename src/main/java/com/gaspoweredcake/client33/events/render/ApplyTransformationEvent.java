/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.events.render;

import com.gaspoweredcake.client33.events.Cancellable;
import net.minecraft.client.render.model.json.Transformation;

public class ApplyTransformationEvent extends Cancellable {
    private static final ApplyTransformationEvent INSTANCE = new ApplyTransformationEvent();

    public Transformation transformation;
    public boolean leftHanded;

    public static ApplyTransformationEvent get(Transformation transformation, boolean leftHanded) {
        INSTANCE.setCancelled(false);

        INSTANCE.transformation = transformation;
        INSTANCE.leftHanded = leftHanded;

        return INSTANCE;
    }
}
