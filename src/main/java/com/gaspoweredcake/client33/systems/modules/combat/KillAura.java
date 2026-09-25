/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.gaspoweredcake.client33.systems.modules.combat;

import com.gaspoweredcake.client33.events.packets.PacketEvent;
import com.gaspoweredcake.client33.events.world.TickEvent;
import com.gaspoweredcake.client33.pathing.PathManagers;
import com.gaspoweredcake.client33.settings.*;
import com.gaspoweredcake.client33.systems.friends.Friends;
import com.gaspoweredcake.client33.systems.modules.Categories;
import com.gaspoweredcake.client33.systems.modules.Module;
import com.gaspoweredcake.client33.systems.modules.Modules;
import com.gaspoweredcake.client33.utils.entity.EntityAgeTest;
import com.gaspoweredcake.client33.utils.entity.EntityUtils;
import com.gaspoweredcake.client33.utils.entity.SortPriority;
import com.gaspoweredcake.client33.utils.entity.Target;
import com.gaspoweredcake.client33.utils.entity.TargetUtils;
import com.gaspoweredcake.client33.utils.entity.fakeplayer.FakePlayerEntity;
import com.gaspoweredcake.client33.utils.player.FindItemResult;
import com.gaspoweredcake.client33.utils.player.InvUtils;
import com.gaspoweredcake.client33.utils.player.PlayerUtils;
import com.gaspoweredcake.client33.utils.player.Rotations;
import com.gaspoweredcake.client33.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.frog.Frog;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zoglin;
import net.minecraft.world.entity.monster.hoglin.Hoglin;
import net.minecraft.world.entity.monster.piglin.Piglin;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.monster.zombie.ZombifiedPiglin;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class KillAura extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgTargeting = settings.createGroup("Targeting");
    private final SettingGroup sgTiming = settings.createGroup("Timing");

    // General

    private final Setting<AttackItems> attackWhenHolding = sgGeneral.add(new EnumSetting.Builder<AttackItems>()
        .name("attack-when-holding")
        .description("Only attacks an entity when a specified item is in your hand.")
        .defaultValue(AttackItems.Weapons)
        .build()
    );

    private final Setting<List<Item>> weapons = sgGeneral.add(new ItemListSetting.Builder()
        .name("selected-weapon-types")
        .description("Which types of weapons to attack with (if you select the diamond sword, any type of sword may be used to attack).")
        .defaultValue(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.TRIDENT)
        .filter(FILTER::contains)
        .visible(() -> attackWhenHolding.get() == AttackItems.Weapons)
        .build()
    );

    private final Setting<RotationMode> rotation = sgGeneral.add(new EnumSetting.Builder<RotationMode>()
        .name("rotate")
        .description("Determines when you should rotate towards the target.")
        .defaultValue(RotationMode.Always)
        .build()
    );

    private final Setting<Target> aimAt = sgGeneral.add(new EnumSetting.Builder<Target>()
        .name("aim-at")
        .description("Which part of the target to aim at.")
        .defaultValue(Target.Body)
        .visible(() -> rotation.get() != RotationMode.None)
        .build()
    );

    private final Setting<Boolean> autoSwitch = sgGeneral.add(new BoolSetting.Builder()
        .name("auto-switch")
        .description("Switches to an acceptable weapon when attacking the target.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> swapBack = sgGeneral.add(new BoolSetting.Builder()
        .name("swap-back")
        .description("Switches to your previous slot when done attacking the target.")
        .defaultValue(false)
        .visible(autoSwitch::get)
        .build()
    );

    private final Setting<ShieldMode> shieldMode = sgGeneral.add(new EnumSetting.Builder<ShieldMode>()
        .name("shield-mode")
        .description("""
                What to do when your target is blocking with a shield:
                - Ignore:   Don't attack them if they are blocking
                - Break:    Swap to an axe to disable the shield (Only if Auto Switch is enabled)
                - None:     Attack them as normal
            """)
        .defaultValue(ShieldMode.None)
        .build()
    );

    private final Setting<Boolean> onlyOnClick = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-click")
        .description("Only attacks when holding left click.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> onlyOnLook = sgGeneral.add(new BoolSetting.Builder()
        .name("only-on-look")
        .description("Only attacks when looking at an entity.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> pauseOnCombat = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-baritone")
        .description("Freezes Baritone temporarily until you are finished attacking the entity.")
        .defaultValue(true)
        .build()
    );

    // Targeting

    private final Setting<Set<EntityType<?>>> entities = sgTargeting.add(new EntityTypeListSetting.Builder()
        .name("entities")
        .description("Entities to attack.")
        .onlyAttackable()
        .defaultValue(EntityTypes.PLAYER)
        .build()
    );

    private final Setting<SortPriority> priority = sgTargeting.add(new EnumSetting.Builder<SortPriority>()
        .name("priority")
        .description("How to filter targets within range.")
        .defaultValue(SortPriority.ClosestAngle)
        .build()
    );

    private final Setting<Integer> maxTargets = sgTargeting.add(new IntSetting.Builder()
        .name("max-targets")
        .description("How many entities to target at once.")
        .defaultValue(1)
        .min(1)
        .sliderRange(1, 5)
        .visible(() -> !onlyOnLook.get())
        .build()
    );

    private final Setting<Double> range = sgTargeting.add(new DoubleSetting.Builder()
        .name("range")
        .description("The maximum range the entity can be to attack it.")
        .defaultValue(4.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> wallsRange = sgTargeting.add(new DoubleSetting.Builder()
        .name("walls-range")
        .description("The maximum range the entity can be attacked through walls.")
        .defaultValue(3.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<EntityAgeTest> passiveMobAgeFilter = sgTargeting.add(new EnumSetting.Builder<EntityAgeTest>()
        .name("passive-mob-age-filter")
        .description("Determines the age of passive mobs to target (animals, villagers).")
        .defaultValue(EntityAgeTest.Adult)
        .build()
    );

    private final Setting<EntityAgeTest> hostileMobAgeFilter = sgTargeting.add(new EnumSetting.Builder<EntityAgeTest>()
        .name("hostile-mob-age-filter")
        .description("Determines the age of hostile mobs to target (zombies, piglins, hoglins, zoglins).")
        .defaultValue(EntityAgeTest.Both)
        .build()
    );

    private final Setting<Boolean> ignoreNamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-named")
        .description("Whether or not to attack mobs with a name.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> ignorePassive = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-passive")
        .description("Will only attack sometimes passive mobs if they are targeting you.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> ignoreTamed = sgTargeting.add(new BoolSetting.Builder()
        .name("ignore-tamed")
        .description("Will avoid attacking mobs you tamed.")
        .defaultValue(false)
        .build()
    );

    // Timing

    private final Setting<Boolean> pauseOnLag = sgTiming.add(new BoolSetting.Builder()
        .name("pause-on-lag")
        .description("Pauses if the server is lagging.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseOnUse = sgTiming.add(new BoolSetting.Builder()
        .name("pause-on-use")
        .description("Does not attack while using an item.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Boolean> pauseOnCA = sgTiming.add(new BoolSetting.Builder()
        .name("pause-on-CA")
        .description("Does not attack while CA is placing.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> tpsSync = sgTiming.add(new BoolSetting.Builder()
        .name("TPS-sync")
        .description("Tries to sync attack delay with the server's TPS.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> customDelay = sgTiming.add(new BoolSetting.Builder()
        .name("custom-delay")
        .description("Use a custom delay instead of the vanilla cooldown.")
        .defaultValue(false)
        .build()
    );

    private final Setting<Integer> hitDelay = sgTiming.add(new IntSetting.Builder()
        .name("hit-delay")
        .description("How fast you hit the entity in ticks.")
        .defaultValue(11)
        .min(0)
        .sliderMax(60)
        .visible(customDelay::get)
        .build()
    );

    private final Setting<Integer> switchDelay = sgTiming.add(new IntSetting.Builder()
        .name("switch-delay")
        .description("How many ticks to wait before hitting an entity after switching hotbar slots.")
        .defaultValue(0)
        .min(0)
        .sliderMax(10)
        .build()
    );

    private final static Set<Item> FILTER = Set.of(Items.DIAMOND_SWORD, Items.DIAMOND_AXE, Items.DIAMOND_PICKAXE, Items.DIAMOND_SHOVEL, Items.DIAMOND_HOE, Items.MACE, Items.DIAMOND_SPEAR, Items.TRIDENT);
    private final List<Entity> targets = new ArrayList<>();
    private int switchTimer, hitTimer;
    private boolean wasPathing = false;
    public boolean attacking, swapped;
    public static int previousSlot;

    public KillAura() {
        super(Categories.Combat, "kill-aura", "Attacks specified entities around you.");
    }

    @Override
    public void onActivate() {
        targets.clear();
        attacking = false;
        wasPathing = false;
        previousSlot = -1;
        swapped = false;
        switchTimer = 0;
        hitTimer = 0;
    }

    @Override
    public void onDeactivate() {
        stopAttacking();
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (!mc.player.isAlive() || PlayerUtils.getGameMode() == GameType.SPECTATOR) {
            stopAttacking();
            return;
        }
        if (pauseOnUse.get() && (mc.gameMode.isDestroying() || mc.player.isUsingItem())) {
            stopAttacking();
            return;
        }
        if (onlyOnClick.get() && !mc.options.keyAttack.isDown()) {
            stopAttacking();
            return;
        }
        if (TickRate.INSTANCE.getTimeSinceLastTick() >= 1f && pauseOnLag.get()) {
            stopAttacking();
            return;
        }
        if (pauseOnCA.get() && Modules.get().get(CrystalAura.class).isActive() && Modules.get().get(CrystalAura.class).kaTimer > 0) {
            stopAttacking();
            return;
        }
        if (onlyOnLook.get()) {
            Entity targeted = mc.crosshairPickEntity;

            if (targeted == null || !entityCheck(targeted)) {
                stopAttacking();
                return;
            }

            targets.clear();
            targets.add(mc.crosshairPickEntity);
        } else {
            targets.clear();
            TargetUtils.getList(targets, this::entityCheck, priority.get(), maxTargets.get());
        }

        if (targets.isEmpty()) {
            stopAttacking();
            return;
        }

        Entity primary = targets.getFirst();

        if (autoSwitch.get()) {
            int selectedSlot = mc.player.getInventory().getSelectedSlot();
            ItemStack heldItem = mc.player.getMainHandItem();
            FindItemResult weaponResult = new FindItemResult(selectedSlot, heldItem.getCount());

            if (shouldShieldBreak()) {
                if (!(heldItem.getItem() instanceof AxeItem)) {
                    weaponResult = InvUtils.find(itemStack -> itemStack.getItem() instanceof AxeItem, 0, 8);
                }
            } else if (!acceptableWeapon(heldItem)) {
                weaponResult = InvUtils.find(this::acceptableWeapon, 0, 8);
            }

            if (!weaponResult.found()) {
                stopAttacking();
                return;
            }

            if (weaponResult.slot() != selectedSlot) {
                if (!swapped) {
                    previousSlot = selectedSlot;
                    swapped = true;
                }
                InvUtils.swap(weaponResult.slot(), false);
            }
        }

        if (!acceptableWeapon(mc.player.getMainHandItem())) {
            stopAttacking();
            return;
        }

        attacking = true;
        if (pauseOnCombat.get() && PathManagers.get().isPathing() && !wasPathing) {
            PathManagers.get().pause();
            wasPathing = true;
        }

        RotationMode rotationMode = rotation.get();
        if (delayCheck()) {
            for (Entity target : targets) {
                if (rotationMode == RotationMode.None) attack(target);
                else rotateTo(target, () -> {
                    if (isActive() && attacking && mc.level != null && entityCheck(target)) attack(target);
                });
            }
        } else if (rotationMode == RotationMode.Always) {
            rotateTo(primary, null);
        }
    }

    @EventHandler
    private void onSendPacket(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundSetCarriedItemPacket) {
            switchTimer = switchDelay.get();
        }
    }

    private void stopAttacking() {
        targets.clear();
        attacking = false;
        if (wasPathing) {
            PathManagers.get().resume();
            wasPathing = false;
        }
        if (swapBack.get() && swapped && previousSlot >= 0 && mc.player != null) {
            InvUtils.swap(previousSlot, false);
        }
        swapped = false;
        previousSlot = -1;
    }

    private boolean shouldShieldBreak() {
        if (!autoSwitch.get() || shieldMode.get() != ShieldMode.Break) return false;

        for (Entity target : targets) {
            if (target instanceof Player player) {
                if (player.isBlocking()) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean entityCheck(Entity entity) {
        if (entity.equals(mc.player) || entity.equals(mc.getCameraEntity())) return false;
        if ((entity instanceof LivingEntity livingEntity && livingEntity.isDeadOrDying()) || !entity.isAlive())
            return false;

        AABB hitbox = entity.getBoundingBox();
        if (!PlayerUtils.isWithin(
            Mth.clamp(mc.player.getX(), hitbox.minX, hitbox.maxX),
            Mth.clamp(mc.player.getY(), hitbox.minY, hitbox.maxY),
            Mth.clamp(mc.player.getZ(), hitbox.minZ, hitbox.maxZ),
            range.get()
        )) return false;

        if (!entities.get().contains(entity.getType())) return false;
        if (ignoreNamed.get() && entity.hasCustomName()) return false;
        if (!PlayerUtils.canSeeEntity(entity) && !PlayerUtils.isWithin(entity, wallsRange.get())) return false;
        if (ignoreTamed.get()) {
            if (entity instanceof OwnableEntity tameable
                && tameable.getOwner() != null
                && tameable.getOwner().equals(mc.player)
            ) return false;
        }
        if (ignorePassive.get()) {
            if (entity instanceof EnderMan enderman && !enderman.isCreepy()) return false;
            if ((entity instanceof Piglin || entity instanceof ZombifiedPiglin || entity instanceof Wolf) && !((Mob) entity).isAggressive())
                return false;
        }
        if (entity instanceof Player player) {
            if (player.isCreative()) return false;
            if (!Friends.get().shouldAttack(player)) return false;
            if (shieldMode.get() == ShieldMode.Ignore && player.isBlocking()) return false;
            if (player instanceof FakePlayerEntity fakePlayer && fakePlayer.noHit) return false;
        }
        if (entity instanceof LivingEntity livingEntity) {
            // Hostile mobs with baby variants (zombies, piglins, hoglins, zoglins)
            if (entity instanceof Zombie || entity instanceof Piglin
                || entity instanceof Hoglin || entity instanceof Zoglin) {
                return hostileMobAgeFilter.get().test(livingEntity);
            }
            // Passive mobs with baby variants (animals, villagers)
            if (entity instanceof AgeableMob && (!(entity instanceof Frog || entity instanceof Parrot))) {
                return passiveMobAgeFilter.get().test(livingEntity);
            }
        }
        return true;
    }

    private boolean delayCheck() {
        if (switchTimer > 0) {
            switchTimer--;
            return false;
        }

        float delay = (customDelay.get()) ? hitDelay.get() : 0.5f;
        if (tpsSync.get()) delay /= (TickRate.INSTANCE.getTickRate() / 20);

        if (customDelay.get()) {
            if (hitTimer < delay) {
                hitTimer++;
                return false;
            } else return true;
        } else return mc.player.getAttackStrengthScale(delay) >= 1;
    }

    private void attack(Entity target) {
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);

        hitTimer = 0;
    }

    private void rotateTo(Entity target, Runnable callback) {
        Rotations.rotate(Rotations.getYaw(target), Rotations.getPitch(target, aimAt.get()), callback);
    }

    private boolean acceptableWeapon(ItemStack stack) {
        if (shouldShieldBreak()) return stack.getItem() instanceof AxeItem;
        if (attackWhenHolding.get() == AttackItems.All) return true;

        if (weapons.get().contains(Items.DIAMOND_SWORD) && stack.is(ItemTags.SWORDS)) return true;
        if (weapons.get().contains(Items.DIAMOND_AXE) && stack.is(ItemTags.AXES)) return true;
        if (weapons.get().contains(Items.DIAMOND_PICKAXE) && stack.is(ItemTags.PICKAXES)) return true;
        if (weapons.get().contains(Items.DIAMOND_SHOVEL) && stack.is(ItemTags.SHOVELS)) return true;
        if (weapons.get().contains(Items.DIAMOND_HOE) && stack.is(ItemTags.HOES)) return true;
        if (weapons.get().contains(Items.MACE) && stack.getItem() instanceof MaceItem) return true;
        if (weapons.get().contains(Items.DIAMOND_SPEAR) && stack.is(ItemTags.SPEARS)) return true;
        return weapons.get().contains(Items.TRIDENT) && stack.getItem() instanceof TridentItem;
    }

    public Entity getTarget() {
        if (!targets.isEmpty()) return targets.getFirst();
        return null;
    }

    @Override
    public String getInfoString() {
        if (!targets.isEmpty()) return EntityUtils.getName(getTarget());
        return null;
    }

    public enum AttackItems {
        Weapons,
        All
    }

    public enum RotationMode {
        Always,
        OnHit,
        None
    }

    public enum ShieldMode {
        Ignore,
        Break,
        None
    }

}
