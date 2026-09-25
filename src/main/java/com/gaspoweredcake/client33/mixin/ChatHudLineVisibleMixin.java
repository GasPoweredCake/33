/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.mojang.authlib.GameProfile;
import com.gaspoweredcake.client33.mixininterface.IChatHudLineVisible;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.OrderedText;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ChatHudLine.Visible.class)
public abstract class ChatHudLineVisibleMixin implements IChatHudLineVisible {
    @Shadow @Final private OrderedText content;
    @Unique private int id;
    @Unique private GameProfile sender;
    @Unique private boolean startOfEntry;

    @Override
    public String client33$getText() {
        StringBuilder sb = new StringBuilder();

        content.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });

        return sb.toString();
    }

    @Override
    public int client33$getId() {
        return id;
    }

    @Override
    public void client33$setId(int id) {
        this.id = id;
    }

    @Override
    public GameProfile client33$getSender() {
        return sender;
    }

    @Override
    public void client33$setSender(GameProfile profile) {
        sender = profile;
    }

    @Override
    public boolean client33$isStartOfEntry() {
        return startOfEntry;
    }

    @Override
    public void client33$setStartOfEntry(boolean start) {
        startOfEntry = start;
    }
}
