/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.mojang.authlib.minecraft.UserApiService;
import com.mojang.authlib.yggdrasil.ProfileResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.SocialInteractionsManager;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.client.session.ProfileKeys;
import net.minecraft.client.session.Session;
import net.minecraft.client.session.report.AbuseReportContext;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.util.ApiServices;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public interface MinecraftClientAccessor {
    @Accessor("currentFps")
    static int client33$getFps() {
        return 0;
    }

    @Mutable
    @Accessor("session")
    void client33$setSession(Session session);

    @Accessor("resourceReloadLogger")
    ResourceReloadLogger client33$getResourceReloadLogger();

    @Accessor("attackCooldown")
    int client33$getAttackCooldown();

    @Accessor("attackCooldown")
    void client33$setAttackCooldown(int attackCooldown);

    @Invoker("doAttack")
    boolean client33$leftClick();

    @Mutable
    @Accessor("profileKeys")
    void client33$setProfileKeys(ProfileKeys keys);

    @Mutable
    @Accessor("userApiService")
    void client33$setUserApiService(UserApiService apiService);

    @Mutable
    @Accessor("skinProvider")
    void client33$setSkinProvider(PlayerSkinProvider skinProvider);

    @Mutable
    @Accessor("socialInteractionsManager")
    void client33$setSocialInteractionsManager(SocialInteractionsManager socialInteractionsManager);

    @Mutable
    @Accessor("abuseReportContext")
    void client33$setAbuseReportContext(AbuseReportContext abuseReportContext);

    @Mutable
    @Accessor("gameProfileFuture")
    void client33$setGameProfileFuture(CompletableFuture<ProfileResult> future);

    @Mutable
    @Accessor("apiServices")
    void client33$setApiServices(ApiServices apiServices);

    @Invoker("handleInputEvents")
    void client33$handleInputEvents();
}
