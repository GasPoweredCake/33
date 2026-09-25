/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.commands.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.gaspoweredcake.client33.commands.Command;
import com.gaspoweredcake.client33.mixininterface.ISimpleOption;
import net.minecraft.command.CommandSource;

public class FovCommand extends Command {
    public FovCommand() {
        super("fov", "Changes your fov.");
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(argument("fov", IntegerArgumentType.integer(1, 180)).executes(context -> {
            ((ISimpleOption) (Object) mc.options.getFov()).client33$set(context.getArgument("fov", Integer.class));
            return SINGLE_SUCCESS;
        }));
    }
}
