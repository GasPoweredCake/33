/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.accounts;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.widgets.containers.WHorizontalList;
import com.gaspoweredcake.client33.gui.widgets.pressable.WButton;
import com.gaspoweredcake.client33.systems.accounts.MicrosoftLogin;
import com.gaspoweredcake.client33.systems.accounts.types.MicrosoftAccount;

import static com.gaspoweredcake.client33.Client33.mc;

public class AddMicrosoftAccountScreen extends AddAccountScreen {
    public AddMicrosoftAccountScreen(GuiTheme theme, AccountsScreen parent) {
        super(theme, "Add Microsoft Account", parent);
    }

    @Override
    public void initWidgets() {
        String url = MicrosoftLogin.getRefreshToken(refreshToken -> {

            if (refreshToken != null) {
                MicrosoftAccount account = new MicrosoftAccount(refreshToken);
                AccountsScreen.addAccount(null, parent, account);
            }

            close();
        });

        add(theme.label("Please select the account to log into in your browser."));
        add(theme.label("If the link does not automatically open in a few seconds, copy it into your browser."));

        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        WButton copy = l.add(theme.button("Copy link")).expandX().widget();
        copy.action = () -> mc.keyboard.setClipboard(url);

        WButton cancel = l.add(theme.button("Cancel")).expandX().widget();
        cancel.action = () -> {
            MicrosoftLogin.cancelLogin();
            close();
        };
    }

    @Override
    public void tick() {}

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
