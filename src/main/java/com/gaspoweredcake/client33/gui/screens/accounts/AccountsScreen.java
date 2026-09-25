/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.gui.screens.accounts;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.WindowScreen;
import com.gaspoweredcake.client33.gui.widgets.WAccount;
import com.gaspoweredcake.client33.gui.widgets.containers.WContainer;
import com.gaspoweredcake.client33.gui.widgets.containers.WHorizontalList;
import com.gaspoweredcake.client33.gui.widgets.pressable.WButton;
import com.gaspoweredcake.client33.systems.accounts.Account;
import com.gaspoweredcake.client33.systems.accounts.AccountType;
import com.gaspoweredcake.client33.systems.accounts.Accounts;
import com.gaspoweredcake.client33.utils.misc.NbtUtils;
import com.gaspoweredcake.client33.utils.network.Client33Executor;
import org.jetbrains.annotations.Nullable;

import static com.gaspoweredcake.client33.Client33.mc;

public class AccountsScreen extends WindowScreen {
    public AccountsScreen(GuiTheme theme) {
        super(theme, "Accounts");
    }

    @Override
    public void initWidgets() {
        // Accounts
        for (Account<?> account : Accounts.get()) {
            WAccount wAccount = add(theme.account(this, account)).expandX().widget();
            wAccount.refreshScreenAction = this::reload;
        }

        // Add account
        WHorizontalList l = add(theme.horizontalList()).expandX().widget();

        addButton(l, "Cracked", () -> mc.setScreen(new AddCrackedAccountScreen(theme, this)));
        addButton(l, "Altening", () -> mc.setScreen(new AddAlteningAccountScreen(theme, this)));
        addButton(l, "Session", () -> mc.setScreen(new AddSessionAccountScreen(theme, this)));
        addButton(l, "Microsoft", () -> mc.setScreen(new AddMicrosoftAccountScreen(theme, this)));
    }

    private void addButton(WContainer c, String text, Runnable action) {
        WButton button = c.add(theme.button(text)).expandX().widget();
        button.action = action;
    }

    public static void addAccount(@Nullable AddAccountScreen screen, AccountsScreen parent, Account<?> account) {
        if (screen != null) screen.locked = true;

        Client33Executor.execute(() -> {
            if (!account.fetchInfo()) {
                mc.execute(() -> {
                    if (screen != null) screen.locked = false;
                });
                return;
            }

            Accounts.get().add(account);

            if (account.login()) {
                if (account.getType() != AccountType.Cracked) account.getCache().loadHead(parent::reload);
                Accounts.get().save();
            }

            mc.execute(() -> {
                if (screen != null) {
                    screen.locked = false;
                    screen.close();
                }

                parent.reload();
            });
        });
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Accounts.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Accounts.get());
    }
}
