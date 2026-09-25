/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.utils.render.prompts;

import com.gaspoweredcake.client33.gui.GuiTheme;
import com.gaspoweredcake.client33.gui.GuiThemes;
import com.gaspoweredcake.client33.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screen.Screen;

import static com.gaspoweredcake.client33.Client33.mc;

public class OkPrompt extends Prompt<OkPrompt> {
    private Runnable onOk = () -> {};

    private OkPrompt(GuiTheme theme, Screen parent) {
        super(theme, parent);
    }

    public static OkPrompt create() {
        return new OkPrompt(GuiThemes.get(), mc.currentScreen);
    }

    public static OkPrompt create(GuiTheme theme, Screen parent) {
        return new OkPrompt(theme, parent);
    }

    public OkPrompt onOk(Runnable action) {
        this.onOk = action;
        return this;
    }

    @Override
    protected void initialiseWidgets(PromptScreen screen) {
        WButton okButton = screen.list.add(theme.button("Ok")).expandX().widget();
        okButton.action = () -> {
            dontShowAgain(screen);
            onOk.run();
            screen.close();
        };
    }
}
