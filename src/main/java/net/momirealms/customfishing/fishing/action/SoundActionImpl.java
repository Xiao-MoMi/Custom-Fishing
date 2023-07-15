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

package net.momirealms.customfishing.fishing.action;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.momirealms.customfishing.fishing.FishMeta;
import net.momirealms.customfishing.util.AdventureUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class SoundActionImpl extends AbstractAction implements Action {

    private final Sound.Source source;
    private final Key key;
    private final float volume;
    private final float pitch;

    public SoundActionImpl(Sound.Source source, Key sound, float volume, float pitch, double chance) {
        super(chance);
        this.pitch = pitch;
        this.source = source;
        this.key = sound;
        this.volume = volume;
    }

    @Override
    public void doOn(Player player, Player another, @Nullable FishMeta fishMeta) {
        AdventureUtils.playerSound(player, source, key, volume, pitch);
    }
}
