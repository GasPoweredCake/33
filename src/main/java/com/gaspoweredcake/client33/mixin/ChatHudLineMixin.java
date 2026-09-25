/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.mojang.authlib.GameProfile;
import com.gaspoweredcake.client33.mixininterface.IChatHudLine;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value = ChatHudLine.class)
public abstract class ChatHudLineMixin implements IChatHudLine {
    @Shadow @Final private Text content;
    @Unique private int id;
    @Unique private GameProfile sender;

    @Override
    public String client33$getText() {
        return content.getString();
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
}
