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
import net.momirealms.customfishing.api.manager.ItemManager;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.item.CustomFishingItemImpl;
import net.momirealms.customfishing.compatibility.item.VanillaItemImpl;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.setting.CFConfig;
import net.momirealms.customfishing.util.NBTUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class ItemManagerImpl implements ItemManager {

    private static ItemManager instance;
    private final CustomFishingPlugin plugin;
    private final HashMap<Key, BuildableItem> buildableItemMap;
    private final HashMap<String, ItemLibrary> itemLibraryMap;

    public ItemManagerImpl(CustomFishingPlugin plugin) {
        instance = this;
        this.plugin = plugin;
        this.itemLibraryMap = new LinkedHashMap<>();
        this.buildableItemMap = new HashMap<>();
        this.registerItemLibrary(new CustomFishingItemImpl());
        this.registerItemLibrary(new VanillaItemImpl());
    }

    public void load() {
        this.loadItemsFromPluginFolder();
        LogUtils.info("Loaded " + buildableItemMap.size() + " items.");
    }

    public void unload() {
        HashMap<Key, BuildableItem> tempMap = new HashMap<>(this.buildableItemMap);
        this.buildableItemMap.clear();
        for (Map.Entry<Key, BuildableItem> entry : tempMap.entrySet()) {
            if (entry.getValue().persist()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public Set<Key> getAllItemsKey() {
        return buildableItemMap.keySet();
    }

    public void disable() {
        this.buildableItemMap.clear();
        this.itemLibraryMap.clear();
    }

    @SuppressWarnings("DuplicatedCode")
    public void loadItemsFromPluginFolder() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("item", "bait", "rod", "util")) {
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

    @Override
    public boolean registerCustomItem(String namespace, String value, BuildableItem buildableItem) {
        Key key = Key.of(namespace, value);
        if (buildableItemMap.containsKey(key)) return false;
        buildableItemMap.put(key, buildableItem);
        return true;
    }

    @Override
    public boolean unregisterCustomItem(String namespace, String value) {
        return buildableItemMap.remove(Key.of(namespace, value)) != null;
    }

    @Override
    public ItemStack build(Player player, String namespace, String value) {
        return build(player, namespace, value, new HashMap<>());
    }

    @Override
    public ItemStack build(Player player, String namespace, String value, Map<String, String> placeholders) {
        BuildableItem buildableItem = buildableItemMap.get(Key.of(namespace, value));
        if (buildableItem == null) return null;
        return buildableItem.build(player, placeholders);
    }

    @NotNull
    @Override
    public ItemStack build(Player player, ItemBuilder builder) {
        return build(player, builder, new HashMap<>());
    }

    @Override
    @Nullable
    public BuildableItem getBuildableItem(String namespace, String value) {
        return buildableItemMap.get(Key.of(namespace, value));
    }

    @Override
    public String getAnyItemID(ItemStack itemStack) {
        for (String plugin : CFConfig.itemDetectOrder) {
            ItemLibrary itemLibrary = itemLibraryMap.get(plugin);
            if (itemLibrary != null) {
                String id = itemLibrary.getItemID(itemStack);
                if (id != null) {
                    return id;
                }
            }
        }
        // should not reach this because vanilla library would always work
        return null;
    }

    @Override
    public ItemStack buildAnyItemByID(Player player, String id) {
        if (id.contains(":")) {
            String[] split = id.split(":", 2);
            return itemLibraryMap.get(split[0]).buildItem(player, split[1]);
        } else {
            return new ItemStack(Material.valueOf(id.toUpperCase(Locale.ENGLISH)));
        }
    }

    @Nullable
    @Override
    public String getItemID(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR) return null;
        NBTItem nbtItem = new NBTItem(itemStack);
        NBTCompound cfCompound = nbtItem.getCompound("CustomFishing");
        if (cfCompound == null) return null;
        return cfCompound.getString("id");
    }

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
                .amount(section.getInt("amount", 1))
                .stackable(section.getBoolean("stackable", true))
                .size(getSizePair(section.getString("size")))
                .price((float) section.getDouble("price.base"), (float) section.getDouble("price.bonus"))
                .customModelData(section.getInt("custom-model-data"))
                .nbt(section.getConfigurationSection("nbt"))
                .maxDurability(section.getInt("max-durability"))
                .itemFlag(section.getStringList("item-flags").stream().map(flag -> ItemFlag.valueOf(flag.toUpperCase())).toList())
                .enchantment(getEnchantmentPair(section.getConfigurationSection("enchantments")), false)
                .enchantment(getEnchantmentPair(section.getConfigurationSection("stored-enchantments")), true)
                .randomEnchantments(getEnchantmentTuple(section.getConfigurationSection("random-enchantments")), false)
                .randomEnchantments(getEnchantmentTuple(section.getConfigurationSection("random-stored-enchantments")), true)
                .tag(section.getBoolean("tag", true), type, id)
                .randomDamage(section.getBoolean("random-durability", false))
                .unbreakable(section.getBoolean("unbreakable", false))
                .preventGrabbing(section.getBoolean("prevent-grabbing", true))
                .head(section.getString("head64"))
                .name(section.getString("display.name"))
                .lore(section.getStringList("display.lore"));
        return itemCFBuilder;
    }

    @Override
    public ItemStack build(Player player, ItemBuilder builder, Map<String, String> placeholders) {
        ItemStack temp = itemLibraryMap.get(builder.getLibrary()).buildItem(player, builder.getId());
        temp.setAmount(builder.getAmount());
        NBTItem nbtItem = new NBTItem(temp);
        for (ItemBuilder.ItemPropertyEditor editor : builder.getEditors()) {
            editor.edit(player, nbtItem, placeholders);
        }
        return nbtItem.getItem();
    }

    @Override
    public boolean registerItemLibrary(ItemLibrary itemLibrary) {
        if (itemLibraryMap.containsKey(itemLibrary.identification())) return false;
        itemLibraryMap.put(itemLibrary.identification(), itemLibrary);
        return true;
    }

    @Override
    public boolean unRegisterItemLibrary(ItemLibrary itemLibrary) {
        return itemLibraryMap.remove(itemLibrary.identification(), itemLibrary);
    }

    @Override
    public boolean unRegisterItemLibrary(String itemLibrary) {
        return itemLibraryMap.remove(itemLibrary) != null;
    }

    @Override
    public void dropItem(Player player, Location hookLocation, Location playerLocation, Loot loot, Map<String, String> args) {
        ItemStack item = build(player, "item", loot.getID(), args);
        if (item == null) {
            LogUtils.warn(String.format("Item %s not exists", loot.getID()));
            return;
        }
        if (item.getType() == Material.AIR) {
            return;
        }
        Entity itemEntity = hookLocation.getWorld().dropItem(hookLocation, item);
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply(0.105);
        vector = vector.setY((vector.getY() + 0.22) * 1.18);
        itemEntity.setVelocity(vector);
    }

    @Override
    public void dropItem(Location hookLocation, Location playerLocation, ItemStack itemStack) {
        Entity itemEntity = hookLocation.getWorld().dropItem(hookLocation, itemStack);
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply(0.105);
        vector = vector.setY((vector.getY() + 0.22) * 1.18);
        itemEntity.setVelocity(vector);
    }

    @NotNull
    private List<Pair<String, Short>> getEnchantmentPair(ConfigurationSection section) {
        List<Pair<String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof Integer integer) {
                list.add(Pair.of(entry.getKey(), Short.valueOf(String.valueOf(integer))));
            }
        }
        return list;
    }

    @NotNull
    private List<Tuple<Double, String, Short>> getEnchantmentTuple(ConfigurationSection section) {
        List<Tuple<Double, String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection inner) {
                Tuple<Double, String, Short> tuple = Tuple.of(
                        inner.getDouble("chance"),
                        inner.getString("enchant"),
                        Short.valueOf(String.valueOf(inner.getInt("level")))
                );
                list.add(tuple);
            }
        }
        return list;
    }

    @Nullable
    private Pair<Float, Float> getSizePair(String size) {
        if (size == null) return null;
        String[] split = size.split("~", 2);
        return Pair.of(Float.parseFloat(split[0]), Float.parseFloat(split[1]));
    }

    public static class CFBuilder implements ItemBuilder {

        private final String library;
        private final String id;
        private int amount;
        private final LinkedHashMap<String, ItemPropertyEditor> editors;

        public CFBuilder(String library, String id) {
            this.id = id;
            this.library = library;
            this.editors = new LinkedHashMap<>();
            this.amount = 1;
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
            this.amount = amount;
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
            editors.put("nbt", (player, nbtItem, placeholders) -> NBTUtils.setTagsFromBukkitYAML(nbtItem, nbt));
            return this;
        }

        @Override
        public ItemBuilder nbt(ConfigurationSection section) {
            if (section == null) return this;
            editors.put("nbt", (player, nbtItem, placeholders) -> NBTUtils.setTagsFromBukkitYAML(nbtItem, section.getValues(false)));
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
                if (base != 0) {
                    placeholders.put("{base}", String.format("%.2f", base));
                }
                if (bonus != 0) {
                    placeholders.put("{bonus}", String.format("%.2f", bonus));
                }
                float size = Float.parseFloat(placeholders.getOrDefault("{size}", "0"));
                double price = CustomFishingPlugin.get().getMarketManager().getPrice(
                        base,
                        bonus,
                        size
                );
                nbtItem.setDouble("Price", price);
                placeholders.put("{price}", String.format("%.2f", price));
            });
            return this;
        }

        @Override
        public ItemBuilder size(Pair<Float, Float> size) {
            if (size == null) return this;
            editors.put("size", (player, nbtItem, placeholders) -> {
                NBTCompound cfCompound = nbtItem.getOrCreateCompound("CustomFishing");
                float random = size.left() + ThreadLocalRandom.current().nextFloat(size.right() - size.left());
                float bonus = Float.parseFloat(placeholders.getOrDefault("{size-multiplier}", "1.0"));
                random *= bonus;
                cfCompound.setFloat("size", random);
                placeholders.put("{size}", String.format("%.2f", random));
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
                nbtCompound.setUUID("Id", UUID.nameUUIDFromBytes(base64.substring(0,8).getBytes()));
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
            return amount;
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

    public static int giveCertainAmountOfItem(Player player, ItemStack itemStack, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemMeta meta = itemStack.getItemMeta();
        int maxStackSize = itemStack.getMaxStackSize();

        if (amount > maxStackSize * 100) {
            LogUtils.warn("Detected too many items spawning. Lowering the amount to " + (maxStackSize * 100));
            amount = maxStackSize * 100;
        }

        int actualAmount = amount;

        for (ItemStack other : inventory.getStorageContents()) {
            if (other != null) {
                if (other.getType() == itemStack.getType() && other.getItemMeta().equals(meta)) {
                    if (other.getAmount() < maxStackSize) {
                        int delta = maxStackSize - other.getAmount();
                        if (amount > delta) {
                            other.setAmount(maxStackSize);
                            amount -= delta;
                        } else {
                            other.setAmount(amount + other.getAmount());
                            return actualAmount;
                        }
                    }
                }
            }
        }

        if (amount > 0) {
            for (ItemStack other : inventory.getStorageContents()) {
                if (other == null) {
                    if (amount > maxStackSize) {
                        amount -= maxStackSize;
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(maxStackSize);
                        inventory.addItem(cloned);
                    } else {
                        ItemStack cloned = itemStack.clone();
                        cloned.setAmount(amount);
                        inventory.addItem(cloned);
                        return actualAmount;
                    }
                }
            }
        }

        if (amount > 0) {
            for (int i = 0; i < amount / maxStackSize; i++) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(maxStackSize);
                player.getWorld().dropItem(player.getLocation(), cloned);
            }
            int left = amount % maxStackSize;
            if (left != 0) {
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(left);
                player.getWorld().dropItem(player.getLocation(), cloned);
            }
        }

        return actualAmount;
    }
}