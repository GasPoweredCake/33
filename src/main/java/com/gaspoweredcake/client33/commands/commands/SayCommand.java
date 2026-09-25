/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.commands.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.gaspoweredcake.client33.commands.Command;
import com.gaspoweredcake.client33.mixin.ClientPlayNetworkHandlerAccessor;
import com.gaspoweredcake.client33.utils.misc.Client33Starscript;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessagesCollector;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.meteordev.starscript.Script;

import java.time.Instant;

public class SayCommand extends Command {
    public SayCommand() {
        super("say", "Sends messages in chat.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("message", StringArgumentType.greedyString()).executes(context -> {
            String msg = context.getArgument("message", String.class);
            Script script = Client33Starscript.compile(msg);

            if (script != null) {
                String message = Client33Starscript.run(script);

                if (message != null) {
                    Instant instant = Instant.now();
                    long l = NetworkEncryptionUtils.SecureRandomUtil.nextLong();
                    ClientPlayNetworkHandler handler = mc.getNetworkHandler();
                    LastSeenMessagesCollector.LastSeenMessages lastSeenMessages = ((ClientPlayNetworkHandlerAccessor) handler).client33$getLastSeenMessagesCollector().collect();
                    MessageSignatureData messageSignatureData = ((ClientPlayNetworkHandlerAccessor) handler).client33$getMessagePacker().pack(new MessageBody(message, instant, l, lastSeenMessages.lastSeen()));
                    handler.sendPacket(new ChatMessageC2SPacket(message, instant, l, messageSignatureData, lastSeenMessages.update()));
                }
            }

            return SINGLE_SUCCESS;
        }));
    }
}
