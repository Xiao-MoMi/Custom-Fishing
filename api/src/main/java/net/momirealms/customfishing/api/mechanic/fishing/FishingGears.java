package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.storage.user.UserData;
import net.momirealms.customfishing.common.util.Pair;
import net.momirealms.customfishing.common.util.TriConsumer;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;

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
    }

    private static BiConsumer<Context<Player>, FishingGears> fishingGearsConsumers = defaultFishingGearsConsumers();
    private final HashMap<GearType, Collection<Pair<String, ItemStack>>> gears = new HashMap<>();
    private final ArrayList<EffectModifier> modifiers = new ArrayList<>();
    private boolean canFish = true;

    public static void fishingGearsConsumers(BiConsumer<Context<Player>, FishingGears> fishingGearsConsumers) {
        FishingGears.fishingGearsConsumers = fishingGearsConsumers;
    }

    public FishingGears(Context<Player> context) {
        fishingGearsConsumers.accept(context, this);
    }

    public boolean canFish() {
        return canFish;
    }

    public void trigger(ActionTrigger trigger, Context<Player> context) {
        for (Map.Entry<GearType, Collection<Pair<String, ItemStack>>> entry : gears.entrySet()) {
            for (Pair<String, ItemStack> itemPair : entry.getValue()) {
                BukkitCustomFishingPlugin.getInstance().debug(entry.getKey() + " | " + itemPair.left() + " | " + trigger);
                triggers.get(trigger).accept(entry.getKey(), context, itemPair.right());
                BukkitCustomFishingPlugin.getInstance().getEventManager().trigger(context, itemPair.left(), entry.getKey().getType(), trigger);
            }
        }
    }

    @NotNull
    public List<EffectModifier> effectModifiers() {
        return modifiers;
    }

    @NotNull
    public Collection<Pair<String, ItemStack>> getItem(GearType type) {
        return gears.getOrDefault(type, List.of());
    }

    public static BiConsumer<Context<Player>, FishingGears> defaultFishingGearsConsumers() {
        return (context, fishingGears) -> {
            Player player = context.getHolder();
            PlayerInventory playerInventory = player.getInventory();
            ItemStack mainHandItem = playerInventory.getItemInMainHand();
            ItemStack offHandItem = playerInventory.getItemInOffHand();
            // set rod
            boolean rodOnMainHand = mainHandItem.getType() == Material.FISHING_ROD;
            String rodID = BukkitCustomFishingPlugin.getInstance().getItemManager().getItemID(rodOnMainHand ? mainHandItem : offHandItem);
            fishingGears.gears.put(GearType.ROD, List.of(Pair.of(rodID, rodOnMainHand ? mainHandItem : offHandItem)));
            context.arg(ContextKeys.ROD, rodID);
            BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(rodID, MechanicType.ROD).ifPresent(fishingGears.modifiers::add);

            // set enchantments
            List<Pair<String, Short>> enchants = BukkitCustomFishingPlugin.getInstance().getIntegrationManager().getEnchantments(rodOnMainHand ? mainHandItem : offHandItem);
            for (Pair<String, Short> enchantment : enchants) {
                String effectID = enchantment.left() + ":" + enchantment.right();
                BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(effectID, MechanicType.ENCHANT).ifPresent(fishingGears.modifiers::add);
            }

            // set bait if it is
            boolean hasBait = false;
            String anotherItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getItemID(rodOnMainHand ? offHandItem : mainHandItem);
            MechanicType type = MechanicType.getTypeByID(anotherItemID);
            if (type == MechanicType.BAIT) {
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
                        MechanicType bagItemType = MechanicType.getTypeByID(bagItemID);
                        if (!hasBait && bagItemType == MechanicType.BAIT) {
                            fishingGears.gears.put(GearType.BAIT, List.of(Pair.of(bagItemID, itemInBag)));
                            context.arg(ContextKeys.BAIT, bagItemID);
                            BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(bagItemID, MechanicType.BAIT).ifPresent(fishingGears.modifiers::add);
                            hasBait = true;
                        }
                        if (bagItemType == MechanicType.UTIL) {
                            uniqueUtils.put(bagItemID, itemInBag);
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
                        BukkitCustomFishingPlugin.getInstance().getItemManager().decreaseDurability(itemStack, 1, false);
                }),
                ((context, itemStack) -> {
                    if (context.getHolder().getGameMode() != GameMode.CREATIVE)
                        BukkitCustomFishingPlugin.getInstance().getItemManager().decreaseDurability(itemStack, 1, false);
                }),
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
                ((context, itemStack) -> {})
        );

        public static final GearType HOOK = new GearType(MechanicType.HOOK,
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
                ((context, itemStack) -> {}),
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

        public GearType(MechanicType type,
                        BiConsumer<Context<Player>, ItemStack> castFunction, BiConsumer<Context<Player>, ItemStack> reelFunction,
                        BiConsumer<Context<Player>, ItemStack> biteFunction, BiConsumer<Context<Player>, ItemStack> successFunction,
                        BiConsumer<Context<Player>, ItemStack> failureFunction, BiConsumer<Context<Player>, ItemStack> lureFunction,
                        BiConsumer<Context<Player>, ItemStack> escapeFunction, BiConsumer<Context<Player>, ItemStack> landFunction
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
