/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.renderer;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public abstract class Client33VertexFormats {
    public static final VertexFormat POS2 = VertexFormat.builder()
        .add("Position", Client33VertexFormatElements.POS2)
        .build();

    public static final VertexFormat POS2_COLOR = VertexFormat.builder()
        .add("Position", Client33VertexFormatElements.POS2)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    public static final VertexFormat POS2_TEXTURE_COLOR = VertexFormat.builder()
        .add("Position", Client33VertexFormatElements.POS2)
        .add("Texture", VertexFormatElement.UV)
        .add("Color", VertexFormatElement.COLOR)
        .build();

    private Client33VertexFormats() {}
}
