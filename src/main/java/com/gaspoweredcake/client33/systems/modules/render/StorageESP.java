/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.render;

import com.gaspoweredcake.client33.events.entity.player.InteractBlockEvent;
import com.gaspoweredcake.client33.events.render.Render3DEvent;
import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.widgets.WWidget;
import com.gaspoweredcake.client33.gui.widgets.containers.WVerticalList;
import com.gaspoweredcake.client33.gui.widgets.pressable.WButton;
import com.gaspoweredcake.client33.renderer.MeshBuilder;
import com.gaspoweredcake.client33.renderer.MeshRenderer;
import com.gaspoweredcake.client33.renderer.Client33RenderPipelines;
import com.gaspoweredcake.client33.renderer.ShapeMode;
import com.gaspoweredcake.client33.settings.*;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.utils.Utils;
import com.gaspoweredcake.client33.utils.player.PlayerUtils;
import com.gaspoweredcake.client33.utils.render.MeshBuilderVertexConsumerProvider;
import com.gaspoweredcake.client33.utils.render.RenderUtils;
import com.gaspoweredcake.client33.utils.render.SimpleBlockRenderer;
import com.gaspoweredcake.client33.utils.render.color.Color;
import com.gaspoweredcake.client33.utils.render.color.SettingColor;
import com.gaspoweredcake.client33.utils.render.postprocess.PostProcessShaders;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.ChestType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageESP extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgOpened = settings.createGroup("Opened Rendering");
    private final Set<BlockPos> interactedBlocks = new HashSet<>();

    private final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>()
        .name("mode")
        .description("Rendering mode.")
        .defaultValue(Mode.Box)
        .build()
    );

    private final Setting<BoxStyle> boxStyle = sgGeneral.add(new EnumSetting.Builder<BoxStyle>()
        .name("box-style")
        .description("Draws full edges or small corner brackets around storage blocks.")
        .defaultValue(BoxStyle.Corners)
        .visible(() -> mode.get() == Mode.Box)
        .build()
    );

    private final Setting<List<BlockEntityType<?>>> storageBlocks = sgGeneral.add(new StorageBlockListSetting.Builder()
        .name("storage-blocks")
        .description("Select the storage blocks to display.")
        .defaultValue(StorageBlockListSetting.STORAGE_BLOCKS)
        .build()
    );

    private final Setting<Boolean> tracers = sgGeneral.add(new BoolSetting.Builder()
        .name("tracers")
        .description("Draws tracers to storage blocks.")
        .defaultValue(false)
        .build()
    );

    public final Setting<ShapeMode> shapeMode = sgGeneral.add(new EnumSetting.Builder<ShapeMode>()
        .name("shape-mode")
        .description("How the shapes are rendered.")
        .defaultValue(ShapeMode.Both)
        .build()
    );

    public final Setting<Integer> fillOpacity = sgGeneral.add(new IntSetting.Builder()
        .name("fill-opacity")
        .description("The opacity of the shape fill.")
        .visible(() -> shapeMode.get() != ShapeMode.Lines)
        .defaultValue(28)
        .range(0, 255)
        .sliderMax(255)
        .build()
    );

    public final Setting<Integer> outlineWidth = sgGeneral.add(new IntSetting.Builder()
        .name("width")
        .description("The width of the shader outline.")
        .visible(() -> mode.get() == Mode.Shader)
        .defaultValue(1)
        .range(1, 10)
        .sliderRange(1, 5)
        .build()
    );

    public final Setting<Double> glowMultiplier = sgGeneral.add(new DoubleSetting.Builder()
        .name("glow-multiplier")
        .description("Multiplier for glow effect")
        .visible(() -> mode.get() == Mode.Shader)
        .decimalPlaces(3)
        .defaultValue(0.7)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final Setting<SettingColor> chest = sgGeneral.add(new ColorSetting.Builder()
        .name("chest")
        .description("The color of chests.")
        .defaultValue(new SettingColor(226, 188, 139, 220))
        .build()
    );

    private final Setting<SettingColor> trappedChest = sgGeneral.add(new ColorSetting.Builder()
        .name("trapped-chest")
        .description("The color of trapped chests.")
        .defaultValue(new SettingColor(231, 141, 148, 220))
        .build()
    );

    private final Setting<SettingColor> barrel = sgGeneral.add(new ColorSetting.Builder()
        .name("barrel")
        .description("The color of barrels.")
        .defaultValue(new SettingColor(140, 204, 176, 220))
        .build()
    );

    private final Setting<SettingColor> shulker = sgGeneral.add(new ColorSetting.Builder()
        .name("shulker")
        .description("The color of Shulker Boxes.")
        .defaultValue(new SettingColor(160, 183, 225, 220))
        .build()
    );

    private final Setting<SettingColor> enderChest = sgGeneral.add(new ColorSetting.Builder()
        .name("ender-chest")
        .description("The color of Ender Chests.")
        .defaultValue(new SettingColor(185, 158, 224, 220))
        .build()
    );

    private final Setting<SettingColor> other = sgGeneral.add(new ColorSetting.Builder()
        .name("other")
        .description("The color of furnaces, dispensers, droppers and hoppers.")
        .defaultValue(new SettingColor(173, 193, 198, 210))
        .build()
    );

    private final Setting<Double> fadeDistance = sgGeneral.add(new DoubleSetting.Builder()
        .name("fade-distance")
        .description("Distance from the camera over which nearby storage fades in.")
        .defaultValue(6)
        .min(0)
        .sliderMax(12)
        .build()
    );

    private final Setting<Integer> maxDistance = sgGeneral.add(new IntSetting.Builder()
        .name("max-distance")
        .description("Stops showing storage beyond this camera distance. Set to 0 for unlimited range.")
        .defaultValue(64)
        .min(0)
        .sliderMax(128)
        .build()
    );

    private final Setting<Integer> farFadeDistance = sgGeneral.add(new IntSetting.Builder()
        .name("far-fade-distance")
        .description("Distance over which storage gently fades out near the maximum range.")
        .defaultValue(12)
        .min(0)
        .sliderMax(32)
        .visible(() -> maxDistance.get() > 0)
        .build()
    );

    private final Setting<Boolean> hideOpened = sgOpened.add(new BoolSetting.Builder()
        .name("hide-opened")
        .description("Hides opened containers.")
        .defaultValue(false)
        .build()
    );

    private final Setting<SettingColor> openedColor = sgOpened.add(new ColorSetting.Builder()
        .name("opened-color")
        .description("Optional setting to change colors of opened chests, as opposed to not rendering. Disabled at zero opacity.")
        .defaultValue(new SettingColor(154, 171, 181, 0)) // Transparent by default.
        .build()
    );


    private final Color lineColor = new Color(0, 0, 0, 0);
    private final Color sideColor = new Color(0, 0, 0, 0);
    private boolean render;
    private int count;

    private final MeshBuilder mesh;
    private final MeshBuilderVertexConsumerProvider vertexConsumerProvider;

    public StorageESP() {
        super(Categories.Render, "storage-esp", "Renders all specified storage blocks.");

        mesh = new MeshBuilder(Client33RenderPipelines.WORLD_COLORED);
        vertexConsumerProvider = new MeshBuilderVertexConsumerProvider(mesh);
    }

    private void getBlockEntityColor(BlockEntity blockEntity) {
        render = false;

        if (!storageBlocks.get().contains(blockEntity.getType())) return;

        if (blockEntity instanceof TrappedChestBlockEntity) lineColor.set(trappedChest.get()); // Must come before ChestBlockEntity as it is the superclass of TrappedChestBlockEntity
        else if (blockEntity instanceof ChestBlockEntity) lineColor.set(chest.get());
        else if (blockEntity instanceof BarrelBlockEntity) lineColor.set(barrel.get());
        else if (blockEntity instanceof ShulkerBoxBlockEntity) lineColor.set(shulker.get());
        else if (blockEntity instanceof EnderChestBlockEntity) lineColor.set(enderChest.get());
        else if (blockEntity instanceof AbstractFurnaceBlockEntity || blockEntity instanceof BrewingStandBlockEntity || blockEntity instanceof ChiseledBookshelfBlockEntity || blockEntity instanceof CrafterBlockEntity || blockEntity instanceof DispenserBlockEntity || blockEntity instanceof DecoratedPotBlockEntity || blockEntity instanceof HopperBlockEntity) lineColor.set(other.get());
        else return;

        render = true;

        if (shapeMode.get() == ShapeMode.Sides || shapeMode.get() == ShapeMode.Both) {
            sideColor.set(lineColor);
            sideColor.a = lineColor.a * fillOpacity.get() / 255;
        }
    }

    @Override
    public WWidget getWidget(GuiTheme theme) {
        WVerticalList list = theme.verticalList();

        // Button to Clear Interacted Blocks
        WButton clear = list.add(theme.button("Clear Rendering Cache")).expandX().widget();

        clear.action = interactedBlocks::clear;

        return list;
    }

    @EventHandler
    private void onBlockInteract(InteractBlockEvent event) {
        BlockPos pos = event.result.getBlockPos();
        BlockEntity blockEntity = mc.world.getBlockEntity(pos);

        if (blockEntity == null) return;

        interactedBlocks.add(pos);
        if (blockEntity instanceof ChestBlockEntity chestBlockEntity) {
            BlockState state = chestBlockEntity.getCachedState();
            ChestType chestType = state.get(ChestBlock.CHEST_TYPE);

            if (chestType == ChestType.LEFT || chestType == ChestType.RIGHT) {
                // It's part of a double chest
                Direction facing = state.get(ChestBlock.FACING);
                BlockPos otherPartPos = pos.offset(chestType == ChestType.LEFT ? facing.rotateYClockwise() : facing.rotateYCounterclockwise());

                interactedBlocks.add(otherPartPos);
            }
        }
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        count = 0;
        Mode renderMode = mode.get();

        for (BlockEntity blockEntity : Utils.blockEntities()) {
            if (renderMode == Mode.Box && isSecondHalfOfDoubleChest(blockEntity)) continue;

            boolean interacted = interactedBlocks.contains(blockEntity.getPos());
            if (interacted && hideOpened.get()) continue;

            getBlockEntityColor(blockEntity);
            if (!render) continue;

            if (interacted && openedColor.get().a > 0) {
                lineColor.set(openedColor.get());
                sideColor.set(openedColor.get());
                sideColor.a = lineColor.a * fillOpacity.get() / 255;
            }

            double opacity = opacityAtDistance(blockEntity.getPos());
            if (opacity <= 0.05) continue;

            if (count == 0 && renderMode == Mode.Shader) mesh.begin();

            int prevLineA = lineColor.a;
            int prevSideA = sideColor.a;

            lineColor.a = (int) Math.round(prevLineA * opacity);
            sideColor.a = (int) Math.round(prevSideA * opacity);

            if (tracers.get()) {
                event.renderer.line(RenderUtils.center.x, RenderUtils.center.y, RenderUtils.center.z, blockEntity.getPos().getX() + 0.5, blockEntity.getPos().getY() + 0.5, blockEntity.getPos().getZ() + 0.5, lineColor);
            }

            if (renderMode == Mode.Box) renderBox(event, blockEntity);
            else renderShader(event, blockEntity);

            lineColor.a = prevLineA;
            sideColor.a = prevSideA;
            count++;
        }

        if (renderMode == Mode.Shader && count > 0) {
            MeshRenderer.begin()
                .attachments(PostProcessShaders.STORAGE_OUTLINE.framebuffer)
                .clearColor(Color.CLEAR)
                .pipeline(Client33RenderPipelines.WORLD_COLORED)
                .mesh(mesh, event.matrices)
                .end();

            PostProcessShaders.STORAGE_OUTLINE.render();
        }
    }

    private double opacityAtDistance(BlockPos pos) {
        double distanceSquared = PlayerUtils.squaredDistanceToCamera(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        int limit = maxDistance.get();
        if (limit > 0 && distanceSquared >= (double) limit * limit) return 0;

        double distance = Math.sqrt(distanceSquared);
        double opacity = 1;

        double nearFade = fadeDistance.get();
        if (nearFade > 0) {
            double t = MathHelper.clamp(distance / nearFade, 0, 1);
            opacity *= t * t * (3 - 2 * t);
        }

        int farFade = farFadeDistance.get();
        if (limit > 0 && farFade > 0) {
            double t = MathHelper.clamp((limit - distance) / Math.min(farFade, limit), 0, 1);
            opacity *= t * t * (3 - 2 * t);
        }

        return opacity;
    }

    private boolean isSecondHalfOfDoubleChest(BlockEntity blockEntity) {
        if (!(blockEntity instanceof ChestBlockEntity)) return false;

        BlockState state = blockEntity.getCachedState();
        if (!(state.getBlock() instanceof ChestBlock) || state.get(ChestBlock.CHEST_TYPE) != ChestType.RIGHT) return false;

        Direction facing = state.get(ChestBlock.FACING);
        BlockEntity otherHalf = mc.world.getBlockEntity(blockEntity.getPos().offset(facing.rotateYCounterclockwise()));
        return otherHalf instanceof ChestBlockEntity && otherHalf.getType() == blockEntity.getType();
    }


    private void renderBox(Render3DEvent event, BlockEntity blockEntity) {
        double x1 = blockEntity.getPos().getX();
        double y1 = blockEntity.getPos().getY();
        double z1 = blockEntity.getPos().getZ();

        double x2 = blockEntity.getPos().getX() + 1;
        double y2 = blockEntity.getPos().getY() + 1;
        double z2 = blockEntity.getPos().getZ() + 1;

        if (blockEntity instanceof ChestBlockEntity) {
            BlockState state = mc.world.getBlockState(blockEntity.getPos());
            if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) == ChestType.LEFT) {
                Direction facing = state.get(ChestBlock.FACING);
                BlockPos otherPos = blockEntity.getPos().offset(facing.rotateYClockwise());
                BlockEntity otherHalf = mc.world.getBlockEntity(otherPos);
                if (otherHalf instanceof ChestBlockEntity && otherHalf.getType() == blockEntity.getType()) {
                    x1 = Math.min(x1, otherPos.getX());
                    z1 = Math.min(z1, otherPos.getZ());
                    x2 = Math.max(x2, otherPos.getX() + 1);
                    z2 = Math.max(z2, otherPos.getZ() + 1);
                }
            }
        }

        if (blockEntity instanceof ChestBlockEntity || blockEntity instanceof EnderChestBlockEntity) {
            double a = 1.0 / 16.0;
            x1 += a;
            z1 += a;
            x2 -= a;
            y2 -= a * 2;
            z2 -= a;
        }

        if (boxStyle.get() == BoxStyle.Full) {
            event.renderer.box(x1, y1, z1, x2, y2, z2, sideColor, lineColor, shapeMode.get(), 0);
        } else {
            if (shapeMode.get().sides()) event.renderer.boxSides(x1, y1, z1, x2, y2, z2, sideColor, 0);
            if (shapeMode.get().lines()) renderCorners(event, x1, y1, z1, x2, y2, z2);
        }
    }

    private void renderCorners(Render3DEvent event, double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = Math.min(0.25, (x2 - x1) * 0.28);
        double dy = Math.min(0.25, (y2 - y1) * 0.28);
        double dz = Math.min(0.25, (z2 - z1) * 0.28);

        for (int ix = 0; ix < 2; ix++) {
            double x = ix == 0 ? x1 : x2;
            double nextX = x + (ix == 0 ? dx : -dx);
            for (int iy = 0; iy < 2; iy++) {
                double y = iy == 0 ? y1 : y2;
                double nextY = y + (iy == 0 ? dy : -dy);
                for (int iz = 0; iz < 2; iz++) {
                    double z = iz == 0 ? z1 : z2;
                    double nextZ = z + (iz == 0 ? dz : -dz);
                    event.renderer.line(x, y, z, nextX, y, z, lineColor);
                    event.renderer.line(x, y, z, x, nextY, z, lineColor);
                    event.renderer.line(x, y, z, x, y, nextZ, lineColor);
                }
            }
        }
    }

    private void renderShader(Render3DEvent event, BlockEntity blockEntity) {
        vertexConsumerProvider.setColor(lineColor);
        SimpleBlockRenderer.renderWithBlockEntity(blockEntity, event.tickDelta, vertexConsumerProvider);
    }

    @Override
    public String getInfoString() {
        return Integer.toString(count);
    }

    public boolean isShader() {
        return isActive() && mode.get() == Mode.Shader;
    }

    public enum Mode {
        Box,
        Shader
    }

    public enum BoxStyle {
        Corners,
        Full
    }
}
