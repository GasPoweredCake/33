/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.accounts;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.widgets.containers.WTable;
import com.gaspoweredcake.client33.gui.widgets.input.WTextBox;
import com.gaspoweredcake.client33.systems.accounts.Accounts;
import com.gaspoweredcake.client33.systems.accounts.types.CrackedAccount;

public class AddCrackedAccountScreen extends AddAccountScreen {
    public AddCrackedAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Cracked Account", parent);
    }

    @Override
    public void initWidgets() {
        WTable t = add(theme.table()).widget();

        // Name
        t.add(theme.label("Name: "));
        WTextBox name = t.add(theme.textBox("", "seasnail8169", (text, c) ->
            /** @see net.minecraft.util.StringHelper#isValidPlayerName */
            c > 32 && c < 127
        )).minWidth(400).expandX().widget();
        name.setFocused(true);
        t.row();

        // Add
        add = t.add(theme.button("Add")).expandX().widget();
        add.action = () -> {
            String username = name.get().trim();
            if (username.length() > 16) return;

            CrackedAccount account = new CrackedAccount(username);
            if (!(Accounts.get().exists(account))) {
                AccountsScreen.addAccount(this, parent, account);
            }
        };

        enterAction = add.action;
    }
}
