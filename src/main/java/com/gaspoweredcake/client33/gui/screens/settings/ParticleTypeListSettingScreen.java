/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.settings;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.screens.settings.base.CollectionListSettingScreen;
import com.gaspoweredcake.client33.gui.widgets.WWidget;
import com.gaspoweredcake.client33.settings.Setting;
import com.gaspoweredcake.client33.utils.misc.Names;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;

import java.util.List;

public class ParticleTypeListSettingScreen extends CollectionListSettingScreen<ParticleType<?>> {
    public ParticleTypeListSettingScreen(GuiTheme theme, Setting<List<ParticleType<?>>> setting) {
        super(theme, "Select Particles", setting, setting.get(), Registries.PARTICLE_TYPE);
    }

    @Override
    protected boolean includeValue(ParticleType<?> value) {
        return value instanceof ParticleEffect;
    }

    @Override
    protected WWidget getValueWidget(ParticleType<?> value) {
        return theme.label(Names.get(value));
    }

    @Override
    protected String[] getValueNames(ParticleType<?> value) {
        return new String[]{
            Names.get(value),
            Registries.PARTICLE_TYPE.getId(value).toString()
        };
    }
}
