/*
 *  Copyright (C) <2022> <XiaoMoMi>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.momirealms.customfishing.api.mechanic.fishing;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.hook.HookConfig;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.customfishing.common.util.TriConsumer;
import net.momirealms.sparrow.heart.feature.inventory.HandSlot;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

/**
 * Represents the fishing gears used by a player.
 */
public class FishingGears {

    private static final Map<ActionTrigger, TriConsumer<GearType, Context<Player>, ItemStack>> triggers = new HashMap<>();

    static {
        triggers.put(ActionTrigger.CAST, ((type, context, itemStack) -> type.castFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.REEL, ((type, context, itemStack) -> type.reelFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.LAND, ((type, context, itemStack) -> type.landFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.ESCAPE, ((type, context, itemStack) -> type.escapeFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.LURE, ((type, context, itemStack) -> type.lureFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.SUCCESS, ((type, context, itemStack) -> type.successFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.FAILURE, ((type, context, itemStack) -> type.failureFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.BITE, ((type, context, itemStack) -> type.biteFunction.accept(context, itemStack)));
        triggers.put(ActionTrigger.HOOK, ((type, context, itemStack) -> type.hookFunction.accept(context, itemStack)));
    }

    private static BiConsumer<Context<Player>, FishingGears> fishingGearsConsumers = defaultFishingGearsConsumers();
    private final HashMap<GearType, Collection<Pair<String, ItemStack>>> gears = new HashMap<>();
    private final ArrayList<EffectModifier> modifiers = new ArrayList<>();
    private boolean canFish = true;
    private HandSlot rodSlot;

    /**
     * Sets the fishing gears consumers.
     *
     * @param fishingGearsConsumers the BiConsumer to set.
     */
    public static void fishingGearsConsumers(BiConsumer<Context<Player>, FishingGears> fishingGearsConsumers) {
        FishingGears.fishingGearsConsumers = fishingGearsConsumers;
    }

    /**
     * Constructs a new FishingGears instance.
     *
     * @param context the context of the player.
     */
    public FishingGears(Context<Player> context) {
        fishingGearsConsumers.accept(context, this);
    }

    /**
     * Checks if the player can fish.
     *
     * @return true if the player can fish, false otherwise.
     */
    public boolean canFish() {
        return canFish;
    }

    /**
     * Triggers an action based on the specified trigger.
     *
     * @param trigger the ActionTrigger.
     * @param context the context of the player.
     */
    public void trigger(ActionTrigger trigger, Context<Player> context) {
        for (Map.Entry<GearType, Collection<Pair<String, ItemStack>>> entry : gears.entrySet()) {
            for (Pair<String, ItemStack> itemPair : entry.getValue()) {
                BukkitCustomFishingPlugin.getInstance().debug(entry.getKey() + " | " + itemPair.left() + " | " + trigger);
                Optional.ofNullable(triggers.get(trigger)).ifPresent(tri -> {
                    tri.accept(entry.getKey(), context, itemPair.right());
                });
                BukkitCustomFishingPlugin.getInstance().getEventManager().trigger(context, itemPair.left(), entry.getKey().getType(), trigger);
            }
        }
    }

    /**
     * Gets the list of effect modifiers.
     *
     * @return the list of effect modifiers.
     */
    @NotNull
    public List<EffectModifier> effectModifiers() {
        return modifiers;
    }

    /**
     * Gets the hand slot of the fishing rod.
     *
     * @return the hand slot of the fishing rod.
     */
    public HandSlot getRodSlot() {
        return rodSlot;
    }

    /**
     * Gets the items for the specified gear type.
     *
     * @param type the gear type.
     * @return the collection of items for the specified gear type.
     */
    @NotNull
    public Collection<Pair<String, ItemStack>> getItem(GearType type) {
        return gears.getOrDefault(type, List.of());
    }

    /**
     * Provides the default fishing gears consumers.
     *
     * @return the BiConsumer for default fishing gears consumers.
     */
    public static BiConsumer<Context<Player>, FishingGears> defaultFishingGearsConsumers() {
        return (context, fishingGears) -> {
            Player player = context.getHolder();
            PlayerInventory playerInventory = player.getInventory();
            ItemStack mainHandItem = playerInventory.getItemInMainHand();
            ItemStack offHandItem = playerInventory.getItemInOffHand();
            // set rod
            boolean rodOnMainHand = mainHandItem.getType() == Material.FISHING_ROD;
            ItemStack rodItem = rodOnMainHand ? mainHandItem : offHandItem;
            String rodID = BukkitCustomFishingPlugin.getInstance().getItemManager().getItemID(rodItem);
            fishingGears.gears.put(GearType.ROD, List.of(Pair.of(rodID, rodItem)));
            context.arg(ContextKeys.ROD, rodID);
            fishingGears.rodSlot = rodOnMainHand ? HandSlot.MAIN : HandSlot.OFF;
            BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(rodID, MechanicType.ROD).ifPresent(fishingGears.modifiers::add);

            // set enchantments
            List<Pair<String, Short>> enchants = BukkitCustomFishingPlugin.getInstance().getIntegrationManager().getEnchantments(rodItem);
            for (Pair<String, Short> enchantment : enchants) {
                String effectID = enchantment.left() + ":" + enchantment.right();
                BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(effectID, MechanicType.ENCHANT).ifPresent(fishingGears.modifiers::add);
            }

            // set hook
            BukkitCustomFishingPlugin.getInstance().getHookManager().getHookID(rodItem).ifPresent(hookID -> {
                fishingGears.gears.put(GearType.HOOK, List.of(Pair.of(hookID, rodItem)));
                context.arg(ContextKeys.HOOK, hookID);
                BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(hookID, MechanicType.HOOK).ifPresent(fishingGears.modifiers::add);
            });

            // set bait if it is
            boolean hasBait = false;
            String anotherItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getItemID(rodOnMainHand ? offHandItem : mainHandItem);
            List<MechanicType> type = MechanicType.getTypeByID(anotherItemID);
            if (type != null && type.contains(MechanicType.BAIT)) {
                fishingGears.gears.put(GearType.BAIT, List.of(Pair.of(anotherItemID, rodOnMainHand ? offHandItem : mainHandItem)));
                context.arg(ContextKeys.BAIT, anotherItemID);
                BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(anotherItemID, MechanicType.BAIT).ifPresent(fishingGears.modifiers::add);
                hasBait = true;
            }

            // search the bag
            if (ConfigManager.enableBag()) {
                Optional<UserData> dataOptional = BukkitCustomFishingPlugin.getInstance().getStorageManager().getOnlineUser(player.getUniqueId());
                if (dataOptional.isPresent()) {
                    UserData data = dataOptional.get();
                    Inventory bag = data.holder().getInventory();
                    HashMap<String, ItemStack> uniqueUtils = new HashMap<>();
                    for (int i = 0; i < bag.getSize(); i++) {
                        ItemStack itemInBag = bag.getItem(i);
                        if (itemInBag == null) continue;
                        String bagItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getItemID(itemInBag);
                        List<MechanicType> bagItemType = MechanicType.getTypeByID(bagItemID);
                        if (bagItemType != null) {
                            if (!hasBait && bagItemType.contains(MechanicType.BAIT)) {
                                fishingGears.gears.put(GearType.BAIT, List.of(Pair.of(bagItemID, itemInBag)));
                                context.arg(ContextKeys.BAIT, bagItemID);
                                BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(bagItemID, MechanicType.BAIT).ifPresent(fishingGears.modifiers::add);
                                hasBait = true;
                            }
                            if (bagItemType.contains(MechanicType.UTIL)) {
                                uniqueUtils.put(bagItemID, itemInBag);
                            }
                        }
                    }
                    if (!uniqueUtils.isEmpty()) {
                        ArrayList<Pair<String, ItemStack>> utils = new ArrayList<>();
                        for (Map.Entry<String, ItemStack> entry : uniqueUtils.entrySet()) {
                            utils.add(Pair.of(entry.getKey(), entry.getValue()));
                            BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(entry.getKey(), MechanicType.UTIL).ifPresent(fishingGears.modifiers::add);
                        }
                        fishingGears.gears.put(GearType.UTIL, utils);
                    }
                }
            }

            // check requirements before checking totems
            for (EffectModifier modifier : fishingGears.modifiers) {
                if (!RequirementManager.isSatisfied(context, modifier.requirements())) {
                    fishingGears.canFish = false;
                }
            }

            // set totems
            Collection<String> totemIDs = BukkitCustomFishingPlugin.getInstance().getTotemManager().getActivatedTotems(player.getLocation());
            for (String id : totemIDs) {
                BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(id, MechanicType.TOTEM).ifPresent(fishingGears.modifiers::add);
            }

            // add global effects
            fishingGears.modifiers.add(
                    EffectModifier.builder()
                            .id("__GLOBAL__")
                            .modifiers(ConfigManager.globalEffects())
                            .build()
            );
        };
    }

    public static class GearType {

        public static final GearType ROD = new GearType(MechanicType.ROD,
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {
                    if (context.getHolder().getGameMode() != GameMode.CREATIVE)
                        BukkitCustomFishingPlugin.getInstance().getItemManager().decreaseDurability(context.getHolder(), itemStack, 1, false);
                }),
                ((context, itemStack) -> {
                    if (context.getHolder().getGameMode() != GameMode.CREATIVE)
                        BukkitCustomFishingPlugin.getInstance().getItemManager().decreaseDurability(context.getHolder(), itemStack, 1, false);
                }),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {})
        );

        public static final GearType BAIT = new GearType(MechanicType.BAIT,
                ((context, itemStack) -> {
                    if (context.getHolder().getGameMode() != GameMode.CREATIVE)
                        itemStack.setAmount(itemStack.getAmount() - 1);
                }),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {})
        );

        public static final GearType HOOK = new GearType(MechanicType.HOOK,
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {
                    if (context.getHolder().getGameMode() != GameMode.CREATIVE) {
                        Item<ItemStack> wrapped = BukkitCustomFishingPlugin.getInstance().getItemManager().wrap(itemStack.clone());
                        String hookID = (String) wrapped.getTag("CustomFishing", "hook_id").orElseThrow(() -> new RuntimeException("This error should never occur"));
                        wrapped.getTag("CustomFishing", "hook_max_damage").ifPresent(max -> {
                            int maxDamage = (int) max;
                            int hookDamage = (int) wrapped.getTag("CustomFishing", "hook_damage").orElse(0) + 1;
                            if (hookDamage >= maxDamage) {
                                wrapped.removeTag("CustomFishing", "hook_damage");
                                wrapped.removeTag("CustomFishing", "hook_id");
                                wrapped.removeTag("CustomFishing", "hook_stack");
                                wrapped.removeTag("CustomFishing", "hook_max_damage");
                                BukkitCustomFishingPlugin.getInstance().getSenderFactory().getAudience(context.getHolder()).playSound(Sound.sound(Key.key("minecraft:entity.item.break"), Sound.Source.PLAYER, 1, 1));
                            } else {
                                wrapped.setTag(hookDamage, "CustomFishing", "hook_damage");
                                HookConfig hookConfig = BukkitCustomFishingPlugin.getInstance().getHookManager().getHook(hookID).orElseThrow();
                                List<String> previousLore = wrapped.lore().orElse(new ArrayList<>());
                                List<String> newLore = new ArrayList<>();
                                List<String> durabilityLore = new ArrayList<>();
                                for (String previous : previousLore) {
                                    Component component = AdventureHelper.jsonToComponent(previous);
                                    if (component instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("cf")) {
                                        if (scoreComponent.objective().equals("hook")) {
                                            continue;
                                        } else if (scoreComponent.objective().equals("durability")) {
                                            durabilityLore.add(previous);
                                            continue;
                                        }
                                    }
                                    newLore.add(previous);
                                }
                                for (String lore : hookConfig.lore()) {
                                    ScoreComponent.Builder builder = Component.score().name("cf").objective("hook");
                                    builder.append(AdventureHelper.miniMessage(lore.replace("{dur}", String.valueOf(maxDamage - hookDamage)).replace("{max}", String.valueOf(maxDamage))));
                                    newLore.add(AdventureHelper.componentToJson(builder.build()));
                                }
                                newLore.addAll(durabilityLore);
                                wrapped.lore(newLore);
                            }
                            itemStack.setItemMeta(wrapped.load().getItemMeta());
                        });
                    }
                }),
                ((context, itemStack) -> {
                    if (context.getHolder().getGameMode() != GameMode.CREATIVE) {
                        Item<ItemStack> wrapped = BukkitCustomFishingPlugin.getInstance().getItemManager().wrap(itemStack.clone());
                        String hookID = (String) wrapped.getTag("CustomFishing", "hook_id").orElseThrow(() -> new RuntimeException("This error should never occur"));
                        wrapped.getTag("CustomFishing", "hook_max_damage").ifPresent(max -> {
                            int maxDamage = (int) max;
                            int hookDamage = (int) wrapped.getTag("CustomFishing", "hook_damage").orElse(0) + 1;
                            if (hookDamage >= maxDamage) {
                                wrapped.removeTag("CustomFishing", "hook_damage");
                                wrapped.removeTag("CustomFishing", "hook_id");
                                wrapped.removeTag("CustomFishing", "hook_stack");
                                wrapped.removeTag("CustomFishing", "hook_max_damage");
                                BukkitCustomFishingPlugin.getInstance().getSenderFactory().getAudience(context.getHolder()).playSound(Sound.sound(Key.key("minecraft:entity.item.break"), Sound.Source.PLAYER, 1, 1));
                            } else {
                                wrapped.setTag(hookDamage, "CustomFishing", "hook_damage");
                                HookConfig hookConfig = BukkitCustomFishingPlugin.getInstance().getHookManager().getHook(hookID).orElseThrow();
                                List<String> previousLore = wrapped.lore().orElse(new ArrayList<>());
                                List<String> newLore = new ArrayList<>();
                                List<String> durabilityLore = new ArrayList<>();
                                for (String previous : previousLore) {
                                    Component component = AdventureHelper.jsonToComponent(previous);
                                    if (component instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("cf")) {
                                        if (scoreComponent.objective().equals("hook")) {
                                            continue;
                                        } else if (scoreComponent.objective().equals("durability")) {
                                            durabilityLore.add(previous);
                                            continue;
                                        }
                                    }
                                    newLore.add(previous);
                                }
                                for (String lore : hookConfig.lore()) {
                                    ScoreComponent.Builder builder = Component.score().name("cf").objective("hook");
                                    builder.append(AdventureHelper.miniMessage(lore.replace("{dur}", String.valueOf(maxDamage - hookDamage)).replace("{max}", String.valueOf(maxDamage))));
                                    newLore.add(AdventureHelper.componentToJson(builder.build()));
                                }
                                newLore.addAll(durabilityLore);
                                wrapped.lore(newLore);
                            }
                            itemStack.setItemMeta(wrapped.load().getItemMeta());
                        });
                    }
                }),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {})
        );

        public static final GearType UTIL = new GearType(MechanicType.UTIL,
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {})
        );

        private final MechanicType type;
        private BiConsumer<Context<Player>, ItemStack> castFunction;
        private BiConsumer<Context<Player>, ItemStack> reelFunction;
        private BiConsumer<Context<Player>, ItemStack> biteFunction;
        private BiConsumer<Context<Player>, ItemStack> successFunction;
        private BiConsumer<Context<Player>, ItemStack> failureFunction;
        private BiConsumer<Context<Player>, ItemStack> lureFunction;
        private BiConsumer<Context<Player>, ItemStack> escapeFunction;
        private BiConsumer<Context<Player>, ItemStack> landFunction;
        private BiConsumer<Context<Player>, ItemStack> hookFunction;

        public GearType(MechanicType type,
                        BiConsumer<Context<Player>, ItemStack> castFunction, BiConsumer<Context<Player>, ItemStack> reelFunction,
                        BiConsumer<Context<Player>, ItemStack> biteFunction, BiConsumer<Context<Player>, ItemStack> successFunction,
                        BiConsumer<Context<Player>, ItemStack> failureFunction, BiConsumer<Context<Player>, ItemStack> lureFunction,
                        BiConsumer<Context<Player>, ItemStack> escapeFunction, BiConsumer<Context<Player>, ItemStack> landFunction,
                        BiConsumer<Context<Player>, ItemStack> hookFunction
        ) {
            this.type = type;
            this.castFunction = castFunction;
            this.reelFunction = reelFunction;
            this.biteFunction = biteFunction;
            this.successFunction = successFunction;
            this.failureFunction = failureFunction;
            this.landFunction = landFunction;
            this.lureFunction = lureFunction;
            this.escapeFunction = escapeFunction;
            this.hookFunction = hookFunction;
        }

        public void castFunction(BiConsumer<Context<Player>, ItemStack> castFunction) {
            this.castFunction = castFunction;
        }

        public void reelFunction(BiConsumer<Context<Player>, ItemStack> reelFunction) {
            this.reelFunction = reelFunction;
        }

        public void biteFunction(BiConsumer<Context<Player>, ItemStack> biteFunction) {
            this.biteFunction = biteFunction;
        }

        public void successFunction(BiConsumer<Context<Player>, ItemStack> successFunction) {
            this.successFunction = successFunction;
        }

        public void failureFunction(BiConsumer<Context<Player>, ItemStack> failureFunction) {
            this.failureFunction = failureFunction;
        }

        public void escapeFunction(BiConsumer<Context<Player>, ItemStack> escapeFunction) {
            this.escapeFunction = escapeFunction;
        }

        public void lureFunction(BiConsumer<Context<Player>, ItemStack> lureFunction) {
            this.lureFunction = lureFunction;
        }

        public void landFunction(BiConsumer<Context<Player>, ItemStack> landFunction) {
            this.landFunction = landFunction;
        }

        public void hookFunction(BiConsumer<Context<Player>, ItemStack> hookFunction) {
            this.hookFunction = hookFunction;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) return true;
            if (object == null || getClass() != object.getClass()) return false;
            GearType gearType = (GearType) object;
            return Objects.equals(type, gearType.type);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(type);
        }

        @Override
        public String toString() {
            return type.toString();
        }

        public MechanicType getType() {
            return type;
        }
    }
}
