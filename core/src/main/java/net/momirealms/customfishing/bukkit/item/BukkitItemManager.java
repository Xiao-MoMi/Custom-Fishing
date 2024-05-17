package net.momirealms.customfishing.bukkit.item;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.key.Key;
import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.integration.ItemProvider;
import net.momirealms.customfishing.api.mechanic.context.Context;
import net.momirealms.customfishing.api.mechanic.item.CustomFishingItem;
import net.momirealms.customfishing.api.mechanic.item.ItemManager;
import net.momirealms.customfishing.api.mechanic.misc.function.FormatFunction;
import net.momirealms.customfishing.api.mechanic.misc.function.ItemPropertyFunction;
import net.momirealms.customfishing.api.mechanic.misc.value.MathValue;
import net.momirealms.customfishing.api.mechanic.misc.value.TextValue;
import net.momirealms.customfishing.common.config.node.Node;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

public class BukkitItemManager implements ItemManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, ItemProvider> itemProviders = new HashMap<>();
    private final HashMap<Key, CustomFishingItem> itemMap = new HashMap<>();
    private final BukkitItemFactory factory;

    public BukkitItemManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
        this.factory = BukkitItemFactory.create(plugin);
        this.registerVanilla();
    }

    private void registerVanilla() {
        this.itemProviders.put("vanilla", new ItemProvider() {
            @NotNull
            @Override
            public ItemStack buildItem(Player player, String id) {
                return new ItemStack(Material.valueOf(id.toUpperCase(Locale.ENGLISH)));
            }
            @NotNull
            @Override
            public String itemID(ItemStack itemStack) {
                return itemStack.getType().name();
            }
            @Override
            public String identifier() {
                return "vanilla";
            }
        });
    }

    @Nullable
    @Override
    public ItemStack build(Context<Player> context, Key key) {
        CustomFishingItem item = requireNonNull(itemMap.get(key), () -> "No item found for " + key);
        ItemStack itemStack = getOriginalStack(context.getHolder(), item.material());
        Item<ItemStack> wrappedItemStack = factory.wrap(itemStack);
        for (BiConsumer<Item<ItemStack>, Context<Player>> consumer : item.tagConsumers()) {
            consumer.accept(wrappedItemStack, context);
        }
        return wrappedItemStack.getItem();
    }

    private ItemStack getOriginalStack(Player player, String material) {
        if (material.contains(":")) {
            try {
                return new ItemStack(Material.valueOf(material.toUpperCase(Locale.ENGLISH)));
            } catch (IllegalArgumentException e) {
                plugin.getPluginLogger().severe("material " + material + " not exists", e);
                return new ItemStack(Material.PAPER);
            }
        } else {
            String[] split = material.split(":", 2);
            ItemProvider provider = requireNonNull(itemProviders.get(split[0]), "item provider " + split[0] + " not found");
            return requireNonNull(provider.buildItem(player, split[0]), "item " + split[0] + " not found");
        }
    }
}
