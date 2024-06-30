package net.momirealms.customfishing.api.mechanic.fishing;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.api.mechanic.requirement.Requirement;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.storage.user.UserData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class FishingGears {

    private static BiConsumer<Context<Player>, FishingGears> fishingGearsConsumers = defaultFishingGearsConsumers();
    private final HashMap<GearType, Collection<ItemStack>> gears = new HashMap<>();
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

    public void cast() {
        for (Map.Entry<GearType, Collection<ItemStack>> entry : gears.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                entry.getKey().castFunction.accept(itemStack);
            }
        }
    }

    public void reel() {
        for (Map.Entry<GearType, Collection<ItemStack>> entry : gears.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                entry.getKey().reelFunction.accept(itemStack);
            }
        }
    }

    public void succeed() {
        for (Map.Entry<GearType, Collection<ItemStack>> entry : gears.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                entry.getKey().successFunction.accept(itemStack);
            }
        }
    }

    public void fail() {
        for (Map.Entry<GearType, Collection<ItemStack>> entry : gears.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                entry.getKey().failureFunction.accept(itemStack);
            }
        }
    }

    public void bite() {
        for (Map.Entry<GearType, Collection<ItemStack>> entry : gears.entrySet()) {
            for (ItemStack itemStack : entry.getValue()) {
                entry.getKey().biteFunction.accept(itemStack);
            }
        }
    }

    @NotNull
    public List<EffectModifier> effectModifiers() {
        return modifiers;
    }

    @NotNull
    public Collection<ItemStack> getItem(GearType type) {
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
            fishingGears.gears.put(GearType.ROD, List.of(rodOnMainHand ? mainHandItem : offHandItem));
            context.arg(ContextKeys.ROD, rodID);
            BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(rodID, MechanicType.ROD).ifPresent(fishingGears.modifiers::add);

            // set bait if it is
            boolean hasBait = false;
            String anotherItemID = BukkitCustomFishingPlugin.getInstance().getItemManager().getItemID(rodOnMainHand ? offHandItem : mainHandItem);
            MechanicType type = MechanicType.getTypeByID(anotherItemID);
            if (type == MechanicType.BAIT) {
                fishingGears.gears.put(GearType.BAIT, List.of(rodOnMainHand ? offHandItem : mainHandItem));
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
                            fishingGears.gears.put(GearType.BAIT, List.of(itemInBag));
                            context.arg(ContextKeys.BAIT, bagItemID);
                            BukkitCustomFishingPlugin.getInstance().getEffectManager().getEffectModifier(bagItemID, MechanicType.BAIT).ifPresent(fishingGears.modifiers::add);
                            hasBait = true;
                        }
                        if (bagItemType == MechanicType.UTIL) {
                            uniqueUtils.put(bagItemID, itemInBag);
                        }
                    }
                    if (!uniqueUtils.isEmpty()) {
                        ArrayList<ItemStack> utils = new ArrayList<>();
                        for (Map.Entry<String, ItemStack> entry : uniqueUtils.entrySet()) {
                            utils.add(entry.getValue());
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
        };
    }

    public static class GearType {

        public static final GearType ROD = new GearType("rod",
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> BukkitCustomFishingPlugin.getInstance().getItemManager().decreaseDurability(itemStack, 1, false)),
                (itemStack -> {}));

        public static final GearType BAIT = new GearType("bait",
                (itemStack -> itemStack.setAmount(itemStack.getAmount() - 1)),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}));

        public static final GearType HOOK = new GearType("hook",
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}));

        public static final GearType UTIL = new GearType("util",
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}),
                (itemStack -> {}));

        private final String type;
        private Consumer<ItemStack> castFunction;
        private Consumer<ItemStack> reelFunction;
        private Consumer<ItemStack> biteFunction;
        private Consumer<ItemStack> successFunction;
        private Consumer<ItemStack> failureFunction;

        public GearType(String type, Consumer<ItemStack> castFunction, Consumer<ItemStack> reelFunction, Consumer<ItemStack> biteFunction, Consumer<ItemStack> successFunction, Consumer<ItemStack> failureFunction) {
            this.type = type;
            this.castFunction = castFunction;
            this.reelFunction = reelFunction;
            this.biteFunction = biteFunction;
            this.successFunction = successFunction;
            this.failureFunction = failureFunction;
        }

        public void castFunction(Consumer<ItemStack> castFunction) {
            this.castFunction = castFunction;
        }

        public void reelFunction(Consumer<ItemStack> reelFunction) {
            this.reelFunction = reelFunction;
        }

        public void biteFunction(Consumer<ItemStack> biteFunction) {
            this.biteFunction = biteFunction;
        }

        public void successFunction(Consumer<ItemStack> successFunction) {
            this.successFunction = successFunction;
        }

        public void failureFunction(Consumer<ItemStack> failureFunction) {
            this.failureFunction = failureFunction;
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
            return type;
        }
    }
}
