/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.accounts;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.WindowScreen;
import com.gaspoweredcake.client33.gui.widgets.containers.WHorizontalList;
import com.gaspoweredcake.client33.gui.widgets.pressable.WButton;
import com.gaspoweredcake.client33.systems.accounts.Account;
import com.gaspoweredcake.client33.systems.accounts.AccountType;
import com.gaspoweredcake.client33.systems.accounts.TokenAccount;
import com.gaspoweredcake.client33.utils.render.color.Color;

import static com.gaspoweredcake.client33.Client33.mc;

public class AccountInfoScreen extends WindowScreen {
    private final Account<?> account;

    public AccountInfoScreen(GuiTheme theme, Account<?> account) {
        super(theme, account.getUsername() + " details");
        this.account = account;
    }

    @Override
    public void initWidgets() {
        TokenAccount e = (TokenAccount) account;
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        String tokenLabel = account.getType() + " token:";
        if (account.getType() == AccountType.Session) tokenLabel = "";

        WButton copy = theme.button("Copy");
        copy.action = () -> mc.keyboardHandler.setClipboard(e.getToken());

        l.add(theme.label(tokenLabel));
        l.add(theme.label(account.getType() == AccountType.Session ? "Click to copy Token" : e.getToken()).color(Color.GRAY)).pad(5);
        l.add(copy);
    }
}
