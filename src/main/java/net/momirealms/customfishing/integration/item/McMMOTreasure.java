package net.momirealms.customfishing.integration.item;

import com.gmail.nossr50.config.treasure.FishingTreasureConfig;
import com.gmail.nossr50.datatypes.skills.SubSkillType;
import com.gmail.nossr50.datatypes.treasure.EnchantmentTreasure;
import com.gmail.nossr50.datatypes.treasure.FishingTreasure;
import com.gmail.nossr50.datatypes.treasure.FishingTreasureBook;
import com.gmail.nossr50.datatypes.treasure.Rarity;
import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.skills.fishing.FishingManager;
import com.gmail.nossr50.util.ItemUtils;
import com.gmail.nossr50.util.Misc;
import com.gmail.nossr50.util.Permissions;
import com.gmail.nossr50.util.player.UserManager;
import com.gmail.nossr50.util.skills.RankUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class McMMOTreasure {

    public static ItemStack getTreasure(Player player){
        FishingManager fishingManager = UserManager.getPlayer(player).getFishingManager();
        FishingTreasure treasure = getFishingTreasure(player, fishingManager.getLootTier());
        ItemStack treasureDrop;
        if (treasure != null) {
            if(treasure instanceof FishingTreasureBook) {
                treasureDrop = ItemUtils.createEnchantBook((FishingTreasureBook) treasure);
            } else {
                treasureDrop = treasure.getDrop().clone();
            }
            Map<Enchantment, Integer> enchants = new HashMap<>();
            if(treasure instanceof FishingTreasureBook) {
                if(treasureDrop.getItemMeta() != null) {
                    enchants = new HashMap<>(treasureDrop.getItemMeta().getEnchants());
                }
            } else {
                if (isMagicHunterEnabled(player) && ItemUtils.isEnchantable(treasureDrop)) {
                    enchants = processMagicHunter(treasureDrop, fishingManager.getLootTier());
                }
            }
            if (!enchants.isEmpty()) {
                treasureDrop.addUnsafeEnchantments(enchants);
            }
            return treasureDrop;
        }
        return null;
    }

    public static boolean isMagicHunterEnabled(Player player) {
        return RankUtils.hasUnlockedSubskill(player, SubSkillType.FISHING_MAGIC_HUNTER)
                && RankUtils.hasUnlockedSubskill(player, SubSkillType.FISHING_TREASURE_HUNTER)
                && Permissions.isSubSkillEnabled(player, SubSkillType.FISHING_TREASURE_HUNTER);
    }

    private static Map<Enchantment, Integer> processMagicHunter(@NotNull ItemStack treasureDrop, int tier) {
        Map<Enchantment, Integer> enchants = new HashMap<>();
        List<EnchantmentTreasure> fishingEnchantments = null;
        double diceRoll = Misc.getRandom().nextDouble() * 100;
        for (Rarity rarity : Rarity.values()) {
            double dropRate = FishingTreasureConfig.getInstance().getEnchantmentDropRate(tier, rarity);
            if (diceRoll <= dropRate) {
                if (treasureDrop.getType() == Material.ENCHANTED_BOOK) {
                    diceRoll = dropRate + 1;
                    continue;
                }
                fishingEnchantments = FishingTreasureConfig.getInstance().fishingEnchantments.get(rarity);
                break;
            }
            diceRoll -= dropRate;
        }
        if (fishingEnchantments == null) {
            return enchants;
        }
        Collections.shuffle(fishingEnchantments, Misc.getRandom());
        int specificChance = 1;
        for (EnchantmentTreasure enchantmentTreasure : fishingEnchantments) {
            Enchantment possibleEnchantment = enchantmentTreasure.getEnchantment();
            if (treasureDrop.getItemMeta().hasConflictingEnchant(possibleEnchantment) || Misc.getRandom().nextInt(specificChance) != 0) {
                continue;
            }
            enchants.put(possibleEnchantment, enchantmentTreasure.getLevel());
            specificChance *= 2;
        }
        return enchants;
    }

    private static @Nullable FishingTreasure getFishingTreasure(Player player, int tier) {
        double diceRoll = Misc.getRandom().nextDouble() * 100;
        int luck;
        if (player.getInventory().getItemInMainHand().getType() == Material.FISHING_ROD) {
            luck = player.getInventory().getItemInMainHand().getEnchantmentLevel(Enchantment.LUCK);
        }
        else {
            luck = player.getInventory().getItemInOffHand().getEnchantmentLevel(Enchantment.LUCK);
        }
        diceRoll *= (1.0 - luck * mcMMO.p.getGeneralConfig().getFishingLureModifier() / 100);
        FishingTreasure treasure = null;
        for (Rarity rarity : Rarity.values()) {
            double dropRate = FishingTreasureConfig.getInstance().getItemDropRate(tier, rarity);
            if (diceRoll <= dropRate) {
                List<FishingTreasure> fishingTreasures = FishingTreasureConfig.getInstance().fishingRewards.get(rarity);
                if (fishingTreasures.isEmpty()) {
                    return null;
                }
                treasure = fishingTreasures.get(Misc.getRandom().nextInt(fishingTreasures.size()));
                break;
            }
            diceRoll -= dropRate;
        }
        if (treasure == null) {
            return null;
        }
        ItemStack treasureDrop = treasure.getDrop().clone();
        short maxDurability = treasureDrop.getType().getMaxDurability();
        if (maxDurability > 0) {
            treasureDrop.setDurability((short) (Misc.getRandom().nextInt(maxDurability)));
        }
        treasure.setDrop(treasureDrop);
        return treasure;
    }
}
