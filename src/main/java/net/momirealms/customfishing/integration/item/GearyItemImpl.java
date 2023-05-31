package net.momirealms.customfishing.integration.item;

import com.mineinabyss.geary.papermc.tracking.items.ItemTrackingKt;
import com.mineinabyss.geary.prefabs.PrefabKey;
import net.momirealms.customfishing.integration.ItemInterface;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

public class GearyItemImpl implements ItemInterface {
    @Override
    public @Nullable ItemStack build(String prefab) {
        if (!prefab.startsWith("Geary:")) return null;
        prefab = prefab.substring(6);
        PrefabKey prefabKey = PrefabKey.Companion.ofOrNull(prefab);
        if (prefabKey == null) return null;
        return ItemTrackingKt.getItemTracking().getProvider().serializePrefabToItemStack(prefabKey, null);
    }

    @Override
    public boolean loseCustomDurability(ItemStack itemStack, Player player) {
        return false;
    }
}
