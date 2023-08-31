package net.momirealms.customfishing.api.mechanic.condition;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class FishingPreparation extends Condition {

    private final boolean rodOnMainHand;
    private final @NotNull ItemStack rodItemStack;
    private final @NotNull String rodItemID;
    private final @Nullable Effect rodEffect;
    private @Nullable ItemStack baitItemStack;
    private @Nullable String baitItemID;
    private @Nullable Effect baitEffect;
    private final List<Effect> utilEffects;
    private boolean canFish = true;

    public FishingPreparation(Player player, CustomFishingPlugin plugin) {
        super(player);

        PlayerInventory playerInventory = player.getInventory();
        ItemStack mainHandItem = playerInventory.getItemInMainHand();
        ItemStack offHandItem = playerInventory.getItemInOffHand();

        this.utilEffects = new ArrayList<>();
        this.rodOnMainHand = mainHandItem.getType() == Material.FISHING_ROD;
        this.rodItemStack = this.rodOnMainHand ? mainHandItem : offHandItem;
        this.rodItemID = plugin.getItemManager().getAnyItemID(this.rodItemStack);
        this.rodEffect = plugin.getEffectManager().getEffect("rod", this.rodItemID);
        super.insertArg("rod", this.rodItemID);

        String baitItemID = plugin.getItemManager().getAnyItemID(this.rodOnMainHand ? offHandItem : mainHandItem);
        Effect baitEffect = plugin.getEffectManager().getEffect("bait", baitItemID);
        if (baitEffect != null) {
            this.baitItemID = baitItemID;
            this.baitItemStack = this.rodOnMainHand ? offHandItem : mainHandItem;
            this.baitEffect = baitEffect;
        } else if (plugin.getBagManager().isBagEnabled()) {
            Inventory fishingBag = plugin.getBagManager().getOnlineBagInventory(player.getUniqueId());
            HashSet<String> uniqueUtils = new HashSet<>(4);
            if (fishingBag != null) {
                for (int i = 0; i < fishingBag.getSize(); i++) {
                    ItemStack itemInBag = fishingBag.getItem(i);
                    String bagItemID = plugin.getItemManager().getItemID(itemInBag);
                    if (bagItemID == null) continue;
                    if (this.baitEffect == null) {
                        Effect effect = plugin.getEffectManager().getEffect("bait", bagItemID);
                        if (effect != null) {
                            this.baitItemID = bagItemID;
                            this.baitItemStack = itemInBag;
                            this.baitEffect = effect;
                            continue;
                        }
                    }
                    Effect utilEffect = plugin.getEffectManager().getEffect("util", bagItemID);
                    if (utilEffect != null
                            && !uniqueUtils.contains(bagItemID)
                            && utilEffect.canMerge(this)) {
                        utilEffects.add(utilEffect);
                        uniqueUtils.add(bagItemID);
                    }
                }
            }
        } else {
            this.baitItemID = null;
            this.baitItemStack = null;
            this.baitEffect = null;
        }

        if (this.baitEffect != null) {
            if (!this.baitEffect.canMerge(this)) {
                this.canFish = false;
                return;
            }
            super.insertArg("bait", this.baitItemID);
        }

        if (this.rodEffect != null) {
            if (!this.rodEffect.canMerge(this)) {
                this.canFish = false;
            }
        }
    }

    public boolean isRodOnMainHand() {
        return rodOnMainHand;
    }

    @NotNull
    public ItemStack getRodItemStack() {
        return rodItemStack;
    }

    @NotNull
    public String getRodItemID() {
        return rodItemID;
    }

    @Nullable
    public ItemStack getBaitItemStack() {
        return baitItemStack;
    }

    @Nullable
    public String getBaitItemID() {
        return baitItemID;
    }

    @Nullable
    public Effect getRodEffect() {
        return rodEffect;
    }

    @Nullable
    public Effect getBaitEffect() {
        return baitEffect;
    }

    public boolean canFish() {
        return this.canFish;
    }

    @Override
    public @NotNull Player getPlayer() {
        assert super.player != null;
        return super.player;
    }

    public List<Effect> getUtilEffects() {
        return utilEffects;
    }
}
