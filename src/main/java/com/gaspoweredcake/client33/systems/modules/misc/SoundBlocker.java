/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.misc;

import com.gaspoweredcake.client33.events.world.PlaySoundEvent;
import com.gaspoweredcake.client33.settings.Setting;
import com.gaspoweredcake.client33.settings.SettingGroup;
import com.gaspoweredcake.client33.settings.SoundEventListSetting;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvent;

import java.util.List;

public class SoundBlocker extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<SoundEvent>> sounds = sgGeneral.add(new SoundEventListSetting.Builder()
        .name("sounds")
        .description("Sounds to block.")
        .build()
    );

    public SoundBlocker() {
        super(Categories.Misc, "sound-blocker", "Cancels out selected sounds.");
    }

    @EventHandler
    private void onPlaySound(PlaySoundEvent event) {
        for (SoundEvent sound : sounds.get()) {
            if (sound.id().equals(event.sound.getId())) {
                event.cancel();
                break;
            }
        }
    }

    public boolean shouldBlock(SoundInstance soundInstance) {
        return isActive() && sounds.get().contains(Setting.parseId(Registries.SOUND_EVENT, soundInstance.getId().getPath()));
    }
}
