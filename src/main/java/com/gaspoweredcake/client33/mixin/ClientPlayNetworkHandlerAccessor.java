/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.featuretoggle.FeatureSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientPlayNetworkHandler.class)
public interface ClientPlayNetworkHandlerAccessor {
    @Accessor("chunkLoadDistance")
    int client33$getChunkLoadDistance();

    @Accessor("messagePacker")
    MessageChain.Packer client33$getMessagePacker();

    @Accessor("lastSeenMessagesCollector")
    LastSeenMessagesCollector client33$getLastSeenMessagesCollector();

    @Accessor("combinedDynamicRegistries")
    DynamicRegistryManager.Immutable client33$getCombinedDynamicRegistries();

    @Accessor("enabledFeatures")
    FeatureSet client33$getEnabledFeatures();

    @Accessor("COMMAND_NODE_FACTORY")
    static CommandTreeS2CPacket.NodeFactory<ClientCommandSource> client33$getCommandNodeFactory() {
        return null;
    }
}
