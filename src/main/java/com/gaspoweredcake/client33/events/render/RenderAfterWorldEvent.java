/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.events.render;

public class RenderAfterWorldEvent {
    private static final RenderAfterWorldEvent INSTANCE = new RenderAfterWorldEvent();

    public static RenderAfterWorldEvent get() {
        return INSTANCE;
    }
}
