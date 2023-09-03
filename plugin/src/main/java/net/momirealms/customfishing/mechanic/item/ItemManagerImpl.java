package net.momirealms.customfishing.mechanic.item;

import de.tr7zw.changeme.nbtapi.*;
import net.momirealms.customfishing.adventure.AdventureManagerImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Key;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.manager.ItemManager;
import net.momirealms.customfishing.api.mechanic.item.BuildableItem;
import net.momirealms.customfishing.api.mechanic.item.ItemBuilder;
import net.momirealms.customfishing.api.mechanic.item.ItemLibrary;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.item.CustomFishingItemImpl;
import net.momirealms.customfishing.compatibility.item.VanillaItemImpl;
import net.momirealms.customfishing.compatibility.papi.PlaceholderManagerImpl;
import net.momirealms.customfishing.setting.Config;
import net.momirealms.customfishing.util.NBTUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
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
        AdventureManagerImpl.getInstance().sendMessageWithPrefix(Bukkit.getConsoleSender(), "<white>Loaded <green>" + buildableItemMap.size() + " <white>items.");
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
        for (String type : List.of("loots", "baits", "rods", "utils")) {
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
                    } else if (subFile.isFile()) {
                        this.loadSingleFile(subFile, StringUtils.chop(type));
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
        for (String plugin : Config.itemDetectOrder) {
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
        String material = section.getString("material", "PAPER");
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
                .tag(section.getBoolean("tag", true), type, id)
                .randomDamage(section.getBoolean("random-durability", false))
                .unbreakable(section.getBoolean("unbreakable", false))
                .preventGrabbing(section.getBoolean("prevent-grabbing", false))
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
        ItemStack item = build(player, "loot", loot.getID(), args);
        if (item == null) {
            LogUtils.warn(String.format("Item %s not exists", loot.getID()));
            return;
        }
        if (item.getType() == Material.AIR) {
            return;
        }
        Entity itemEntity = hookLocation.getWorld().dropItem(hookLocation, item);
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply(0.105);
        vector = vector.setY((vector.getY() + 0.2) * 1.18);
        itemEntity.setVelocity(vector);
    }

    @Override
    public void dropItem(Location hookLocation, Location playerLocation, ItemStack itemStack) {
        Entity itemEntity = hookLocation.getWorld().dropItem(hookLocation, itemStack);
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply(0.105);
        vector = vector.setY((vector.getY() + 0.2) * 1.18);
        itemEntity.setVelocity(vector);
    }

    @NotNull
    private List<Pair<String, Short>> getEnchantmentPair(ConfigurationSection section) {
        List<Pair<String, Short>> list = new ArrayList<>();
        if (section == null) return list;
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            list.add(Pair.of(entry.getKey(), (short) entry.getValue()));
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
            String replacedName = AdventureManagerImpl.getInstance().legacyToMiniMessage(name);
            editors.put("name", (player, nbtItem, placeholders) -> {
                NBTCompound displayCompound = nbtItem.getOrCreateCompound("display");
                displayCompound.setString("Name", AdventureManagerImpl.getInstance().componentToJson(
                        AdventureManagerImpl.getInstance().getComponentFromMiniMessage(
                                "<!i>" + PlaceholderManagerImpl.getInstance().parse(player, replacedName, placeholders)
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
            List<String> replacedList = lore.stream().map(s -> AdventureManagerImpl.getInstance().legacyToMiniMessage(s)).toList();
            editors.put("lore", (player, nbtItem, placeholders) -> {
                NBTCompound displayCompound = nbtItem.getOrCreateCompound("display");
                NBTList<String> list = displayCompound.getStringList("Lore");
                list.clear();
                list.addAll(replacedList.stream().map(s -> AdventureManagerImpl.getInstance().componentToJson(
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
}