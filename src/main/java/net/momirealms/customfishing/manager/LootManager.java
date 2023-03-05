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

package net.momirealms.customfishing.manager;

import net.momirealms.customfishing.CustomFishing;
import net.momirealms.customfishing.fishing.MiniGameConfig;
import net.momirealms.customfishing.fishing.loot.*;
import net.momirealms.customfishing.fishing.requirements.*;
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.LeveledEnchantment;
import net.momirealms.customfishing.object.action.*;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class LootManager extends Function {

    private final CustomFishing plugin;
    private final HashMap<String, Loot> waterLoots;
    private final HashMap<String, Loot> lavaLoots;
    private final HashMap<String, ItemStack> lootItems;

    public LootManager(CustomFishing plugin) {
        this.plugin = plugin;
        this.waterLoots = new HashMap<>();
        this.lavaLoots = new HashMap<>();
        this.lootItems = new HashMap<>();
    }

    @Nullable
    public ItemStack build(String key) {
        ItemStack itemStack = this.lootItems.get(key);
        return itemStack == null ? null : itemStack.clone();
    }

    @Override
    public void load() {
        this.loadItems();
        this.loadMobs();
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + (this.lavaLoots.size() + this.waterLoots.size()) + " <gray>loot(s)");
    }

    @Override
    public void unload() {
        this.waterLoots.clear();
        this.lavaLoots.clear();
        this.lootItems.clear();
    }

    @Nullable
    public Loot getLoot(String key) {
        Loot loot = this.waterLoots.get(key);
        if (loot == null) {
            loot = this.lavaLoots.get(key);
        }
        return loot;
    }

    private void loadMobs() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) return;
        File mob_file = new File(plugin.getDataFolder() + File.separator + "mobs");
        if (!mob_file.exists()) {
            if (!mob_file.mkdir()) return;
            plugin.saveResource("mobs" + File.separator + "default.yml", false);
        }
        File[] files = mob_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection mobSection = config.getConfigurationSection(key);
                if (mobSection == null) continue;
                if (!mobSection.getBoolean("enable", true)) continue;
                Mob loot = new Mob(
                        key,
                        mobSection.contains("nick") ? mobSection.getString("nick") : AdventureUtil.replaceLegacy(mobSection.getString("mobID", key)),
                        getMiniGames(mobSection),
                        mobSection.getInt("weight",10),
                        mobSection.getBoolean("show-in-fishfinder", true),
                        mobSection.getDouble("score",10d),
                        mobSection.getString(key + ".mobID", key),
                        mobSection.getInt(key + ".level", 0),
                        new MobVector(
                                mobSection.getDouble("vector.horizontal",1.1),
                                mobSection.getDouble("vector.vertical",1.3)
                        )
                );

                setActions(mobSection, loot);
                setRequirements(mobSection.getConfigurationSection("requirements"), loot);

                if (mobSection.getBoolean("in-lava", false)) lavaLoots.put(key, loot);
                else waterLoots.put(key, loot);
            }
        }
    }

    private void loadItems() {
        File loot_file = new File(plugin.getDataFolder() + File.separator + "loots");
        if (!loot_file.exists()) {
            if (!loot_file.mkdir()) return;
            plugin.saveResource("loots" + File.separator + "default.yml", false);
            plugin.saveResource("loots" + File.separator + "example.yml", false);
        }
        File[] files = loot_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.getName().endsWith(".yml")) continue;
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                ConfigurationSection lootSection = config.getConfigurationSection(key);
                if (lootSection == null) continue;
                if (!lootSection.getBoolean("enable", true)) continue;
                String material = lootSection.getString("material","COD");

                DroppedItem loot = new DroppedItem(
                        key,
                        lootSection.contains("nick") ? lootSection.getString("nick") : AdventureUtil.replaceLegacy(lootSection.getString("display.name", key)),
                        material.contains(":") ? material : key,
                        getMiniGames(lootSection),
                        lootSection.getInt("weight",10),
                        lootSection.getBoolean("show-in-fishfinder", true),
                        lootSection.getDouble("score"),
                        lootSection.getBoolean("random-durability", false)
                );

                if (lootSection.contains("size")) {
                    String[] size = StringUtils.split(lootSection.getString("size", "1~10"), "~");
                    if (size.length != 2) {
                        AdventureUtil.consoleMessage("<red>[CustomFishing] Wrong size found at " + key);
                        continue;
                    }
                    loot.setSize(size);
                }
                if (lootSection.contains("price")) {
                    loot.setBasicPrice((float) lootSection.getDouble("price.base", 0));
                    loot.setSizeBonus((float) lootSection.getDouble("price.bonus", 0));
                }
                if (lootSection.contains("random-enchantments")) {
                    List<LeveledEnchantment> randomEnchants = new ArrayList<>();
                    ConfigurationSection enchantSection = lootSection.getConfigurationSection("random-enchantments");
                    if (enchantSection != null) {
                        for (Map.Entry<String, Object> entry : enchantSection.getValues(false).entrySet()) {
                            if (entry.getValue() instanceof MemorySection memorySection){
                                LeveledEnchantment enchantment = new LeveledEnchantment(NamespacedKey.fromString(memorySection.getString("enchant", "minecraft:sharpness")), memorySection.getInt("level"));
                                enchantment.setChance(memorySection.getDouble("chance"));
                                randomEnchants.add(enchantment);
                            }
                        }
                    }
                    loot.setRandomEnchants(randomEnchants.toArray(new LeveledEnchantment[0]));
                }

                setActions(lootSection, loot);
                setRequirements(lootSection.getConfigurationSection("requirements"), loot);

                if (lootSection.getBoolean("in-lava", false)) lavaLoots.put(key, loot);
                else waterLoots.put(key, loot);

                if (material.contains(":")) continue;

                Item item = new Item(lootSection, key);
                if (item.getMaterial() == Material.AIR) lootItems.put(key, new ItemStack(Material.AIR));
                else lootItems.put(key, ItemStackUtil.getFromItem(item));
            }
        }
    }

    private void setActions(ConfigurationSection section, Loot loot) {
        loot.setSuccessActions(getActions(section.getConfigurationSection("action.success"), loot.getNick()));
        loot.setFailureActions(getActions(section.getConfigurationSection("action.failure"), loot.getNick()));
        loot.setHookActions(getActions(section.getConfigurationSection("action.hook"), loot.getNick()));
        loot.setConsumeActions(getActions(section.getConfigurationSection("action.consume"), loot.getNick()));
    }

    private void setRequirements(ConfigurationSection section, Loot loot) {
        loot.setRequirements(getRequirements(section));
    }

    private ActionInterface[] getActions(ConfigurationSection section, String nick) {
        List<ActionInterface> actions = new ArrayList<>();
        if (section != null) {
            for (String action : section.getKeys(false)) {
                switch (action) {
                    case "message" -> actions.add(new MessageActionImpl(section.getStringList(action).toArray(new String[0]), nick));
                    case "command" -> actions.add(new CommandActionImpl(section.getStringList(action).toArray(new String[0]), nick));
                    case "exp" -> actions.add(new VanillaXPImpl(section.getInt(action), false));
                    case "mending" -> actions.add(new VanillaXPImpl(section.getInt(action), true));
                    case "skill-xp" -> actions.add(new SkillXPImpl(section.getInt(action)));
                    case "sound" -> actions.add(new SoundActionImpl(
                            section.getString(action + ".source"),
                            section.getString(action + ".key"),
                            (float) section.getDouble(action + ".volume"),
                            (float) section.getDouble(action + ".pitch")
                    ));
                    case "potion-effect" -> {
                        List<PotionEffect> potionEffectList = new ArrayList<>();
                        for (String key : section.getConfigurationSection(action).getKeys(false)) {
                            PotionEffectType type = PotionEffectType.getByName(section.getString(action + "." + key + ".type", "BLINDNESS").toUpperCase());
                            if (type == null) AdventureUtil.consoleMessage("<red>[CustomFishing] Potion effect " + section.getString(action + "." + key + ".type", "BLINDNESS") + " doesn't exists");
                            potionEffectList.add(new PotionEffect(
                                    type == null ? PotionEffectType.LUCK : type,
                                    section.getInt(action + "." + key + ".duration"),
                                    section.getInt(action + "." + key + ".amplifier")
                            ));
                        }
                        actions.add(new PotionEffectImpl(potionEffectList.toArray(new PotionEffect[0])));
                    }
                }
            }
        }
        return actions.toArray(new ActionInterface[0]);
    }

    private RequirementInterface[] getRequirements(ConfigurationSection section) {
        List<RequirementInterface> requirements = new ArrayList<>();
        if (section != null) {
            for (String type : section.getKeys(false)) {
                switch (type) {
                    case "biome" -> requirements.add(new BiomeImpl(section.getStringList(type)));
                    case "weather" -> requirements.add(new WeatherImpl(section.getStringList(type)));
                    case "ypos" -> requirements.add(new YPosImpl(section.getStringList(type)));
                    case "season" -> requirements.add(new SeasonImpl(section.getStringList(type)));
                    case "world" -> requirements.add(new WorldImpl(section.getStringList(type)));
                    case "permission" -> requirements.add(new PermissionImpl(section.getString(type)));
                    case "time" -> requirements.add(new TimeImpl(section.getStringList(type)));
                    case "skill-level" -> requirements.add(new SkillLevelImpl(section.getInt(type)));
                    case "papi-condition" -> requirements.add(new CustomPapi(Objects.requireNonNull(section.getConfigurationSection(type)).getValues(false)));
                }
            }
        }
        return requirements.toArray(new RequirementInterface[0]);
    }

    private MiniGameConfig[] getMiniGames(ConfigurationSection section) {
        String[] games = section.getStringList("mini-game").size() == 0 ? new String[]{section.getString("mini-game", null)} : section.getStringList("mini-game").toArray(new String[0]);
        MiniGameConfig[] gameConfigs = new MiniGameConfig[games.length];
        for (int i = 0, size = games.length; i < size; i++) {
            if (games[i] == null) {
                return null;
            }
            MiniGameConfig gameConfig = plugin.getBarMechanicManager().getGameConfig(games[i]);
            if (gameConfig == null) {
                AdventureUtil.consoleMessage("<red>[CustomFishing] Mini game " + games[i] + " doesn't exist");
                return null;
            }
            gameConfigs[i] = gameConfig;
        }
        return gameConfigs;
    }

    public HashMap<String, Loot> getWaterLoots() {
        return waterLoots;
    }

    public HashMap<String, Loot> getLavaLoots() {
        return lavaLoots;
    }

    public ArrayList<Loot> getAllLoots() {
        ArrayList<Loot> loots = new ArrayList<>(getWaterLoots().values());
        loots.addAll(getLavaLoots().values());
        return loots;
    }
}
