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

package net.momirealms.customfishing.api.mechanic.hook;

import java.util.ArrayList;
import java.util.List;

public class HookSetting {

    private final String key;
    private int maxDurability;
    private List<String> lore;

    public HookSetting(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public List<String> getLore() {
        return lore == null ? new ArrayList<>() : lore;
    }

    public static class Builder {

        private final HookSetting setting;

        public Builder(String key) {
            this.setting = new HookSetting(key);
        }

        public Builder durability(int maxDurability) {
            setting.maxDurability = maxDurability;
            return this;
        }

        public Builder lore(List<String> lore) {
            setting.lore = lore;
            return this;
        }

        public HookSetting build() {
            return setting;
        }
    }
}
