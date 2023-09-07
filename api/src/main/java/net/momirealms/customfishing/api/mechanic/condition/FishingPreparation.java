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

package net.momirealms.customfishing.api.mechanic.condition;

import net.momirealms.customfishing.api.CustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.effect.Effect;
import net.momirealms.customfishing.api.mechanic.effect.EffectCarrier;
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
    private final @Nullable EffectCarrier rodEffect;
    private @Nullable ItemStack baitItemStack;
    private @Nullable String baitItemID;
    private @Nullable EffectCarrier baitEffect;
    private final List<EffectCarrier> utilEffects;
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
        super.insertArg("{rod}", this.rodItemID);

        String baitItemID = plugin.getItemManager().getAnyItemID(this.rodOnMainHand ? offHandItem : mainHandItem);
        EffectCarrier baitEffect = plugin.getEffectManager().getEffect("bait", baitItemID);

        if (baitEffect != null) {
            this.baitItemID = baitItemID;
            this.baitItemStack = this.rodOnMainHand ? offHandItem : mainHandItem;
            this.baitEffect = baitEffect;
        }

        if (plugin.getBagManager().isBagEnabled()) {
            Inventory fishingBag = plugin.getBagManager().getOnlineBagInventory(player.getUniqueId());
            HashSet<String> uniqueUtils = new HashSet<>(4);
            if (fishingBag != null) {
                for (int i = 0; i < fishingBag.getSize(); i++) {
                    ItemStack itemInBag = fishingBag.getItem(i);
                    String bagItemID = plugin.getItemManager().getItemID(itemInBag);
                    if (bagItemID == null) continue;
                    if (this.baitItemID == null) {
                        EffectCarrier effect = plugin.getEffectManager().getEffect("bait", bagItemID);
                        if (effect != null) {
                            this.baitItemID = bagItemID;
                            this.baitItemStack = itemInBag;
                            this.baitEffect = effect;
                            continue;
                        }
                    }
                    EffectCarrier utilEffect = plugin.getEffectManager().getEffect("util", bagItemID);
                    if (utilEffect != null && !uniqueUtils.contains(bagItemID) && utilEffect.isConditionMet(this)) {
                        utilEffects.add(utilEffect);
                        uniqueUtils.add(bagItemID);
                    }
                }
            }
        }

        if (this.baitEffect != null) {
            if (!this.baitEffect.isConditionMet(this)) {
                this.canFish = false;
                return;
            }
            super.insertArg("{bait}", this.baitItemID);
        }

        if (this.rodEffect != null) {
            if (!this.rodEffect.isConditionMet(this)) {
                this.canFish = false;
                return;
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

    public boolean canFish() {
        return this.canFish;
    }

    @NotNull
    @Override
    public Player getPlayer() {
        return super.player;
    }

    public Effect mergeEffect(Effect effect) {
        if (this.rodEffect != null)
            effect.merge(this.rodEffect.getEffect());
        if (this.baitEffect != null)
            effect.merge(this.baitEffect.getEffect());
        for (EffectCarrier util : utilEffects) {
            effect.merge(util.getEffect());
        }
        return effect;
    }
}
