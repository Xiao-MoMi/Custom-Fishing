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

package net.momirealms.customfishing.mechanic.item;

import de.tr7zw.changeme.nbtapi.*;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.common.Tuple;
import net.momirealms.customfishing.api.event.FishingBagPreCollectEvent;
import net.momirealms.customfishing.api.event.FishingLootPreSpawnEvent;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import net.momirealms.customfishing.api.manager.ActionManager;
import net.momirealms.customfishing.api.manager.ItemManager;
import net.momirealms.customfishing.api.manager.RequirementManager;
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
import net.momirealms.customfishing.api.mechanic.condition.Condition;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.mechanic.misc.Value;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.api.util.WeightUtils;
import net.momirealms.customfishing.compatibility.item.CustomFishingItemImpl;
import net.momirealms.customfishing.compatibility.item.VanillaItemImpl;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.util.ConfigUtils;
import net.momirealms.customfishing.util.ItemUtils;
import net.momirealms.customfishing.util.LocationUtils;
import net.momirealms.customfishing.util.NBTUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class ItemManagerImpl implements ItemManager, Listener {

    private static ItemManager instance;
    private final CustomFishingPlugin plugin;
    private final HashMap<Key, BuildableItem> buildableItemMap;
    private final HashMap<String, ItemLibrary> itemLibraryMap;
    private ItemLibrary[] itemDetectionArray;
    private NamespacedKey blockKey;

    public ItemManagerImpl(CustomFishingPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.itemLibraryMap = new LinkedHashMap<>();
        this.buildableItemMap = new HashMap<>();
        this.blockKey = NamespacedKey.fromString("block", plugin);
        this.registerItemLibrary(new CustomFishingItemImpl());
        this.registerItemLibrary(new VanillaItemImpl());
    }

    public void load() {
        this.loadItemsFromPluginFolder();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.resetItemDetectionOrder();
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        HashMap<Key, BuildableItem> tempMap = new HashMap<>(this.buildableItemMap);
        this.buildableItemMap.clear();
        for (Map.Entry<Key, BuildableItem> entry : tempMap.entrySet()) {
            if (entry.getValue().persist()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    private void resetItemDetectionOrder() {
        ArrayList<ItemLibrary> list = new ArrayList<>();
        for (String plugin : CFConfig.itemDetectOrder) {
            ItemLibrary library = itemLibraryMap.get(plugin);
            if (library != null) {
                list.add(library);
            }
        }
        this.itemDetectionArray = list.toArray(new ItemLibrary[0]);
    }

    public Collection<String> getItemLibraries() {
        return itemLibraryMap.keySet();
    }

    /**
     * Get a set of all item keys in the CustomFishing plugin.
     *
     * @return A set of item keys.
     */
    @Override
    public Set<Key> getAllItemsKey() {
        return buildableItemMap.keySet();
    }

    public void disable() {
        HandlerList.unregisterAll(this);
        this.buildableItemMap.clear();
        this.itemLibraryMap.clear();
    }

    /**
     * Loads items from the plugin folder.
     * This method scans various item types (item, bait, rod, util, hook) in the plugin's content folder and loads their configurations.
     */
    @SuppressWarnings("DuplicatedCode")
    public void loadItemsFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("item", "bait", "rod", "util", "hook")) {
            File typeFolder = new File(plugin.getDataFolder() + File.separator + "contents" + File.separator + type);
            if (!typeFolder.exists()) {
                if (!typeFolder.mkdirs()) return;
                plugin.saveResource("contents" + File.separator + type + File.separator + "default.yml", false);
            }
            fileDeque.push(typeFolder);
            while (!fileDeque.isEmpty()) {
                File file = fileDeque.pop();
                File[] files = file.listFiles();
                if (files == null) continue;
                for (File subFile : files) {
                    if (subFile.isDirectory()) {
                        fileDeque.push(subFile);
                    } else if (subFile.isFile() && subFile.getName().endsWith(".yml")) {
                        this.loadSingleFile(subFile, type);
                    }
                }
            }
        }
    }

    /**
     * Loads a single item configuration file.
     *
     * @param file      The YAML configuration file to load.
     * @param namespace The namespace of the item type (item, bait, rod, util, hook).
     */
    private void loadSingleFile(File file, String namespace) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : yaml.getValues(false).entrySet()) {
            String value = entry.getKey();
            if (entry.getValue() instanceof ConfigurationSection section) {
                Key key = Key.of(namespace, value);
                if (buildableItemMap.containsKey(key)) {
                    LogUtils.severe("Duplicated item key found: " + key + ".");
                } else {
                    buildableItemMap.put(key, getItemBuilder(section, namespace, value));
                }
            }
        }
    }

    /**
     * Build an ItemStack with a specified namespace and value for a player.
     *
     * @param player   The player for whom the ItemStack is being built.
     * @param namespace The namespace of the item.
     * @param value    The value of the item.
     * @return The constructed ItemStack.
     */
    @Override
    public ItemStack build(Player player, String namespace, String value) {
        return build(player, namespace, value, new HashMap<>());
    }

    /**
     * Build an ItemStack with a specified namespace and value, replacing placeholders,
     * for a player.
     *
     * @param player      The player for whom the ItemStack is being built.
     * @param namespace   The namespace of the item.
     * @param value       The value of the item.
     * @param placeholders The placeholders to replace in the item's attributes.
     * @return The constructed ItemStack, or null if the item doesn't exist.
     */
    @Override
    public ItemStack build(Player player, String namespace, String value, Map<String, String> placeholders) {
        BuildableItem buildableItem = buildableItemMap.get(Key.of(namespace, value));
        if (buildableItem == null) return null;
        return buildableItem.build(player, placeholders);
    }

    /**
     * Build an ItemStack using an ItemBuilder for a player.
     *
     * @param player      The player for whom the ItemStack is being built.
     * @param builder     The ItemBuilder used to construct the ItemStack.
     * @return The constructed ItemStack.
     */
    @NotNull
    @Override
    public ItemStack build(Player player, ItemBuilder builder) {
        return build(player, builder, new HashMap<>());
    }

    /**
     * Retrieve a BuildableItem by its namespace and value.
     *
     * @param namespace The namespace of the BuildableItem.
     * @param value     The value of the BuildableItem.
     * @return The BuildableItem with the specified namespace and value, or null if not found.
     */
    @Override
    @Nullable
    public BuildableItem getBuildableItem(String namespace, String value) {
        return buildableItemMap.get(Key.of(namespace, value));
    }

    /**
     * Get the item ID associated with the given ItemStack by checking all available item libraries.
     * The detection order is determined by the configuration.
     *
     * @param itemStack The ItemStack to retrieve the item ID from.
     * @return The item ID or "AIR" if not found or if the ItemStack is null or empty.
     */
    @NotNull
    @Override
    public String getAnyPluginItemID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR)
            return "AIR";
        for (ItemLibrary library : itemDetectionArray) {
            String id = library.getItemID(itemStack);
            if (id != null) {
                return id;
            }
        }
        // should not reach this because vanilla library would always work
        return "AIR";
    }

    /**
     * Build an ItemStack for a player based on the provided item ID.
     *
     * @param player The player for whom the ItemStack is being built.
     * @param id     The item ID, which may include a namespace (e.g., "namespace:id").
     * @return The constructed ItemStack or null if the ID is not valid.
     */
    @Override
    public ItemStack buildAnyPluginItemByID(Player player, String id) {
        if (id.contains(":")) {
            String[] split = id.split(":", 2);
            return itemLibraryMap.get(split[0]).buildItem(player, split[1]);
        } else {
            try {
                return new ItemStack(Material.valueOf(id.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                return new ItemStack(Material.COD);
            }
        }
    }

    /**
     * Checks if the provided ItemStack is a custom fishing item
     *
     * @param itemStack The ItemStack to check.
     * @return True if the ItemStack is a custom fishing item; otherwise, false.
     */
    @Override
    public boolean isCustomFishingItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return false;
        NBTItem nbtItem = new NBTItem(itemStack);
        return nbtItem.hasTag("CustomFishing") && !nbtItem.getCompound("CustomFishing").getString("id").equals("");
    }

    /**
     * Get the item ID associated with the given ItemStack, if available.
     *
     * @param itemStack The ItemStack to retrieve the item ID from.
     * @return The item ID or null if not found or if the ItemStack is null or empty.
     */
    @Nullable
    @Override
    public String getCustomFishingItemID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound == null) return null;
        return cfCompound.getString("id");
    }

    /**
     * Create a CFBuilder instance for an item configuration section
     *
     * @param section The configuration section containing item settings.
     * @param type The type of the item (e.g., "rod", "bait").
     * @param id The unique identifier for the item.
     * @return A CFBuilder instance representing the configured item, or null if the section is null.
     */
    @Nullable
    @Override
    public CFBuilder getItemBuilder(ConfigurationSection section, String type, String id) {
        if (section == null) return null;
        String material = section.getString("material", type.equals("rod") ? "FISHING_ROD" : "PAPER");
        CFBuilder itemCFBuilder;
        if (material.contains(":")) {
            String[] split = material.split(":", 2);
            itemCFBuilder = CFBuilder.of(split[0], split[1]);
        } else {
            itemCFBuilder = CFBuilder.of("vanilla", material.toUpperCase(Locale.ENGLISH));
        }
        itemCFBuilder
                .stackable(section.getBoolean("stackable", true))
                .size(ConfigUtils.getFloatPair(section.getString("size")))
                .price((float) section.getDouble("price.base"), (float) section.getDouble("price.bonus"))
                .customModelData(section.getInt("custom-model-data"))
                .nbt(section.getConfigurationSection("nbt"))
                .maxDurability(section.getInt("max-durability"))
                .itemFlag(section.getStringList("item-flags").stream().map(flag -> ItemFlag.valueOf(flag.toUpperCase())).toList())
                .enchantment(ConfigUtils.getEnchantmentPair(section.getConfigurationSection("enchantments")), false)
                .enchantment(ConfigUtils.getEnchantmentPair(section.getConfigurationSection("stored-enchantments")), true)
                .enchantmentPool(ConfigUtils.getEnchantAmountPair(section.getConfigurationSection("enchantment-pool.amount")), ConfigUtils.getEnchantPoolPair(section.getConfigurationSection("enchantment-pool.pool")), false)
                .enchantmentPool(ConfigUtils.getEnchantAmountPair(section.getConfigurationSection("stored-enchantment-pool.amount")), ConfigUtils.getEnchantPoolPair(section.getConfigurationSection("stored-enchantment-pool.pool")), true)
                .randomEnchantments(ConfigUtils.getEnchantmentTuple(section.getConfigurationSection("random-enchantments")), false)
                .randomEnchantments(ConfigUtils.getEnchantmentTuple(section.getConfigurationSection("random-stored-enchantments")), true)
                .tag(section.getBoolean("tag", true), type, id)
                .randomDamage(section.getBoolean("random-durability", false))
                .unbreakable(section.getBoolean("unbreakable", false))
                .preventGrabbing(section.getBoolean("prevent-grabbing", true))
                .placeable(section.getBoolean("placeable", false))
                .head(section.getString("head64"))
                .name(section.getString("display.name"))
                .lore(section.getStringList("display.lore"));
        if (section.get("amount") instanceof String s) {
            Pair<Integer, Integer> pair = ConfigUtils.getIntegerPair(s);
            itemCFBuilder.amount(pair.left(), pair.right());
        } else {
            itemCFBuilder.amount(section.getInt("amount", 1));
        }
        return itemCFBuilder;
    }

    /**
     * Build an ItemStack using the provided ItemBuilder, player, and placeholders.
     *
     * @param player       The player for whom the item is being built.
     * @param builder      The ItemBuilder that defines the item's properties.
     * @param placeholders A map of placeholders and their corresponding values to be applied to the item.
     * @return The constructed ItemStack.
     */
    @Override
    @NotNull
    public ItemStack build(Player player, ItemBuilder builder, Map<String, String> placeholders) {
        ItemStack temp = itemLibraryMap.get(builder.getLibrary()).buildItem(player, builder.getId());
        if (temp.getType() == Material.AIR) {
            return temp;
        }
        int amount = builder.getAmount();
        temp.setAmount(amount);
        NBTItem nbtItem = new NBTItem(temp);
        for (ItemBuilder.ItemPropertyEditor editor : builder.getEditors()) {
            editor.edit(player, nbtItem, placeholders);
        }
        ItemUtils.updateNBTItemLore(nbtItem);
        return nbtItem.getItem();
    }

    @Override
    public ItemStack getItemStackAppearance(Player player, String material) {
        if (material != null) {
            ItemStack itemStack = buildAnyPluginItemByID(player, material);
            if (itemStack != null) {
                NBTItem nbtItem = new NBTItem(itemStack);
                nbtItem.removeKey("display");
                return nbtItem.getItem();
            } else {
                return new ItemStack(Material.BARRIER);
            }
        } else {
            return new ItemStack(Material.STRUCTURE_VOID);
        }
    }

    /**
     * Register an item library.
     *
     * @param itemLibrary The item library to register.
     * @return True if the item library was successfully registered, false if it already exists.
     */
    @Override
    public boolean registerItemLibrary(ItemLibrary itemLibrary) {
        if (itemLibraryMap.containsKey(itemLibrary.identification())) return false;
        itemLibraryMap.put(itemLibrary.identification(), itemLibrary);
        this.resetItemDetectionOrder();
        return true;
    }

    /**
     * Unregister an item library.
     *
     * @param identification The item library to unregister.
     * @return True if the item library was successfully unregistered, false if it doesn't exist.
     */
    @Override
    public boolean unRegisterItemLibrary(String identification) {
        boolean success = itemLibraryMap.remove(identification) != null;
        if (success)
            this.resetItemDetectionOrder();
        return success;
    }

    @Override
    public void dropItem(Player player, Location hookLocation, Location playerLocation, ItemStack item, Condition condition) {
        if (item.getType() == Material.AIR) {
            return;
        }

        if (CFConfig.enableFishingBag && plugin.getBagManager().doesBagStoreLoots() && RequirementManager.isRequirementMet(condition, plugin.getBagManager().getCollectRequirements())) {
            var bag = plugin.getBagManager().getOnlineBagInventory(player.getUniqueId());

            FishingBagPreCollectEvent preCollectEvent = new FishingBagPreCollectEvent(player, item, bag);
            Bukkit.getPluginManager().callEvent(preCollectEvent);
            if (preCollectEvent.isCancelled()) {
                return;
            }

            int cannotPut = ItemUtils.putLootsToBag(bag, item, item.getAmount());
            // some are put into bag
            if (cannotPut != item.getAmount()) {
                ActionManager.triggerActions(condition, plugin.getBagManager().getCollectLootActions());
            }
            // all are put
            if (cannotPut == 0) {
                return;
            }
            // bag is full
            item.setAmount(cannotPut);
            ActionManager.triggerActions(condition, plugin.getBagManager().getBagFullActions());
        }

        FishingLootPreSpawnEvent preSpawnEvent = new FishingLootPreSpawnEvent(player, hookLocation, item);
        Bukkit.getPluginManager().callEvent(preSpawnEvent);
        if (preSpawnEvent.isCancelled()) {
            return;
        }

        Item itemEntity = hookLocation.getWorld().dropItem(hookLocation, item);
        FishingLootSpawnEvent spawnEvent = new FishingLootSpawnEvent(player, hookLocation, itemEntity);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled()) {
            itemEntity.remove();
            return;
        }

        itemEntity.setInvulnerable(true);
        plugin.getScheduler().runTaskAsyncLater(() -> {
            if (itemEntity.isValid()) {
                itemEntity.setInvulnerable(false);
            }
        }, 1, TimeUnit.SECONDS);

        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply(0.105);
        vector = vector.setY((vector.getY() + 0.22) * 1.18);
        itemEntity.setVelocity(vector);
    }

    /**
     * Decreases the durability of an ItemStack by a specified amount and optionally updates its lore.
     *
     * @param player      Player
     * @param itemStack   The ItemStack to modify.
     * @param amount      The amount by which to decrease the durability.
     * @param updateLore  Whether to update the lore of the ItemStack.
     */
    @Override
    public void decreaseDurability(Player player, ItemStack itemStack, int amount, boolean updateLore) {
        ItemUtils.decreaseDurability(player, itemStack, amount, updateLore);
    }

    /**
     * Increases the durability of an ItemStack by a specified amount and optionally updates its lore.
     *
     * @param itemStack   The ItemStack to modify.
     * @param amount      The amount by which to increase the durability.
     * @param updateLore  Whether to update the lore of the ItemStack.
     */
    @Override
    public void increaseDurability(ItemStack itemStack, int amount, boolean updateLore) {
        ItemUtils.increaseDurability(itemStack, amount, updateLore);
    }

    /**
     * Sets the durability of an ItemStack to a specific amount and optionally updates its lore.
     *
     * @param itemStack   The ItemStack to modify.
     * @param amount      The new durability value.
     * @param updateLore  Whether to update the lore of the ItemStack.
     */
    @Override
    public void setDurability(ItemStack itemStack, int amount, boolean updateLore) {
        ItemUtils.setDurability(itemStack, amount, updateLore);
    }

    public static class CFBuilder implements ItemBuilder {

        private final String library;
        private final String id;
        private int min_amount;
        private int max_amount;
        private final LinkedHashMap<String, ItemPropertyEditor> editors;

        public CFBuilder(String library, String id) {
            this.id = id;
            this.library = library;
            this.editors = new LinkedHashMap<>();
            this.min_amount = (max_amount = 1);
        }

        public static CFBuilder of(String library, String id) {
            return new CFBuilder(library, id);
        }

        @Override
        public ItemStack build(Player player, Map<String, String> placeholders) {
            return ItemManagerImpl.instance.build(player, this, placeholders);
        }

        @Override
        public boolean persist() {
            return false;
        }

        @Override
        public ItemBuilder customModelData(int value) {
            if (value == 0) return this;
            editors.put("custom-model-data", (player, nbtItem, placeholders) -> nbtItem.setInteger("CustomModelData", value));
            return this;
        }

        @Override
        public ItemBuilder name(String name) {
            if (name == null) return this;
            editors.put("name", (player, nbtItem, placeholders) -> {
                NBTCompound displayCompound = nbtItem.getOrCreateCompound("display");
                displayCompound.setString("Name", AdventureManagerImpl.getInstance().componentToJson(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<!i>" + PlaceholderManagerImpl.getInstance().parse(player, name, placeholders)
                        )
                ));
            });
            return this;
        }

        @Override
        public ItemBuilder amount(int amount) {
            this.min_amount = amount;
            this.max_amount = amount;
            return this;
        }

        @Override
        public ItemBuilder amount(int min_amount, int max_amount) {
            this.min_amount = min_amount;
            this.max_amount = max_amount;
            return this;
        }

        @Override
        public ItemBuilder tag(boolean tag, String type, String id) {
            editors.put("tag", (player, nbtItem, placeholders) -> {
                if (!tag) return;
                NBTCompound cfCompound = nbtItem.getOrCreateCompound("CustomFishing");
                cfCompound.setString("type", type);
                cfCompound.setString("id", id);
            });
            return this;
        }

        @Override
        public ItemBuilder unbreakable(boolean unbreakable) {
            editors.put("unbreakable", (player, nbtItem, placeholders) -> {
                if (!unbreakable) return;
                nbtItem.setByte("Unbreakable", (byte) 1);
            });
            return this;
        }

        @Override
        public ItemBuilder placeable(boolean placeable) {
            editors.put("placeable", (player, nbtItem, placeholders) -> {
                if (!placeable) return;
                NBTCompound cfCompound = nbtItem.getOrCreateCompound("CustomFishing");
                cfCompound.setByte("placeable", (byte) 1);
            });
            return this;
        }

        @Override
        public ItemBuilder lore(List<String> lore) {
            if (lore.size() == 0) return this;
            editors.put("lore", (player, nbtItem, placeholders) -> {
                NBTCompound displayCompound = nbtItem.getOrCreateCompound("display");
                NBTList<String> list = displayCompound.getStringList("Lore");
                list.clear();
                list.addAll(lore.stream().map(s -> AdventureManagerImpl.getInstance().componentToJson(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<!i>" + PlaceholderManagerImpl.getInstance().parse(player, s, placeholders)
                        )
                )).toList());
            });
            return this;
        }

        @Override
        public ItemBuilder nbt(Map<String, Object> nbt) {
            if (nbt.size() == 0) return this;
            editors.put("nbt", (player, nbtItem, placeholders) -> NBTUtils.setTagsFromBukkitYAML(player, placeholders, nbtItem, nbt));
            return this;
        }

        @Override
        public ItemBuilder nbt(ConfigurationSection section) {
            if (section == null) return this;
            editors.put("nbt", (player, nbtItem, placeholders) -> NBTUtils.setTagsFromBukkitYAML(player, placeholders, nbtItem, section.getValues(false)));
            return this;
        }

        @Override
        public ItemBuilder itemFlag(List<ItemFlag> itemFlags) {
            if (itemFlags.size() == 0) return this;
            editors.put("item-flag", (player, nbtItem, placeholders) -> {
                int flag = 0;
                for (ItemFlag itemFlag : itemFlags) {
                    flag = flag | 1 << itemFlag.ordinal();
                }
                nbtItem.setInteger("HideFlags", flag);
            });
            return this;
        }

        @Override
        public ItemBuilder enchantment(List<Pair<String, Short>> enchantments, boolean store) {
            if (enchantments.size() == 0) return this;
            editors.put("enchantment", (player, nbtItem, placeholders) -> {
                NBTCompoundList list = nbtItem.getCompoundList(store ? "StoredEnchantments" : "Enchantments");
                for (Pair<String, Short> pair : enchantments) {
                    NBTCompound nbtCompound = list.addCompound();
                    nbtCompound.setString("id", pair.left());
                    nbtCompound.setShort("lvl", pair.right());
                }
            });
            return this;
        }

        @Override
        public ItemBuilder randomEnchantments(List<Tuple<Double, String, Short>> enchantments, boolean store) {
            if (enchantments.size() == 0) return this;
            editors.put("random-enchantment", (player, nbtItem, placeholders) -> {
                NBTCompoundList list = nbtItem.getCompoundList(store ? "StoredEnchantments" : "Enchantments");
                HashSet<String> ids = new HashSet<>();
                for (Tuple<Double, String, Short> pair : enchantments) {
                    if (Math.random() < pair.getLeft() && !ids.contains(pair.getMid())) {
                        NBTCompound nbtCompound = list.addCompound();
                        nbtCompound.setString("id", pair.getMid());
                        nbtCompound.setShort("lvl", pair.getRight());
                        ids.add(pair.getMid());
                    }
                }
            });
            return this;
        }

        @Override
        public ItemBuilder enchantmentPool(List<Pair<Integer, Value>> amountPairs, List<Pair<Pair<String, Short>, Value>> enchantments, boolean store) {
            if (enchantments.size() == 0 || amountPairs.size() == 0) return this;
            editors.put("enchantment-pool", (player, nbtItem, placeholders) -> {
                List<Pair<Integer, Double>> parsedAmountPair = new ArrayList<>(amountPairs.size());
                Map<String, String> map = new HashMap<>();
                for (Pair<Integer, Value> rawValue : amountPairs) {
                    parsedAmountPair.add(Pair.of(rawValue.left(), rawValue.right().get(player, map)));
                }

                int amount = WeightUtils.getRandom(parsedAmountPair);
                if (amount <= 0) return;
                NBTCompoundList list = nbtItem.getCompoundList(store ? "StoredEnchantments" : "Enchantments");

                HashSet<Enchantment> addedEnchantments = new HashSet<>();

                List<Pair<Pair<String, Short>, Double>> cloned = new ArrayList<>(enchantments.size());
                for (Pair<Pair<String, Short>, Value> rawValue : enchantments) {
                    cloned.add(Pair.of(rawValue.left(), rawValue.right().get(player, map)));
                }

                int i = 0;
                outer:
                while (i < amount && cloned.size() != 0) {
                    Pair<String, Short> enchantPair = WeightUtils.getRandom(cloned);
                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.fromString(enchantPair.left()));
                    if (enchantment == null) {
                        throw new NullPointerException("Enchantment: " + enchantPair.left() + " doesn't exist on your server.");
                    }
                    for (Enchantment added : addedEnchantments) {
                        if (enchantment.conflictsWith(added)) {
                            cloned.removeIf(pair -> pair.left().left().equals(enchantPair.left()));
                            continue outer;
                        }
                    }
                    NBTCompound nbtCompound = list.addCompound();
                    nbtCompound.setString("id", enchantPair.left());
                    nbtCompound.setShort("lvl", enchantPair.right());
                    addedEnchantments.add(enchantment);
                    cloned.removeIf(pair -> pair.left().left().equals(enchantPair.left()));
                    i++;
                }
            });
            return this;
        }

        @Override
        public ItemBuilder maxDurability(int max) {
            if (max == 0) return this;
            editors.put("durability", (player, nbtItem, placeholders) -> {
                NBTCompound cfCompound = nbtItem.getOrCreateCompound("CustomFishing");
                cfCompound.setInteger("max_dur", max);
                cfCompound.setInteger("cur_dur", max);
            });
            return this;
        }

        @Override
        public ItemBuilder price(float base, float bonus) {
            if (base == 0 && bonus == 0) return this;
            editors.put("price", (player, nbtItem, placeholders) -> {
                placeholders.put("{base}", String.format("%.2f", base));
                placeholders.put("{BASE}", String.valueOf(base));
                placeholders.put("{bonus}", String.format("%.2f", bonus));
                placeholders.put("{BONUS}", String.valueOf(bonus));
                double price;
                price = CustomFishingPlugin.get().getMarketManager().getFishPrice(
                        player,
                        placeholders
                );
                nbtItem.setDouble("Price", price);
                placeholders.put("{price}", String.format("%.2f", price));
                placeholders.put("{PRICE}", String.valueOf(price));
            });
            return this;
        }

        @Override
        public ItemBuilder size(Pair<Float, Float> size) {
            if (size == null) return this;
            editors.put("size", (player, nbtItem, placeholders) -> {
                NBTCompound cfCompound = nbtItem.getOrCreateCompound("CustomFishing");
                float random = size.left() + (size.left() >= size.right() ? 0 : ThreadLocalRandom.current().nextFloat(size.right() - size.left()));
                float bonus = Float.parseFloat(placeholders.getOrDefault("{size-multiplier}", "1.0"));
                double fixed = Double.parseDouble(placeholders.getOrDefault("{size-fixed}", "0.0"));
                random *= bonus;
                random += fixed;
                if (CFConfig.restrictedSizeRange) {
                    if (random > size.right()) {
                        random = size.right();
                    } else if (random < size.left()) {
                        random = size.left();
                    }
                }
                cfCompound.setFloat("size", random);
                placeholders.put("{size}", String.format("%.2f", random));
                placeholders.put("{SIZE}", String.valueOf(random));
                placeholders.put("{min_size}", String.valueOf(size.left()));
                placeholders.put("{max_size}", String.valueOf(size.right()));
            });
            return this;
        }

        @Override
        public ItemBuilder stackable(boolean stackable) {
            if (stackable) return this;
            editors.put("stackable", (player, nbtItem, placeholders) -> {
                NBTCompound cfCompound = nbtItem.getOrCreateCompound("CustomFishing");
                cfCompound.setUUID("uuid", UUID.randomUUID());
            });
            return this;
        }

        @Override
        public ItemBuilder preventGrabbing(boolean prevent) {
            if (!prevent) return this;
            editors.put("grabbing", (player, nbtItem, placeholders) -> {
                nbtItem.setString("owner", placeholders.get("player"));
            });
            return this;
        }

        @Override
        public ItemBuilder head(String base64) {
            if (base64 == null) return this;
            editors.put("head", (player, nbtItem, placeholders) -> {
                NBTCompound nbtCompound = nbtItem.addCompound("SkullOwner");
                nbtCompound.setUUID("Id", UUID.nameUUIDFromBytes(id.getBytes()));
                NBTListCompound texture = nbtCompound.addCompound("Properties").getCompoundList("textures").addCompound();
                texture.setString("Value", base64);
            });
            return this;
        }

        @Override
        public ItemBuilder randomDamage(boolean damage) {
            if (!damage) return this;
            editors.put("damage", (player, nbtItem, placeholders) -> {
                NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
                if (cfCompound != null) {
                    int i = cfCompound.getInteger("max_dur");
                    if (i != 0) {
                        int dur = ThreadLocalRandom.current().nextInt(i);
                        cfCompound.setInteger("cur_dur", dur);
                        nbtItem.setInteger("Damage", (int) (nbtItem.getItem().getType().getMaxDurability() * ((double) dur / i)));
                    } else {
                        nbtItem.setInteger("Damage", ThreadLocalRandom.current().nextInt(nbtItem.getItem().getType().getMaxDurability()));
                    }
                } else {
                    nbtItem.setInteger("Damage", ThreadLocalRandom.current().nextInt(nbtItem.getItem().getType().getMaxDurability()));
                }
            });
            return this;
        }

        @Override
        public @NotNull String getId() {
            return id;
        }

        @Override
        public @NotNull String getLibrary() {
            return library;
        }

        @Override
        public int getAmount() {
            return ThreadLocalRandom.current().nextInt(min_amount, max_amount + 1);
        }

        @Override
        public Collection<ItemPropertyEditor> getEditors() {
            return editors.values();
        }

        @Override
        public ItemBuilder removeEditor(String type) {
            editors.remove(type);
            return this;
        }

        @Override
        public ItemBuilder registerCustomEditor(String type, ItemPropertyEditor editor) {
            editors.put(type, editor);
            return this;
        }
    }

    /**
     * Handles item pickup by players.
     *
     * @param event The PlayerAttemptPickupItemEvent.
     */
    @EventHandler (ignoreCancelled = true)
    public void onPickUp(PlayerAttemptPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("owner")) return;
        if (!Objects.equals(nbtItem.getString("owner"), event.getPlayer().getName())) {
            event.setCancelled(true);
        } else {
            nbtItem.removeKey("owner");
            itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
        }
    }

    /**
     * Handles item movement in inventories.
     *
     * @param event The InventoryPickupItemEvent.
     */
    @EventHandler (ignoreCancelled = true)
    public void onInvPickItem(InventoryPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        NBTItem nbtItem = new NBTItem(itemStack);
        if (!nbtItem.hasTag("owner")) return;
        nbtItem.removeKey("owner");
        itemStack.setItemMeta(nbtItem.getItem().getItemMeta());
    }

    /**
     * Handles item consumption by players.
     *
     * @param event The PlayerItemConsumeEvent.
     */
    @EventHandler (ignoreCancelled = true)
    public void onConsumeItem(PlayerItemConsumeEvent event) {
        ItemStack itemStack = event.getItem();
        String id = getAnyPluginItemID(itemStack);
        Loot loot = plugin.getLootManager().getLoot(id);
        if (loot != null) {
            Condition condition = new Condition(event.getPlayer());
            if (!loot.disableGlobalAction())
                GlobalSettings.triggerLootActions(ActionTrigger.CONSUME, condition);
            loot.triggerActions(ActionTrigger.CONSUME, condition);
        }
    }

    /**
     * Handles the repair of custom items in an anvil.
     *
     * @param event The PrepareAnvilEvent.
     */
    @EventHandler (ignoreCancelled = true)
    public void onRepairItem(PrepareAnvilEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() == Material.AIR) return;
        NBTItem nbtItem = new NBTItem(result);
        NBTCompound compound = nbtItem.getCompound("CustomFishing");
        if (compound == null || !compound.hasTag("max_dur")) return;
        if (!(result.getItemMeta() instanceof Damageable damageable)) {
            return;
        }
        int max_dur = compound.getInteger("max_dur");
        compound.setInteger("cur_dur", (int) (max_dur * (1 - (double) damageable.getDamage() / result.getType().getMaxDurability())));
        event.setResult(nbtItem.getItem());
    }

    /**
     * Handles the mending of custom items.
     *
     * @param event The PlayerItemMendEvent.
     */
    @EventHandler (ignoreCancelled = true)
    public void onMending(PlayerItemMendEvent event) {
        ItemStack itemStack = event.getItem();
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound compound = nbtItem.getCompound("CustomFishing");
        if (compound == null) return;
        event.setCancelled(true);
        ItemUtils.increaseDurability(itemStack, event.getRepairAmount(), true);
    }

    /**
     * Handles interactions with custom utility items.
     *
     * @param event The PlayerInteractEvent.
     */
    @EventHandler
    public void onInteractWithItems(PlayerInteractEvent event) {
        if (event.useItemInHand() == Event.Result.DENY)
            return;
        if (event.getHand() != EquipmentSlot.HAND)
            return;
        ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
        if (itemStack.getType() == Material.AIR)
            return;
        if (event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_AIR && event.getAction() != org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK)
            return;
        String id = getAnyPluginItemID(itemStack);
        Condition condition = new Condition(event.getPlayer());

        Loot loot = plugin.getLootManager().getLoot(id);
        if (loot != null) {
            loot.triggerActions(ActionTrigger.INTERACT, condition);
            return;
        }

        // because the id can be from other plugins, so we can't infer the type of the item
        for (String type : List.of("util", "bait", "hook")) {
            EffectCarrier carrier = plugin.getEffectManager().getEffectCarrier(type, id);
            if (carrier != null) {
                Action[] actions = carrier.getActions(ActionTrigger.INTERACT);
                if (actions != null)
                    for (Action action : actions) {
                        action.trigger(condition);
                    }
                break;
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (itemStack.getType() == Material.AIR || itemStack.getAmount() == 0 || !itemStack.hasItemMeta()) {
            return;
        }

        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound compound = nbtItem.getCompound("CustomFishing");
        if (compound != null) {

            if (!compound.hasTag("placeable") || compound.getByte("placeable") != 1) {
                event.setCancelled(true);
                return;
            }

            Block block = event.getBlock();
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(1);
                pdc.set(new NamespacedKey(plugin, LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING, ItemUtils.toBase64(cloned));
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onBreakBlock(BlockBreakEvent event) {
        final Block block = event.getBlock();
        if (block.getState() instanceof Skull) {
            PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
            String base64 = pdc.get(new NamespacedKey(plugin, LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING);
            if (base64 != null) {
                ItemStack itemStack = ItemUtils.fromBase64(base64);
                event.setDropItems(false);
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPiston(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin, LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onExplosion(BlockExplodeEvent event) {
        handleExplode(event.blockList());
    }

    @EventHandler (ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        handleExplode(event.blockList());
    }

    private void handleExplode(List<Block> blocks) {
        ArrayList<Block> blockToRemove = new ArrayList<>();
        for (Block block : blocks) {
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                var nk = new NamespacedKey(plugin, LocationUtils.toChunkPosString(block.getLocation()));
                String base64 = pdc.get(nk, PersistentDataType.STRING);
                if (base64 != null) {
                    ItemStack itemStack = ItemUtils.fromBase64(base64);
                    block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
                    blockToRemove.add(block);
                    block.setType(Material.AIR);
                    pdc.remove(nk);
                }
            }
        }
        blocks.removeAll(blockToRemove);
    }
}