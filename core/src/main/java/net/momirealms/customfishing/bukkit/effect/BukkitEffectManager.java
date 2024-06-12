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

package net.momirealms.customfishing.bukkit.effect;

import net.momirealms.customfishing.api.BukkitCustomFishingPlugin;
import net.momirealms.customfishing.api.mechanic.effect.EffectManager;
import net.momirealms.customfishing.api.mechanic.effect.EffectModifier;

import java.util.HashMap;
import java.util.Optional;

public class BukkitEffectManager implements EffectManager {

    private final BukkitCustomFishingPlugin plugin;
    private final HashMap<String, EffectModifier> effectModifiers = new HashMap<>();

    public BukkitEffectManager(BukkitCustomFishingPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void unload() {
        this.effectModifiers.clear();
    }

    @Override
    public void load() {

    }

    @Override
    public boolean registerEffectModifier(EffectModifier effect) {
        if (effectModifiers.containsKey(effect.id())) return false;
        this.effectModifiers.put(effect.id(), effect);
        return true;
    }

    @Override
    public Optional<EffectModifier> getEffectModifier(String id) {
        return Optional.ofNullable(this.effectModifiers.get(id));
    }
}
