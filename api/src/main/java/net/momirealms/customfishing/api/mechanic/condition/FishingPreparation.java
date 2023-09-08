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
import net.momirealms.customfishing.api.mechanic.GlobalSettings;
import net.momirealms.customfishing.api.mechanic.action.Action;
import net.momirealms.customfishing.api.mechanic.action.ActionTrigger;
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

    private @Nullable EffectCarrier baitEffect;
    private final @Nullable EffectCarrier rodEffect;
    private @Nullable ItemStack baitItemStack;
    private final @NotNull ItemStack rodItemStack;
    private final List<EffectCarrier> utilEffects;
    private final Effect enchantEffect;
    private boolean canFish = true;

    public FishingPreparation(Player player, CustomFishingPlugin plugin) {
        super(player);

        PlayerInventory playerInventory = player.getInventory();
        ItemStack mainHandItem = playerInventory.getItemInMainHand();
        ItemStack offHandItem = playerInventory.getItemInOffHand();

        this.utilEffects = new ArrayList<>();
        this.enchantEffect = plugin.getEffectManager().getInitialEffect();
        boolean rodOnMainHand = mainHandItem.getType() == Material.FISHING_ROD;
        this.rodItemStack = rodOnMainHand ? mainHandItem : offHandItem;
        String rodItemID = plugin.getItemManager().getAnyItemID(this.rodItemStack);
        this.rodEffect = plugin.getEffectManager().getEffect("rod", rodItemID);
        super.insertArg("{rod}", rodItemID);

        String baitItemID = plugin.getItemManager().getAnyItemID(rodOnMainHand ? offHandItem : mainHandItem);
        EffectCarrier baitEffect = plugin.getEffectManager().getEffect("bait", baitItemID);

        if (baitEffect != null) {
            this.baitItemStack = rodOnMainHand ? offHandItem : mainHandItem;
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
                    if (this.baitEffect == null) {
                        EffectCarrier effect = plugin.getEffectManager().getEffect("bait", bagItemID);
                        if (effect != null) {
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
            super.insertArg("{bait}", this.baitEffect.getKey().value());
        }

        if (this.rodEffect != null) {
            if (!this.rodEffect.isConditionMet(this)) {
                this.canFish = false;
                return;
            }
        }

        for (String enchant : plugin.getIntegrationManager().getEnchantments(rodItemStack)) {
            EffectCarrier enchantEffect = plugin.getEffectManager().getEffect("enchant", enchant);
            if (enchantEffect != null && enchantEffect.isConditionMet(this)) {
                this.enchantEffect.merge(enchantEffect.getEffect());
            }
        }
    }

    @NotNull
    public ItemStack getRodItemStack() {
        return rodItemStack;
    }

    @Nullable
    public ItemStack getBaitItemStack() {
        return baitItemStack;
    }

    @Nullable
    public EffectCarrier getBaitEffect() {
        return baitEffect;
    }

    @Nullable
    public EffectCarrier getRodEffect() {
        return rodEffect;
    }

    public boolean canFish() {
        return this.canFish;
    }

    public void mergeEffect(Effect effect) {
        if (this.rodEffect != null)
            effect.merge(this.rodEffect.getEffect());
        if (this.enchantEffect != null)
            effect.merge(this.enchantEffect);
        if (this.baitEffect != null)
            effect.merge(this.baitEffect.getEffect());
        for (EffectCarrier util : utilEffects) {
            effect.merge(util.getEffect());
        }
    }

    public void triggerActions(ActionTrigger actionTrigger) {
        GlobalSettings.triggerRodActions(actionTrigger, this);
        if (rodEffect != null) {
            Action[] actions = rodEffect.getActions(actionTrigger);
            if (actions != null)
                for (Action action : actions) {
                    action.trigger(this);
                }
        }

        if (baitEffect != null) {
            GlobalSettings.triggerBaitActions(actionTrigger, this);
            Action[] actions = baitEffect.getActions(actionTrigger);
            if (actions != null)
                for (Action action : actions) {
                    action.trigger(this);
                }
        }
    }
}
