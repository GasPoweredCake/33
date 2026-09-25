/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.social.PlayerSocialManager;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.multiplayer.ProfileKeyPairManager;
import net.minecraft.client.User;
import net.minecraft.client.multiplayer.chat.report.ReportingContext;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.server.Services;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Accessor("fps")
    static int client33$getFps() {
        return 0;
    }

    @Mutable
    @Accessor("user")
    void client33$setUser(User session);

    @Accessor("reloadStateTracker")
    ResourceLoadStateTracker client33$getReloadStateTracker();

    @Accessor("missTime")
    int client33$getMissTime();

    @Accessor("missTime")
    void client33$setMissTime(int attackCooldown);

    @Invoker("startAttack")
    boolean client33$leftClick();

    @Mutable
    @Accessor("profileKeyPairManager")
    void client33$setProfileKeyPairManager(ProfileKeyPairManager keys);

    @Mutable
    @Accessor("userApiService")
    void client33$setUserApiService(UserApiService apiService);

    @Mutable
    @Accessor("skinManager")
    void client33$setSkinManager(SkinManager skinProvider);

    @Mutable
    @Accessor("playerSocialManager")
    void client33$setPlayerSocialManager(PlayerSocialManager socialInteractionsManager);

    @Mutable
    @Accessor("reportingContext")
    void client33$setReportingContext(ReportingContext abuseReportContext);

    @Mutable
    @Accessor("profileFuture")
    void client33$setProfileFuture(CompletableFuture<ProfileResult> future);

    @Mutable
    @Accessor("services")
    void client33$setServices(Services apiServices);

    @Invoker("handleKeybinds")
    void client33$handleInputEvents();
}
