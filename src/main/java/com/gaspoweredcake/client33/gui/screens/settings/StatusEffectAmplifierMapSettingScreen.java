/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.settings;

import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.WindowScreen;
import com.gaspoweredcake.client33.gui.widgets.containers.WTable;
import com.gaspoweredcake.client33.gui.widgets.input.WIntEdit;
import com.gaspoweredcake.client33.gui.widgets.input.WTextBox;
import com.gaspoweredcake.client33.settings.Setting;
import com.gaspoweredcake.client33.utils.misc.Names;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.apache.commons.lang3.Strings;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class StatusEffectAmplifierMapSettingScreen extends WindowScreen {
    private final Setting<Reference2IntMap<StatusEffect>> setting;

    private WTable table;

    private String filterText = "";

    public StatusEffectAmplifierMapSettingScreen(GuiTheme theme, Setting<Reference2IntMap<StatusEffect>> setting) {
        super(theme, "Modify Amplifiers");

        this.setting = setting;
    }

    @Override
    public void initWidgets() {
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {
            filterText = filter.get().trim();

            table.clear();
            initTable();
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    private void initTable() {
        List<StatusEffect> statusEffects = new ArrayList<>(setting.get().keySet());
        statusEffects.sort(Comparator.comparing(Names::get));

        for (StatusEffect statusEffect : statusEffects) {
            String name = Names.get(statusEffect);
            if (!Strings.CI.contains(name, filterText)) continue;

            table.add(theme.itemWithLabel(getPotionStack(statusEffect), name)).expandCellX();

            WIntEdit level = theme.intEdit(setting.get().getInt(statusEffect), 0, Integer.MAX_VALUE, true);
            level.action = () -> {
                setting.get().put(statusEffect, level.get());
                setting.onChanged();
            };

            table.add(level).minWidth(50);
            table.row();
        }
    }

    private ItemStack getPotionStack(StatusEffect effect) {
        ItemStack potion = Items.POTION.getDefaultStack();

        potion.set(
            DataComponentTypes.POTION_CONTENTS,
            new PotionContentsComponent(
                potion.get(DataComponentTypes.POTION_CONTENTS).potion(),
                Optional.of(effect.getColor()),
                potion.get(DataComponentTypes.POTION_CONTENTS).customEffects(),
                Optional.empty()
            )
        );

        return potion;
    }
}
