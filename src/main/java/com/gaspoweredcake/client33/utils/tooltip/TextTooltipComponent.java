/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.utils.tooltip;

import net.minecraft.client.gui.tooltip.OrderedTextTooltipComponent;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

public class TextTooltipComponent extends OrderedTextTooltipComponent implements Client33TooltipData {
    public TextTooltipComponent(OrderedText text) {
        super(text);
    }

    public TextTooltipComponent(Text text) {
        this(text.asOrderedText());
    }

    @Override
    public TooltipComponent getComponent() {
        return this;
    }
}
