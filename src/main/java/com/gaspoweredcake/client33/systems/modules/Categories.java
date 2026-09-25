/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules;

import com.gaspoweredcake.client33.addons.AddonManager;
import com.gaspoweredcake.client33.addons.Client33Addon;
import net.minecraft.item.Items;

public class Categories {
    public static final Category Combat = new Category("Combat", Items.GOLDEN_SWORD.getDefaultStack());
    public static final Category Player = new Category("Player", Items.ARMOR_STAND.getDefaultStack());
    public static final Category Movement = new Category("Movement", Items.DIAMOND_BOOTS.getDefaultStack());
    public static final Category Render = new Category("Render", Items.GLASS.getDefaultStack());
    public static final Category World = new Category("World", Items.GRASS_BLOCK.getDefaultStack());
    public static final Category Misc = new Category("Misc", Items.LAVA_BUCKET.getDefaultStack());

    public static boolean REGISTERING;

    public static void init() {
        REGISTERING = true;

        // Client33
        Modules.registerCategory(Combat);
        Modules.registerCategory(Player);
        Modules.registerCategory(Movement);
        Modules.registerCategory(Render);
        Modules.registerCategory(World);
        Modules.registerCategory(Misc);

        // Addons
        AddonManager.ADDONS.forEach(Client33Addon::onRegisterCategories);

        REGISTERING = false;
    }
}
