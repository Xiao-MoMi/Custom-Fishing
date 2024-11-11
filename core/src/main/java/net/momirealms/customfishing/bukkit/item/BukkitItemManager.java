/*
 *  Copyright (C) <2024> <XiaoMoMi>
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

package net.momirealms.customfishing.bukkit.item;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.integration.ExternalProvider;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.context.ContextKeys;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.api.util.EventUtils;
import net.momirealms.customfishing.bukkit.integration.item.CustomFishingItemProvider;
import net.momirealms.customfishing.bukkit.item.damage.CustomDurabilityItem;
import net.momirealms.customfishing.bukkit.item.damage.DurabilityItem;
import net.momirealms.customfishing.bukkit.item.damage.VanillaDurabilityItem;
import net.momirealms.customfishing.bukkit.util.ItemStackUtils;
import net.momirealms.customfishing.bukkit.util.LocationUtils;
import net.momirealms.customfishing.common.item.Item;
import net.momirealms.sparrow.heart.SparrowHeart;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.Skull;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerItemMendEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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
                try {
                    return new ItemStack(Material.valueOf(id.toUpperCase(Locale.ENGLISH)));
                } catch (IllegalArgumentException e) {
                    return new ItemStack(requireNonNull(Registry.MATERIAL.get(new NamespacedKey("minecraft", id.toLowerCase(Locale.ENGLISH)))));
                }
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
        Bukkit.getPluginManager().registerEvents(this, plugin.getBootstrap());
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
        ItemStack itemStack = getOriginalStack(context.holder(), item.material());
        if (itemStack.getType() == Material.AIR) return itemStack;
        plugin.getLootManager().getLoot(item.id()).ifPresent(loot -> {
            for (Map.Entry<String, TextValue<Player>> entry : loot.customData().entrySet()) {
                context.arg(ContextKeys.of("data_" + entry.getKey(), String.class), entry.getValue().render(context));
            }
        });
        itemStack.setAmount(Math.max(1, (int) item.amount().evaluate(context)));
        Item<ItemStack> wrappedItemStack = factory.wrap(itemStack);
        for (BiConsumer<Item<ItemStack>, Context<Player>> consumer : item.tagConsumers()) {
            consumer.accept(wrappedItemStack, context);
        }
        return wrappedItemStack.load();
    }

    @Override
    public ItemStack buildAny(@NotNull Context<Player> context, @NotNull String item) {
        return getOriginalStack(context.holder(), item);
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

    @NotNull
    @Override
    public ItemStack getItemLoot(@NotNull Context<Player> context, ItemStack rod, FishHook hook) {
        String id = requireNonNull(context.arg(ContextKeys.ID));
        ItemStack itemStack;
        if (id.equals("vanilla")) {
            itemStack = SparrowHeart.getInstance().getFishingLoot(context.holder(), hook, rod).stream().findAny().orElseThrow(() -> new RuntimeException("new EntityItem would throw if for whatever reason (mostly shitty datapacks) the fishing loot turns out to be empty"));
        } else {
            itemStack = requireNonNull(buildInternal(context, id));
        }
        return itemStack;
    }

    @Nullable
    @Override
    public org.bukkit.entity.Item dropItemLoot(@NotNull Context<Player> context, ItemStack rod, FishHook hook) {
        ItemStack itemStack = getItemLoot(context, rod, hook);
        if (itemStack.getType() == Material.AIR) {
            return null;
        }

        Player player = context.holder();
        Location playerLocation = player.getLocation();
        Location hookLocation = requireNonNull(context.arg(ContextKeys.OTHER_LOCATION));

        double d0 = playerLocation.getX() - hookLocation.getX();
        double d1 = playerLocation.getY() - hookLocation.getY();
        double d2 = playerLocation.getZ() - hookLocation.getZ();
        Vector vector = new Vector(d0 * 0.1D, d1 * 0.1D + Math.sqrt(Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2)) * 0.08D, d2 * 0.1D);

        org.bukkit.entity.Item itemEntity = hookLocation.getWorld().dropItem(hookLocation, itemStack);

        itemEntity.setInvulnerable(true);
        // prevent from being killed by lava
        plugin.getScheduler().sync().runLater(() -> {
            if (itemEntity.isValid())
                itemEntity.setInvulnerable(false);
        }, 20, hookLocation);

        itemEntity.setVelocity(vector);

        return itemEntity;
    }

    private ItemStack getOriginalStack(Player player, String material) {
        if (!material.contains(":")) {
            try {
                return new ItemStack(Material.valueOf(material.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                Material another = Registry.MATERIAL.get(new NamespacedKey("minecraft", material.toLowerCase(Locale.ENGLISH)));
                if (another != null) {
                    return new ItemStack(another);
                }
                plugin.getPluginLogger().severe("material " + material + " not exists", e);
                return new ItemStack(Material.PAPER);
            }
        } else {
            String[] split = material.split(":", 2);
            ItemProvider provider = requireNonNull(itemProviders.get(split[0]), "Item provider: " + split[0] + " not found");
            return requireNonNull(provider.buildItem(player, split[1]), "Item: " + split[0] + " not found");
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
    public boolean hasCustomMaxDamage(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
            return false;
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        return wrapped.hasTag("CustomFishing", "max_dur");
    }

    @Override
    public int getMaxDamage(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
            return 0;
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        if (wrapped.hasTag("CustomFishing", "max_dur")) {
            return new CustomDurabilityItem(wrapped).maxDamage();
        } else {
            return new VanillaDurabilityItem(wrapped).maxDamage();
        }
    }

    @Override
    public void decreaseDamage(Player player, ItemStack itemStack, int amount) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
            return;
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        DurabilityItem durabilityItem;
        if (wrapped.hasTag("CustomFishing", "max_dur")) {
            durabilityItem = new CustomDurabilityItem(wrapped);
        } else {
            durabilityItem = new VanillaDurabilityItem(wrapped);
        }
        durabilityItem.damage(Math.max(0, durabilityItem.damage() - amount));
        wrapped.load();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void increaseDamage(Player player, ItemStack itemStack, int amount, boolean incorrectUsage) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
            return;
        if (!incorrectUsage) {
            int unBreakingLevel = itemStack.getEnchantmentLevel(Enchantment.DURABILITY);
            if (Math.random() > (double) 1 / (unBreakingLevel + 1)) {
                return;
            }
        }
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        if (wrapped.unbreakable())
            return;

        ItemMeta previousMeta = itemStack.getItemMeta().clone();
        // use event from Spigot for compatibility
        PlayerItemDamageEvent itemDamageEvent = new PlayerItemDamageEvent(player, itemStack, amount);
        if (EventUtils.fireAndCheckCancel(itemDamageEvent)) {
            plugin.debug("Another plugin modified the item from `PlayerItemDamageEvent` called by CustomFishing");
            return;
        }
        if (!itemStack.getItemMeta().equals(previousMeta)) {
            return;
        }

        DurabilityItem durabilityItem = wrapDurabilityItem(wrapped);
        int damage = durabilityItem.damage();
        if (damage + amount >= durabilityItem.maxDamage()) {
            plugin.getSenderFactory().getAudience(player).playSound(Sound.sound(Key.key("minecraft:entity.item.break"), Sound.Source.PLAYER, 1, 1));
            itemStack.setAmount(0);
            return;
        }

        durabilityItem.damage(damage + amount);
        wrapped.load();
    }

    @Override
    public void setDamage(Player player, ItemStack itemStack, int damage) {
        if (itemStack == null || itemStack.getType() == Material.AIR || itemStack.getAmount() == 0)
            return;
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        if (wrapped.unbreakable())
            return;
        DurabilityItem wrappedDurability = wrapDurabilityItem(wrapped);
        if (damage >= wrappedDurability.maxDamage()) {
            if (player != null)
                plugin.getSenderFactory().getAudience(player).playSound(Sound.sound(Key.key("minecraft:entity.item.break"), Sound.Source.PLAYER, 1, 1));
            itemStack.setAmount(0);
            return;
        }
        wrappedDurability.damage(damage);
        wrapped.load();
    }

    public DurabilityItem wrapDurabilityItem(Item<ItemStack> wrapped) {
        if (wrapped.hasTag("CustomFishing", "max_dur")) {
            return new CustomDurabilityItem(wrapped);
        } else {
            return new VanillaDurabilityItem(wrapped);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onMending(PlayerItemMendEvent event) {
        ItemStack itemStack = event.getItem();
        if (!hasCustomMaxDamage(itemStack)) {
            return;
        }
        event.setCancelled(true);
        Item<ItemStack> wrapped = factory.wrap(itemStack);
        if (wrapped.unbreakable())
            return;
        DurabilityItem wrappedDurability = wrapDurabilityItem(wrapped);
        setDamage(event.getPlayer(), itemStack, Math.max(wrappedDurability.damage() - event.getRepairAmount(), 0));
    }

    @EventHandler (ignoreCancelled = true)
    public void onAnvil(PrepareAnvilEvent event) {
        AnvilInventory anvil = event.getInventory();
        ItemStack first = anvil.getFirstItem();
        ItemStack second = anvil.getSecondItem();
        if (first != null && second != null
                && first.getType() == Material.FISHING_ROD && second.getType() == Material.FISHING_ROD && event.getResult() != null
                && hasCustomMaxDamage(first)) {
            Item<ItemStack> wrapped1 = factory.wrap(anvil.getResult());
            DurabilityItem wrappedDurability1 = wrapDurabilityItem(wrapped1);

            Item<ItemStack> wrapped2 = factory.wrap(second);
            DurabilityItem wrappedDurability2 = wrapDurabilityItem(wrapped2);

            int durability2 = wrappedDurability2.maxDamage() - wrappedDurability2.damage();
            int damage1 = Math.max(wrappedDurability1.damage() - durability2, 0);
            wrappedDurability1.damage(damage1);
            event.setResult(wrapped1.load());
        }
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
            if (!wrapped.hasTag("CustomFishing", "placeable") || ((int) wrapped.getTag("CustomFishing", "placeable").get()) != 1) {
                event.setCancelled(true);
                return;
            }
            Block block = event.getBlock();
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                ItemStack cloned = itemStack.clone();
                cloned.setAmount(1);
                pdc.set(new NamespacedKey(plugin.getBootstrap(), LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING, ItemStackUtils.toBase64(cloned));
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
            NamespacedKey key = new NamespacedKey(plugin.getBootstrap(), LocationUtils.toChunkPosString(block.getLocation()));
            String base64 = pdc.get(key, PersistentDataType.STRING);
            if (base64 != null) {
                pdc.remove(key);
                if (event.getPlayer().getGameMode() == GameMode.CREATIVE)
                    return;
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
                if (pdc.has(new NamespacedKey(plugin.getBootstrap(), LocationUtils.toChunkPosString(block.getLocation())), PersistentDataType.STRING)) {
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

    @EventHandler (ignoreCancelled = true)
    public void onPickUpItem(EntityPickupItemEvent event) {
        String owner = event.getItem().getPersistentDataContainer().get(requireNonNull(NamespacedKey.fromString("owner", plugin.getBootstrap())), PersistentDataType.STRING);
        if (owner != null) {
            if (!(event.getEntity() instanceof Player player)) {
                event.setCancelled(true);
                return;
            }
            if (!owner.equals(player.getName())) {
                event.setCancelled(true);
            }
        }
    }

    private void handleExplosion(List<Block> blocks) {
        ArrayList<Block> blockToRemove = new ArrayList<>();
        for (Block block : blocks) {
            if (block.getState() instanceof Skull) {
                PersistentDataContainer pdc = block.getChunk().getPersistentDataContainer();
                var nk = new NamespacedKey(plugin.getBootstrap(), LocationUtils.toChunkPosString(block.getLocation()));
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
