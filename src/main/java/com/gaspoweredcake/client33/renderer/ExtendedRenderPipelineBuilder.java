/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.renderer;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.gaspoweredcake.client33.mixininterface.IRenderPipeline;
import org.jspecify.annotations.NonNull;

public class ExtendedRenderPipelineBuilder extends RenderPipeline.Builder {
    private boolean lineSmooth;

    public ExtendedRenderPipelineBuilder(RenderPipeline.Snippet... snippets) {
        for (RenderPipeline.Snippet snippet : snippets) {
            withSnippet(snippet);
        }
    }

    public ExtendedRenderPipelineBuilder withLineSmooth() {
        lineSmooth = true;
        return this;
    }

    @Override
    public @NonNull RenderPipeline build() {
        RenderPipeline pipeline = super.build();
        ((IRenderPipeline) pipeline).client33$setLineSmooth(lineSmooth);

        return pipeline;
    }
}
