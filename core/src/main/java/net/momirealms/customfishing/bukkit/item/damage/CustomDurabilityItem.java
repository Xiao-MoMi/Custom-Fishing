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

package net.momirealms.customfishing.bukkit.item.damage;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ScoreComponent;
import net.momirealms.customfishing.api.mechanic.config.ConfigManager;
import net.momirealms.customfishing.common.helper.AdventureHelper;
import net.momirealms.customfishing.common.item.Item;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomDurabilityItem implements DurabilityItem {

    private final Item<ItemStack> item;

    public CustomDurabilityItem(Item<ItemStack> item) {
        this.item = item;
    }

    @Override
    public void damage(int value) {
        int customMaxDamage = (int) item.getTag("CustomFishing", "max_dur").get();
        int maxDamage = item.maxDamage().get();
        double ratio = (double) maxDamage / (double) customMaxDamage;
        int fakeDamage = (int) (value * ratio);
        item.damage(fakeDamage);
        item.setTag(customMaxDamage - value, "CustomFishing", "cur_dur");
        List<String> durabilityLore = ConfigManager.durabilityLore();
        List<String> previousLore = item.lore().orElse(new ArrayList<>());
        List<String> newLore = new ArrayList<>();
        for (String previous : previousLore) {
            Component component = AdventureHelper.jsonToComponent(previous);
            if (component instanceof ScoreComponent scoreComponent && scoreComponent.name().equals("cf")) {
                if (scoreComponent.objective().equals("durability")) {
                    continue;
                }
            }
            newLore.add(previous);
        }
        for (String lore : durabilityLore) {
            ScoreComponent.Builder builder = Component.score().name("cf").objective("durability");
            builder.append(AdventureHelper.miniMessage(lore.replace("{dur}", String.valueOf(customMaxDamage - value)).replace("{max}", String.valueOf(customMaxDamage))));
            newLore.add(AdventureHelper.componentToJson(builder.build()));
        }
        item.lore(newLore);
    }

    @Override
    public int damage() {
        int customMaxDamage = (int) item.getTag("CustomFishing", "max_dur").get();
        return customMaxDamage - (int) item.getTag("CustomFishing", "cur_dur").orElse(0);
    }

    @Override
    public int maxDamage() {
        return (int) item.getTag("CustomFishing", "max_dur").get();
    }
}
