/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.friends;

import com.mojang.util.UndashedUuid;
import com.gaspoweredcake.client33.Client33;
import com.gaspoweredcake.client33.utils.misc.ISerializable;
import com.gaspoweredcake.client33.utils.network.FailedHttpResponse;
import com.gaspoweredcake.client33.utils.network.Http;
import com.gaspoweredcake.client33.utils.render.PlayerHeadTexture;
import com.gaspoweredcake.client33.utils.render.PlayerHeadUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.net.http.HttpResponse;
import java.util.Objects;
import java.util.UUID;

import static com.gaspoweredcake.client33.Client33.mc;

public class Friend implements ISerializable<Friend>, Comparable<Friend> {
    public volatile String name;
    private volatile @Nullable UUID id;
    private volatile @Nullable PlayerHeadTexture headTexture;
    private volatile boolean updating;

    public Friend(String name, @Nullable UUID id) {
        this.name = name;
        this.id = id;
        this.headTexture = null;
    }

    public Friend(PlayerEntity player) {
        this(player.getName().getString(), player.getUuid());
    }
    public Friend(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public PlayerHeadTexture getHead() {
        return headTexture != null ? headTexture : PlayerHeadUtils.STEVE_HEAD;
    }

    public void updateInfo() {
        updating = true;
        HttpResponse<APIResponse> res = null;

        if (id != null) {
            res = Http.get("https://sessionserver.mojang.com/session/minecraft/profile/" + UndashedUuid.toString(id))
                .exceptionHandler(e -> Client33.LOG.error("Error while trying to connect session server for friend '{}'", name))
                .sendJsonResponse(APIResponse.class);
        }

        // Fallback to name-based lookup
        if (res == null || res.statusCode() != 200) {
            res = Http.get("https://api.mojang.com/users/profiles/minecraft/" + name)
                .exceptionHandler(e -> Client33.LOG.error("Error while trying to update info for friend '{}'", name))
                .sendJsonResponse(APIResponse.class);
        }

        if (res != null && res.statusCode() == 200) {
            name = res.body().name;
            id = UndashedUuid.fromStringLenient(res.body().id);

            byte[] head = PlayerHeadUtils.fetchHead(id);
            mc.execute(() -> {
                if (head != null) headTexture = new PlayerHeadTexture(head, true);
            });
        }

        // cracked accounts shouldn't be assigned ids
        else if (!(res instanceof FailedHttpResponse)) {
            id = null;
        }

        updating = false;
    }

    public boolean headTextureNeedsUpdate() {
        return !this.updating && headTexture == null;
    }

    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putString("name", name);
        if (id != null) tag.putString("id", UndashedUuid.toString(id));

        return tag;
    }

    @Override
    public Friend fromTag(NbtCompound tag) {
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friend friend = (Friend) o;
        return Objects.equals(name, friend.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public int compareTo(@NotNull Friend friend) {
        return name.compareToIgnoreCase(friend.name);
    }

    private static class APIResponse {
        String name, id;
    }
}
