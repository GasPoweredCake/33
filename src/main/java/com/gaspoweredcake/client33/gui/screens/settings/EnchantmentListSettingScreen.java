/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.settings;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.screens.settings.base.DynamicRegistryListSettingScreen;
import com.gaspoweredcake.client33.gui.widgets.WWidget;
import com.gaspoweredcake.client33.settings.Setting;
import com.gaspoweredcake.client33.utils.misc.Names;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.Set;

public class EnchantmentListSettingScreen extends DynamicRegistryListSettingScreen<Enchantment> {
    public EnchantmentListSettingScreen(GuiTheme theme, Setting<Set<RegistryKey<Enchantment>>> setting) {
        super(theme, "Select Enchantments", setting, setting.get(), RegistryKeys.ENCHANTMENT);
    }

    @Override
    protected WWidget getValueWidget(RegistryKey<Enchantment> value) {
        return theme.label(Names.get(value));
    }

    @Override
    protected String[] getValueNames(RegistryKey<Enchantment> value) {
        return new String[]{
            Names.get(value),
            value.getValue().toString()
        };
    }
}
