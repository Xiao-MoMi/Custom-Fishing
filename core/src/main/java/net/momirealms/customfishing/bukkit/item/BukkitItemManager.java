package net.momirealms.customfishing.bukkit.item;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.event.FishingLootSpawnEvent;
import net.momirealms.customfishing.api.integration.ExternalProvider;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.api.mechanic.item.MechanicType;
import net.momirealms.customfishing.bukkit.integration.item.CustomFishingItemProvider;
import net.momirealms.customfishing.bukkit.util.ItemStackUtils;
import net.momirealms.customfishing.bukkit.util.LocationUtils;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

import static java.util.Objects.requireNonNull;

public class BukkitItemManager implements ItemManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, ItemProvider> itemProviders = new HashMap<>();
    private final HashMap<String, CustomFishingItem> items = new HashMap<>();
    private final BukkitItemFactory factory;
    private ItemProvider[] itemDetectArray;

    public BukkitItemManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.factory = BukkitItemFactory.create(plugin);
        this.registerItemProvider(new ItemProvider() {
            @NotNull
            @Override
            public ItemStack buildItem(@NotNull Player player, @NotNull String id) {
                return new ItemStack(Material.valueOf(id.toUpperCase(Locale.ENGLISH)));
            }
            @NotNull
            @Override
            public String itemID(@NotNull ItemStack itemStack) {
                return itemStack.getType().name();
            }
            @Override
            public String identifier() {
                return "vanilla";
            }
        });
        this.registerItemProvider(new CustomFishingItemProvider());
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        this.items.clear();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin.getBoostrap());
        this.resetItemDetectionOrder();
        for (ItemProvider provider : itemProviders.values()) {
            plugin.debug("Registered ItemProvider: " + provider.identifier());
        }
        plugin.debug("Loaded " + items.size() + " items");
        plugin.debug("Item order: " + Arrays.toString(Arrays.stream(itemDetectArray).map(ExternalProvider::identifier).toList().toArray(new String[0])));
    }

    @Override
    public boolean registerItem(@NotNull CustomFishingItem item) {
        if (items.containsKey(item.id())) return false;
        items.put(item.id(), item);
        return true;
    }

    @Nullable
    @Override
    public ItemStack buildInternal(@NotNull Context<Player> context, @NotNull String id) {
//        CustomFishingItem item = requireNonNull(items.get(id), () -> "No item found for " + id);
        CustomFishingItem item = items.get(id);
        if (item == null) return null;
        return build(context, item);
    }

    @NotNull
    @Override
    public ItemStack build(@NotNull Context<Player> context, @NotNull CustomFishingItem item) {
        ItemStack itemStack = getOriginalStack(context.getHolder(), item.material());
        if (itemStack.getType() == Material.AIR) return itemStack;
        Item<ItemStack> wrappedItemStack = factory.wrap(itemStack);
        for (BiConsumer<Item<ItemStack>, Context<Player>> consumer : item.tagConsumers()) {
            consumer.accept(wrappedItemStack, context);
        }
        return wrappedItemStack.load();
    }

    @Override
    public ItemStack buildAny(@NotNull Context<Player> context, @NotNull String item) {
        return getOriginalStack(context.getHolder(), item);
    }

    @NotNull
    @Override
    public String getItemID(@NotNull ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR)
            return "AIR";
        for (ItemProvider library : itemDetectArray) {
            String id = library.itemID(itemStack);
            if (id != null)
                return id;
        }
        // should not reach this because vanilla library would always work
        return "AIR";
    }

    @Override
    public String getCustomFishingItemID(@NotNull ItemStack itemStack) {
        return (String) factory.wrap(itemStack).getTag("CustomFishing", "id").orElse(null);
    }

    @Nullable
    @Override
    public MechanicType getItemType(@NotNull ItemStack itemStack) {
        return MechanicType.getTypeByID(getCustomFishingItemID(itemStack));
    }

    @Nullable
    @Override
    public MechanicType getItemType(@NotNull String id) {
        return MechanicType.getTypeByID(id);
    }

    @Nullable
    @Override
    public org.bukkit.entity.Item dropItemLoot(@NotNull Context<Player> context, ItemStack rod, FishHook hook) {
        String id = requireNonNull(context.arg(ContextKeys.ID));
        ItemStack itemStack;
        if (id.equals("vanilla")) {
            itemStack = SparrowHeart.getInstance().getFishingLoot(context.getHolder(), hook, rod).stream().findAny().orElseThrow(() -> new RuntimeException("new EntityItem would throw if for whatever reason (mostly shitty datapacks) the fishing loot turns out to be empty"));
        } else {
            itemStack = requireNonNull(buildInternal(context, id));
        }

        if (itemStack.getType() == Material.AIR) {
            return null;
        }

        Player player = context.getHolder();
        Location playerLocation = player.getLocation();
        Location hookLocation = requireNonNull(context.arg(ContextKeys.HOOK_LOCATION));

        double d0 = playerLocation.getX() - hookLocation.getX();
        double d1 = playerLocation.getY() - hookLocation.getY();
        double d2 = playerLocation.getZ() - hookLocation.getZ();
        Vector vector = new Vector(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);

        org.bukkit.entity.Item itemEntity = hookLocation.getWorld().dropItem(hookLocation, itemStack);
        FishingLootSpawnEvent spawnEvent = new FishingLootSpawnEvent(player, hookLocation, plugin.getLootManager().getLoot(id).orElseThrow(), itemEntity);
        Bukkit.getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled()) {
            itemEntity.remove();
            return itemEntity;
        }

        itemEntity.setInvulnerable(true);
        // prevent from being killed by lava
        plugin.getScheduler().asyncLater(() -> {
            if (itemEntity.isValid())
                itemEntity.setInvulnerable(false);
        }, 1, TimeUnit.SECONDS);

        itemEntity.setVelocity(vector);

        return itemEntity;
    }

    private ItemStack getOriginalStack(Player player, String material) {
        if (!material.contains(":")) {
            try {
                return new ItemStack(Material.valueOf(material.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                plugin.getPluginLogger().severe("material " + material + " not exists", e);
                return new ItemStack(Material.PAPER);
            }
        } else {
            String[] split = material.split(":", 2);
            ItemProvider provider = requireNonNull(itemProviders.get(split[0]), "Item provider: " + split[0] + " not found");
            return requireNonNull(provider.buildItem(player, split[0]), "Item: " + split[0] + " not found");
        }
    }

    private void resetItemDetectionOrder() {
        ArrayList<ItemProvider> list = new ArrayList<>();
        for (String plugin : ConfigManager.itemDetectOrder()) {
            ItemProvider provider = itemProviders.get(plugin);
            if (provider != null)
                list.add(provider);
        }
        this.itemDetectArray = list.toArray(new ItemProvider[0]);
    }

    public boolean registerItemProvider(ItemProvider item) {
        if (itemProviders.containsKey(item.identifier())) return false;
        itemProviders.put(item.identifier(), item);
        this.resetItemDetectionOrder();
        return true;
    }

    public boolean unregisterItemProvider(String id) {
        boolean success = itemProviders.remove(id) != null;
        if (success)
            this.resetItemDetectionOrder();
        return success;
    }

    @Override
    public void decreaseDurability(ItemStack itemStack, int amount, boolean incorrectUsage) {

    }

    @EventHandler(ignoreCancelled = true)
    public void onInvPickItem(InventoryPickupItemEvent event) {
        ItemStack itemStack = event.getItem().getItemStack();
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        if (wrapped.hasTag("owner")) {
            wrapped.removeTag("owner");
            itemStack.setItemMeta(wrapped.getItem().getItemMeta());
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPlaceBlock(BlockPlaceEvent event) {
        ItemStack itemStack = event.getItemInHand();
        if (itemStack.getType() == Material.AIR || itemStack.getAmount() == 0 || !itemStack.hasItemMeta()) {
            return;
        }

        Item<ItemStack> wrapped = factory.wrap(itemStack);
        if (wrapped.hasTag("CustomFishing")) {
            if (!wrapped.hasTag("CustomFishing", "placeable")) {
                event.setCancelled(true);
                return;
            }
            Block block = event.getBlock();
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(1);
                pdc.set(new NamespacedKey(plugin.getBoostrap(), LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING, ItemStackUtils.toBase64(cloned));
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
            String base64 = pdc.get(new NamespacedKey(plugin.getBoostrap(), LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING);
            if (base64 != null) {
                ItemStack itemStack = ItemStackUtils.fromBase64(base64);
                event.setDropItems(false);
                block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onPiston(BlockPistonExtendEvent event) {
        handlePiston(event, event.getBlocks());
    }

    @EventHandler (ignoreCancelled = true)
    public void onPiston(BlockPistonRetractEvent event) {
        handlePiston(event, event.getBlocks());
    }

    private void handlePiston(Cancellable event, List<Block> blockList) {
        for (Block block : blockList) {
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                if (pdc.has(new NamespacedKey(plugin.getBoostrap(), LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING)) {
                    event.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onExplosion(BlockExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    @EventHandler (ignoreCancelled = true)
    public void onExplosion(EntityExplodeEvent event) {
        handleExplosion(event.blockList());
    }

    private void handleExplosion(List<Block> blocks) {
        ArrayList<Block> blockToRemove = new ArrayList<>();
        for (Block block : blocks) {
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                var nk = new NamespacedKey(plugin.getBoostrap(), LocationUtils.toChunkPosString(block.getLocation()));
                String base64 = pdc.get(nk, PersistentDataType.STRING);
                if (base64 != null) {
                    ItemStack itemStack = ItemStackUtils.fromBase64(base64);
                    block.getLocation().getWorld().dropItemNaturally(block.getLocation(), itemStack);
                    blockToRemove.add(block);
                    block.setType(Material.AIR);
                    pdc.remove(nk);
                }
            }
        }
        blocks.removeAll(blockToRemove);
    }

    @Override
    public BukkitItemFactory getFactory() {
        return factory;
    }

    @Override
    public ItemProvider[] getItemProviders() {
        return itemProviders.values().toArray(new ItemProvider[0]);
    }

    @Override
    public Collection<String> getItemIDs() {
        return items.keySet();
    }

    @Override
    public Item<ItemStack> wrap(ItemStack itemStack) {
        return factory.wrap(itemStack);
    }
}