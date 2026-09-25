/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.mixin;

import com.gaspoweredcake.client33.Client33;
import com.gaspoweredcake.client33.events.client33.CharTypedEvent;
import com.gaspoweredcake.client33.events.client33.KeyEvent;
import com.gaspoweredcake.client33.gui.GuiKeyEvents;
import com.gaspoweredcake.client33.gui.WidgetScreen;
import com.gaspoweredcake.client33.utils.Utils;
import com.gaspoweredcake.client33.utils.misc.input.Input;
import com.gaspoweredcake.client33.utils.misc.input.KeyAction;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    public void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        int modifiers = input.modifiers();
        if (input.key() != GLFW.GLFW_KEY_UNKNOWN) {
            // on Linux/X11 the modifier is not active when the key is pressed and still active when the key is released
            // https://github.com/glfw/glfw/issues/1630
            if (action == GLFW.GLFW_PRESS) {
                modifiers |= Input.getModifier(input.key());
            } else if (action == GLFW.GLFW_RELEASE) {
                modifiers &= ~Input.getModifier(input.key());
            }

            if (client.currentScreen instanceof WidgetScreen && action == GLFW.GLFW_REPEAT) {
                ((WidgetScreen) client.currentScreen).keyRepeated(new KeyInput(input.key(), input.scancode(), modifiers));
            }

            if (GuiKeyEvents.canUseKeys) {
                Input.setKeyState(input.key(), action != GLFW.GLFW_RELEASE);
                if (Client33.EVENT_BUS.post(KeyEvent.get(new KeyInput(input.key(), input.scancode(), modifiers), KeyAction.get(action))).isCancelled()) ci.cancel();
            }
        }
    }

    @Inject(method = "onChar", at = @At("HEAD"), cancellable = true)
    private void onChar(long window, CharInput input, CallbackInfo ci) {
        if (Utils.canUpdate() && !client.isPaused() && (client.currentScreen == null || client.currentScreen instanceof WidgetScreen)) {
            if (Client33.EVENT_BUS.post(CharTypedEvent.get((char) input.codepoint())).isCancelled()) ci.cancel();
        }
    }
}
