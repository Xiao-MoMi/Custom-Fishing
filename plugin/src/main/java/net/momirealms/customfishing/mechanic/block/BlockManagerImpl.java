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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Barrel;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityChangeBlockEvent;
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
        this.registerStorage();
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
        fallingBlock.setDropItem(false);
        Vector vector = playerLocation.subtract(hookLocation).toVector().multiply((config.getHorizontalVector()) - 1);
        vector = vector.setY((vector.getY() + 0.2) * config.getVerticalVector());
        fallingBlock.setVelocity(vector);
    }

    private void registerDirectional() {
        this.registerBlockDataModifierBuilder("directional", (args) -> {
            boolean arg = (boolean) args;
            return (player, blockData) -> {
                if (arg && blockData instanceof Directional directional) {
                    directional.setFacing(BlockFace.values()[ThreadLocalRandom.current().nextInt(0,6)]);
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
                    LinkedList<Integer> unused = new LinkedList<>();
                    for (int i = 0; i < 27; i++) {
                        unused.add(i);
                    }
                    Collections.shuffle(unused);
                    if (blockState instanceof Chest chest) {
                        for (Tuple<Double, String, Pair<Integer, Integer>> tuple : tempChanceList) {
                            ItemStack itemStack = plugin.getItemManager().buildAnyItemByID(player, tuple.getMid());
                            itemStack.setAmount(ThreadLocalRandom.current().nextInt(tuple.getRight().left(), tuple.getRight().right() + 1));
                            if (tuple.getLeft() > Math.random()) {
                                chest.getBlockInventory().setItem(unused.pop(), itemStack);
                            }
                        }
                        return;
                    }
                    if (blockState instanceof Barrel barrel) {
                        for (Tuple<Double, String, Pair<Integer, Integer>> tuple : tempChanceList) {
                            ItemStack itemStack = plugin.getItemManager().buildAnyItemByID(player, tuple.getMid());
                            itemStack.setAmount(ThreadLocalRandom.current().nextInt(tuple.getRight().left(), tuple.getRight().right() + 1));
                            if (tuple.getLeft() > Math.random()) {
                                barrel.getInventory().setItem(unused.pop(), itemStack);
                            }
                        }
                    }
                };
            } else {
                LogUtils.warn("Invalid property format found at block storage.");
                return null;
            }
        });
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
}
