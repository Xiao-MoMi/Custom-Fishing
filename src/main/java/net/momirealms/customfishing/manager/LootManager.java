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
import net.momirealms.customfishing.object.Function;
import net.momirealms.customfishing.object.action.*;
import net.momirealms.customfishing.object.fishing.Difficulty;
import net.momirealms.customfishing.object.fishing.Layout;
import net.momirealms.customfishing.object.loot.*;
import net.momirealms.customfishing.object.requirements.*;
import net.momirealms.customfishing.util.AdventureUtil;
import net.momirealms.customfishing.util.ItemStackUtil;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class LootManager extends Function {

    public static HashMap<String, Loot> WATERLOOTS;
    public static HashMap<String, Loot> LAVALOOTS;
    public static HashMap<String, ItemStack> LOOTITEMS;

    @Nullable
    public static ItemStack build(String key) {
        ItemStack itemStack = LOOTITEMS.get(key);
        return itemStack == null ? null : itemStack.clone();
    }

    @Override
    public void load() {
        WATERLOOTS = new HashMap<>();
        LAVALOOTS = new HashMap<>();
        LOOTITEMS = new HashMap<>();
        loadItems();
        loadMobs();
        AdventureUtil.consoleMessage("[CustomFishing] Loaded <green>" + (LAVALOOTS.size() + WATERLOOTS.size()) + " <gray>loots");
    }

    @Override
    public void unload() {
        if (WATERLOOTS != null) WATERLOOTS.clear();
        if (LAVALOOTS != null) LAVALOOTS.clear();
        if (LOOTITEMS != null) LOOTITEMS.clear();
    }

    private void loadMobs() {
        if (Bukkit.getPluginManager().getPlugin("MythicMobs") == null) return;
        File mob_file = new File(CustomFishing.plugin.getDataFolder() + File.separator + "mobs");
        if (!mob_file.exists()) {
            if (!mob_file.mkdir()) return;
            CustomFishing.plugin.saveResource("mobs" + File.separator + "example.yml", false);
        }
        File[] files = mob_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                if (!config.getBoolean(key + ".enable", true)) continue;
                // Bar mechanic
                String[] diff = StringUtils.split(config.getString(key + ".difficulty", "1-1"),"-");
                Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));
                int weight = config.getInt(key + ".weight",10);
                int time = config.getInt(key + ".time",10000);
                Mob loot = new Mob(
                        key,
                        difficulty,
                        time,
                        weight,
                        config.getString(key + ".mobID", key),
                        config.getInt(key + ".level", 0),
                        new MobVector(
                                config.getDouble(key + ".vector.horizontal",1.1),
                                config.getDouble(key + ".vector.vertical",1.3)
                        ));
                if (config.contains(key + ".nick")) loot.setNick(config.getString(key + ".nick"));
                else loot.setNick(ChatColor.stripColor(config.getString(key + ".mobID", key)));
                setActionsAndRequirements(config, loot, key);
                if (config.getBoolean(key + ".in-lava", false)) {
                    LAVALOOTS.put(key, loot);
                } else {
                    WATERLOOTS.put(key, loot);
                }
            }
        }
    }

    private void loadItems() {
        File loot_file = new File(CustomFishing.plugin.getDataFolder() + File.separator + "loots");
        if (!loot_file.exists()) {
            if (!loot_file.mkdir()) return;
            CustomFishing.plugin.saveResource("loots" + File.separator + "default.yml", false);
            CustomFishing.plugin.saveResource("loots" + File.separator + "example.yml", false);
        }
        File[] files = loot_file.listFiles();
        if (files == null) return;
        for (File file : files) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            Set<String> keys = config.getKeys(false);
            for (String key : keys) {
                if (!config.getBoolean(key + ".enable", true)) continue;

                String material = config.getString(key + ".material","COD");
                // Bar mechanic
                String[] diff = StringUtils.split(config.getString(key + ".difficulty", "1-1"),"-");
                Difficulty difficulty = new Difficulty(Integer.parseInt(diff[0]), Integer.parseInt(diff[1]));
                int weight = config.getInt(key + ".weight",10);
                int time = config.getInt(key + ".time",10000);
                DroppedItem loot = new DroppedItem(
                        key,
                        difficulty,
                        time,
                        weight,
                        material.contains(":") ? material : key);
                // Set nick
                if (config.contains(key + ".nick")) loot.setNick(config.getString(key + ".nick"));
                else loot.setNick(ChatColor.stripColor(config.getString(key + ".display.name", key)));
                loot.setRandomDurability(config.getBoolean(key + ".random-durability", false));
                if (config.contains(key + ".size")) {
                    String[] size = StringUtils.split(config.getString(key + ".size", "1~10"), "~");
                    if (size.length != 2) {
                        AdventureUtil.consoleMessage("<red>[CustomFishing] Wrong size found at " + key);
                        continue;
                    }
                    loot.setSize(size);
                }
                if (config.contains(key + ".price")) {
                    loot.setBasicPrice((float) config.getDouble(key + ".price.basic", 0));
                    loot.setSizeBonus((float) config.getDouble(key + ".price.bonus", 0));
                }
                if (config.contains(key + ".random-enchantments")){
                    List<LeveledEnchantment> randomEnchants = new ArrayList<>();
                    config.getConfigurationSection(key + ".random-enchantments").getValues(false).forEach((order, enchant) -> {
                        if (enchant instanceof MemorySection memorySection){
                            LeveledEnchantment enchantment = new LeveledEnchantment(NamespacedKey.fromString(memorySection.getString("enchant")), memorySection.getInt("level"));
                            enchantment.setChance(memorySection.getDouble("chance"));
                            randomEnchants.add(enchantment);
                        }
                    });
                    loot.setRandomEnchants(randomEnchants.toArray(new LeveledEnchantment[0]));
                }
                setActionsAndRequirements(config, loot, key);
                if (config.getBoolean(key + ".in-lava", false)) {
                    LAVALOOTS.put(key, loot);
                } else {
                    WATERLOOTS.put(key, loot);
                }
                // Construct ItemStack
                if (material.contains(":")) {
                    continue;
                }
                Item item = new Item(Material.valueOf(material.toUpperCase()));
                BonusManager.setItemProperties(config, key, item);
                if (item.getMaterial() == Material.AIR) LOOTITEMS.put(key, new ItemStack(Material.AIR));
                else LOOTITEMS.put(key, ItemStackUtil.getFromItem(item));
            }
        }
    }

    private void setActionsAndRequirements(YamlConfiguration config, Loot loot, String key) {

        if (config.contains(key + ".layout")) {
            List<Layout> layoutList = new ArrayList<>();
            for (String layoutName : config.getStringList(key + ".layout")) {
                Layout layout = LayoutManager.LAYOUTS.get(layoutName);
                if (layout == null) {
                    AdventureUtil.consoleMessage("<red>[CustomFishing] Bar " + layoutName + " doesn't exist");
                    continue;
                }
                layoutList.add(layout);
            }
            loot.setLayout(layoutList.toArray(new Layout[0]));
        }
        loot.setScore(config.getDouble(key + ".score", 0));
        loot.setShowInFinder(config.getBoolean(key + ".show-in-fishfinder", true));

        if (config.contains(key + ".group")) loot.setGroup(config.getString(key + ".group"));
        // Set actions
        List<ActionInterface> successActions = new ArrayList<>();
        if (config.contains(key + ".action.success.message")) successActions.add(new MessageActionImpl(config.getStringList(key + ".action.success.message").toArray(new String[0]), loot.getNick()));
        if (config.contains(key + ".action.success.command")) successActions.add(new CommandActionImpl(config.getStringList(key + ".action.success.command").toArray(new String[0]), loot.getNick()));
        if (config.contains(key + ".action.success.exp")) successActions.add(new VanillaXPImpl(config.getInt(key + ".action.success.exp"), false));
        if (config.contains(key + ".action.success.mending")) successActions.add(new VanillaXPImpl(config.getInt(key + ".action.success.mending"), true));
        if (config.contains(key + ".action.success.skill-xp")) successActions.add(new SkillXPImpl(config.getInt(key + ".action.success.skill-xp")));
        if (config.contains(key + ".action.success.sound")) successActions.add(new SoundActionImpl(config.getString(key + ".action.success.sound")));
        loot.setSuccessActions(successActions.toArray(new ActionInterface[0]));

        List<ActionInterface> failureActions = new ArrayList<>();
        if (config.contains(key + ".action.failure.message")) failureActions.add(new MessageActionImpl(config.getStringList(key + ".action.failure.message").toArray(new String[0]), loot.getNick()));
        if (config.contains(key + ".action.failure.command")) failureActions.add(new CommandActionImpl(config.getStringList(key + ".action.failure.command").toArray(new String[0]), loot.getNick()));
        if (config.contains(key + ".action.failure.exp")) failureActions.add(new VanillaXPImpl(config.getInt(key + ".action.failure.exp"), false));
        if (config.contains(key + ".action.failure.mending")) failureActions.add(new VanillaXPImpl(config.getInt(key + ".action.failure.mending"), true));
        if (config.contains(key + ".action.failure.skill-xp")) failureActions.add(new SkillXPImpl(config.getInt(key + ".action.failure.skill-xp")));
        if (config.contains(key + ".action.failure.sound")) failureActions.add(new SoundActionImpl(config.getString(key + ".action.failure.sound")));
        loot.setFailureActions(failureActions.toArray(new ActionInterface[0]));

        List<ActionInterface> hookActions = new ArrayList<>();
        if (config.contains(key + ".action.hook.message")) hookActions.add(new MessageActionImpl(config.getStringList(key + ".action.hook.message").toArray(new String[0]), loot.getNick()));
        if (config.contains(key + ".action.hook.command")) hookActions.add(new CommandActionImpl(config.getStringList(key + ".action.hook.command").toArray(new String[0]), loot.getNick()));
        if (config.contains(key + ".action.hook.exp")) hookActions.add(new VanillaXPImpl(config.getInt(key + ".action.hook.exp"), false));
        if (config.contains(key + ".action.hook.mending")) hookActions.add(new VanillaXPImpl(config.getInt(key + ".action.hook.mending"), true));
        if (config.contains(key + ".action.hook.skill-xp")) hookActions.add(new SkillXPImpl(config.getInt(key + ".action.hook.skill-xp")));
        if (config.contains(key + ".action.hook.sound")) hookActions.add(new SoundActionImpl(config.getString(key + ".action.hook.sound")));
        loot.setHookActions(hookActions.toArray(new ActionInterface[0]));

        if (config.contains(key + ".requirements")){
            List<RequirementInterface> requirements = new ArrayList<>();
            config.getConfigurationSection(key + ".requirements").getKeys(false).forEach(requirement -> {
                switch (requirement){
                    case "weather" -> requirements.add(new WeatherImpl(config.getStringList(key + ".requirements.weather")));
                    case "ypos" -> requirements.add(new YPosImpl(config.getStringList(key + ".requirements.ypos")));
                    case "season" -> requirements.add(new SeasonImpl(config.getStringList(key + ".requirements.season")));
                    case "world" -> requirements.add(new WorldImpl(config.getStringList(key + ".requirements.world")));
                    case "biome" -> requirements.add(new BiomeImpl(config.getStringList(key + ".requirements.biome")));
                    case "permission" -> requirements.add(new PermissionImpl(config.getString(key + ".requirements.permission")));
                    case "time" -> requirements.add(new TimeImpl(config.getStringList(key + ".requirements.time")));
                    case "skill-level" -> requirements.add(new SkillLevelImpl(config.getInt(key + ".requirements.skill-level")));
                    case "papi-condition" -> requirements.add(new CustomPapi(config.getConfigurationSection(key + ".requirements.papi-condition").getValues(false)));
                }
            });
            loot.setRequirements(requirements.toArray(new RequirementInterface[0]));
        }
    }
}
