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

package net.momirealms.customfishing.mechanic.block;

import net.momirealms.customfishing.CustomFishingPluginImpl;
import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.common.Pair;
import net.momirealms.customfishing.api.common.Tuple;
import net.momirealms.customfishing.api.manager.BlockManager;
import net.momirealms.customfishing.api.mechanic.block.*;
import net.momirealms.customfishing.api.mechanic.loot.Loot;
import net.momirealms.customfishing.api.util.LogUtils;
import net.momirealms.customfishing.compatibility.block.VanillaBlockImpl;
import net.momirealms.customfishing.util.ConfigUtils;
import org.bukkit.*;
import org.bukkit.block.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Campfire;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.block.data.type.TurtleEgg;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class BlockManagerImpl implements BlockManager, Listener {

    private final CustomFishingPlugin plugin;
    private final HashMap<String, BlockLibrary> blockLibraryMap;
    private final HashMap<String, BlockConfig> blockConfigMap;
    private final HashMap<String, BlockDataModifierBuilder> dataBuilderMap;
    private final HashMap<String, BlockStateModifierBuilder> stateBuilderMap;

    public BlockManagerImpl(CustomFishingPluginImpl plugin) {
        this.plugin = plugin;
        this.blockLibraryMap = new HashMap<>();
        this.blockConfigMap = new HashMap<>();
        this.dataBuilderMap = new HashMap<>();
        this.stateBuilderMap = new HashMap<>();
        this.registerBlockLibrary(new VanillaBlockImpl());
        this.registerInbuiltProperties();
    }

    @EventHandler
    public void onBlockLands(EntityChangeBlockEvent event) {
        if (event.isCancelled()) return;
        String temp = event.getEntity().getPersistentDataContainer().get(
                Objects.requireNonNull(NamespacedKey.fromString("block", CustomFishingPlugin.get())),
                PersistentDataType.STRING
        );
        if (temp == null) return;
        String[] split = temp.split(";");
        BlockConfig blockConfig = blockConfigMap.get(split[0]);
        if (blockConfig == null) return;
        Player player = Bukkit.getPlayer(split[1]);
        if (player == null) {
            event.getEntity().remove();
            event.getBlock().setType(Material.AIR);
            return;
        }
        Location location = event.getBlock().getLocation();
        plugin.getScheduler().runTaskSyncLater(() -> {
            BlockState state = location.getBlock().getState();
            for (BlockStateModifier modifier : blockConfig.getStateModifierList()) {
                modifier.apply(player, state);
            }
        }, location, 50, TimeUnit.MILLISECONDS);
    }

    @Override
    public boolean registerBlockLibrary(BlockLibrary library) {
        if (this.blockLibraryMap.containsKey(library.identification())) return false;
        this.blockLibraryMap.put(library.identification(), library);
        return true;
    }

    @Override
    public boolean unregisterBlockLibrary(BlockLibrary library) {
        return unregisterBlockLibrary(library.identification());
    }

    @Override
    public boolean unregisterBlockLibrary(String library) {
        return blockLibraryMap.remove(library) != null;
    }

    @Override
    public boolean registerBlockDataModifierBuilder(String type, BlockDataModifierBuilder builder) {
        if (dataBuilderMap.containsKey(type)) return false;
        dataBuilderMap.put(type, builder);
        return true;
    }

    @Override
    public boolean registerBlockStateModifierBuilder(String type, BlockStateModifierBuilder builder) {
        if (stateBuilderMap.containsKey(type)) return false;
        stateBuilderMap.put(type, builder);
        return true;
    }

    public void load() {
        this.loadConfig();
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private void registerInbuiltProperties() {
        this.registerDirectional();
        this.registerStorage();
        this.registerRotatable();
        this.registerTurtleEggs();
        this.registerMoisture();
        this.registerNoteBlock();
        this.registerCampfire();
        this.registerAge();
    }

    public void unload() {
        HandlerList.unregisterAll(this);
        HashMap<String, BlockConfig> tempMap = new HashMap<>(this.blockConfigMap);
        this.blockConfigMap.clear();
        for (Map.Entry<String, BlockConfig> entry : tempMap.entrySet()) {
            if (entry.getValue().isPersist()) {
                tempMap.put(entry.getKey(), entry.getValue());
            }
        }
    }

    public void disable() {
        this.blockLibraryMap.clear();
    }

    @SuppressWarnings("DuplicatedCode")
    private void loadConfig() {
        Deque<File> fileDeque = new ArrayDeque<>();
        for (String type : List.of("blocks")) {
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
                        this.loadSingleFile(subFile);
                    }
                }
            }
        }
    }

    private void loadSingleFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        for (Map.Entry<String, Object> entry : config.getValues(false).entrySet()) {
            if (entry.getValue() instanceof ConfigurationSection section) {
                String blockID = section.getString("block");
                if (blockID == null) {
                    LogUtils.warn("Block can't be null. File:" + file.getAbsolutePath() + "; Section:" + section.getCurrentPath());
                    continue;
                }
                List<BlockDataModifier> dataModifiers = new ArrayList<>();
                List<BlockStateModifier> stateModifiers = new ArrayList<>();
                ConfigurationSection property = section.getConfigurationSection("properties");
                if (property != null) {
                    for (Map.Entry<String, Object> innerEntry : property.getValues(false).entrySet()) {
                        BlockDataModifierBuilder dataBuilder = dataBuilderMap.get(innerEntry.getKey());
                        if (dataBuilder != null) {
                            dataModifiers.add(dataBuilder.build(innerEntry.getValue()));
                            continue;
                        }
                        BlockStateModifierBuilder stateBuilder = stateBuilderMap.get(innerEntry.getKey());
                        if (stateBuilder != null) {
                            stateModifiers.add(stateBuilder.build(innerEntry.getValue()));
                        }
                    }
                }
                BlockConfig blockConfig = new BlockConfig.Builder()
                        .blockID(blockID)
                        .persist(false)
                        .horizontalVector(section.getDouble("vector.horizontal", 1.1))
                        .verticalVector(section.getDouble("vector.vertical", 1.2))
                        .dataModifiers(dataModifiers)
                        .stateModifiers(stateModifiers)
                        .build();
                blockConfigMap.put(entry.getKey(), blockConfig);
            }
        }
    }

    @Override
    public void summonBlock(Player player, Location hookLocation, Location playerLocation, Loot loot) {
        BlockConfig config = blockConfigMap.get(loot.getID());
        if (config == null) {
            LogUtils.warn("Block: " + loot.getID() + " doesn't exist.");
            return;
        }
        String blockID = config.getBlockID();
        BlockData blockData;
        if (blockID.contains(":")) {
            String[] split = blockID.split(":", 2);
            String lib = split[0];
            String id = split[1];
            blockData = blockLibraryMap.get(lib).getBlockData(player, id, config.getDataModifier());
        } else {
            blockData = blockLibraryMap.get("vanilla").getBlockData(player, blockID, config.getDataModifier());
        }
        FallingBlock fallingBlock = hookLocation.getWorld().spawnFallingBlock(hookLocation, blockData);
        fallingBlock.getPersistentDataContainer().set(
                Objects.requireNonNull(NamespacedKey.fromString("block", CustomFishingPlugin.get())),
                PersistentDataType.STRING,
                loot.getID() + ";" + player.getName()
        );
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply((config.getHorizontalVector()) - 1);
        vector = vector.setY((vector.getY() + 0.2) * config.getVerticalVector());
        fallingBlock.setVelocity(vector);
    }

    private void registerDirectional() {
        this.registerBlockDataModifierBuilder("directional-4", (args) -> (player, blockData) -> {
            boolean arg = (boolean) args;
            if (arg && blockData instanceof Directional directional) {
                directional.setFacing(BlockFace.values()[ThreadLocalRandom.current().nextInt(0, 4)]);
            }
        });
        this.registerBlockDataModifierBuilder("directional-6", (args) -> (player, blockData) -> {
            boolean arg = (boolean) args;
            if (arg && blockData instanceof Directional directional) {
                directional.setFacing(BlockFace.values()[ThreadLocalRandom.current().nextInt(0, 6)]);
            }
        });
    }

    private void registerMoisture() {
        this.registerBlockDataModifierBuilder("moisture", (args) -> {
            int arg = (int) args;
            return (player, blockData) -> {
                if (blockData instanceof Farmland farmland) {
                    farmland.setMoisture(arg);
                }
            };
        });
    }

    private void registerCampfire() {
        this.registerBlockDataModifierBuilder("campfire", (args) -> {
            boolean arg = (boolean) args;
            return (player, blockData) -> {
                if (blockData instanceof Campfire campfire) {
                    campfire.setSignalFire(arg);
                }
            };
        });
    }

    private void registerRotatable() {
        this.registerBlockDataModifierBuilder("rotatable", (args) -> {
            boolean arg = (boolean) args;
            return (player, blockData) -> {
                if (arg && blockData instanceof Rotatable rotatable) {
                    rotatable.setRotation(BlockFace.values()[ThreadLocalRandom.current().nextInt(BlockFace.values().length)]);
                }
            };
        });
    }

    private void registerNoteBlock() {
        this.registerBlockDataModifierBuilder("noteblock", (args) -> {
            if (args instanceof ConfigurationSection section) {
                var instrument = Instrument.valueOf(section.getString("instrument"));
                var note = new Note(section.getInt("note"));
                return (player, blockData) -> {
                    if (blockData instanceof NoteBlock noteBlock) {
                        noteBlock.setNote(note);
                        noteBlock.setInstrument(instrument);
                    }
                };
            } else {
                LogUtils.warn("Invalid property format found at block noteblock.");
                return null;
            }
        });
    }

    private void registerAge() {
        this.registerBlockDataModifierBuilder("age", (args) -> {
            int arg = (int) args;
            return (player, blockData) -> {
                if (blockData instanceof Ageable ageable) {
                    ageable.setAge(arg);
                }
            };
        });
    }

    private void registerTurtleEggs() {
        this.registerBlockDataModifierBuilder("turtle-eggs", (args) -> {
            int arg = (int) args;
            return (player, blockData) -> {
                if (blockData instanceof TurtleEgg egg) {
                    egg.setEggs(arg);
                }
            };
        });
    }

    private void registerStorage() {
        this.registerBlockStateModifierBuilder("storage", (args) -> {
            if (args instanceof ConfigurationSection section) {
                ArrayList<Tuple<Double, String, Pair<Integer, Integer>>> tempChanceList = new ArrayList<>();
                for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
                    if (entry.getValue() instanceof ConfigurationSection inner) {
                        String item = inner.getString("item");
                        Pair<Integer, Integer> amountPair = ConfigUtils.splitStringIntegerArgs(inner.getString("amount","1~1"));
                        double chance = inner.getDouble("chance", 1);
                        tempChanceList.add(Tuple.of(chance, item, amountPair));
                    }
                }
                return (player, blockState) -> {
                    if (blockState instanceof Chest chest) {
                        setInventoryItems(tempChanceList, player, chest.getInventory());
                        return;
                    }
                    if (blockState instanceof Barrel barrel) {
                        setInventoryItems(tempChanceList, player, barrel.getInventory());
                        return;
                    }
                    if (blockState instanceof ShulkerBox shulkerBox) {
                        setInventoryItems(tempChanceList, player, shulkerBox.getInventory());
                        return;
                    }
                };
            } else {
                LogUtils.warn("Invalid property format found at block storage.");
                return null;
            }
        });
    }

    private void setInventoryItems(
            ArrayList<Tuple<Double, String, Pair<Integer, Integer>>> tempChanceList,
            Player player,
            Inventory inventory
    ) {
        LinkedList<Integer> unused = new LinkedList<>();
        for (int i = 0; i < 27; i++) {
            unused.add(i);
        }
        Collections.shuffle(unused);
        for (Tuple<Double, String, Pair<Integer, Integer>> tuple : tempChanceList) {
            ItemStack itemStack = plugin.getItemManager().buildAnyItemByID(player, tuple.getMid());
            itemStack.setAmount(ThreadLocalRandom.current().nextInt(tuple.getRight().left(), tuple.getRight().right() + 1));
            if (tuple.getLeft() > Math.random()) {
                inventory.setItem(unused.pop(), itemStack);
            }
        }
    }
}
