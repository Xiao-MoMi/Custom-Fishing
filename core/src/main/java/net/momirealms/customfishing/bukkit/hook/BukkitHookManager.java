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

package net.momirealms.customfishing.bukkit.hook;

import com.saicone.rtag.item.ItemTagStream;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.MechanicType;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;
import net.momirealms.customfishing.api.mechanic.hook.HookConfig;
import net.momirealms.customfishing.api.mechanic.hook.HookManager;
import net.momirealms.customfishing.api.mechanic.requirement.RequirementManager;
import net.momirealms.customfishing.api.util.PlayerUtils;
import net.momirealms.customfishing.bukkit.item.damage.CustomDurabilityItem;
import net.momirealms.customfishing.bukkit.item.damage.DurabilityItem;
import net.momirealms.customfishing.bukkit.item.damage.VanillaDurabilityItem;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class BukkitHookManager implements HookManager, Listener {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, HookConfig> hooks = new HashMap<>();
    private final LZ4Factory factory;

    public BukkitHookManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.factory = LZ4Factory.fastestInstance();
    }

    @Override
    public void unload() {
        HandlerList.unregisterAll(this);
        hooks.clear();
    }

    @Override
    public void load() {
        Bukkit.getPluginManager().registerEvents(this, plugin.getBootstrap());
        plugin.debug("Loaded " + hooks.size() + " hooks");
    }

    @Override
    public boolean registerHook(HookConfig hook) {
        if (hooks.containsKey(hook.id())) return false;
        hooks.put(hook.id(), hook);
        return true;
    }

    @NotNull
    @Override
    public Optional<HookConfig> getHook(String id) {
        return Optional.ofNullable(hooks.get(id));
    }

    @Override
    public Optional<String> getHookID(ItemStack rod) {
        if (rod == null || rod.getType() != Material.FISHING_ROD || rod.getAmount() == 0)
            return Optional.empty();

        Item<ItemStack> wrapped = plugin.getItemManager().wrap(rod);
        return wrapped.getTag("CustomFishing", "hook_id").map(o -> (String) o);
    }

    @SuppressWarnings("deprecation")
    @EventHandler (ignoreCancelled = true)
    public void onDragDrop(InventoryClickEvent event) {
        final Player player = (Player) event.getWhoClicked();
        if (event.getClickedInventory() != player.getInventory())
            return;
        if (player.getGameMode() != GameMode.SURVIVAL)
            return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() != Material.FISHING_ROD)
            return;
        if (plugin.getFishingManager().getFishHook(player).isPresent())
            return;
        ItemStack cursor = event.getCursor();
        if (cursor.getType() == Material.AIR) {
            if (event.getClick() != ClickType.RIGHT) {
                return;
            }
            Item<ItemStack> wrapped = plugin.getItemManager().wrap(clicked);
            if (!wrapped.hasTag("CustomFishing", "hook_id")) {
                return;
            }
            event.setCancelled(true);
            String id = (String) wrapped.getTag("CustomFishing", "hook_id").orElseThrow();
            byte[] hookItemBase64 = (byte[]) wrapped.getTag("CustomFishing", "hook_stack").orElse(null);
            int damage = (int) wrapped.getTag("CustomFishing", "hook_damage").orElse(0);
            ItemStack itemStack;
            if (hookItemBase64 != null) {
                itemStack = bytesToHook(hookItemBase64);
            } else {
                itemStack = plugin.getItemManager().buildInternal(Context.player(player), id);
            }
            plugin.getItemManager().setDamage(player, itemStack, damage);

            wrapped.removeTag("CustomFishing", "hook_id");
            wrapped.removeTag("CustomFishing", "hook_stack");
            wrapped.removeTag("CustomFishing", "hook_damage");
            wrapped.removeTag("CustomFishing", "hook_max_damage");

            // unsafe but have to use this
            event.setCursor(itemStack);

            List<String> previousLore = wrapped.lore().orElse(new ArrayList<>());
            List<String> newLore = new ArrayList<>();
            for (String previous : previousLore) {
                Component component = AdventureHelper.jsonToComponent(previous);
                if (component instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("cf") && scoreComponent.objective().equals("hook")) {
                    continue;
                }
                newLore.add(previous);
            }
            wrapped.lore(newLore);
            wrapped.load();
            return;
        }

        String hookID = plugin.getItemManager().getItemID(cursor);
        Optional<HookConfig> setting = getHook(hookID);
        if (setting.isEmpty()) {
            return;
        }

        Context<Player> context = Context.player(player);
        HookConfig hookConfig = setting.get();
        Optional<EffectModifier> modifier = plugin.getEffectManager().getEffectModifier(hookID, MechanicType.HOOK);
        if (modifier.isPresent()) {
            if (!RequirementManager.isSatisfied(context, modifier.get().requirements())) {
                return;
            }
        }
        event.setCancelled(true);

        ItemStack clonedHook = cursor.clone();
        clonedHook.setAmount(1);
        cursor.setAmount(cursor.getAmount() - 1);

        Item<ItemStack> wrapped = plugin.getItemManager().wrap(clicked);
        String previousHookID = (String) wrapped.getTag("CustomFishing", "hook_id").orElse(null);
        if (previousHookID != null) {
            int previousHookDamage = (int) wrapped.getTag("CustomFishing", "hook_damage").orElse(0);
            ItemStack previousItemStack;
            byte[] stackBytes = (byte[]) wrapped.getTag("CustomFishing", "hook_stack").orElse(null);
            if (stackBytes != null) {
                previousItemStack = bytesToHook(stackBytes);
            } else {
                previousItemStack = plugin.getItemManager().buildInternal(Context.player(player), previousHookID);
            }
            if (previousItemStack != null) {
                plugin.getItemManager().setDamage(player, previousItemStack, previousHookDamage);
                if (cursor.getAmount() == 0) {
                    event.setCursor(previousItemStack);
                } else {
                    PlayerUtils.giveItem(player, previousItemStack, 1);
                }
            }
        }

        Item<ItemStack> wrappedHook = plugin.getItemManager().wrap(clonedHook);
        DurabilityItem durabilityItem;
        if (wrappedHook.hasTag("CustomFishing", "max_dur")) {
            durabilityItem = new CustomDurabilityItem(wrappedHook);
        } else if (hookConfig.maxUsages() > 0) {
            wrappedHook.setTag(hookConfig.maxUsages(), "CustomFishing", "max_dur");
            durabilityItem = new CustomDurabilityItem(wrappedHook);
        } else {
            durabilityItem = new VanillaDurabilityItem(wrappedHook);
        }

        wrapped.setTag(hookID, "CustomFishing", "hook_id");
        wrapped.setTag(hookToBytes(clonedHook), "CustomFishing", "hook_stack");
        wrapped.setTag(durabilityItem.damage(), "CustomFishing", "hook_damage");
        wrapped.setTag(durabilityItem.maxDamage(), "CustomFishing", "hook_max_damage");

        List<String> previousLore = wrapped.lore().orElse(new ArrayList<>());
        List<String> newLore = new ArrayList<>();
        List<String> durabilityLore = new ArrayList<>();
        for (String previous : previousLore) {
            Component component = AdventureHelper.jsonToComponent(previous);
            if (component instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("cf")) {
                if (scoreComponent.objective().equals("hook")) {
                    continue;
                } else if (scoreComponent.objective().equals("durability")) {
                    durabilityLore.add(previous);
                    continue;
                }
            }
            newLore.add(previous);
        }
        for (String lore : hookConfig.lore()) {
            ScoreComponent.Builder builder = Component.score().name("cf").objective("hook");
            builder.append(AdventureHelper.miniMessage(lore.replace("{dur}", String.valueOf(durabilityItem.maxDamage() - durabilityItem.damage())).replace("{max}", String.valueOf(durabilityItem.maxDamage()))));
            newLore.add(AdventureHelper.componentToJson(builder.build()));
        }
        newLore.addAll(durabilityLore);
        wrapped.lore(newLore);
        wrapped.load();
    }

    private byte[] hookToBytes(ItemStack hook) {
        try {
            byte[] data = ItemTagStream.INSTANCE.toBytes(hook);
            int decompressedLength = data.length;
            LZ4Compressor compressor = factory.fastCompressor();
            int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
            byte[] compressed = new byte[maxCompressedLength];
            int compressedLength = compressor.compress(data, 0, decompressedLength, compressed, 0, maxCompressedLength);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            DataOutputStream outputStream = new DataOutputStream(byteArrayOutputStream);
            outputStream.writeInt(decompressedLength);
            outputStream.write(compressed, 0, compressedLength);
            outputStream.close();

            return byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private ItemStack bytesToHook(byte[] bytes) {
        try {
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytes));
            int decompressedLength = inputStream.readInt();
            byte[] compressed = new byte[inputStream.available()];
            inputStream.readFully(compressed);

            LZ4FastDecompressor decompressor = factory.fastDecompressor();
            byte[] restored = new byte[decompressedLength];
            decompressor.decompress(compressed, 0, restored, 0, decompressedLength);

            return ItemTagStream.INSTANCE.fromBytes(restored);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
