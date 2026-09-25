/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.accounts;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.widgets.containers.WTable;
import com.gaspoweredcake.client33.gui.widgets.input.WTextBox;
import com.gaspoweredcake.client33.systems.accounts.types.TheAlteningAccount;

public class AddAlteningAccountScreen extends AddAccountScreen {
    public AddAlteningAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add The Altening Account", parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Token
        t.add(theme.label("Token: "));
        WTextBox token = t.add(theme.textBox("")).minWidth(400).expandX().widget();
        token.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            if (!token.get().isEmpty()) {
                AccountsScreen.addAccount(this, parent, new TheAlteningAccount(token.get()));
            }
        };

        enterAction = add.action;
    }
}
