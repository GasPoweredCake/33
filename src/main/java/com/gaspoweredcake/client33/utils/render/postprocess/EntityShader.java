package com.gaspoweredcake.client33.utils.render.postprocess;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.gaspoweredcake.client33.mixininterface.IWorldRenderer;
import com.gaspoweredcake.client33.utils.render.CustomOutlineVertexConsumerProvider;
import net.minecraft.entity.Entity;

import static com.gaspoweredcake.client33.Client33.mc;

public abstract class EntityShader extends PostProcessShader {
    public final CustomOutlineVertexConsumerProvider vertexConsumerProvider;

    protected EntityShader(RenderPipeline pipeline) {
        super(pipeline);
        this.vertexConsumerProvider = new CustomOutlineVertexConsumerProvider();
    }

    public abstract boolean shouldDraw(Entity entity);

    @Override
    protected void preDraw() {
        ((IWorldRenderer) mc.worldRenderer).client33$pushEntityOutlineFramebuffer(framebuffer);
    }

    @Override
    protected void postDraw() {
        ((IWorldRenderer) mc.worldRenderer).client33$popEntityOutlineFramebuffer();
    }

    public void submitVertices() {
        submitVertices(vertexConsumerProvider::draw);
    }
}
