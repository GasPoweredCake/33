/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.gaspoweredcake.client33.Client33;
import com.gaspoweredcake.client33.commands.Commands;
import com.gaspoweredcake.client33.events.entity.EntityDestroyEvent;
import com.gaspoweredcake.client33.events.entity.player.PickItemsEvent;
import com.gaspoweredcake.client33.events.game.GameJoinedEvent;
import com.gaspoweredcake.client33.events.game.GameLeftEvent;
import com.gaspoweredcake.client33.events.game.SendMessageEvent;
import com.gaspoweredcake.client33.events.packets.ContainerSlotUpdateEvent;
import com.gaspoweredcake.client33.events.packets.InventoryEvent;
import com.gaspoweredcake.client33.events.packets.PlaySoundPacketEvent;
import com.gaspoweredcake.client33.events.world.ChunkDataEvent;
import com.gaspoweredcake.client33.mixininterface.IExplosionS2CPacket;
import com.gaspoweredcake.client33.pathing.BaritoneUtils;
import com.gaspoweredcake.client33.systems.config.Config;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.systems.modules.movement.Velocity;
import com.gaspoweredcake.client33.systems.modules.player.NoRotate;
import com.gaspoweredcake.client33.systems.modules.render.NoRender;
import com.gaspoweredcake.client33.utils.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.client.network.ClientConnectionState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin extends ClientCommonNetworkHandler {
    @Shadow
    private ClientWorld world;

    protected ClientPlayNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Inject(method = "onEntitySpawn", at = @At("HEAD"), cancellable = true)
    private void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo info) {
        if (packet != null && packet.getEntityType() != null) {
            if (Modules.get().get(NoRender.class).noEntity(packet.getEntityType()) && Modules.get().get(NoRender.class).getDropSpawnPacket()) {
                info.cancel();
            }
        }
    }

    @Inject(method = "onGameJoin", at = @At("HEAD"))
    private void onGameJoinHead(GameJoinS2CPacket packet, CallbackInfo info, @Share("worldNotNull") LocalBooleanRef worldNotNull) {
        worldNotNull.set(world != null);
    }

    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoinTail(GameJoinS2CPacket packet, CallbackInfo info, @Share("worldNotNull") LocalBooleanRef worldNotNull) {
        if (worldNotNull.get()) {
            Client33.EVENT_BUS.post(GameLeftEvent.get());
        }

        Client33.EVENT_BUS.post(GameJoinedEvent.get());
    }

    // the server sends a GameJoin packet after the reconfiguration phase
    @Inject(method = "onEnterReconfiguration", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V", shift = At.Shift.AFTER))
    private void onEnterReconfiguration(EnterReconfigurationS2CPacket packet, CallbackInfo info) {
        Client33.EVENT_BUS.post(GameLeftEvent.get());
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    private void onPlaySound(PlaySoundS2CPacket packet, CallbackInfo info) {
        Client33.EVENT_BUS.post(PlaySoundPacketEvent.get(packet));
    }

    @Inject(method = "onChunkData", at = @At("TAIL"))
    private void onChunkData(ChunkDataS2CPacket packet, CallbackInfo info) {
        WorldChunk chunk = client.world.getChunk(packet.getChunkX(), packet.getChunkZ());
        Client33.EVENT_BUS.post(new ChunkDataEvent(chunk));
    }

    @Inject(method = "onScreenHandlerSlotUpdate", at = @At("TAIL"))
    private void onContainerSlotUpdate(ScreenHandlerSlotUpdateS2CPacket packet, CallbackInfo info) {
        Client33.EVENT_BUS.post(ContainerSlotUpdateEvent.get(packet));
    }

    @Inject(method = "onInventory", at = @At("TAIL"))
    private void onInventory(InventoryS2CPacket packet, CallbackInfo info) {
        Client33.EVENT_BUS.post(InventoryEvent.get(packet));
    }

    @Inject(method = "onEntitiesDestroy", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/EntitiesDestroyS2CPacket;getEntityIds()Lit/unimi/dsi/fastutil/ints/IntList;"))
    private void onEntitiesDestroy(EntitiesDestroyS2CPacket packet, CallbackInfo ci) {
        for (int id : packet.getEntityIds()) {
            Client33.EVENT_BUS.post(EntityDestroyEvent.get(client.world.getEntityById(id)));
        }
    }

    @Inject(method = "onExplosion", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/network/PacketApplyBatcher;)V", shift = At.Shift.AFTER))
    private void onExplosionVelocity(ExplosionS2CPacket packet, CallbackInfo ci) {
        Velocity velocity = Modules.get().get(Velocity.class);
        if (!velocity.explosions.get()) return;

        IExplosionS2CPacket explosionPacket = (IExplosionS2CPacket) (Object) packet;
        explosionPacket.client33$setVelocityX((float) (packet.playerKnockback().orElse(Vec3d.ZERO).x * velocity.getHorizontal(velocity.explosionsHorizontal)));
        explosionPacket.client33$setVelocityY((float) (packet.playerKnockback().orElse(Vec3d.ZERO).y * velocity.getVertical(velocity.explosionsVertical)));
        explosionPacket.client33$setVelocityZ((float) (packet.playerKnockback().orElse(Vec3d.ZERO).z * velocity.getHorizontal(velocity.explosionsHorizontal)));
    }

    @Inject(method = "onItemPickupAnimation", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getEntityById(I)Lnet/minecraft/entity/Entity;", ordinal = 0))
    private void onItemPickupAnimation(ItemPickupAnimationS2CPacket packet, CallbackInfo info) {
        Entity itemEntity = client.world.getEntityById(packet.getEntityId());
        Entity entity = client.world.getEntityById(packet.getCollectorEntityId());

        if (itemEntity instanceof ItemEntity && entity == client.player) {
            Client33.EVENT_BUS.post(PickItemsEvent.get(((ItemEntity) itemEntity).getStack(), packet.getStackAmount()));
        }
    }

    @Inject(method = "onPlayerPositionLook", at = @At("HEAD"))
    private void onPlayerPositionLookHead(PlayerPositionLookS2CPacket packet, CallbackInfo ci,
          @Share("noRotateYaw") LocalFloatRef yawRef,
          @Share("noRotatePitch") LocalFloatRef pitchRef) {
        NoRotate noRotate = Modules.get().get(NoRotate.class);
        if (!noRotate.isActive() || client.player == null) return;

        yawRef.set(client.player.getYaw());
        pitchRef.set(client.player.getPitch());
    }

    @Inject(method = "onPlayerPositionLook", at = @At("RETURN"))
    private void onPlayerPositionLookReturn(PlayerPositionLookS2CPacket packet, CallbackInfo ci,
        @Share("noRotateYaw") LocalFloatRef yawRef,
        @Share("noRotatePitch") LocalFloatRef pitchRef) {
        NoRotate noRotate = Modules.get().get(NoRotate.class);
        if (!noRotate.isActive() || client.player == null) return;

        float savedYaw = yawRef.get();
        float savedPitch = pitchRef.get();

        //not noticeable by player but forces a server update
        client.player.setYaw(savedYaw + 0.000001f);
        client.player.setPitch(savedPitch + 0.000001f);
        client.player.headYaw = savedYaw;
        client.player.bodyYaw = savedYaw;
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void onSendChatMessage(String message, CallbackInfo ci, @Local(argsOnly = true) LocalRef<String> messageRef) {
        if (!message.startsWith(Config.get().prefix.get()) && !(BaritoneUtils.IS_AVAILABLE && message.startsWith(BaritoneUtils.getPrefix()))) {
            SendMessageEvent event = Client33.EVENT_BUS.post(SendMessageEvent.get(message));

            if (!event.isCancelled()) {
                messageRef.set(event.message);
            } else {
                ci.cancel();
            }

            return;
        }

        if (message.startsWith(Config.get().prefix.get())) {
            try {
                Commands.dispatch(message.substring(Config.get().prefix.get().length()));
            } catch (CommandSyntaxException e) {
                ChatUtils.error(e.getMessage());
            }

            client.inGameHud.getChatHud().addToMessageHistory(message);
            ci.cancel();
        }
    }
}
